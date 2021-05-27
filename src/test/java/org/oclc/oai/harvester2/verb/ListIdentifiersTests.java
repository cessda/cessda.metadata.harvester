package org.oclc.oai.harvester2.verb;

import eu.cessda.eqb.harvester.HttpClient;
import eu.cessda.eqb.harvester.RecordHeadersMock;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListIdentifiersTests
{
    @Test
    void shouldReturnRecordHeaders() throws IOException, SAXException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        when( httpClient.getHttpResponse( any(URL.class), eq(Duration.ofSeconds( 10 ) )) )
                .thenReturn( new ByteArrayInputStream(
                        RecordHeadersMock.getListIdentifiersXMLResumptionEmpty().getBytes( StandardCharsets.UTF_8 )
                ) );

        var identifiers = new ListIdentifiers( httpClient,
                "https://oai.ukdataservice.ac.uk:8443/oai/provider",
                null,
                null,
                null,
                "ddi",
                10
        );

        var identifiersIDs = identifiers.getDocument().getElementsByTagName( "identifier" );

        assertEquals( 3, identifiersIDs.getLength());

        IntStream.range( 0, identifiersIDs.getLength() ).mapToObj( identifiersIDs::item ).forEach( i ->
                assertThat(
                    i.getTextContent(),
                    anyOf( is( "850229" ), is( "850232" ), is( "850235" ) )
                )
        );
    }

    @Test
    void shouldReturnResumptionToken() throws IOException, SAXException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        when( httpClient.getHttpResponse( any(URL.class), eq(Duration.ofSeconds( 10 ) )) )
                .thenReturn( new ByteArrayInputStream(
                        RecordHeadersMock.getListIdentifiersXMLWithResumption().getBytes( StandardCharsets.UTF_8 )
                ) );

        var identifiers = new ListIdentifiers( httpClient,
                "https://oai.ukdataservice.ac.uk:8443/oai/provider",
                null,
                null,
                null,
                "ddi",
                10
        );

        assertEquals("3/6/7/ddi/null/2017-01-01/null", identifiers.getResumptionToken() );
    }

    @Test
    void shouldReturnDocumentWhenResumingWithToken() throws IOException, SAXException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        when( httpClient.getHttpResponse( any(URL.class), eq(Duration.ofSeconds( 10 ) )) )
                .thenReturn( new ByteArrayInputStream(
                        RecordHeadersMock.getListIdentifiersXMLWithResumptionLastList().getBytes( StandardCharsets.UTF_8 )
                ) );

        var identifiers = new ListIdentifiers( httpClient,
                "https://oai.ukdataservice.ac.uk:8443/oai/provider",
                "3/6/7/ddi/null/2017-01-01/null",
                10
        );

        // Then
        var identifiersIDs = identifiers.getDocument().getElementsByTagName( "identifier" );

        assertEquals( 1, identifiersIDs.getLength());

        assertEquals( "998", identifiersIDs.item( 0 ).getTextContent() );
    }
}