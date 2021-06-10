package org.oclc.oai.harvester2.verb;

import eu.cessda.eqb.harvester.HttpClient;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListSetsTests
{
    private static final URI BASE_URL = URI.create( "https://oai.ukdataservice.ac.uk:8443/oai/provider" );

    @Test
    void shouldReturnSets() throws IOException, SAXException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        when( httpClient.getHttpResponse( any( URL.class), any(Duration.class) ) )
                .thenReturn( new ByteArrayInputStream(
                        ListSetsMock.LIST_SETS_XML.getBytes( StandardCharsets.UTF_8 )
                ) );

        // Then
        var listSets = new ListSets( httpClient, BASE_URL );

        var sets = listSets.getSets();

        assertEquals(4, sets.size());

        assertThat( sets ).containsExactlyInAnyOrder( "music", "music:(muzak)", "music:(elec)", "video" );
    }

    @Test
    void shouldReturnErrorWhenRepositoryDoesNotSupportSets() throws IOException, SAXException
    {
        // Given
        var httpClient = mock( HttpClient.class );

        when( httpClient.getHttpResponse( any( URL.class), any(Duration.class) ) )
                .thenReturn( new ByteArrayInputStream(
                        ListSetsMock.SETS_NOT_SUPPORTED_ERROR.getBytes( StandardCharsets.UTF_8 )
                ) );

        // Then
        var listSets = new ListSets( httpClient, BASE_URL );

        var sets = listSets.getSets();

        assertEquals( 0, sets.size() );

        var errors = listSets.getErrors();

        assertEquals( 1, errors.getLength() );

        assertEquals(
                "noSetHierarchy",
                errors.item( 0 ).getAttributes().getNamedItem( "code" ).getTextContent()
        );
    }
}
