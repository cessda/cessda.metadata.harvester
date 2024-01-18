package org.oclc.oai.harvester2.verb;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2024 CESSDA ERIC
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


import eu.cessda.oaiharvester.HttpClient;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.oclc.oai.harvester2.verb.RecordHeadersMock.*;

class ListIdentifiersTests
{
    //language=XML
    private static final String NSD_DATE_FORMAT_RESPONSE = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/">
            <responseDate>2021-07-04T00:00:29.482Z</responseDate>
            <request verb="ListIdentifiers" metadataPrefix="oai_ddi">http://129.177.90.182/</request>
            <ListIdentifiers>
                <header>
                    <identifier>http://nsddata.nsd.uib.no:80/obj/fStudy/NSD2200-2</identifier>
                    <datestamp>2020-09-02T15:12:03+0000</datestamp>
                </header>
            </ListIdentifiers>
        </OAI-PMH>""";

    @Test
    void shouldReturnRecordHeaders() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers( new ByteArrayInputStream(
                GET_LIST_IDENTIFIERS_XML_RESUMPTION_EMPTY.getBytes( UTF_8 )
        ) );

        var identifiersIDs = identifiers.getIdentifiers();

        assertEquals( 3, identifiersIDs.size());

        assertThat( identifiersIDs.stream().map( RecordHeader::identifier ).collect( Collectors.toList()) )
            .containsExactlyInAnyOrder( "850229", "850232", "850235" );
    }

    @Test
    void shouldReturnResumptionToken() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers( new ByteArrayInputStream(
                GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION.getBytes( UTF_8 )
        ));

        assertEquals("3/6/7/ddi/null/2017-01-01/null", identifiers.getResumptionToken().orElseThrow() );
    }

    @Test
    void shouldReturnEmptyOptionalForAnEmptyResumptionToken() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers(new ByteArrayInputStream(
                GET_LIST_IDENTIFIERS_XML_RESUMPTION_EMPTY.getBytes( UTF_8 )
        ));

        assertTrue( identifiers.getResumptionToken().isEmpty() );
    }

    @Test
    void shouldReturnDocumentWhenResumingWithToken() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers(new ByteArrayInputStream(
                GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION_LAST_LIST.getBytes( UTF_8 )
        ));

        // Then
        var identifiersIDs = identifiers.getIdentifiers();

        assertEquals( 1, identifiersIDs.size());

        assertEquals( "998", identifiersIDs.get( 0 ).identifier() );
    }

    @Test
    void shouldReturnNoRecordsOnError() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers( new ByteArrayInputStream(
                GET_LIST_IDENTIFIERS_XML_WITH_CANNOT_DISSEMINATE_FORMAT_ERROR.getBytes( UTF_8 )
        ));

        // Then
        var identifiersIDs = identifiers.getIdentifiers();

        assertTrue( identifiersIDs.isEmpty() );
        assertTrue( identifiers.getResumptionToken().isEmpty() );

        var errors = identifiers.getErrors();
        assertFalse( errors.isEmpty() );

        var error = errors.get( 0 );
        assertEquals( OAIError.Code.cannotDisseminateFormat, error.getCode() );
        assertTrue( error.getMessage().isEmpty() );
    }

    @Test
    void shouldConstructFromResumptionToken() throws IOException, SAXException
    {
        var httpClient = mock( HttpClient.class );

        // When
        var resumptionToken = "3/6/7/ddi/null/2017-01-01/null";
        when( httpClient.getHttpResponse(
            URI.create("http://localhost:4556/?verb=ListIdentifiers&resumptionToken=" + encode( resumptionToken, UTF_8 ) ) )
        ).thenReturn( new ByteArrayInputStream(
            GET_LIST_IDENTIFIERS_XML_RESUMPTION_TOKEN_NOT_MOCKED_FOR_INVALID.getBytes( UTF_8 )
        ));

        // Then
        var listIdentifiers = ListIdentifiers.instance(
            httpClient, URI.create("http://localhost:4556/"), resumptionToken
        );

        assertEquals(resumptionToken, listIdentifiers.getResumptionToken().orElseThrow());
    }

    @Test
    void shouldHandleNSDDateFormat() throws IOException, SAXException
    {
        // When
        var listIdentifiers = new ListIdentifiers(
            new ByteArrayInputStream( NSD_DATE_FORMAT_RESPONSE.getBytes( UTF_8 ) )
        );

        // Then
        var identifiersIDs = listIdentifiers.getIdentifiers();

        var firstIdentifer = identifiersIDs.get( 0 );

        // Assert the header has the expected content
        assertEquals( "http://nsddata.nsd.uib.no:80/obj/fStudy/NSD2200-2", firstIdentifer.identifier() );
        assertEquals( OffsetDateTime.parse( "2020-09-02T15:12:03Z" ), firstIdentifer.datestamp() );
    }
}
