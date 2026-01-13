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

import org.junit.jupiter.api.Test;
import org.oclc.oai.harvester2.verb.RecordHeader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.oclc.oai.harvester2.verb.RecordHeadersMock.*;

class RepositoryClientTests
{
    private final Repo.MetadataFormat metadataFormat = new Repo.MetadataFormat(
        "ddi",
        null,
        null,
        null
    );

    private final Repo repo = new Repo(
        Set.of( metadataFormat ),
        "TEST",
        "Test Repository",
        URI.create( "http://localhost:4556/" ),
        true,
        null,
        null
    );

    @Test
    void shouldGetIdentifiers() throws IOException, RecordHeaderException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        // Initial request
        when(httpClient.getHttpResponse(
            URI.create( "http://localhost:4556/?verb=ListIdentifiers&metadataPrefix=ddi" )
        )).thenReturn( new ByteArrayInputStream(
            GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION.getBytes( UTF_8 )
        ));

        // Resumption token request
        var resumptionToken = "3/6/7/ddi/null/2017-01-01/null";
        when(httpClient.getHttpResponse(
            URI.create("http://localhost:4556/?verb=ListIdentifiers&resumptionToken=" + resumptionToken)
        )).thenReturn( new ByteArrayInputStream(
            GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION_LAST_LIST.getBytes( UTF_8 )
        ));

        var repositoryClient = new RepositoryClient( httpClient );

        // Then
        var recordHeaders = repositoryClient.retrieveRecordHeaders( repo, metadataFormat, null );

        assertThat( recordHeaders )
            .hasSize( 4 )
            .map( RecordHeader::identifier )
            .containsExactlyInAnyOrder( "998", "7753", "8300", "8301");
    }

    @Test
    void shouldHandleOAIErrors() throws IOException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        // Initial request
        when(httpClient.getHttpResponse(
            URI.create( "http://localhost:4556/?verb=ListIdentifiers&metadataPrefix=ddi" )
        )).thenReturn( new ByteArrayInputStream(
            GET_LIST_IDENTIFIERS_XML_WITH_CANNOT_DISSEMINATE_FORMAT_ERROR.getBytes( UTF_8 )
        ));

        var repositoryClient = new RepositoryClient( httpClient );

        // Then
        assertThatThrownBy( () -> repositoryClient.retrieveRecordHeaders( repo, metadataFormat, null ) )
            .isInstanceOf( RecordHeaderException.class )
            .extracting( RecordHeaderException.class::cast )
            .extracting( RecordHeaderException::getHeaders, as( list( RecordHeader.class ) ) )
            .isEmpty();
    }

    @Test
    void shouldHandleExceptions() throws IOException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        // Initial request
        when(httpClient.getHttpResponse(
            URI.create( "http://localhost:4556/?verb=ListIdentifiers&metadataPrefix=ddi" )
        )).thenReturn( new ByteArrayInputStream(
            GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION.getBytes( UTF_8 )
        ));

        // Resumption token request
        var resumptionToken = "3/6/7/ddi/null/2017-01-01/null";
        when(httpClient.getHttpResponse(
            URI.create("http://localhost:4556/?verb=ListIdentifiers&resumptionToken=" + resumptionToken)
        )).thenThrow( IOException.class );

        var repositoryClient = new RepositoryClient( httpClient );

        // Then
        assertThatThrownBy( () -> repositoryClient.retrieveRecordHeaders( repo, metadataFormat, null ))
            .isInstanceOf( RecordHeaderException.class )
            .extracting( RecordHeaderException.class::cast )
            .extracting( RecordHeaderException::getHeaders, as( list( RecordHeader.class ) ) )
            .hasSize( 3 )
            .map( RecordHeader::identifier )
            .containsExactlyInAnyOrder( "7753", "8300", "8301");
    }

    @Test
    void shouldDiscoverSets() throws IOException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        // Initial request
        when(httpClient.getHttpResponse(
            URI.create( "http://localhost:4556/?verb=ListSets" )
        )).thenReturn( new ByteArrayInputStream(
            // The XML prelude declares the encoding as UTF-16
            GET_LIST_SETS_XML.getBytes( UTF_16 )
        ));

        // Resumption token request
        var resumptionToken = "token";
        when(httpClient.getHttpResponse(
            URI.create( "http://localhost:4556/?verb=ListSets&resumptionToken=" + resumptionToken )
        )).thenReturn( new ByteArrayInputStream(
            // The XML prelude declares the encoding as UTF-16
            GET_LIST_SETS_XML_LAST_LIST.getBytes( UTF_16 )
        ));

        var repositoryClient = new RepositoryClient( httpClient );

        var recordHeaders = repositoryClient.discoverSets( repo );

        assertThat( recordHeaders )
            .hasSize(9)
            .map( Repo.MetadataFormat::setSpec )
            .containsExactlyInAnyOrder(
                "f2b9352a-d976-4eac-8ee1-0c76da7cfca4",
                "4bd6eef6-99df-40e6-9b11-5b8f64e5cb23",
                "30ea0200-7121-4f01-8d21-a931a182b86d",
                "679a61f5-4246-4c89-b482-924dec09af98",
                "f196cc07-9c99-4725-ad55-5b34f479cf7d",
                "a1bb19bd-a24a-4443-8728-a6ad80eb42b8",
                "8b108ef8-b642-4484-9c49-f88e4bf7cf1d",
                "a51e85bb-6259-4488-8df2-f08cb43485f8",
                "683889c6-f74b-4d5e-92ed-908c0a42bb2d"
            );
    }

    @Test
    void shouldAddDefaultSetOnException() throws IOException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        // Initial request
        when(httpClient.getHttpResponse(
            URI.create( "http://localhost:4556/?verb=ListSets" )
        )).thenThrow( IOException.class );

        var repositoryClient = new RepositoryClient( httpClient );

        var recordHeaders = repositoryClient.discoverSets( repo );

        assertThat( recordHeaders )
            .hasSize(1)
            .map( Repo.MetadataFormat::setSpec )
            .containsExactly( (String) null );
    }
}
