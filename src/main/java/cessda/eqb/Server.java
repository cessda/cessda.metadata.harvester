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
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.annotation.PreDestroy;
import javax.xml.XMLConstants;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@EnableScheduling
@SpringBootApplication
@ManagedResource
public class Server extends SpringBootServletInitializer
{

    private static final Logger log = LoggerFactory.getLogger( Server.class );
    private static final Logger hlog = LoggerFactory.getLogger( Server.class );

    private final HarvesterConfiguration harvesterConfiguration;
    private final String mailHost;
    private final TransformerFactory factory;
    private boolean fullIsRunning = false;
    private boolean incrementalIsRunning = false;
    private String to = "";
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

        if ( u.endsWith( "/" ) )
        {
            return u;
        }
        if ( u.contains( "?" ) )
        {
            u = u.substring( 0, u.indexOf( '?' ) );
        }
        return u;
    }

    private static String shortened( String oaiUrl )
    {
        try
        {
            log.trace( oaiUrl );
            return new URI( oaiUrl ).getHost().replace( ".", "_" ).replace( ":", "-" ).toLowerCase();
        }
        catch (URISyntaxException e)
        {
            log.error( e.getMessage(), e );
            return "";
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
        hlog.info( "Single harvesting starting from {}", harvesterConfiguration.getFrom().getSingle() );
        try
        {
            runSingleHarvest( harvesterConfiguration.getFrom().getSingle(), positionInRepoList );
        }
        finally
        {
            incrementalIsRunning = false;
        }
        hlog.info( "Single harvesting finished from {}", harvesterConfiguration.getFrom().getSingle() );
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
    @Scheduled( initialDelay = 10000L, fixedDelay = 315360000000L )
    public String initialHarvesting()
    {

        log.info( harvesterConfiguration.getMetadataFormat() );
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
            return "Not started. An incremental harvesting progress is already running";
        }
        hlog.info( "Full harvest schedule: {}", harvesterConfiguration.getCron().getFull() );
        hlog.info( "Incremental harvest schedule: {}", harvesterConfiguration.getCron().getIncremental() );

        hlog.info( "Initial harvesting starting from {}", harvesterConfiguration.getFrom().getInitial() );
        hlog.info( "Incremental harvesting will start with cron schedule {} from {}",
                harvesterConfiguration.getCron().getIncremental(), harvesterConfiguration.getFrom().getIncremental() );
        try
        {
            runHarvest( harvesterConfiguration.getFrom().getInitial() );
        }
        finally
        {
            incrementalIsRunning = false;
        }
        hlog.info( "Initial harvesting finished from {}", harvesterConfiguration.getFrom().getInitial() );
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
        hlog.info( "Full harvesting started from {}", harvesterConfiguration.getFrom().getFull() );
        fullIsRunning = true;
        runHarvest( harvesterConfiguration.getFrom().getFull() );
        hlog.info( "Full harvesting finished" );
        fullIsRunning = false;
        hlog.info( "Full harvesting finished from {}", harvesterConfiguration.getFrom().getFull() );
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
                hlog.info( "Incremental harvesting started from {}",
                        harvesterConfiguration.getFrom().getIncremental() );
                runHarvest( harvesterConfiguration.getFrom().getIncremental() );
                hlog.info( "Incremental harvesting finished" );

                msg = "Incremental harvesting finished from " + harvesterConfiguration.getFrom().getIncremental();

                harvesterConfiguration.getFrom().setIncremental( newIncFrom );
                hlog.info( "Next incremental harvest will start from {}", newIncFrom );
            }
            else
            {
                msg = "Incremental harvesting already running.";
            }
            hlog.info( msg );
        }
        else
        {
            hlog.info( "No incremental harvesting, as full harvesting is in progress." );
            return "No incremental harvesting, as full harvesting is in progress.";
        }

        incrementalIsRunning = false;

        return msg;
    }

    @Async
    public void runSingleHarvest( String fromDate, Integer position )
    {

        hlog.info( "Harvesting started from {} for repo {}", fromDate, position );
        try
        {
            mdFormat = harvesterConfiguration.getMetadataFormat() != null ? harvesterConfiguration.getMetadataFormat()
                    : "oai_dc";
            to = LocalDate.now().toString();

            String baseUrl = harvesterConfiguration.getRepoBaseUrls().get( position );
            hlog.info( "Single harvesting {} from {}", baseUrl, fromDate );
            if ( !baseUrl.trim().isEmpty() )
            {
                if ( baseUrl.contains( "#" ) )
                {
                    baseUrl = baseUrl.substring( 0, baseUrl.indexOf( '#' ) );
                }
                log.trace( "{} {}", baseUrl, fromDate );
                for ( String set : getSpecs( baseUrl ) )
                {
                    hlog.info( "Start to get  records for {} / {} from {}", baseUrl, set, fromDate );
                    fetchDCRecords( oaiBase( baseUrl ), set, fromDate );
                }
            }
        }
        catch (SecurityException | IllegalArgumentException e)
        {
            log.error( e.getMessage(), e );
            incrementalIsRunning = false;
        }
    }

    private void runHarvest( String fromDate )
    {

        hlog.info( "Harvesting started from {}", fromDate );

        mdFormat = harvesterConfiguration.getMetadataFormat() != null ? harvesterConfiguration.getMetadataFormat() : "oai_dc";
        to = LocalDate.now().toString();

        for ( HarvesterConfiguration.Repo repo : harvesterConfiguration.getRepos() )
        {
            String baseUrl = repo.getUrl();
            if ( !baseUrl.trim().isEmpty() )
            {
                if ( baseUrl.contains( "#" ) )
                {
                    baseUrl = baseUrl.substring( 0, baseUrl.indexOf( '#' ) );
                }

                Set<String> sets;
                if ( repo.getSetName() != null )
                {
                    sets = Collections.singleton( repo.getSetName() );
                }
                else
                {
                    sets = getSpecs( baseUrl );
                }

                for ( String set : sets )
                {
                    hlog.info( "Start to get records for {} / {} from {}", baseUrl, set, fromDate );
                    fetchDCRecords( oaiBase( baseUrl ), set, fromDate );
                }
            }
        }
    }

    private void fetchDCRecords( String repoBase, String setspec, String fromDate )
    {

        log.info( harvesterConfiguration.getDir() );
        File f = new File( harvesterConfiguration.getDir() );

        log.info( "Fetching records for repo {} and pmh set {}. Be patient, this can take hours.", repoBase, setspec );

        final ArrayList<String> currentlyRetrievedSet = new ArrayList<>();
        getIdentifiersForSet( repoBase, setspec, null, currentlyRetrievedSet, mdFormat,
                fromDate );
        writeToLocalFileSystem( currentlyRetrievedSet, repoBase, setspec, f.getAbsolutePath() );

        log.info( "retrieved files: {}", currentlyRetrievedSet.size() );
        log.info( "\tSET\t{}\tsize:\t{}\tURL\t{}", setspec, currentlyRetrievedSet.size(), repoBase );
    }

    private void getIdentifiersForSet(
            String url,
            String set,
            String resumptionToken,
            List<String> records,
            String overwrite,
            String fromDate )
    {

        final String oaiBaseUrl = oaiBase( url );
        log.info( "URL: {}, set: {}, list size: {}, restoken {}", url, set, records.size(), resumptionToken );
        try
        {
            log.trace( "recurse:\turl {} set {} token {}", url, set, resumptionToken );
            ListIdentifiers li;
            if ( resumptionToken != null )
            {
                log.trace( url );
                li = new ListIdentifiers( oaiBaseUrl, resumptionToken, harvesterConfiguration.getTimeout() );
            }
            else
            {
                // TODO Due to changes by setting set not to all in an exception I need to check for empty set.
                // set must be null or set = "all", if in configuration set is not set....
                if ( set.compareTo( url ) == 0 || set.isEmpty() )
                {
                    set = null;
                }
                log.debug( "From {}, until {}, {}, {}, {}", fromDate, to, oaiBaseUrl, set, mdFormat );
                li = new ListIdentifiers( oaiBaseUrl, fromDate, to, set,
                        Optional.ofNullable( overwrite ).orElse( mdFormat ), harvesterConfiguration.getTimeout() );
            }
            log.trace( li.getRequestURL() );
            Document identifiers = li.getDocument();

            // add to list of records to fetch
            NodeList identifiersIDs = identifiers.getElementsByTagName( "identifier" );
            IntStream.range( 0, identifiersIDs.getLength() ).mapToObj( identifiersIDs::item )
                    .map( Node::getTextContent ).filter( Objects::nonNull )
                    .forEach( records::add );

            // need to recurse?
            NodeList resumptionTokenReq = identifiers.getElementsByTagName( "resumptionToken" );
            if ( resumptionTokenReq.getLength() > 0 )
            {
                Node resumptionTokenNode = resumptionTokenReq.item( 0 );
                if ( !resumptionTokenNode.getTextContent().isEmpty() )
                {
                    String rTok = resumptionTokenNode.getTextContent();
                    getIdentifiersForSet( url, set, rTok, records, null, fromDate );
                    // need to interrupt recursion?
                }
                if ( resumptionTokenNode.hasAttributes() && resumptionTokenNode.getAttributes().getNamedItem( "completeListSize" ) != null )
                {
                    long itemsInCurrentSet = Long.parseLong(
                            resumptionTokenNode.getAttributes().getNamedItem( "completeListSize" )
                    .getTextContent() );
                    log.info( "Items in current set: {}", itemsInCurrentSet );
                }
            }
        }
        catch (IOException | SAXException | DOMException | TransformerException | NumberFormatException e)
        {
            try (StringWriter stackTraceWriter = new StringWriter())
            {
                e.printStackTrace( new PrintWriter( stackTraceWriter ) );
                this.notifyOnError(
                        "Harvesting failed for " + oaiBaseUrl + "?verb=ListRecords" + "&set=" + set + "&metadataPrefix="
                                + mdFormat + "&from=" + fromDate + "&resumptionToken=" + resumptionToken,
                        stackTraceWriter.toString() );
            }
            catch (IOException ioException)
            {
                log.error( "Error occurred when printing stacktrace: {}", ioException.toString() );
            }
        }
        log.trace( "Records to fetch : {}", records.size() );
    }

    protected void notifyOnError( String subject, String msg )
    {

        hlog.error( "{}\n{}", subject, msg );
        if ( harvesterConfiguration.getRecipient().compareTo( "" ) == 0 )
        {
            log.warn( "no recipient for notifications given in config." );
            return;
        }
        try
        {
            java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();

            Email noti = new Email();
            String fromEmail = "system.wts@gesis.org";
            noti.from( fromEmail );
            log.debug( fromEmail );
            if ( log.isDebugEnabled() )
                log.debug( harvesterConfiguration.getRecipient() );
            noti.to( harvesterConfiguration.getRecipient().split( "," ) );
            noti.setSubject( localMachine.getHostName() + " : " + subject );

            msg += "\n" + InetAddress.getLoopbackAddress().getHostAddress() + "\n"
                    + InetAddress.getLoopbackAddress().getHostName();
            noti.addText( msg );
            SmtpSslServer smtpServer = SmtpSslServer.create( this.mailHost );
            log.warn( "smtp port {}", smtpServer.getPort() );
            SendMailSession session = smtpServer.createSession();
            session.open();
            if ( log.isErrorEnabled() )
                log.error( session.sendMail( noti ) );
            log.debug( "mail sent to {}", harvesterConfiguration.getRecipient() );
            session.close();
        }
        catch (MailException | UnknownHostException e)
        {
            log.error( "Failed to send notification: {}", e.toString() );
        }

    }

    protected void writeToLocalFileSystem( Collection<String> records, String oaiUrl, String specId, String path )
    {

        log.info( "{}\t{}\t{}", oaiUrl, specId, path );
        String indexName = shortened( oaiUrl ) + "-" + specId;
        Path dest = Paths.get( path, indexName.replace( ":", "-" ).replace( "\\", "-" ).replace( "/", "-" ) );
        try
        {
            Files.createDirectories( dest );

            log.trace( "{}  {}", dest.toAbsolutePath(), Files.exists( dest ) );

            records.stream().map( String::trim ).forEach( currentRecord ->
            {

                String fname = (indexName + "__" + currentRecord + "_"
                        + harvesterConfiguration.getDialectDefinitionName()
                        + ".xml").replace( ":", "-" ).replace( "\\", "-" ).replace( "/", "-" );

                try
                {
                    GetRecord pmhRecord = new GetRecord( oaiUrl, currentRecord, mdFormat,
                            harvesterConfiguration.getTimeout() );
                    log.trace( pmhRecord.toString() );

                    Path fdest = Paths.get( path,
                            indexName.replace( ":", "-" ).replace( "\\", "-" ).replace( "/", "-" ), fname );
                    if ( pmhRecord.getDocument().getElementsByTagName( "metadata" ).getLength() > 0 )
                    {
                        final DOMSource source;

                        // remove envelope?
                        if ( harvesterConfiguration.isRemoveOAIEnvelope() )
                        {
                            NodeList metadataElements = pmhRecord.getDocument().getElementsByTagName( "metadata" ).item( 0 )
                                        .getChildNodes();
                                source = IntStream.range( 0, metadataElements.getLength() ).mapToObj( metadataElements::item )
                                        .filter( Element.class::isInstance )
                                        .map( DOMSource::new )
                                    .findAny().orElseThrow( () -> new NoSuchElementException(
                                                "No elements with the tag name 'metadata' were found" ) );
                        }
                        else
                        {
                            source = new DOMSource( pmhRecord.getDocument() );
                        }

                        try ( OutputStream fOutputStream = Files.newOutputStream( fdest ) )
                        {
                            factory.newTransformer().transform( source, new StreamResult( fOutputStream ) );
                        }

                        log.trace( "Stored : {}", fdest.toAbsolutePath() );
                    }
                    else
                    {
                        NodeList errorList = pmhRecord.getDocument().getElementsByTagName( "error" );
                        if ( errorList.getLength() == 0 )
                        {
                            log.debug( " no error provided\n" + fdest.toString() + "\n" +
                                    oaiUrl + "\n" + currentRecord + "\n" );
                            NodeList header = pmhRecord.getDocument().getElementsByTagName( "header" );
                            Node status = header.item( 0 ).getAttributes().getNamedItem( "status" );
                            log.warn( "Status: {}", status.getTextContent() );
                        }
                        else
                        {
                            log.error( " error provided:\n{}", errorList.item( 0 ).getTextContent() );
                        }
                    }
                }
                catch ( NoSuchElementException e1 )
                {
                    log.warn( "Error processing {}. Skip and continue: {}", fname, e1.getMessage() );
                }
                catch ( IOException | SAXException | TransformerException e1 )
                {
                    log.error( "Failed to harvest record {}: {}", currentRecord, e1.getMessage() );
                }
            } );
        }
        catch (IOException e)
        {
            log.error( "{}", oaiUrl, e );
        }
    }

    private static void addSet( String url, Set<String> unfoldedSets )
    {
        if ( unfoldedSets.add( url ) )
        {
            log.info( "Set: {}", url );
        }
    }

    Set<String> getSpecs( String url )
    {

        HashSet<String> unfoldedSets = new HashSet<>();
        // skip if set is explicitly referenced
        if ( url.isEmpty() )
        {
            return unfoldedSets;
        }
        if ( url.contains( "set=" ) )
        {
            unfoldedSets.add( url.substring( url.indexOf( "set=" ) + 4 ) );
            return unfoldedSets;
        }
        try
        {
            getSetStrings( url, unfoldedSets );
        }
        catch (IOException | TransformerException | SAXException e)
        {
            log.error( "Repository has no sets defined / no response: set set=all", e );
            // set set=all in case of no sets found
            addSet( "all", unfoldedSets );
            return unfoldedSets;
        }
        log.info( "No. of sets: {}", unfoldedSets.size() );
        return unfoldedSets;
    }

    private void getSetStrings( String url, Set<String> unfoldedSets )
            throws IOException, SAXException, TransformerException
    {
        try
        {
            StringBuilder urlBuilder = new StringBuilder( url );
            ListSets ls;
            do
            {
                if ( log.isWarnEnabled() )
                {
                    log.warn( urlBuilder.toString() );
                }
                ls = new ListSets( urlBuilder.toString().trim(), harvesterConfiguration.getTimeout() );

                Document document = ls.getDocument();

                NodeList nl = document.getElementsByTagName( "setSpec" );

                for ( int i = 0; i <= nl.getLength() - 1; i++ )
                {
                    String setSpec = nl.item( i ).getTextContent();
                    addSet( setSpec, unfoldedSets );
                }
                if ( ls.toString().contains( "error" ) )
                {
                    log.error( "Invalid request {}", ls );

                }
                if ( !ls.getResumptionToken().isEmpty() )
                {
                    log.info( ls.getResumptionToken() );
                    urlBuilder.append( "?verb=ListSets&resumptionToken=" ).append( ls.getResumptionToken() );
                    if ( log.isInfoEnabled() )
                    {
                        log.info( urlBuilder.toString() );
                    }
                }
            } while (unfoldedSets.size() % 50 == 0 && !ls.getResumptionToken().isEmpty()
                    && !urlBuilder.toString().trim().isEmpty());
        }

        catch (SocketTimeoutException ste)
        {
            log.error( "Request to oai endpoint timed out {}", harvesterConfiguration.getTimeout() );
        }
    }

    @PreDestroy
    void printConfig()
    {
        if ( log.isInfoEnabled() )
            hlog.info( harvesterConfiguration.toString() );
    }
}
