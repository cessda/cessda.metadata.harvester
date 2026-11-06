package eu.cessda.oaiharvester;

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


import com.github.mizosoft.methanol.Methanol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.net.http.HttpClient.Redirect.NORMAL;

@Component
public class HttpClient extends java.net.http.HttpClient
{
    private static final int MAX_RETRIES = 3;

    private final java.net.http.HttpClient client;
    private final Duration retryDelay;

    @Autowired
    HttpClient( HarvesterConfiguration harvesterConfiguration )
    {
        // TK added default timeout for dataverses taking too long to respond / stall
        this.client = Methanol.newBuilder()
            .autoAcceptEncoding( true )
            .followRedirects( NORMAL )
            .userAgent( "OAIHarvester/2.0" )
            .requestTimeout( harvesterConfiguration.getTimeout() )
            .build();
        this.retryDelay = Duration.ofMillis( 1000 );
    }

    /**
     * Testing constructor, takes a mock HTTP client
     */
    HttpClient( java.net.http.HttpClient client )
    {
        this.client = client;
        this.retryDelay = Duration.ZERO;
    }

    /**
     * Parse the Retry-After header of the HTTP response.
     *
     * @param headers the headers of the HTTP response.
     * @return the delay in milliseconds, or -1 if the header was missing or invalid.
     */
    @SuppressWarnings( "java:S8688" )
    private static Duration parseRetryAfterHeader( HttpHeaders headers )
    {
        var retryAfterHeader = headers.firstValue( "Retry-After" );

        if ( retryAfterHeader.isPresent() )
        {
            try
            {
                // Try parsing as an integer, sleep for the time specified
                var delaySeconds = Long.parseLong( retryAfterHeader.get() );
                return Duration.ofSeconds( delaySeconds );
            }
            catch ( NumberFormatException _ )
            {
                // catch parsing failure if header is a HTTP date
            }

            try
            {
                // Compute the difference between the header time and now
                var httpDate = ZonedDateTime.parse( retryAfterHeader.get(), DateTimeFormatter.RFC_1123_DATE_TIME );
                return Duration.between( ZonedDateTime.now(), httpDate );
            }
            catch ( DateTimeParseException _ )
            {
                // catch parsing failure if header an invalid format
            }
        }

        // header not present or invalid
        return null;
    }

    @Deprecated(forRemoval = true)
    InputStream getHttpResponse( URI requestURL ) throws IOException, InterruptedException
    {
        var httpRequest = HttpRequest.newBuilder( requestURL ).build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();
        return send( httpRequest, bodyHandler ).body();
    }

    /**
     * Perform the HTTP request, retrying in case of connection errors.
     *
     * @param httpRequest the request to perform.
     * @param bodyHandler the body handler for the response.
     * @return the response.
     * @throws IOException          if an IO error occurred when sending the response and the maximum retries have been exceeded.
     * @throws InterruptedException if the operation is interrupted.
     */
    @SuppressWarnings( { "java:S3776", "java:S135" } ) // Any other way of implementing this logic is more complicated
    public <T> HttpResponse<T> send( HttpRequest httpRequest, HttpResponse.BodyHandler<T> bodyHandler ) throws InterruptedException, IOException
    {
        // Retry counter
        int retries = 0;

        // Stores any previous errors thrown during previous attempts.
        IOException previousException = null;

        // Container for IO errors thrown during the current attempt.
        IOException currentException;

        while ( true )
        {
            try
            {
                var response = client.send( httpRequest, bodyHandler );

                int responseCode = response.statusCode();

                if ( responseCode < 400 )
                {
                    // If successful, return the response
                    return response;
                }
                else if ( responseCode == 429 || responseCode == 503 )
                {
                    // Try to parse the Retry-After header, fall back to default behaviour if the header is not present
                    var delayMilliseconds = parseRetryAfterHeader( response.headers() );
                    if ( delayMilliseconds != null )
                    {
                        Thread.sleep( delayMilliseconds );

                        // Reset the loop without incrementing the amount of retries
                        continue;
                    }
                }

                // Request failed, set error status
                currentException = new HTTPException( httpRequest.uri(), response.statusCode() );
                if ( responseCode != 429 && responseCode < 500 )
                {
                    // 400 response codes, apart from 429, are caused by client errors; break and throw the error
                    break;
                }
            }
            catch ( IOException e )
            {
                // Store the IO error (i.e. connection timeouts, failed DNS lookups, etc.)
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

            if ( retries >= MAX_RETRIES )
            {
                // Maximum amount of retries reached; give up and throw the error
                break;
            }

            // Store the current error for the next loop and increment the amount of retires
            previousException = currentException;
            retries++;

            Thread.sleep( retryDelay );
        }

        // Request failed
        throw currentException;
    }

    @Override
    public Optional<CookieHandler> cookieHandler()
    {
        return client.cookieHandler();
    }

    @Override
    public Optional<Duration> connectTimeout()
    {
        return client.connectTimeout();
    }

    @Override
    public Redirect followRedirects()
    {
        return client.followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy()
    {
        return client.proxy();
    }

    @Override
    public SSLContext sslContext()
    {
        return client.sslContext();
    }

    @Override
    public SSLParameters sslParameters()
    {
        return client.sslParameters();
    }

    @Override
    public Optional<Authenticator> authenticator()
    {
        return client.authenticator();
    }

    @Override
    public Version version()
    {
        return client.version();
    }

    @Override
    public Optional<Executor> executor()
    {
        return client.executor();
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync( HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler )
    {
        throw new UnsupportedOperationException("async is not supported");
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync( HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler )
    {
        throw new UnsupportedOperationException("async is not supported");
    }
}
