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
    private static final String LIST_SETS_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\"\n" +
            "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">\n" +
            "    <responseDate>2002-08-11T07:21:33Z</responseDate>\n" +
            "    <request verb=\"ListSets\">http://an.oa.org/OAI-script</request>\n" +
            "    <ListSets>\n" +
            "        <set>\n" +
            "            <setSpec>music</setSpec>\n" +
            "            <setName>Music collection</setName>\n" +
            "        </set>\n" +
            "        <set>\n" +
            "            <setSpec>music:(muzak)</setSpec>\n" +
            "            <setName>Muzak collection</setName>\n" +
            "        </set>\n" +
            "        <set>\n" +
            "            <setSpec>music:(elec)</setSpec>\n" +
            "            <setName>Electronic Music Collection</setName>\n" +
            "            <setDescription>\n" +
            "                <oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\"\n" +
            "                      xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n" +
            "                      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "                      xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">\n" +
            "                    <dc:description>This set contains metadata describing\n" +
            "                        electronic music recordings made during the 1950ies\n" +
            "                    </dc:description>\n" +
            "                </oai_dc:dc>\n" +
            "            </setDescription>\n" +
            "        </set>\n" +
            "        <set>\n" +
            "            <setSpec>video</setSpec>\n" +
            "            <setName>Video Collection</setName>\n" +
            "        </set>\n" +
            "    </ListSets>\n" +
            "</OAI-PMH>";

    private static final String SETS_NOT_SUPPORTED_ERROR = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\"\n" +
            "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/\n" +
            "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">\n" +
            "    <responseDate>2001-06-01T19:20:30Z</responseDate>\n" +
            "    <request verb=\"ListSets\">http://purl.org/alcme/etdcat/servlet/OAIHandler</request>\n" +
            "    <error code=\"noSetHierarchy\">This repository does not support sets</error>\n" +
            "</OAI-PMH>";

    @Test
    void shouldReturnSets() throws IOException, SAXException
    {
        // Given
        var inputStream = new ByteArrayInputStream(
                LIST_SETS_XML.getBytes( StandardCharsets.UTF_8 )
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
                SETS_NOT_SUPPORTED_ERROR.getBytes( StandardCharsets.UTF_8 )
        ) ;

        // Then
        var listSets = new ListSets( inputStream );

        var sets = listSets.getSets();

        assertEquals( 0, sets.size() );

        var errors = listSets.getErrors();

        assertEquals( 1, errors.size() );

        assertEquals( OAIError.Code.noSetHierarchy, errors.get( 0 ).getCode() );
        assertEquals( "This repository does not support sets", errors.get( 0 ).getMessage().orElseThrow() );
    }
}
