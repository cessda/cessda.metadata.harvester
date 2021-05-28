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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.scheduling.annotation.Scheduled;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PreDestroy;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@EnableConfigurationProperties
@SpringBootApplication
public class Server implements CommandLineRunner
{

    private static final Logger log = LoggerFactory.getLogger( Server.class );

    private final HarvesterConfiguration harvesterConfiguration;
    private final HttpClient httpClient;
    private final TransformerFactory factory;
    private boolean fullIsRunning = false;
    private boolean incrementalIsRunning = false;

    @Autowired
    public Server( HarvesterConfiguration harvesterConfiguration, HttpClient httpClient )
            throws TransformerConfigurationException
    {
        this.harvesterConfiguration = harvesterConfiguration;
        this.httpClient = httpClient;
        factory = TransformerFactory.newInstance();
        factory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
    }

    public static void main( String[] args )
    {
        SpringApplication.run( Server.class, args );
    }

    @Override
    public void run( String... args )
    {
        fullHarvesting();
    }

    @ManagedOperation(
            description = "Run harvesting on several repo starting from 'harvester.from.single'. " +
                    "Separate more than one repo with comma. Can be used to harvest an new repository, " +
                    "after the list of repos has been cleared, and the newly added repo url is set."
                    + "The position corresponds to the number given in the list of repos in the configuration view,"
                    + "starting from 0. See environments tab and search for 'harvester.repos'" )
    public String bundleHarvesting( String commaSeparatedIntegerPositionInRepoList )
    {

        String res = Arrays.stream( commaSeparatedIntegerPositionInRepoList.split( "," ) )
                .map( String::trim ).mapToInt( Integer::parseInt )
                .mapToObj( i -> "Repo " + i + " : " + singleHarvesting( i ) + "\n" )
                .collect( Collectors.joining() );
        return res + "Bundle harvesting finished from " + harvesterConfiguration.getFrom().getSingle();
    }

    /**
     * Returns the host portion of the URL string provided.
     * The resulting string is encoded using {@link StandardCharsets#UTF_8}.
     *
     * @param oaiUrl the URL string.
     * @return the {@link StandardCharsets#UTF_8} encoded host.
     * @throws IllegalArgumentException if the given string is not a valid {@link URI}.
     */
    private static String shortened( String oaiUrl, String setspec )
    {
        try
        {
            String indexName = new URI( oaiUrl ).getHost();
            if ( setspec != null )
            {
                return URLEncoder.encode( indexName + "-" + setspec, StandardCharsets.UTF_8.name() );
            }
            else
            {
                return URLEncoder.encode( indexName, StandardCharsets.UTF_8.name() );
            }
        }
        catch ( URISyntaxException | UnsupportedEncodingException e )
        {
            throw new IllegalArgumentException( e );
        }
    }

    @ManagedOperation(
            description = "Run harvesting on one single repo starting from 'harvester.from.single'. Can be used to harvest an new repository, after the list of repos has been cleared, and the newly added repo url is set. The position corresponds to the number given in the list of repos in the configuration view, starting from 0. See environments tab and search for 'harvester.repos'" )
    public String singleHarvesting( Integer positionInRepoList )
    {

        if ( incrementalIsRunning )
        {
            return "Not started. An incremental harvesting progress is already running";
        }

        incrementalIsRunning = true;
        log.info( "Single harvesting starting from {}", harvesterConfiguration.getFrom().getSingle() );
        try
        {
            runSingleHarvest( harvesterConfiguration.getFrom().getSingle(), positionInRepoList );
        }
        finally
        {
            incrementalIsRunning = false;
        }
        log.info( "Single harvesting finished from {}", harvesterConfiguration.getFrom().getSingle() );
        return "Single harvesting for " + positionInRepoList + "th repository started. See log section for details";
    }

    /**
     * runs right after service startup takes place
     */
    @ManagedOperation(
            description = "Run initial harvesting. Set from date with key harvester.cron.initial. Can be used to harvest an new repository, after the list of repos has been cleared, and the newly added repo url is set. Don't forget to reset the environment and update application.yml for persistent configuration" )
    @Scheduled( initialDelayString = "${harvester.cron.initialDelay:1000}", fixedDelay = 315360000000L )
    public void initialHarvesting()
    {
        LocalDate date = LocalDate.now().minusDays( 2 );
        String newInitial = date.toString();
        // set initial value dynamically
        if ( harvesterConfiguration.getFrom().getInitial() == null )
        {
            harvesterConfiguration.getFrom().setInitial( newInitial );
        }
        log.info( harvesterConfiguration.getFrom().getInitial() );

        if ( incrementalIsRunning )
        {
            return;
        }

        log.info( "Initial harvesting starting from {}", harvesterConfiguration.getFrom().getInitial() );

        try
        {
            runHarvest( harvesterConfiguration.getFrom().getInitial() );
        }
        finally
        {
            incrementalIsRunning = false;
        }
        log.info( "Initial harvesting finished from {}", harvesterConfiguration.getFrom().getInitial() );
    }

    /**
     * runs once in a year, no incremental harvesting takes place
     *
     */
    public void fullHarvesting()
    {
        if ( fullIsRunning )
        {
            return;
        }

        try
        {
            log.info( "Full harvesting started" );
            fullIsRunning = true;
            runHarvest( null );
            log.info( "Full harvesting finished" );
        }
        finally
        {
            fullIsRunning = false;
        }
    }

    /**
     * runs always.
     *
     */
    public void incrementalHarvesting()
    {

        LocalDate date = LocalDate.now().minusDays( 2 );
        String newIncFrom = date.toString();
        String msg;
        if ( !fullIsRunning )
        {
            if ( !incrementalIsRunning )
            {
                incrementalIsRunning = true;
                log.info( "Incremental harvesting started from {}",
                        harvesterConfiguration.getFrom().getIncremental() );
                runHarvest( harvesterConfiguration.getFrom().getIncremental() );
                log.info( "Incremental harvesting finished" );

                msg = "Incremental harvesting finished from " + harvesterConfiguration.getFrom().getIncremental();

                harvesterConfiguration.getFrom().setIncremental( newIncFrom );
                log.info( "Next incremental harvest will start from {}", newIncFrom );
            }
            else
            {
                msg = "Incremental harvesting already running.";
            }
            log.info( msg );
        }
        else
        {
            log.info( "No incremental harvesting, as full harvesting is in progress." );
            return;
        }

        incrementalIsRunning = false;

    }

    public void runSingleHarvest( String fromDate, Integer position )
    {

        log.info( "Harvesting started from {} for repo {}", fromDate, position );
        try
        {
            var repo = harvesterConfiguration.getRepos().get( position );

            final String mdFormat;
            if ( repo.getMetadataFormat() != null )
            {
                mdFormat = repo.getMetadataFormat();
            }
            else
            {
                throw new IllegalArgumentException( "Repository " + repo.getUrl() + " has no metadata format configured." );
            }

            var baseUrl = repo.getUrl();
            log.info( "Single harvesting {} from {}", baseUrl, fromDate );
            for ( String set : discoverSets( repo ) )
            {
                harvestSet( baseUrl.toString(), set, fromDate, mdFormat );
            }
        }
        finally
        {
            incrementalIsRunning = false;
        }
    }

    private void runHarvest( String fromDate )
    {

        log.info( "Harvesting started from {}", fromDate );

        var executor = Executors.newFixedThreadPool(harvesterConfiguration.getRepos().size());

        var futures = new ArrayList<CompletableFuture<Void>>();
        for ( var repo : harvesterConfiguration.getRepos() )
        {
            log.info( "Harvesting repository {}", repo );

            Set<String> sets = discoverSets( repo );

            final String mdFormat;
            if ( repo.getMetadataFormat() != null )
            {
                mdFormat = repo.getMetadataFormat();
            }
            else
            {
                throw new IllegalArgumentException( "Repository " + repo + " has no metadata format configured." );
            }

            futures.add(
                    CompletableFuture.runAsync( () -> sets.forEach( set -> harvestSet( repo.getUrl().toString(), set, fromDate, mdFormat ) ), executor )
            );
        }
        futures.forEach( CompletableFuture::join );
        executor.shutdown();
    }

    /**
     * Discover sets from the remote repository.
     * <ol>
     *     <li>If the repository already has sets configured, these are returned.</li>
     *     <li>If set discovery is enabled, the repository is enquired.</li>
     *     <li>Otherwise, a set containing {@code null} will be returned.</li>
     * </ol>
     *
     * @param repo the repository to get sets for
     * @return a {@link Set} of setSpecs
     */
    private Set<String> discoverSets( Repo repo )
    {
        if ( repo.getSet() != null )
        {
            return Collections.singleton( repo.getSet() );
        }
        else if ( repo.discoverSets() )
        {
            try
            {
                var unfoldedSets = getSetStrings( repo.getUrl() );
                log.debug( "No. of sets: {}", unfoldedSets.size() );
                return unfoldedSets;
            }
            catch ( IOException | TransformerException | SAXException e )
            {
                log.warn( "Failed to discover sets from the remote repository: set set=all", e );
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
     * @param baseUrl the repository to harvest.
     * @param set the set to harvest, set to {@code null} to disable set based harvesting.
     * @param fromDate the date to harvest from.
     * @param mdFormat the metadata format to harvest.
     */
    private void harvestSet( String baseUrl, String set, String fromDate, String mdFormat )
    {
        try
        {
            fetchDCRecords( baseUrl, set, fromDate, mdFormat );
        }
        catch ( HarvesterFailedException e )
        {
            log.error( "Could not harvest repository: {}, set: {}: {}", baseUrl, set, e.toString() );
        }
    }

    private void fetchDCRecords( String repoBase, String setspec, String fromDate, String mdFormat ) throws HarvesterFailedException
    {

        String indexName = shortened( repoBase, setspec );

        final var repositoryDirectory = harvesterConfiguration.getDir().resolve( indexName );
        try
        {
            log.debug( "Creating destination directory: {}", repositoryDirectory );
            Files.createDirectories( repositoryDirectory );
        }
        catch ( IOException e )
        {
            throw new DirectoryCreationFailedException( repositoryDirectory, e );
        }

        log.debug( "Fetching records for repository: {}, set: {}.", repoBase, setspec );

        var currentlyRetrievedSet = getIdentifiersForSet( repoBase, setspec, fromDate, mdFormat );

        log.info( "Retrieved {} record headers from {}, set: {}.", currentlyRetrievedSet.size(), repoBase, setspec );

        var retrievedRecords = writeToLocalFileSystem( currentlyRetrievedSet, repoBase, repositoryDirectory, mdFormat );

        log.info( "Retrieved {} records from {}, set: {}.", retrievedRecords, repoBase, setspec );
    }

    private List<String> getIdentifiersForSet( String oaiBaseUrl, String set, String fromDate, String mdFormat ) throws HarvesterFailedException
    {
        try
        {
            final var records = new ArrayList<String>();
            var li = new ListIdentifiers( httpClient, oaiBaseUrl, fromDate, null, set, mdFormat, harvesterConfiguration.getTimeout() );

            Optional<String> resumptionToken;

            do
            {
                log.debug( "URL: {}, set: {}, list size: {}", oaiBaseUrl, set, records.size() );

                // add to list of records to fetch
                records.addAll( li.getIdentifiers() );

                // need to continue looping?
                resumptionToken = li.getResumptionToken();

                if (resumptionToken.isPresent())
                {
                    log.trace( "recurse: url {}\ttoken: {}", oaiBaseUrl, resumptionToken );
                    li = new ListIdentifiers( httpClient, oaiBaseUrl, resumptionToken.orElseThrow(), harvesterConfiguration.getTimeout() );
                }
            }
            while ( resumptionToken.isPresent() );

            return records;
        }
        catch ( IOException | SAXException e )
        {
            throw new HarvesterFailedException( "Fetching identifiers failed for " + oaiBaseUrl + ": " + e.toString(), e );
        }
    }

    private int writeToLocalFileSystem( Collection<String> records, String oaiUrl, Path repositoryDirectory, String mdFormat ) throws DirectoryCreationFailedException
    {
        var unwrappedOutputDirectory = repositoryDirectory.resolve( "unwrapped" );
        var wrappedOutputDirectory = repositoryDirectory.resolve( "wrapped" );

        try
        {
            log.debug( "Creating destination directory: {}", repositoryDirectory );
            Files.createDirectories( unwrappedOutputDirectory );
            Files.createDirectories( wrappedOutputDirectory );
        }
        catch ( IOException e )
        {
            throw new DirectoryCreationFailedException( repositoryDirectory, e );
        }

        int retrievedRecords = 0;

        for ( var currentRecord : records )
        {
            var fileName = URLEncoder.encode( currentRecord + "_" + mdFormat + ".xml", StandardCharsets.UTF_8 );

            try
            {
                log.debug( "Harvesting {} from {}", currentRecord, oaiUrl );
                var pmhRecord = new GetRecord( httpClient, oaiUrl, currentRecord, mdFormat, harvesterConfiguration.getTimeout() );

                // Check for errors
                if (pmhRecord.getErrors().getLength() != 0)
                {
                    var error = pmhRecord.getErrors().item( 0 );
                    log.warn( "Failed to harvest record {}: {}: {}", currentRecord,
                            error.getAttributes().getNamedItem( "code" ).getTextContent(),
                            error.getTextContent()
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
                        writeDomSource( source, unwrappedOutputDirectory.resolve( fileName ) );
                    }
                }

                // Keep envelope
                if (harvesterConfiguration.keepOAIEnvelope())
                {
                    var source = new DOMSource( pmhRecord.getDocument() );
                    writeDomSource( source, wrappedOutputDirectory.resolve( fileName ) );
                }
            }
            catch ( TransformerConfigurationException e )
            {
                // This is not recoverable
                throw new IllegalStateException( e );
            }
            catch ( IOException | SAXException | TransformerException e1 )
            {
                log.warn( "Failed to harvest record {} from {}: {}", currentRecord.trim(), oaiUrl, e1.toString() );
            }
        }

        return retrievedRecords;
    }

    /**
     * Writes the given {@link Source} to the specified {@link Path}.
     * @throws IOException if an IO error occurs while writing the file.
     * @throws TransformerException if an unrecoverable error occurs whilst writing the source.
     */
    private void writeDomSource( Source source, Path fdest ) throws IOException, TransformerException
    {
        try ( var fOutputStream = Files.newOutputStream( fdest ) )
        {
            log.trace( "Writing to {}", fdest );
            factory.newTransformer().transform( source, new StreamResult( fOutputStream ) );
        }
    }

    /**
     * Retrieves the sets from the OAI-PMH repository using the ListSets verb.
     *
     * @param url the URL of the repository.
     * @return a {@link Set} containing all of the sets in the remote repository.
     */
    public Set<String> getSetStrings( final URI url ) throws IOException, SAXException, TransformerException
    {
        HashSet<String> unfoldedSets = new HashSet<>();

        URI urlToSend = url;
        Optional<String> resumptionToken;
        do
        {
            var ls = new ListSets( httpClient, urlToSend.toString(), harvesterConfiguration.getTimeout() );

            if ( ls.getErrors().getLength() != 0 )
            {
                log.error( "Invalid request {}", ls );
            }

            var document = ls.getDocument();

            NodeList nl = document.getElementsByTagName( "setSpec" );

            for ( int i = 0; i < nl.getLength(); i++ )
            {
                unfoldedSets.add( nl.item( i ).getTextContent() );
            }

            resumptionToken = ls.getResumptionToken();
            if ( resumptionToken.isPresent() )
            {
                urlToSend = URI.create( url + "?verb=ListSets&resumptionToken=" + resumptionToken.orElseThrow() );
            }
        }
        while ( resumptionToken.isPresent() );

        return unfoldedSets;
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
