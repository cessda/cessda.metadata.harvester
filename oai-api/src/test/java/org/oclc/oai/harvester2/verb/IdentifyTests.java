package org.oclc.oai.harvester2.verb;

/*-
 * #%L
 * CESSDA OAI-PMH API
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IdentifyTests
{
    @Test
    void shouldConstruct() throws IOException, InterruptedException, OAIException
    {
        var httpClient = mock( HttpClient.class );

        var httpRequest = HttpRequest.newBuilder( URI.create( "http://localhost:4556/?verb=Identify" ) ).GET().build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();

        Identify identify;

        try ( var inputStream = this.getClass().getResourceAsStream( "/Identify.xml" ) )
        {
            when( httpClient.send( httpRequest, bodyHandler ) ).thenAnswer(
                invocation -> RecordHeadersMock.buildResponse(
                    invocation.getArgument( 0 ), inputStream
                )
            );

            // Then
            identify = Identify.instance( httpClient, URI.create("http://localhost:4556/") );
        }

        assertTrue(identify.getErrors().isEmpty());
    }
}
