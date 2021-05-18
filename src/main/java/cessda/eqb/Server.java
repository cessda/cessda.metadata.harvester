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
package cessda.eqb;

import jodd.mail.Email;
import jodd.mail.MailException;
import jodd.mail.SendMailSession;
import jodd.mail.SmtpSslServer;
import org.oclc.oai.harvester2.verb.GetRecord;
import org.oclc.oai.harvester2.verb.ListIdentifiers;
import org.oclc.oai.harvester2.verb.ListSets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.support.RegistrationPolicy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.PreDestroy;
import javax.xml.XMLConstants;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@EnableMBeanExport( registration = RegistrationPolicy.REPLACE_EXISTING )
@EnableScheduling
@SpringBootApplication
@ManagedResource
public class Server extends SpringBootServletInitializer
{

    private static final Logger log = LoggerFactory.getLogger( Server.class );
    public static final String METADATA = "metadata";

    private final HarvesterConfiguration harvesterConfiguration;
    private final String mailHost;
    private final TransformerFactory factory;
    private boolean fullIsRunning = false;
    private boolean incrementalIsRunning = false;
    private String mdFormat = "oai_ddi";

    @Autowired
    public Server( HarvesterConfiguration harvesterConfiguration, @Value( "${spring.mail.host}" ) String mailHost )
            throws TransformerConfigurationException
    {
        this.harvesterConfiguration = harvesterConfiguration;
        this.mailHost = mailHost;
        factory = TransformerFactory.newInstance();
        factory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
    }

    public static void main( String[] args )
    {
        SpringApplication.run( Server.class, args );
        log.info( "Harvester running. " );
    }

    @Async
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

    private static String oaiBase( String u )
    {
        final int endIndex = u.indexOf( '?' );
        if ( endIndex != -1 )
        {
            return u.substring( 0, endIndex );
        }
        else
        {
            return u;
        }
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

    @Async
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
     *
     * @return status
     */
    @Async
    @ManagedOperation(
            description = "Run initial harvesting. Set from date with key harvester.cron.initial. Can be used to harvest an new repository, after the list of repos has been cleared, and the newly added repo url is set. Don't forget to reset the environment and update application.yml for persistent configuration" )
    @Scheduled( initialDelayString = "${harvester.cron.initialDelay:1000}", fixedDelay = 315360000000L )
    public String initialHarvesting()
    {

        log.info( harvesterConfiguration.getMetadataFormat() );
        log.info( harvesterConfiguration.getFrom().getInitial() );

        if ( incrementalIsRunning )
        {
            return "Not started. An incremental harvesting progress is already running";
        }
        log.info( "Full harvest schedule: {}", harvesterConfiguration.getCron().getFull() );
        log.info( "Incremental harvest schedule: {}", harvesterConfiguration.getCron().getIncremental() );

        log.info( "Initial harvesting starting from {}", harvesterConfiguration.getFrom().getInitial() );
        log.info( "Incremental harvesting will start with cron schedule {} from {}",
                harvesterConfiguration.getCron().getIncremental(), harvesterConfiguration.getFrom().getIncremental() );
        try
        {
            runHarvest( harvesterConfiguration.getFrom().getInitial() );
        }
        finally
        {
            incrementalIsRunning = false;
        }
        log.info( "Initial harvesting finished from {}", harvesterConfiguration.getFrom().getInitial() );
        return "Initial harvesting finished from " + harvesterConfiguration.getFrom().getInitial();
    }

    @Override
    protected SpringApplicationBuilder configure( SpringApplicationBuilder application )
    {
        return application.sources( Server.class );
    }

    /**
     * runs once in a year, no incremental harvesting takes place
     *
     * @return status
     */
    @Async
    @ManagedOperation( description = "Run full harvesting. Set from date with key harvester.cron.full" )
    @Scheduled( cron = "${harvester.cron.full:0 30 1 15 * ?}" )
    public String fullHarvesting()
    {

        if ( fullIsRunning )
        {
            return "Not started. A full harvesting progress is already running";
        }
        log.info( "Full harvesting started from {}", harvesterConfiguration.getFrom().getFull() );
        fullIsRunning = true;
        runHarvest( harvesterConfiguration.getFrom().getFull() );
        log.info( "Full harvesting finished" );
        fullIsRunning = false;
        log.info( "Full harvesting finished from {}", harvesterConfiguration.getFrom().getFull() );
        return "Full harvesting finished from " + harvesterConfiguration.getFrom().getFull();
    }

    /**
     * runs always.
     *
     * @return status
     */
    @Async
    @ManagedOperation( description = "Run incremental harvesting. Set from date with key harvester.cron.incremental" )
    @Scheduled( cron = "${harvester.cron.incremental:0 0 4 * * *}" )
    public String incrementalHarvesting()
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
            return "No incremental harvesting, as full harvesting is in progress.";
        }

        incrementalIsRunning = false;

        return msg;
    }

    @Async
    public void runSingleHarvest( String fromDate, Integer position )
    {

        log.info( "Harvesting started from {} for repo {}", fromDate, position );
        try
        {
            mdFormat = harvesterConfiguration.getMetadataFormat() != null ? harvesterConfiguration.getMetadataFormat() : "oai_dc";

            URI baseUrl = harvesterConfiguration.getRepos().get( position ).getUrl();
            log.info( "Single harvesting {} from {}", baseUrl, fromDate );

            for ( String set : getSpecs( baseUrl ) )
            {
                harvestSet( baseUrl.toString(), set, fromDate );
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

        mdFormat = harvesterConfiguration.getMetadataFormat() != null ? harvesterConfiguration.getMetadataFormat() : "oai_dc";

        if ( fromDate.isEmpty() )
        {
            fromDate = null;
        }

        for ( HarvesterConfiguration.Repo repo : harvesterConfiguration.getRepos() )
        {
            URI baseUrl = repo.getUrl();

            Set<String> sets;
            if ( repo.getSetName() != null && !repo.getSetName().isEmpty() )
            {
                sets = Collections.singleton( repo.getSetName() );
            }
            else if ( repo.discoverSets() )
            {
                sets = getSpecs( baseUrl );
            }
            else
            {
                // detect if the set is explicitly referenced
                if ( baseUrl.getQuery().contains( "set=" ) )
                {
                    sets = Collections.singleton( baseUrl.getQuery().substring( baseUrl.getQuery().indexOf( "set=" ) + 4 ) );
                }
                else
                {
                    sets = Collections.singleton( null );
                }
            }

            for ( String set : sets )
            {
                harvestSet( baseUrl.toString(), set, fromDate );
            }
        }
    }

    private void harvestSet( String baseUrl, String set, String fromDate )
    {
        log.info( "Start to get records for {} / {} from {}", baseUrl, set, fromDate );
        try
        {
            fetchDCRecords( oaiBase( baseUrl ), set, fromDate );
        }
        catch ( HarvesterFailedException e )
        {
            log.error( "Could not harvest {} / {}, {}", baseUrl, set, e.toString() );
        }
    }

    private void fetchDCRecords( String repoBase, String setspec, String fromDate ) throws HarvesterFailedException
    {

        log.info( harvesterConfiguration.getDir() );
        Path path = Paths.get( harvesterConfiguration.getDir() );

        String indexName = shortened( repoBase, setspec );

        final Path repositoryDirectory = path.resolve( indexName );
        try
        {
            Files.createDirectories( repositoryDirectory );
        }
        catch ( IOException e )
        {
            throw new DirectoryCreationFailedException( repositoryDirectory, e );
        }

        log.info( "Fetching records for repo {} and pmh set {}. Be patient, this can take hours.", repoBase, setspec );

        final List<String> currentlyRetrievedSet = getIdentifiersForSet( repoBase, setspec, mdFormat, fromDate );

        writeToLocalFileSystem( currentlyRetrievedSet, repoBase, repositoryDirectory );

        log.info( "retrieved files: {}", currentlyRetrievedSet.size() );
        log.info( "\tSET\t{}\tsize:\t{}\tURL\t{}", setspec, currentlyRetrievedSet.size(), repoBase );
    }

    private List<String> getIdentifiersForSet( String url, String set, String overwrite, String fromDate ) throws HarvesterFailedException
    {

        final String oaiBaseUrl = oaiBase( url );
        try
        {
            String resumptionToken = null;
            final ArrayList<String> records = new ArrayList<>();

            do
            {
                log.debug( "URL: {}, set: {}, list size: {}", url, set, records.size() );

                ListIdentifiers li;
                if ( resumptionToken == null )
                {
                    li = new ListIdentifiers( oaiBaseUrl, fromDate, null, set,
                            Optional.ofNullable( overwrite ).orElse( mdFormat ), harvesterConfiguration.getTimeout() );
                }
                else
                {
                    log.trace( "recurse: url {}\tset {}\ttoken {}", url, set, resumptionToken );
                    li = new ListIdentifiers( oaiBaseUrl, resumptionToken, harvesterConfiguration.getTimeout() );
                }

                Document identifiers = li.getDocument();

                // add to list of records to fetch
                NodeList identifiersIDs = identifiers.getElementsByTagName( "identifier" );
                for ( int i = 0; i < identifiersIDs.getLength(); i++ )
                {
                    String identifier = identifiersIDs.item( i ).getTextContent();
                    records.add( identifier );
                }

                // need to continue looping?
                NodeList resumptionTokenReq = identifiers.getElementsByTagName( "resumptionToken" );
                if ( resumptionTokenReq.getLength() > 0 && !resumptionTokenReq.item( 0 ).getTextContent().isEmpty() )
                {
                    resumptionToken = resumptionTokenReq.item( 0 ).getTextContent();
                }
                else
                {
                    resumptionToken = null;
                }
            }
            while ( resumptionToken != null );

            return records;
        }
        catch ( IOException | SAXException e )
        {
            throw new HarvesterFailedException( "Harvesting failed for " + oaiBaseUrl + "?verb=ListRecords" + "&set=" + set + "&metadataPrefix="
                    + mdFormat + "&from=" + fromDate + ": " + e.toString(), e );
        }
    }

    public void notifyOnError( String subject, String msg )
    {

        log.error( "{}\n{}", subject, msg );
        if ( harvesterConfiguration.getRecipient().compareTo( "" ) == 0 )
        {
            return;
        }
        try
        {
            java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();

            Email noti = new Email();
            String fromEmail = "system.wts@gesis.org";
            noti.from( fromEmail );
            noti.to( harvesterConfiguration.getRecipient().split( "," ) );
            noti.setSubject( localMachine.getHostName() + " : " + subject );

            msg += "\n" + InetAddress.getLoopbackAddress().getHostAddress() + "\n"
                    + InetAddress.getLoopbackAddress().getHostName();
            noti.addText( msg );
            SmtpSslServer smtpServer = SmtpSslServer.create( this.mailHost );
            SendMailSession session = smtpServer.createSession();
            session.open();
            session.close();
        }
        catch ( MailException | UnknownHostException e )
        {
            log.error( "Failed to send notification: {}", e.toString() );
        }

    }

    private void writeToLocalFileSystem( Collection<String> records, String oaiUrl, Path repositoryDirectory )
    {
        for ( String currentRecord : records )
        {
            String fileName = currentRecord + "_" + harvesterConfiguration.getDialectDefinitionName() + ".xml";

            try
            {
                log.debug( "Harvesting {} from {}", currentRecord, oaiUrl );
                GetRecord pmhRecord = new GetRecord( oaiUrl, currentRecord, mdFormat, harvesterConfiguration.getTimeout() );

                final NodeList metadataElements = pmhRecord.getDocument().getElementsByTagName( METADATA );
                if ( metadataElements.getLength() > 0 )
                {
                    final DOMSource source;

                    // remove envelope?
                    if ( harvesterConfiguration.isRemoveOAIEnvelope() )
                    {
                        NodeList metadataChildNodes = metadataElements.item( 0 ).getChildNodes();
                        source = IntStream.range( 0, metadataChildNodes.getLength() ).mapToObj( metadataChildNodes::item )
                                .filter( Element.class::isInstance )
                                .map( DOMSource::new )
                                .findAny().orElseThrow( () ->
                                        new NoSuchElementException( "No elements with the tag name '" + METADATA + "' were found" )
                                );
                    }
                    else
                    {
                        source = new DOMSource( pmhRecord.getDocument() );
                    }

                    Path fdest = repositoryDirectory.resolve( URLEncoder.encode( fileName, StandardCharsets.UTF_8.name() ) );
                    try ( OutputStream fOutputStream = Files.newOutputStream( fdest ) )
                    {
                        log.trace( "Writing to {}", fdest );
                        factory.newTransformer().transform( source, new StreamResult( fOutputStream ) );
                    }
                }
            }
            catch ( TransformerConfigurationException e )
            {
                // This is not recoverable
                throw new IllegalStateException( e );
            }
            catch ( IOException | SAXException | TransformerException e1 )
            {
                log.error( "Failed to harvest record {}: {}", currentRecord.trim(), e1.toString() );
            }
        }
    }

    private Set<String> getSpecs( URI url )
    {
        try
        {
            final Set<String> unfoldedSets = getSetStrings( url );
            log.info( "No. of sets: {}", unfoldedSets.size() );
            return unfoldedSets;
        }
        catch ( IOException | TransformerException | SAXException e )
        {
            log.warn( "Repository has no sets defined / no response: set set=all", e );
            // set set=all in case of no sets found
            return Collections.singleton( null );
        }
    }

    /**
     * Retrieves the sets from the OAI-PMH repository using the ListSets verb.
     *
     * @param url the URL of the repository.
     * @return a {@link Set} containing all of the sets in the remote repository.
     */
    public Set<String> getSetStrings( final URI url )
            throws IOException, SAXException, TransformerException
    {
        HashSet<String> unfoldedSets = new HashSet<>();

        URI urlToSend = url;
        String resumptionToken;
        do
        {
            ListSets ls = new ListSets( urlToSend.toString(), harvesterConfiguration.getTimeout() );

            Document document = ls.getDocument();

            NodeList nl = document.getElementsByTagName( "setSpec" );

            for ( int i = 0; i < nl.getLength(); i++ )
            {
                unfoldedSets.add( nl.item( i ).getTextContent() );
            }

            if ( ls.toString().contains( "error" ) )
            {
                log.error( "Invalid request {}", ls );
            }

            resumptionToken = ls.getResumptionToken();
            if ( !resumptionToken.isEmpty() )
            {
                log.info( resumptionToken );
                urlToSend = URI.create( url + "?verb=ListSets&resumptionToken=" + resumptionToken);
            }
        }
        while ( !resumptionToken.isEmpty() );

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
