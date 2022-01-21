/*-
 * #%L
 * CESSDA Euro Question Bank: Metadata Harvester
 * %%
 * Copyright (C) 2020 CESSDA ERIC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.cessda.eqb.harvester;

import org.oclc.oai.harvester2.verb.GetRecord;
import org.oclc.oai.harvester2.verb.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.xml.sax.SAXException;

import javax.annotation.PreDestroy;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;

import static eu.cessda.eqb.harvester.LoggingConstants.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.logstash.logback.argument.StructuredArguments.value;

@EnableConfigurationProperties
@SpringBootApplication
public class Harvester implements CommandLineRunner
{

    private static final Logger log = LoggerFactory.getLogger( Harvester.class );
    private static final String WRAPPED_DIRECTORY_NAME = "wrapped";
    private static final String UNWRAPPED_DIRECTORY_NAME = "unwrapped";

    private final HttpClient httpClient;
    private final HarvesterConfiguration harvesterConfiguration;
    private final IOUtilities ioUtilities;
    private final RepositoryClient repositoryClient;

    @Autowired
    public Harvester( HttpClient httpClient, HarvesterConfiguration harvesterConfiguration, IOUtilities ioUtilities, RepositoryClient repositoryClient )
    {
        this.httpClient = httpClient;
        this.harvesterConfiguration = harvesterConfiguration;
        this.ioUtilities = ioUtilities;
        this.repositoryClient = repositoryClient;
    }

    public static void main( String[] args )
    {
        new SpringApplicationBuilder( Harvester.class ).bannerMode( Banner.Mode.OFF ).run( args );
    }

    /**
     * Validate that a repository has all the required parameters.
     * @param repo the repository to validate.
     * @throws IllegalArgumentException if a parameter fails validation.
     */
    private static void validateRepository( Repo repo )
    {
        if ( repo.getCode() == null)
        {
            throw new IllegalArgumentException( "Repository " + repo + " has no identifier configured." );
        }

        if ( repo.getMetadataPrefixes().isEmpty() )
        {
            throw new IllegalArgumentException( "Repository " + repo + " has no metadata prefixes configured." );
        }
    }

    @Override
    public void run( String... args )
    {
        if ( harvesterConfiguration.incremental() )
        {
            incrementalHarvesting();
        }
        else
        {
            runHarvest( null );
        }
    }

    /**
     * runs always.
     *
     */
    public void incrementalHarvesting()
    {
        var from = harvesterConfiguration.getFrom();

        final LocalDate incremental;

        if ( from != null && from.getIncremental() != null )
        {
            incremental = from.getIncremental();
        }
        else
        {
            // If a specific incremental date is not configured default to harvesting the last week
            incremental = LocalDate.now().minusDays( 7 );
        }

        log.info( "Incremental harvesting started from {}", incremental );
        runHarvest( incremental );
        log.info( "Incremental harvesting finished" );
    }

    /**
     * Runs the harvest.
     *
     * @param fromDate the date to harvest from. If set to {@code null}, no date restrictions will be applied.
     */
    private void runHarvest( LocalDate fromDate )
    {

        log.info( "Harvesting started from {}", fromDate );

        var repositories = harvesterConfiguration.getRepos();

        // Validate expected parameters are present before starting the harvest
        repositories.forEach( Harvester::validateRepository );

        // Create an executor that will harvest each repository in parallel
        var executor = Executors.newFixedThreadPool( repositories.size() );

        // Start the harvest for each repository
        var futures = repositories.stream().map( repo ->
                CompletableFuture.runAsync( () -> harvestRepository( fromDate, repo ), executor )
        ).toArray(CompletableFuture[]::new);

        try
        {
            CompletableFuture.allOf( futures ).join();
        }
        catch ( CancellationException | CompletionException e )
        {
            log.error("Unexpected error occurred when harvesting!", e);
        }

        executor.shutdown();
    }

    private void harvestRepository( LocalDate fromDate, Repo repo )
    {
        log.info( "Harvesting repository {}", value( "repo", repo ) );

        var sets = repositoryClient.discoverSets( repo );

        for ( var set : sets )
        {
            try
            {
                harvestSet( repo, set, fromDate );
            }
            catch ( HarvesterFailedException e )
            {
                log.error( "Could not harvest repository: {}, set: {}: {}: {}",
                        value( LoggingConstants.OAI_URL, repo.getCode()),
                        value( LoggingConstants.OAI_SET, set),
                        value( LoggingConstants.EXCEPTION_NAME, e.getClass().getName()),
                        value( LoggingConstants.EXCEPTION_MESSAGE, e.getMessage())
                );
            }
        }
    }


    /**
     * Harvest a specific set from a repository.
     * @param repo the repository to harvest.
     * @param setspec the set to harvest, set to {@code null} to disable set based harvesting.
     * @param fromDate the date to harvest from, set to {@code null} to harvest from the beginning.
     */
    private void harvestSet( Repo repo, String setspec, LocalDate fromDate) throws HarvesterFailedException
    {
        int retrievedRecords = 0;

        for ( var metadataPrefix : repo.getMetadataPrefixes() )
        {
            // The folder structure is repo/set(optional)/metadataPrefix/record.xml
            var repositoryDirectory = Path.of( repo.getCode() );

            // Sets are nested in their own directories
            if (setspec != null)
            {
                repositoryDirectory = repositoryDirectory.resolve( setspec );
            }

            repositoryDirectory = repositoryDirectory.resolve( metadataPrefix );

            if ( harvesterConfiguration.keepOAIEnvelope() )
            {
                var wrappedDirectory = harvesterConfiguration.getDir().resolve( WRAPPED_DIRECTORY_NAME );
                IOUtilities.createDestinationDirectory( wrappedDirectory, repositoryDirectory );
            }

            if ( harvesterConfiguration.removeOAIEnvelope() )
            {
                var unwrappedDirectory = harvesterConfiguration.getDir().resolve( UNWRAPPED_DIRECTORY_NAME );
                IOUtilities.createDestinationDirectory( unwrappedDirectory, repositoryDirectory );
            }

            log.debug( "{}: Set: {}: Prefix: {} Fetching records.", repo.getCode(), setspec, metadataPrefix );

            var recordIdentifiers = repositoryClient.retrieveRecordHeaders( repo, setspec, metadataPrefix, fromDate );

            log.info( "{}: Set: {}: Retrieved {} record headers.",
                    value( REPO_NAME, repo.getCode() ),
                    value( OAI_SET, setspec ),
                    value( RETRIEVED_RECORD_HEADERS, recordIdentifiers.size())
            );

            retrievedRecords += harvestRecords( recordIdentifiers, repo, metadataPrefix, repositoryDirectory );
        }

        log.info( "{}: Set: {}: Retrieved {} records.",
                value( OAI_RECORD, repo.getCode() ),
                value( OAI_SET, setspec ),
                value( RETRIEVED_RECORDS, retrievedRecords )
        );
    }

    /**
     * Harvest the collection of {@link RecordHeader}s from the remote repository.
     *
     * @param records        the collection of records to harvest.
     * @param repo           the repository to harvest.
     * @param metadataFormat the metadata prefix to harvest.
     * @param repoDirectory  the destination directory of the harvest.
     * @return the number of records successfully harvested.
     */
    private int harvestRecords( Collection<RecordHeader> records, Repo repo, String metadataFormat, Path repoDirectory )
    {
        int retrievedRecords = 0;

        // Destination directories
        var unwrappedDirectory = harvesterConfiguration.getDir()
            .resolve( UNWRAPPED_DIRECTORY_NAME )
            .resolve( repoDirectory );
        var wrappedDirectory = harvesterConfiguration.getDir()
            .resolve( Harvester.WRAPPED_DIRECTORY_NAME )
            .resolve( repoDirectory );

        for ( var currentRecord : records )
        {
            var fileName = URLEncoder.encode( currentRecord.identifier(), UTF_8 ) + ".xml";

            try
            {
                log.debug( "Harvesting {} from {}", currentRecord, repo.getUrl() );
                var pmhRecord = GetRecord.instance( httpClient, repo.getUrl(), currentRecord.identifier(), metadataFormat );

                // Check for errors
                if (!pmhRecord.getErrors().isEmpty())
                {
                    var error = pmhRecord.getErrors().get( 0 );
                    log.warn( "{}: Failed to harvest record {}: {}: {}",
                            value( REPO_NAME, repo.getCode()),
                            value( OAI_RECORD, currentRecord.identifier() ),
                            value( OAI_ERROR_CODE, error.getCode() ),
                            value( OAI_ERROR_MESSAGE, error.getMessage().orElse( "" ) )
                    );
                    continue;
                }

                retrievedRecords++;

                // Remove envelope
                if (harvesterConfiguration.removeOAIEnvelope())
                {
                    var metadata = pmhRecord.getMetadata();
                    var destinationFile = unwrappedDirectory.resolve( fileName );

                    if (metadata.isPresent())
                    {
                        var source = new DOMSource( metadata.orElseThrow() );
                        ioUtilities.writeDomSource( source, destinationFile );
                    }
                    else if (RecordHeader.Status.deleted.equals( currentRecord.status() ))
                    {
                        // Delete the unwrapped XML
                        Files.deleteIfExists(destinationFile);
                    }
                }

                // Keep envelope
                if (harvesterConfiguration.keepOAIEnvelope())
                {
                    var destinationFile = wrappedDirectory.resolve( fileName );
                    var source = new DOMSource( pmhRecord.getDocument() );
                    ioUtilities.writeDomSource( source, destinationFile );
                }
            }
            catch ( IOException | SAXException | TransformerException e1 )
            {
                log.warn( "{}: Failed to harvest record {} from {}: {}: {}",
                        value( REPO_NAME, repo.getCode()),
                        value( LoggingConstants.OAI_RECORD, currentRecord.identifier()),
                        value( LoggingConstants.OAI_URL, repo.getUrl()),
                        value( LoggingConstants.EXCEPTION_NAME, e1.getClass().getName()),
                        value( LoggingConstants.EXCEPTION_MESSAGE, e1.getMessage())
                );
            }
        }

        // Clean up - this should only run on full harvests.
        if (!harvesterConfiguration.incremental())
        {
            log.info( "{}: Removing orphaned records.", value( REPO_NAME, repo.getCode()));
            IOUtilities.deleteOrphanedRecords( repo, records, unwrappedDirectory );
            IOUtilities.deleteOrphanedRecords( repo, records, wrappedDirectory );
        }

        return retrievedRecords;
    }

    @PreDestroy
    void printConfig()
    {
        if ( log.isInfoEnabled() )
        {
            log.info( harvesterConfiguration.toString() );
        }
    }
}
