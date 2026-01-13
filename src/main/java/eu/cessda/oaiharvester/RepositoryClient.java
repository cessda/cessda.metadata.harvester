package eu.cessda.oaiharvester;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2026 CESSDA ERIC
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
     *     <li>If set discovery is enabled, the repository is queried for sets using the {@code ListSets} verb.</li>
     *     <li>
     *         Otherwise, a {@link Repo.OAIConfiguration} with a {@code setSpec} set to {@code null} will be returned.
     *         This will prevent the harvester from using set based harvesting.
     *     </li>
     * </ol>
     *
     * @param repo the repository to get sets for
     * @return a {@link Set} of setSpecs
     */
    @SuppressWarnings( "java:S3776" )
    Set<Repo.OAIConfiguration> discoverSets( Repo repo )
    {
        var mfs = new HashSet<Repo.OAIConfiguration>();

        // If a set is already configured, or if discovery of sets is not configured, return the current configuration.
        if (repo.oaiConfiguration().setSpec() != null || !repo.oaiConfiguration().discoverSets())
        {
            return Set.of(repo.oaiConfiguration());
        }

        int unfoldedSets = 0;

        ListSets ls;

        try
        {
            ls = ListSets.instance( httpClient, repo.oaiConfiguration().url() );
        }
        catch ( IOException | SAXException e )
        {
            log.warn( "Failed to discover sets from {}: set set=all: {}", repo.code(), e.toString() );
            // set set=all in case of no sets found
            return Set.of(repo.oaiConfiguration());
        }


        Optional<String> resumptionToken;
        do
        {
            if ( !ls.getErrors().isEmpty() )
            {
                log.error( "{}: Error while retrieving the list of sets: {}", repo.code(), ls.getErrors() );
                return Set.of(repo.oaiConfiguration());
            }

            for ( String s : ls.getSets() )
            {
                mfs.add( new Repo.OAIConfiguration( repo.oaiConfiguration().url(), repo.oaiConfiguration().metadataPrefix(), s, false ) );
                unfoldedSets++;
            }

            resumptionToken = ls.getResumptionToken();
            if ( resumptionToken.isPresent() )
            {
                try
                {
                    ls = ListSets.instance( httpClient, repo.oaiConfiguration().url(), resumptionToken.orElseThrow() );
                }
                catch ( IOException | SAXException e )
                {
                    log.warn( "Partially discovered sets from {}: {}", repo.code(), e.toString() );
                    return mfs;
                }
            }
        }
        while ( resumptionToken.isPresent() );

        log.debug( "No. of sets: {}", unfoldedSets);

        return mfs;
    }

    /**
     * Retrieve record headers from the remote repository.
     * @param repo the repository to harvest.
     * @param metadataFormat the metadata format to harvest.
     * @param fromDate the date to harvest from.
     * @return a list of {@link RecordHeader}s
     * @throws RecordHeaderException if an error occurs retrieving the record headers
     */
    List<RecordHeader> retrieveRecordHeaders( Repo repo, Repo.OAIConfiguration metadataFormat, LocalDate fromDate ) throws RecordHeaderException
    {
        log.trace( "URL: {}, set: {}", metadataFormat.url(), metadataFormat.setSpec() );
        final var recordMap = new HashMap<String, RecordHeader>();

        try
        {
            var li = ListIdentifiers.instance( httpClient, metadataFormat.url(), metadataFormat.metadataPrefix(), metadataFormat.setSpec(), fromDate, null );

            Optional<String> resumptionToken;

            do
            {
                // Check for errors, abort if any are found
                if (!li.getErrors().isEmpty())
                {
                    var records = List.copyOf( recordMap.values() );
                    throw new RecordHeaderException( repo, metadataFormat.setSpec(), records, li.getErrors() );
                }

                // add to list of records to fetch
                for (var id : li.getIdentifiers())
                {
                    recordMap.putIfAbsent( id.identifier(), id );
                }

                // need to continue looping?
                resumptionToken = li.getResumptionToken();

                if (resumptionToken.isPresent())
                {
                    log.trace( "recurse: url {}\ttoken: {}", metadataFormat.url(), resumptionToken );
                    li = ListIdentifiers.instance( httpClient, metadataFormat.url(), resumptionToken.orElseThrow() );
                }
            }
            while ( resumptionToken.isPresent() );

            return List.copyOf( recordMap.values() );
        }
        catch ( IOException | SAXException | DateTimeParseException e )
        {
            var records = List.copyOf( recordMap.values() );
            throw new RecordHeaderException( repo, metadataFormat.setSpec(), records, e );
        }
    }
}
