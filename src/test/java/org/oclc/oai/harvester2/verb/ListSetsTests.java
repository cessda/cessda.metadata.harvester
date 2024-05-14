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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ListSetsTests
{
    // language=XML
    private static final String LIST_SETS_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
                <responseDate>2002-08-11T07:21:33Z</responseDate>
                <request verb="ListSets">http://an.oa.org/OAI-script</request>
                <ListSets>
                    <set>
                        <setSpec>music</setSpec>
                        <setName>Music collection</setName>
                    </set>
                    <set>
                        <setSpec>music:(muzak)</setSpec>
                        <setName>Muzak collection</setName>
                    </set>
                    <set>
                        <setSpec>music:(elec)</setSpec>
                        <setName>Electronic Music Collection</setName>
                        <setDescription>
                            <oai_dc:dc xmlns:oai_dc="http://www.openarchives.org/OAI/2.0/oai_dc/"
                                  xmlns:dc="http://purl.org/dc/elements/1.1/"
                                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                                  xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd">
                                <dc:description>This set contains metadata describing
                                    electronic music recordings made during the 1950ies
                                </dc:description>
                            </oai_dc:dc>
                        </setDescription>
                    </set>
                    <set>
                        <setSpec>video</setSpec>
                        <setName>Video Collection</setName>
                    </set>
                </ListSets>
            </OAI-PMH>""";

    // language=XML
    private static final String SETS_NOT_SUPPORTED_ERROR = """
            <?xml version="1.0" encoding="UTF-8"?>
            <OAI-PMH xmlns="http://www.openarchives.org/OAI/2.0/"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://www.openarchives.org/OAI/2.0/
                     http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd">
                <responseDate>2001-06-01T19:20:30Z</responseDate>
                <request verb="ListSets">http://purl.org/alcme/etdcat/servlet/OAIHandler</request>
                <error code="noSetHierarchy">This repository does not support sets</error>
            </OAI-PMH>""";

    @Test
    void shouldReturnSets() throws IOException, SAXException
    {
        // Given
        var inputStream = new ByteArrayInputStream(
                LIST_SETS_XML.getBytes( StandardCharsets.UTF_8 )
        );

        // Then
        var listSets = new ListSets( new InputSource( inputStream ) );

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
        var listSets = new ListSets(  new InputSource( inputStream ) );

        var sets = listSets.getSets();

        assertEquals( 0, sets.size() );

        var errors = listSets.getErrors();

        assertEquals( 1, errors.size() );

        assertEquals( OAIError.Code.noSetHierarchy, errors.getFirst().getCode() );
        assertEquals( "This repository does not support sets", errors.getFirst().getMessage().orElseThrow() );
    }
}
