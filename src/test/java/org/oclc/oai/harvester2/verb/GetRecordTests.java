package org.oclc.oai.harvester2.verb;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetRecordTests
{
    //language=XML
    private static final String GET_RECORD_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n" +
        "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" \n" +
        "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/\n" +
        "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">\n" +
        "  <responseDate>2002-02-08T08:55:46Z</responseDate>\n" +
        "  <request verb=\"GetRecord\" identifier=\"oai:arXiv.org:cs/0112017\"\n" +
        "           metadataPrefix=\"oai_dc\">http://arXiv.org/oai2</request>\n" +
        "  <GetRecord>\n" +
        "   <record> \n" +
        "    <header>\n" +
        "      <identifier>oai:arXiv.org:cs/0112017</identifier>\n" +
        "      <datestamp>2001-12-14</datestamp>\n" +
        "      <setSpec>cs</setSpec> \n" +
        "      <setSpec>math</setSpec>\n" +
        "    </header>\n" +
        "    <metadata>\n" +
        "      <oai_dc:dc \n" +
        "         xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" \n" +
        "         xmlns:dc=\"http://purl.org/dc/elements/1.1/\" \n" +
        "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
        "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/\n" +
        "         http://www.openarchives.org/OAI/2.0/oai_dc.xsd\">\n" +
        "        <dc:title>Using Structural Metadata to Localize Experience of Digital Content</dc:title>\n" +
        "        <dc:creator>Dushay, Naomi</dc:creator>\n" +
        "        <dc:subject>Digital Libraries</dc:subject> \n" +
        "        <dc:description>\n" +
        "            With the increasing technical sophistication of \n" +
        "            both information consumers and providers, there is \n" +
        "            increasing demand for more meaningful experiences of digital \n" +
        "            information. We present a framework that separates digital \n" +
        "            object experience, or rendering, from digital object storage \n" +
        "            and manipulation, so the rendering can be tailored to \n" +
        "            particular communities of users.\n" +
        "        </dc:description>\n" +
        "        <dc:description>Comment: 23 pages including 2 appendices, 8 figures</dc:description> \n" +
        "        <dc:date>2001-12-14</dc:date>\n" +
        "      </oai_dc:dc>\n" +
        "    </metadata>\n" +
        "  </record>\n" +
        " </GetRecord>\n" +
        "</OAI-PMH>";

    //language=XML
    private static final String DELETED_RECORD = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
        "<?xml-stylesheet type='text/xsl' href='oai2.xsl' ?>\n" +
        "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\"\n" +
        "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/\n" +
        " http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">\n" +
        "  <responseDate>2018-01-18T17:11:52Z</responseDate>\n" +
        "  <request verb=\"GetRecord\" identifier=\"1031\" metadataPrefix=\"ddi\">https://oai.ukdataservice.ac.uk:8443/oai/provider</request>\n" +
        "  <GetRecord>\n" +
        "    <record>\n" +
        "      <header status=\"deleted\">\n" +
        "        <identifier>1031</identifier>\n" +
        "        <datestamp>2017-05-02T08:31:32Z</datestamp>\n" +
        "        <setSpec>DataCollections</setSpec>\n" +
        "      </header>\n" +
        "    </record>\n" +
        "  </GetRecord>\n" +
        "</OAI-PMH>";

    @Test
    void shouldReturnARecordHeader() throws IOException, SAXException
    {
        var record = new GetRecord(
            new ByteArrayInputStream( GET_RECORD_RESPONSE.getBytes( StandardCharsets.UTF_8 ) )
        );

        var header = record.getHeader();

        assertEquals(
            new RecordHeader(
                "oai:arXiv.org:cs/0112017",
                LocalDate.of( 2001, 12, 14 ),
                Set.of("cs", "math"),
                null
            ),
            header
        );
    }

    @Test
    void shouldHandleADeletedRecord() throws IOException, SAXException
    {
        var deletedRecord = new GetRecord(
            new ByteArrayInputStream( DELETED_RECORD.getBytes( StandardCharsets.UTF_8 ) )
        );

        var header = deletedRecord.getHeader();

        assertEquals( RecordHeader.Status.deleted, header.getStatus().orElseThrow() );
    }
}
