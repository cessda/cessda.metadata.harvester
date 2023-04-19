package eu.cessda.oaiharvester;

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
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static java.io.InputStream.nullInputStream;
import static java.net.http.HttpClient.Redirect.NORMAL;

@Component
public class HttpClient
{
    // Logger
    private static final Logger log = LoggerFactory.getLogger( HttpClient.class );
    private static final int MAX_RETRIES = 3;

    private final java.net.http.HttpClient client;
    private final int retryDelay;

    public HttpClient(HarvesterConfiguration harvesterConfiguration)
    {
        // TK added default timeout for dataverses taking too long to respond / stall
        this.client = Methanol.newBuilder()
            .autoAcceptEncoding( true )
            .followRedirects( NORMAL )
            .userAgent( "OAIHarvester/2.0" )
            .requestTimeout( harvesterConfiguration.getTimeout() )
            .build();
        this.retryDelay = 1000;
    }

    /**
     * Testing constructor, takes a mock HTTP client
     */
    HttpClient(java.net.http.HttpClient client)
    {
        this.client = client;
        this.retryDelay = 0;
    }

    private static long parseRetryAfterHeader( HttpResponse<?> response )
    {
        var retryAfterHeader = response.headers().firstValue( "Retry-After" );

        if (retryAfterHeader.isPresent())
        {
            try
            {
                // Try parsing as an integer, sleep for the time specified
                var delaySeconds = Long.parseLong(retryAfterHeader.get());
                return delaySeconds * 1000L;
            }
            catch ( NumberFormatException ignored )
            {
                // catch parsing failure if header is a HTTP date
            }

            try
            {
                var httpDate = ZonedDateTime.parse( retryAfterHeader.get(), DateTimeFormatter.RFC_1123_DATE_TIME );
                var delay = Duration.between( ZonedDateTime.now(), httpDate );
                if ( delay.isNegative() ) {
                    // Negative delays are invalid
                    return -1;
                }
                return delay.toMillis();
            }
            catch ( DateTimeParseException ignored )
            {
                // catch parsing failure if header an invalid format
            }
        }

        // header not present or invalid
        return -1;
    }

    public InputStream getHttpResponse( URI requestURL ) throws IOException
    {

        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();

        try
        {
            return performRequest( httpRequest, bodyHandler ).body();
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
            return nullInputStream();
        }
    }

    /**
     * Perform the HTTP request, retrying in case of connection errors.
     * @param httpRequest the request to perform.
     * @param bodyHandler the body handler for the response.
     * @return the response.
     * @throws IOException if an IO error occurred when sending the response and the maximum retries have been exceeded.
     * @throws InterruptedException if the operation is interrupted.
     */
    @SuppressWarnings( {"java:S3776", "java:S6212"} ) // Any other way of implementing this logic is more complicated
    private <T> HttpResponse<T> performRequest( HttpRequest httpRequest, HttpResponse.BodyHandler<T> bodyHandler ) throws InterruptedException, IOException
    {
        // Retry counter
        int retries = 0;

        // Error variables
        IOException previousException = null;
        IOException currentException;

        while ( true )
        {
            try
            {
                var response = client.send( httpRequest, bodyHandler );

                int responseCode = response.statusCode();
                log.trace( "responseCode={}", responseCode );

                if ( responseCode < 400 )
                {
                    // If successful, return the response
                    return response;
                }
                else if ( responseCode == 429 || responseCode == 503 )
                {
                    // Try to parse the Retry-After header, fall back to default behavior if the header is not present
                    var delayMilliseconds = parseRetryAfterHeader( response );
                    if ( delayMilliseconds != -1 )
                    {
                        Thread.sleep( delayMilliseconds );
                        continue;
                    }
                }

                // Request failed, set error status
                currentException = new HTTPException( httpRequest.uri(), response.statusCode() );
                if ( responseCode != 429 && responseCode < 500 )
                {
                    // 400 response codes, apart from 429, are caused by client errors, do not retry
                    break;
                }
            }
            catch ( IOException e )
            {
                currentException = e;
            }

            /*
             * Error handling
             */
            if ( previousException != null )
            {
                // Suppress the previous exception
                currentException.addSuppressed( previousException );
            }

            if ( retries < MAX_RETRIES )
            {
                retries++;
                Thread.sleep( retryDelay );
            }
            else
            {
                break;
            }

            previousException = currentException;
        }

        // An error has occurred
        throw currentException;
    }
}
