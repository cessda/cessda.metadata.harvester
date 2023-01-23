package eu.cessda.eqb.harvester;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2023 CESSDA ERIC
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


import com.github.mizosoft.methanol.Methanol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.io.InputStream.nullInputStream;
import static java.net.http.HttpClient.Redirect.NORMAL;

@Component
public class HttpClient
{
    // Logger
    private static final Logger log = LoggerFactory.getLogger( HttpClient.class );

    private final Methanol client;

    public HttpClient(HarvesterConfiguration harvesterConfiguration)
    {
        // TK added default timeout for dataverses taking too long to respond / stall
        this.client = Methanol.newBuilder()
            .autoAcceptEncoding( true )
            .followRedirects( NORMAL )
            .userAgent( "OAIHarvester/2.0" )
            .requestTimeout( harvesterConfiguration.getTimeout() )
            .build();
    }

    public InputStream getHttpResponse( URI requestURL ) throws IOException
    {
        int responseCode;

        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();

        try
        {
            var response = client.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() );

            responseCode = response.statusCode();
            log.trace( "responseCode={}", responseCode );


            if ( responseCode >= 400 )
            {
                throw new HTTPException( requestURL, response.statusCode() );
            }

            return response.body();
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
            return nullInputStream();
        }
    }
}
