package eu.cessda.eqb.harvester;

import org.oclc.oai.harvester2.verb.ListIdentifiers;
import org.oclc.oai.harvester2.verb.ListSets;
import org.oclc.oai.harvester2.verb.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Holds methods used to access OAI-PMH repositories
 */
@Component
class RepositoryClient
{
    private static final Logger log = LoggerFactory.getLogger( RepositoryClient.class );

    private final HttpClient httpClient;

    /**
     * Create a new instance of a {@link RepositoryClient}.
     */
    RepositoryClient( HttpClient httpClient)
    {
        this.httpClient = httpClient;
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
    Set<String> discoverSets( Repo repo )
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
     * Retrieve record headers from the remote repository.
     * @param repo the repository to harvest.
     * @param set the set to harvest.
     * @param metadataFormat the metadata format to harvest.
     * @param fromDate the date to harvest from.
     * @return a list of {@link RecordHeader}s
     * @throws RecordHeaderException if an error occurs retrieving the record headers
     */
    List<RecordHeader> retrieveRecordHeaders( Repo repo, String set, String metadataFormat, LocalDate fromDate ) throws RecordHeaderException
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
        catch ( IOException | SAXException | DateTimeParseException e )
        {
            throw new RecordHeaderException( repo, set, e );
        }
    }
}
