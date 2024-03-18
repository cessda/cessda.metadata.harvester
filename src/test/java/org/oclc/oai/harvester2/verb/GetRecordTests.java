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


import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GetRecordTests
{
    //language=XML
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
              <oai_dc:dc
                 xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
                 xmlns:dc="http://purl.org/dc/elements/1.1/"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
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

    //language=XML
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
    void shouldReturnARecordHeader() throws IOException, SAXException
    {
        var record = new GetRecord(
            new InputSource( new ByteArrayInputStream( GET_RECORD_RESPONSE.getBytes( StandardCharsets.UTF_8 ) ) )
        );

        var header = record.getHeader();

        assertEquals(
            new RecordHeader(
                "oai:arXiv.org:cs/0112017",
                LocalDate.of( 2001, 12, 14 ),
                Set.of( "cs", "math" ),
                null
            ),
            header
        );
    }

    @Test
    void shouldReturnARecordsMetadata() throws IOException, SAXException
    {
        var record = new GetRecord(
            new InputSource( new ByteArrayInputStream( GET_RECORD_RESPONSE.getBytes( StandardCharsets.UTF_8 ) ) )
        );

        assertThat( record.getMetadata() )
            .isPresent()
            .hasValueSatisfying( element ->
            {
                assertThat( element.getNamespaceURI() ).isEqualTo( "http://www.openarchives.org/OAI/2.0/oai_dc/" );
                assertThat( element.getLocalName() ).isEqualTo( "dc" );
            } );
    }


    @Test
    void shouldHandleADeletedRecord() throws IOException, SAXException
    {
        var deletedRecord = new GetRecord(
            new InputSource( new ByteArrayInputStream( DELETED_RECORD.getBytes( StandardCharsets.UTF_8 ) ) )
        );

        var header = deletedRecord.getHeader();

        assertEquals( RecordHeader.Status.deleted, header.status() );
    }
}
