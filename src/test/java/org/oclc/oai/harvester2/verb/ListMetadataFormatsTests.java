package org.oclc.oai.harvester2.verb;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ListMetadataFormatsTests {
    // language=XML
    private static final String LIST_METADATA_FORMATS_RESPONSE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"\s
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
                     http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
              <responseDate>2002-06-08T15:19:13Z</responseDate>
              <request verb="ListMetadataFormats">http://memory.loc.gov/cgi-bin/oai</request>
              <ListMetadataFormats>
               <metadataFormat>
                <metadataPrefix>oai_dc</metadataPrefix>
                <schema>http://www.openarchives.org/OAI/2.0/oai_dc.xsd</schema>
                <metadataNamespace>http://www.openarchives.org/OAI/2.0/oai_dc/</metadataNamespace>
               </metadataFormat>
               <metadataFormat>
                <metadataPrefix>oai_marc</metadataPrefix>
                <schema>http://www.openarchives.org/OAI/1.1/oai_marc.xsd</schema>
                <metadataNamespace>http://www.openarchives.org/OAI/1.1/oai_marc</metadataNamespace>
               </metadataFormat>
              </ListMetadataFormats>
            </OAI-PMH>""";

    @Test
    void shouldReturnAListOfMetadataFormats() throws IOException, SAXException, URISyntaxException {
        // Given
        var metadataFormats = new ListMetadataFormats(new ByteArrayInputStream(
                LIST_METADATA_FORMATS_RESPONSE.getBytes(StandardCharsets.UTF_8)));

        // Then
        assertThat(metadataFormats.getMetadataFormats()).containsExactly(
                new ListMetadataFormats.MetadataFormat(
                        "oai_dc",
                        URI.create("http://www.openarchives.org/OAI/2.0/oai_dc.xsd"),
                        URI.create("http://www.openarchives.org/OAI/2.0/oai_dc/")),
                new ListMetadataFormats.MetadataFormat(
                        "oai_marc",
                        URI.create("http://www.openarchives.org/OAI/1.1/oai_marc.xsd"),
                        URI.create("http://www.openarchives.org/OAI/1.1/oai_marc")));
    }
}
