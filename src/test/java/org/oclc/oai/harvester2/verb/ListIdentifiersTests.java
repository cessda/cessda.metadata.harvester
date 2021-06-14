package org.oclc.oai.harvester2.verb;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListIdentifiersTests
{
    @Test
    void shouldReturnRecordHeaders() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers( new ByteArrayInputStream(
                RecordHeadersMock.getListIdentifiersXMLResumptionEmpty().getBytes( StandardCharsets.UTF_8 )
        ) );

        var identifiersIDs = identifiers.getIdentifiers();

        assertEquals( 3, identifiersIDs.size());

        assertThat( identifiersIDs ).containsExactlyInAnyOrder( "850229", "850232", "850235" );
    }

    @Test
    void shouldReturnResumptionToken() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers( new ByteArrayInputStream(
                RecordHeadersMock.getListIdentifiersXMLWithResumption().getBytes( StandardCharsets.UTF_8 )
        ));

        assertEquals("3/6/7/ddi/null/2017-01-01/null", identifiers.getResumptionToken().orElseThrow() );
    }

    @Test
    void shouldReturnEmptyOptionalForAnEmptyResumptionToken() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers(new ByteArrayInputStream(
                RecordHeadersMock.getListIdentifiersXMLResumptionEmpty().getBytes( StandardCharsets.UTF_8 )
        ));

        assertTrue( identifiers.getResumptionToken().isEmpty() );
    }

    @Test
    void shouldReturnDocumentWhenResumingWithToken() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers(new ByteArrayInputStream(
                RecordHeadersMock.getListIdentifiersXMLWithResumptionLastList().getBytes( StandardCharsets.UTF_8 )
        ));

        // Then
        var identifiersIDs = identifiers.getIdentifiers();

        assertEquals( 1, identifiersIDs.size());

        assertEquals( "998", identifiersIDs.get( 0 ) );
    }

    @Test
    void shouldReturnNoRecordsOnError() throws IOException, SAXException
    {
        // Given
        var identifiers = new ListIdentifiers( new ByteArrayInputStream(
                RecordHeadersMock.getListIdentifiersXMLWithCannotDisseminateFormatError().getBytes( StandardCharsets.UTF_8 )
        ));

        // Then
        var identifiersIDs = identifiers.getIdentifiers();

        assertTrue( identifiersIDs.isEmpty() );
        assertTrue( identifiers.getResumptionToken().isEmpty() );
    }
}