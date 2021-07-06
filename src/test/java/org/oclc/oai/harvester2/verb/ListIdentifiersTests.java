package org.oclc.oai.harvester2.verb;

import eu.cessda.eqb.harvester.HttpClient;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
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
    @Test
    void shouldReturnRecordHeaders() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers( new ByteArrayInputStream(
                getListIdentifiersXMLResumptionEmpty().getBytes( UTF_8 )
        ) );

        var identifiersIDs = identifiers.getIdentifiers();

        assertEquals( 3, identifiersIDs.size());

        assertThat( identifiersIDs.stream().map( RecordHeader::getIdentifier ).collect( Collectors.toList()) )
            .containsExactlyInAnyOrder( "850229", "850232", "850235" );
    }

    @Test
    void shouldReturnResumptionToken() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers( new ByteArrayInputStream(
                getListIdentifiersXMLWithResumption().getBytes( UTF_8 )
        ));

        assertEquals("3/6/7/ddi/null/2017-01-01/null", identifiers.getResumptionToken().orElseThrow() );
    }

    @Test
    void shouldReturnEmptyOptionalForAnEmptyResumptionToken() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers(new ByteArrayInputStream(
                getListIdentifiersXMLResumptionEmpty().getBytes( UTF_8 )
        ));

        assertTrue( identifiers.getResumptionToken().isEmpty() );
    }

    @Test
    void shouldReturnDocumentWhenResumingWithToken() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers(new ByteArrayInputStream(
                getListIdentifiersXMLWithResumptionLastList().getBytes( UTF_8 )
        ));

        // Then
        var identifiersIDs = identifiers.getIdentifiers();

        assertEquals( 1, identifiersIDs.size());

        assertEquals( "998", identifiersIDs.get( 0 ).getIdentifier() );
    }

    @Test
    void shouldReturnNoRecordsOnError() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers( new ByteArrayInputStream(
                getListIdentifiersXMLWithCannotDisseminateFormatError().getBytes( UTF_8 )
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
            getListIdentifiersXMLResumptionTokenNotMockedForInvalid().getBytes( UTF_8 )
        ));

        // Then
        var listIdentifiers = ListIdentifiers.instance(
            httpClient, URI.create("http://localhost:4556/"), resumptionToken
        );

        assertEquals(resumptionToken, listIdentifiers.getResumptionToken().orElseThrow());
    }
}
