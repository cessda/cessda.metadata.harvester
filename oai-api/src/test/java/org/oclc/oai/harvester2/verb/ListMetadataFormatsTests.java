package org.oclc.oai.harvester2.verb;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2026 CESSDA ERIC
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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListMetadataFormatsTests
{
    @Test
    void shouldReturnAListOfMetadataFormats() throws IOException, OAIException, URISyntaxException, InterruptedException
    {
        var httpClient = mock( HttpClient.class );

        var httpRequest = HttpRequest.newBuilder( URI.create("http://localhost:4556/?verb=ListMetadataFormats" ) ).GET().build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();



        // Given
        ListMetadataFormats metadataFormats;
        try(var listMetadataFormatsResponse = this.getClass().getResourceAsStream( "/ListMetadataFormatsResponse.xml" ))
        {
            when( httpClient.send( httpRequest, bodyHandler ) ).thenAnswer(
                invocation -> RecordHeadersMock.buildResponse(
                    invocation.getArgument( 0 ), listMetadataFormatsResponse
                )
            );

            metadataFormats = ListMetadataFormats.instance( httpClient, URI.create("http://localhost:4556/") );
        }

        // Then
        assertThat( metadataFormats.getMetadataFormats() ).containsExactly(
                new ListMetadataFormats.MetadataFormat(
                        "oai_dc",
                        URI.create( "http://www.openarchives.org/OAI/2.0/oai_dc.xsd" ),
                        URI.create( "http://www.openarchives.org/OAI/2.0/oai_dc/" )
                ),
                new ListMetadataFormats.MetadataFormat(
                        "oai_marc",
                        URI.create( "http://www.openarchives.org/OAI/1.1/oai_marc.xsd" ),
                        URI.create( "http://www.openarchives.org/OAI/1.1/oai_marc" )
                )
        );
    }
}
