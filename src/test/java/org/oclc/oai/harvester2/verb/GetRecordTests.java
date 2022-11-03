package org.oclc.oai.harvester2.verb;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetRecordTests {
    // language=XML
    private static final String GET_RECORD_RESPONSE = """
            <?xml version="1.0" encoding="UTF-8"?>\s
            <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"\s
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
                     http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
              <responseDate>2002-02-08T08:55:46Z</responseDate>
              <request verb="GetRecord" identifier="oai:arXiv.org:cs/0112017"
                       metadataPrefix="oai_dc">http://arXiv.org/oai2</request>
              <GetRecord>
               <record>\s
                <header>
                  <identifier>oai:arXiv.org:cs/0112017</identifier>
                  <datestamp>2001-12-14</datestamp>
                  <setSpec>cs</setSpec>\s
                  <setSpec>math</setSpec>
                </header>
                <metadata>
                  <oai_dc:dc\s
                     xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"\s
                     xmlns:dc="http://purl.org/dc/elements/1.1/"\s
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\s
                     xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/
                     http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
                    <dc:title>Using Structural Metadata to Localize Experience of Digital Content</dc:title>
                    <dc:creator>Dushay, Naomi</dc:creator>
                    <dc:subject>Digital Libraries</dc:subject>\s
                    <dc:description>
                        With the increasing technical sophistication of\s
                        both information consumers and providers, there is\s
                        increasing demand for more meaningful experiences of digital\s
                        information. We present a framework that separates digital\s
                        object experience, or rendering, from digital object storage\s
                        and manipulation, so the rendering can be tailored to\s
                        particular communities of users.
                    </dc:description>
                    <dc:description>Comment: 23 pages including 2 appendices, 8 figures</dc:description>\s
                    <dc:date>2001-12-14</dc:date>
                  </oai_dc:dc>
                </metadata>
              </record>
             </GetRecord>
            </OAI-PMH>""";

    // language=XML
    private static final String DELETED_RECORD = """
            <?xml version="1.0" encoding="UTF-8" ?>
            <?xml-stylesheet type='text/xsl' href='oai2.xsl' ?>
            <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
             http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
              <responseDate>2018-01-18T17:11:52Z</responseDate>
              <request verb="GetRecord" identifier="1031" metadataPrefix="ddi">https://oai.ukdataservice.ac.uk:8443/oai/provider</request>
              <GetRecord>
                <record>
                  <header status="deleted">
                    <identifier>1031</identifier>
                    <datestamp>2017-05-02T08:31:32Z</datestamp>
                    <setSpec>DataCollections</setSpec>
                  </header>
                </record>
              </GetRecord>
            </OAI-PMH>""";

    @Test
    void shouldReturnARecordHeader() throws IOException, SAXException {
        var record = new GetRecord(
                new ByteArrayInputStream(GET_RECORD_RESPONSE.getBytes(StandardCharsets.UTF_8)));

        var header = record.getHeader();

        assertEquals(
                new RecordHeader(
                        "oai:arXiv.org:cs/0112017",
                        LocalDate.of(2001, 12, 14),
                        Set.of("cs", "math"),
                        null),
                header);
    }

    @Test
    void shouldHandleADeletedRecord() throws IOException, SAXException {
        var deletedRecord = new GetRecord(
                new ByteArrayInputStream(DELETED_RECORD.getBytes(StandardCharsets.UTF_8)));

        var header = deletedRecord.getHeader();

        assertEquals(RecordHeader.Status.deleted, header.status());
    }
}
