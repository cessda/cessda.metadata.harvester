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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListIdentifiersTests
{

    public static final Duration TIMEOUT = Duration.ofSeconds( 10 );

    @Test
    void shouldReturnRecordHeaders() throws IOException, SAXException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        when( httpClient.getHttpResponse( any(URL.class), eq( TIMEOUT )) )
                .thenReturn( new ByteArrayInputStream(
                        RecordHeadersMock.getListIdentifiersXMLResumptionEmpty().getBytes( StandardCharsets.UTF_8 )
                ) );

        var identifiers = new ListIdentifiers( httpClient,
                "https://oai.ukdataservice.ac.uk:8443/oai/provider",
                null,
                null,
                null,
                "ddi",
                TIMEOUT
        );

        var identifiersIDs = identifiers.getIdentifiers();

        assertEquals( 3, identifiersIDs.size());

        for ( var record : identifiersIDs )
        {
            assertThat(
                    record,
                    anyOf( is( "850229" ), is( "850232" ), is( "850235" ) )
            );
        }
    }

    @Test
    void shouldReturnResumptionToken() throws IOException, SAXException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        when( httpClient.getHttpResponse( any(URL.class), eq( TIMEOUT )) )
                .thenReturn( new ByteArrayInputStream(
                        RecordHeadersMock.getListIdentifiersXMLWithResumption().getBytes( StandardCharsets.UTF_8 )
                ) );

        var identifiers = new ListIdentifiers( httpClient,
                "https://oai.ukdataservice.ac.uk:8443/oai/provider",
                null,
                null,
                null,
                "ddi",
                TIMEOUT
        );

        assertEquals("3/6/7/ddi/null/2017-01-01/null", identifiers.getResumptionToken().orElseThrow() );
    }

    @Test
    void shouldReturnEmptyOptionalForAnEmptyResumptionToken() throws IOException, SAXException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        when( httpClient.getHttpResponse( any(URL.class), eq( TIMEOUT )) )
                .thenReturn( new ByteArrayInputStream(
                        RecordHeadersMock.getListIdentifiersXMLResumptionEmpty().getBytes( StandardCharsets.UTF_8 )
                ) );

        var identifiers = new ListIdentifiers( httpClient,
                "https://oai.ukdataservice.ac.uk:8443/oai/provider",
                null,
                null,
                null,
                "ddi",
                TIMEOUT
        );

        assertTrue( identifiers.getResumptionToken().isEmpty() );
    }

    @Test
    void shouldReturnDocumentWhenResumingWithToken() throws IOException, SAXException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        when( httpClient.getHttpResponse( any(URL.class), eq( TIMEOUT )) )
                .thenReturn( new ByteArrayInputStream(
                        RecordHeadersMock.getListIdentifiersXMLWithResumptionLastList().getBytes( StandardCharsets.UTF_8 )
                ) );

        var identifiers = new ListIdentifiers( httpClient,
                "https://oai.ukdataservice.ac.uk:8443/oai/provider",
                "3/6/7/ddi/null/2017-01-01/null",
                TIMEOUT
        );

        // Then
        var identifiersIDs = identifiers.getIdentifiers();

        assertEquals( 1, identifiersIDs.size());

        assertEquals( "998", identifiersIDs.get( 0 ) );
    }

    @Test
    void shouldReturnNoRecordsOnError() throws IOException, SAXException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        when( httpClient.getHttpResponse( any(URL.class), eq( TIMEOUT )) )
                .thenReturn( new ByteArrayInputStream(
                        RecordHeadersMock.getListIdentifiersXMLWithCannotDisseminateFormatError().getBytes( StandardCharsets.UTF_8 )
                ) );

        var identifiers = new ListIdentifiers( httpClient,
                "https://oai.ukdataservice.ac.uk:8443/oai/provider",
                "3/6/7/ddi/null/2017-01-01/null",
                TIMEOUT
        );

        // Then
        var identifiersIDs = identifiers.getIdentifiers();

        assertTrue( identifiersIDs.isEmpty() );
        assertTrue( identifiers.getResumptionToken().isEmpty() );
    }
}