package org.oclc.oai.harvester2.verb;

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
import org.xml.sax.InputSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.oclc.oai.harvester2.verb.RecordHeadersMock.*;

class ListIdentifiersTests
{

    @Test
    void shouldReturnRecordHeaders() throws IOException, OAIException
    {
        assert GET_LIST_IDENTIFIERS_XML_RESUMPTION_EMPTY != null;

        // Given
        ListIdentifiers identifiers;
        try ( var inputStream = GET_LIST_IDENTIFIERS_XML_RESUMPTION_EMPTY.openStream() )
        {
            identifiers = new ListIdentifiers( new InputSource( inputStream ) );
        }

        var identifiersIDs = identifiers.getIdentifiers();

        assertEquals( 3, identifiersIDs.size());

        assertThat( identifiersIDs ).map( RecordHeader::identifier )
            .containsExactlyInAnyOrder( "850229", "850232", "850235" );
    }

    @Test
    void shouldReturnResumptionToken() throws IOException, OAIException
    {
        assert GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION != null;

        // Given
        ListIdentifiers identifiers;
        try ( var inputStream = GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION.openStream() )
        {
            identifiers = new ListIdentifiers( new InputSource( inputStream ) );
        }

        assertEquals("3/6/7/ddi/null/2017-01-01/null", identifiers.getResumptionToken().orElseThrow() );
    }

    @Test
    void shouldReturnEmptyOptionalForAnEmptyResumptionToken() throws IOException, OAIException
    {
        assert GET_LIST_IDENTIFIERS_XML_RESUMPTION_EMPTY != null;

        // Given
        ListIdentifiers identifiers;
        try ( var inputStream = GET_LIST_IDENTIFIERS_XML_RESUMPTION_EMPTY.openStream() )
        {
            identifiers = new ListIdentifiers( new InputSource( inputStream ) );
        }

        assertTrue( identifiers.getResumptionToken().isEmpty() );
    }

    @Test
    void shouldReturnDocumentWhenResumingWithToken() throws IOException, OAIException
    {
        assert GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION_LAST_LIST != null;

        // Given
        ListIdentifiers identifiers;
        try ( var inputStream = GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION_LAST_LIST.openStream() )
        {
            identifiers = new ListIdentifiers( new InputSource( inputStream ) );
        }

        // Then
        var identifiersIDs = identifiers.getIdentifiers();

        assertEquals( 1, identifiersIDs.size());
        assertEquals( "998", identifiersIDs.get( 0 ).identifier() );
    }

    @Test
    void shouldReturnNoRecordsOnError() throws IOException
    {
        assert GET_LIST_IDENTIFIERS_XML_WITH_CANNOT_DISSEMINATE_FORMAT_ERROR != null;

        // Given
        OAIException oaiException;
        try (var inputStream = GET_LIST_IDENTIFIERS_XML_WITH_CANNOT_DISSEMINATE_FORMAT_ERROR.openStream() ) {
            oaiException = assertThrows( OAIException.class, () -> new ListIdentifiers( new InputSource( inputStream )));
        }

        // Then
        var errors = oaiException.getErrors();
        assertFalse( errors.isEmpty() );

        var error = errors.get( 0 );
        assertEquals( OAIError.Code.cannotDisseminateFormat, error.getCode() );
        assertTrue( error.getMessage().isEmpty() );
    }

    @Test
    void shouldConstructFromResumptionToken() throws IOException, OAIException, InterruptedException
    {
        assert GET_LIST_IDENTIFIERS_XML_RESUMPTION_TOKEN_NOT_MOCKED_FOR_INVALID != null;

        var httpClient = mock( HttpClient.class );

        // When
        var resumptionToken = "3/6/7/ddi/null/2017-01-01/null";

        var httpRequest = HttpRequest.newBuilder( URI.create("http://localhost:4556/?verb=ListIdentifiers&resumptionToken=" + resumptionToken ) ).GET().build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();

        // Given
        when( httpClient.send( httpRequest, bodyHandler ) ).thenAnswer(
            invocation -> RecordHeadersMock.buildResponse(
                invocation.getArgument( 0 ), GET_LIST_IDENTIFIERS_XML_RESUMPTION_TOKEN_NOT_MOCKED_FOR_INVALID.openStream()
            )
        );

        // Then
        var listIdentifiers = ListIdentifiers.instance(
            httpClient, URI.create("http://localhost:4556/"), resumptionToken
        );

        assertEquals(resumptionToken, listIdentifiers.getResumptionToken().orElseThrow());
    }
}
