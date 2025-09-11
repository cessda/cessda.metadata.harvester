package org.oclc.oai.harvester2.verb;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2025 CESSDA ERIC
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


import eu.cessda.oaiharvester.HttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListRecordsTests
{
    //language=XML
    private static final String LIST_RECORDS_RESPONSE = """
        <?xml version="1.0" encoding="UTF-8"?>
        <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"\s
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
                 http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
         <responseDate>2002-06-01T19:20:30Z</responseDate>\s
         <request verb="ListRecords" from="1998-01-15"
                  set="physics:hep"
                  metadataPrefix="oai_rfc1807">
                  http://an.oa.org/OAI-script</request>
         <ListRecords>
          <record>
            <header>
              <identifier>oai:arXiv.org:hep-th/9901001</identifier>
              <datestamp>1999-12-25</datestamp>
              <setSpec>physics:hep</setSpec>
              <setSpec>math</setSpec>
            </header>
            <metadata>
             <rfc1807 xmlns=
                "http://info.internet.isi.edu:80/in-notes/rfc/files/rfc1807.txt"\s
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\s
              xsi:schemaLocation=
               "http://info.internet.isi.edu:80/in-notes/rfc/files/rfc1807.txt
                http://www.openarchives.org/OAI/1.1/rfc1807.xsd">
                <bib-version>v2</bib-version>
                <id>hep-th/9901001</id>
                <entry>January 1, 1999</entry>
                <title>Investigations of Radioactivity</title>
                <author>Ernest Rutherford</author>
                <date>March 30, 1999</date>
             </rfc1807>
            </metadata>
            <about>
              <oai_dc:dc\s
                  xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"\s
                  xmlns:dc="http://purl.org/dc/elements/1.1/"\s
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\s
                  xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/\s
                  http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
                <dc:publisher>Los Alamos arXiv</dc:publisher>
                <dc:rights>Metadata may be used without restrictions as long as\s
                   the oai identifier remains attached to it.</dc:rights>
              </oai_dc:dc>
            </about>
          </record>
          <record>
            <header status="deleted">
              <identifier>oai:arXiv.org:hep-th/9901007</identifier>
              <datestamp>1999-12-21</datestamp>
            </header>
          </record>
         </ListRecords>
        </OAI-PMH>
        """;

    @Test
    void shouldReturnAListOfRecords() throws IOException, SAXException
    {
        // When
        var recordList = new ListRecords(
            new InputSource( new ByteArrayInputStream( LIST_RECORDS_RESPONSE.getBytes( StandardCharsets.UTF_8 ) ) )
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

    @Test
    void shouldHandleResumptionToken() throws IOException, SAXException
    {
        // When
        var token = UUID.randomUUID();
        var baseURL = URI.create( "http://localhost:8012/oai" );

        var httpClient = Mockito.mock( HttpClient.class );
        Mockito.when( httpClient.getHttpResponse(  URI.create(baseURL + "?verb=ListRecords&resumptionToken=" + token ) ) )
            .thenReturn( new ByteArrayInputStream( LIST_RECORDS_RESPONSE.getBytes( StandardCharsets.UTF_8 ) ) );

        // Then
        var recordList = ListRecords.instance( httpClient, baseURL, token.toString() );
        assertEquals( 2, recordList.getRecords().getLength() );
    }
}
