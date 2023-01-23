package eu.cessda.eqb.harvester;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2023 CESSDA ERIC
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     *     <li>
     *         Otherwise, a {@link Repo.MetadataFormat} with a {@code setSpec} set to {@code null} will be returned.
     *         This will prevent the harvester from using set based harvesting.
     *     </li>
     * </ol>
     *
     * @param repo the repository to get sets for
     * @return a {@link Set} of setSpecs
     */
    Set<Repo.MetadataFormat> discoverSets( Repo repo )
    {
        return repo.metadataPrefixes().stream()
            .flatMap( mf -> {
                // If a set is already configured, or if discovery of sets is not configured, return the current configuration.
                if (mf.setSpec() != null || !repo.discoverSets()) {
                    return Stream.of(mf);
                }

                try
                {
                    var unfoldedSets = new ArrayList<Repo.MetadataFormat>();
                    var ls = ListSets.instance( httpClient, repo.url() );

                    Optional<String> resumptionToken;
                    do
                    {
                        if ( !ls.getErrors().isEmpty() )
                        {
                            log.error( "{}: Error while retrieving the list of sets: {}", repo.code(), ls.getErrors() );
                            break;
                        }

                        unfoldedSets.addAll( ls.getSets().stream().map( s -> new Repo.MetadataFormat( s, mf.metadataPrefix(), mf.ddiVersion(), mf.validationProfile()) ).toList() );

                        resumptionToken = ls.getResumptionToken();
                        if ( resumptionToken.isPresent() )
                        {
                            ls =  ListSets.instance( httpClient, repo.url(), resumptionToken.orElseThrow() );
                        }
                    }
                    while ( resumptionToken.isPresent() );

                    log.debug( "No. of sets: {}", unfoldedSets.size() );
                    return unfoldedSets.stream();
                }
                catch ( IOException | SAXException e )
                {
                    log.warn( "Failed to discover sets from {}: set set=all: {}", repo.code(), e.toString() );
                    // set set=all in case of no sets found
                    return Stream.of(mf);
                }
            } ).collect( Collectors.toSet());
    }

    /**
     * Retrieve record headers from the remote repository.
     * @param repo the repository to harvest.
     * @param metadataFormat the metadata format to harvest.
     * @param fromDate the date to harvest from.
     * @return a list of {@link RecordHeader}s
     * @throws RecordHeaderException if an error occurs retrieving the record headers
     */
    List<RecordHeader> retrieveRecordHeaders( Repo repo, Repo.MetadataFormat metadataFormat, LocalDate fromDate ) throws RecordHeaderException
    {
        log.trace( "URL: {}, set: {}", repo.url(), metadataFormat.setSpec() );
        try
        {
            final var records = new ArrayList<RecordHeader>();
            var li = ListIdentifiers.instance( httpClient, repo.url(), fromDate, null, metadataFormat.setSpec(), metadataFormat.metadataPrefix() );

            Optional<String> resumptionToken;

            do
            {
                // Check for errors, abort if any are found
                if (!li.getErrors().isEmpty()) {
                    log.warn( "[{}]: OAI-PMH errors: {}", repo.code(), li.getErrors() );
                    break;
                }

                // add to list of records to fetch
                records.addAll( li.getIdentifiers() );

                // need to continue looping?
                resumptionToken = li.getResumptionToken();

                if (resumptionToken.isPresent())
                {
                    log.trace( "recurse: url {}\ttoken: {}", repo.url(), resumptionToken );
                    li = ListIdentifiers.instance( httpClient, repo.url(), resumptionToken.orElseThrow() );
                }
            }
            while ( resumptionToken.isPresent() );

            return records;
        }
        catch ( IOException | SAXException | DateTimeParseException e )
        {
            throw new RecordHeaderException( repo, metadataFormat.setSpec(), e );
        }
    }
}
