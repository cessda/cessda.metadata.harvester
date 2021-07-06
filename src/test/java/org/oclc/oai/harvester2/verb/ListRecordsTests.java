package org.oclc.oai.harvester2.verb;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListRecordsTests
{
    //language=XML
    private static final String LIST_RECORDS_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" \n" +
        "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/\n" +
        "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">\n" +
        " <responseDate>2002-06-01T19:20:30Z</responseDate> \n" +
        " <request verb=\"ListRecords\" from=\"1998-01-15\"\n" +
        "          set=\"physics:hep\"\n" +
        "          metadataPrefix=\"oai_rfc1807\">\n" +
        "          http://an.oa.org/OAI-script</request>\n" +
        " <ListRecords>\n" +
        "  <record>\n" +
        "    <header>\n" +
        "      <identifier>oai:arXiv.org:hep-th/9901001</identifier>\n" +
        "      <datestamp>1999-12-25</datestamp>\n" +
        "      <setSpec>physics:hep</setSpec>\n" +
        "      <setSpec>math</setSpec>\n" +
        "    </header>\n" +
        "    <metadata>\n" +
        "     <rfc1807 xmlns=\n" +
        "        \"http://info.internet.isi.edu:80/in-notes/rfc/files/rfc1807.txt\" \n" +
        "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
        "      xsi:schemaLocation=\n" +
        "       \"http://info.internet.isi.edu:80/in-notes/rfc/files/rfc1807.txt\n" +
        "        http://www.openarchives.org/OAI/1.1/rfc1807.xsd\">\n" +
        "        <bib-version>v2</bib-version>\n" +
        "        <id>hep-th/9901001</id>\n" +
        "        <entry>January 1, 1999</entry>\n" +
        "        <title>Investigations of Radioactivity</title>\n" +
        "        <author>Ernest Rutherford</author>\n" +
        "        <date>March 30, 1999</date>\n" +
        "     </rfc1807>\n" +
        "    </metadata>\n" +
        "    <about>\n" +
        "      <oai_dc:dc \n" +
        "          xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" \n" +
        "          xmlns:dc=\"http://purl.org/dc/elements/1.1/\" \n" +
        "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
        "          xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ \n" +
        "          http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">\n" +
        "        <dc:publisher>Los Alamos arXiv</dc:publisher>\n" +
        "        <dc:rights>Metadata may be used without restrictions as long as \n" +
        "           the oai identifier remains attached to it.</dc:rights>\n" +
        "      </oai_dc:dc>\n" +
        "    </about>\n" +
        "  </record>\n" +
        "  <record>\n" +
        "    <header status=\"deleted\">\n" +
        "      <identifier>oai:arXiv.org:hep-th/9901007</identifier>\n" +
        "      <datestamp>1999-12-21</datestamp>\n" +
        "    </header>\n" +
        "  </record>\n" +
        " </ListRecords>\n" +
        "</OAI-PMH>\n";

    @Test
    void shouldReturnAListOfRecords() throws IOException, SAXException
    {
        // When
        var recordList = new ListRecords(
            new ByteArrayInputStream( LIST_RECORDS_RESPONSE.getBytes( StandardCharsets.UTF_8 ) )
        );

        // Then
        assertEquals( 2, recordList.getRecords().getLength() );

        // Assert correct record identifier for the first record
        var firstRecord = (Element) recordList.getRecords().item( 0 );
        assertEquals(
            "oai:arXiv.org:hep-th/9901001",
            firstRecord.getElementsByTagName( "identifier" ).item( 0 ).getTextContent()
        );

        // Assert that the second record is deleted
        var deletedRecord = (Element) recordList.getRecords().item( 1 );
        var deletedHeader = deletedRecord.getElementsByTagName( "header" ).item( 0 );
        assertEquals(
            "deleted",
            deletedHeader.getAttributes().getNamedItem( "status" ).getTextContent()
        );
    }
}
