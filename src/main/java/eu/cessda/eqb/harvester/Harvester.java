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
import org.oclc.oai.harvester2.verb.ListIdentifiers;
import org.oclc.oai.harvester2.verb.ListSets;
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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

    @Autowired
    public Harvester( HttpClient httpClient, HarvesterConfiguration harvesterConfiguration, IOUtilities ioUtilities )
            throws TransformerConfigurationException
    {
        this.httpClient = httpClient;
        this.harvesterConfiguration = harvesterConfiguration;
        this.ioUtilities = ioUtilities;
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
        var incremental = harvesterConfiguration.getFrom().getIncremental();

        // If a specific incremental date is not configured default to harvesting the last week
        if (incremental == null)
        {
            incremental = LocalDate.now().minusDays( 7 );
        }

        log.info( "Incremental harvesting started from {}", incremental );
        runHarvest( incremental );
        log.info( "Incremental harvesting finished" );
    }

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
        ).collect( Collectors.toCollection( ArrayList::new ) );

        try
        {
            futures.forEach( CompletableFuture::join );
        }
        catch ( CancellationException | CompletionException e )
        {
            log.error("Unexpected error occurred when harvesting!", e.getCause());
        }

        executor.shutdown();
    }

    private void harvestRepository( LocalDate fromDate, Repo repo )
    {
        log.info( "Harvesting repository {}", value( "repo", repo ) );

        var sets = discoverSets( repo );

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
     * Discover sets from the remote repository.
     * <ol>
     *     <li>If the repository already has sets configured, these are returned.</li>
     *     <li>If set discovery is enabled, the repository is enquired using the {@code ListSets} verb.</li>
     *     <li>Otherwise, a {@link Set} containing {@code null} will be returned.</li>
     * </ol>
     *
     * @param repo the repository to get sets for
     * @return a {@link Set} of setSpecs
     */
    private Set<String> discoverSets( Repo repo )
    {
        if ( !repo.getSets().isEmpty() )
        {
            return repo.getSets();
        }
        else if ( repo.discoverSets() )
        {
            try
            {
                var unfoldedSets = new HashSet<String>();
                var ls = ListSets.instance( httpClient, repo.getUrl() );

                Optional<String> resumptionToken;
                do
                {
                    if ( !ls.getErrors().isEmpty() )
                    {
                        log.error( "{}: Error while retrieving the list of sets: {}", repo.getCode(), ls.getErrors() );
                        break;
                    }

                    unfoldedSets.addAll( ls.getSets() );

                    resumptionToken = ls.getResumptionToken();
                    if ( resumptionToken.isPresent() )
                    {
                        ls =  ListSets.instance( httpClient, repo.getUrl(), resumptionToken.orElseThrow() );
                    }
                }
                while ( resumptionToken.isPresent() );

                log.debug( "No. of sets: {}", unfoldedSets.size() );
                return unfoldedSets;
            }
            catch ( IOException | SAXException e )
            {
                log.warn( "Failed to discover sets from {}: set set=all: {}", repo.getCode(), e.toString() );
                // set set=all in case of no sets found
                return Collections.singleton( null );
            }
        }
        else
        {
            return Collections.singleton( null );
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
                ioUtilities.createDestinationDirectory( wrappedDirectory, repositoryDirectory );
            }

            if ( harvesterConfiguration.removeOAIEnvelope() )
            {
                var unwrappedDirectory = harvesterConfiguration.getDir().resolve( UNWRAPPED_DIRECTORY_NAME );
                ioUtilities.createDestinationDirectory( unwrappedDirectory, repositoryDirectory );
            }

            log.debug( "{}: Set: {}: Prefix: {} Fetching records", repo.getCode(), setspec, metadataPrefix );

            var recordIdentifiers = getIdentifiersForSet( repo, setspec, metadataPrefix, fromDate );

            log.info( "{}: Set: {}: Retrieved {} record headers.",
                    value( REPO_NAME, repo.getCode() ),
                    value( OAI_SET, setspec ),
                    recordIdentifiers.size()
            );

            retrievedRecords += writeToLocalFileSystem( recordIdentifiers, repo, metadataPrefix, repositoryDirectory );
        }

        log.info( "{}: Set: {}: Retrieved {} records.",
                value(OAI_RECORD, repo.getCode()),
                value(OAI_SET, setspec),
                retrievedRecords
        );
    }

    private List<RecordHeader> getIdentifiersForSet( Repo repo, String set, String metadataFormat, LocalDate fromDate ) throws HarvesterFailedException
    {
        log.trace( "URL: {}, set: {}", repo.getUrl(), set );
        try
        {
            final var records = new ArrayList<RecordHeader>();
            var li = ListIdentifiers.instance( httpClient, repo.getUrl(), fromDate, null, set, metadataFormat );

            Optional<String> resumptionToken;

            do
            {
                // add to list of records to fetch
                records.addAll( li.getIdentifiers() );

                // need to continue looping?
                resumptionToken = li.getResumptionToken();

                if (resumptionToken.isPresent())
                {
                    log.trace( "recurse: url {}\ttoken: {}", repo.getUrl(), resumptionToken );
                    li = ListIdentifiers.instance( httpClient, repo.getUrl(), resumptionToken.orElseThrow() );
                }
            }
            while ( resumptionToken.isPresent() );

            return records;
        }
        catch ( IOException | SAXException e )
        {
            throw new HarvesterFailedException( repo.getCode() + ": Fetching identifiers failed: " + e, e );
        }
    }

    private int writeToLocalFileSystem( Collection<RecordHeader> records, Repo repo, String metadataFormat, Path repoDirectory )
    {
        int retrievedRecords = 0;

        for ( var currentRecord : records )
        {
            var fileName = URLEncoder.encode( currentRecord.getIdentifier(), UTF_8 ) + ".xml";

            try
            {
                log.debug( "Harvesting {} from {}", currentRecord, repo.getUrl() );
                var pmhRecord = GetRecord.instance( httpClient, repo.getUrl(), currentRecord.getIdentifier(), metadataFormat );

                // Check for errors
                if (!pmhRecord.getErrors().isEmpty())
                {
                    var error = pmhRecord.getErrors().get( 0 );
                    log.warn( "{}: Failed to harvest record {}: {}: {}",
                            value( REPO_NAME, repo.getCode()),
                            value( OAI_RECORD, currentRecord.getIdentifier() ),
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
                    if (metadata.isPresent())
                    {
                        var source = new DOMSource( metadata.orElseThrow() );
                        ioUtilities.writeDomSource( source, harvesterConfiguration.getDir().resolve( UNWRAPPED_DIRECTORY_NAME ).resolve( repoDirectory ).resolve( fileName ) );
                    }
                }

                // Keep envelope
                if (harvesterConfiguration.keepOAIEnvelope())
                {
                    var source = new DOMSource( pmhRecord.getDocument() );
                    ioUtilities.writeDomSource( source, harvesterConfiguration.getDir().resolve( WRAPPED_DIRECTORY_NAME ).resolve( repoDirectory ).resolve( fileName ) );
                }
            }
            catch ( TransformerConfigurationException e )
            {
                // This is not recoverable
                throw new IllegalStateException( e );
            }
            catch ( IOException | SAXException | TransformerException e1 )
            {
                log.warn( "{}: Failed to harvest record {} from {}: {}: {}",
                        value( REPO_NAME, repo.getCode()),
                        value( LoggingConstants.OAI_RECORD, currentRecord.getIdentifier()),
                        value( LoggingConstants.OAI_URL, repo.getUrl()),
                        value( LoggingConstants.EXCEPTION_NAME, e1.getClass().getName()),
                        value( LoggingConstants.EXCEPTION_MESSAGE, e1.getMessage())
                );
            }
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
