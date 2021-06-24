package org.oclc.oai.harvester2.verb;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListMetadataFormatsTests
{
    //language=XML
    private static final String LIST_METADATA_FORMATS_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" \n" +
        "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/\n" +
        "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">\n" +
        "  <responseDate>2002-06-08T15:19:13Z</responseDate>\n" +
        "  <request verb=\"ListMetadataFormats\">http://memory.loc.gov/cgi-bin/oai</request>\n" +
        "  <ListMetadataFormats>\n" +
        "   <metadataFormat>\n" +
        "    <metadataPrefix>oai_dc</metadataPrefix>\n" +
        "    <schema>http://www.openarchives.org/OAI/2.0/oai_dc.xsd</schema>\n" +
        "    <metadataNamespace>http://www.openarchives.org/OAI/2.0/oai_dc/</metadataNamespace>\n" +
        "   </metadataFormat>\n" +
        "   <metadataFormat>\n" +
        "    <metadataPrefix>oai_marc</metadataPrefix>\n" +
        "    <schema>http://www.openarchives.org/OAI/1.1/oai_marc.xsd</schema>\n" +
        "    <metadataNamespace>http://www.openarchives.org/OAI/1.1/oai_marc</metadataNamespace>\n" +
        "   </metadataFormat>\n" +
        "  </ListMetadataFormats>\n" +
        "</OAI-PMH>";

    //language=XML
    private static final String LIST_METADATA_FORMATS_IDENTIFIER_RESPONSE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" \n" +
        "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
        "         xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/\n" +
        "         http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\">\n" +
        "  <responseDate>2002-02-08T14:27:19Z</responseDate>\n" +
        "  <request verb=\"ListMetadataFormats\" identifier=\"oai:perseus.tufts.edu:Perseus:text:1999.02.0119\">\n" +
        "    http://www.perseus.tufts.edu/cgi-bin/pdataprov\n" +
        "  </request>\n" +
        "  <ListMetadataFormats>\n" +
        "   <metadataFormat>\n" +
        "     <metadataPrefix>oai_dc</metadataPrefix>\n" +
        "     <schema>http://www.openarchives.org/OAI/2.0/oai_dc.xsd</schema>\n" +
        "     <metadataNamespace>http://www.openarchives.org/OAI/2.0/oai_dc/</metadataNamespace>\n" +
        "   </metadataFormat>\n" +
        "   <metadataFormat>\n" +
        "     <metadataPrefix>olac</metadataPrefix>\n" +
        "     <schema>http://www.language-archives.org/OLAC/olac-0.2.xsd</schema>\n" +
        "     <metadataNamespace>http://www.language-archives.org/OLAC/0.2/</metadataNamespace>\n" +
        "   </metadataFormat>\n" +
        "   <metadataFormat>\n" +
        "     <metadataPrefix>perseus</metadataPrefix>\n" +
        "     <schema>http://www.perseus.tufts.edu/persmeta.xsd</schema>\n" +
        "     <metadataNamespace>http://www.perseus.tufts.edu/persmeta.dtd</metadataNamespace>\n" +
        "   </metadataFormat>\n" +
        " </ListMetadataFormats>\n" +
        "</OAI-PMH>";

    @Test
    void getMetadataFormats() throws IOException, SAXException, URISyntaxException
    {
        // Given
        var metadataFormats = new ListMetadataFormats( new ByteArrayInputStream(
            LIST_METADATA_FORMATS_RESPONSE.getBytes( StandardCharsets.UTF_8 )
        ));

        // Then
        var list = metadataFormats.getMetadataFormats();
        assertEquals( 2, list.size() );
    }
}
