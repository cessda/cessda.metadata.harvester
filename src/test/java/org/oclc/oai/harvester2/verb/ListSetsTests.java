package org.oclc.oai.harvester2.verb;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ListSetsTests
{
    @Test
    void shouldReturnSets() throws IOException, SAXException
    {
        // Given
        var inputStream = new ByteArrayInputStream(
                ListSetsMock.LIST_SETS_XML.getBytes( StandardCharsets.UTF_8 )
        );

        // Then
        var listSets = new ListSets( inputStream );

        var sets = listSets.getSets();

        assertEquals(4, sets.size());

        assertThat( sets ).containsExactlyInAnyOrder( "music", "music:(muzak)", "music:(elec)", "video" );
    }

    @Test
    void shouldReturnErrorWhenRepositoryDoesNotSupportSets() throws IOException, SAXException
    {
        // Given
        var inputStream = new ByteArrayInputStream(
                ListSetsMock.SETS_NOT_SUPPORTED_ERROR.getBytes( StandardCharsets.UTF_8 )
        ) ;

        // Then
        var listSets = new ListSets( inputStream );

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
