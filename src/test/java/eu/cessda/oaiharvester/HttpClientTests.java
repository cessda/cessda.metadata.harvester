package eu.cessda.oaiharvester;

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
import org.mockito.Mockito;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.io.InputStream.nullInputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings( { "unchecked", "resource" } ) // HttpResponse.BodyHandler.class cannot be cast to generic type
class HttpClientTests
{
    private final HttpClient clientMock = Mockito.mock( HttpClient.class );

    private static HttpResponse<InputStream> getHttpResponse( HttpRequest httpRequest, int status, Map<String, List<String>> responseHeaders )
    {
        return new HttpResponse<>()
        {
            @Override
            public int statusCode()
            {
                return status;
            }

            @Override
            public HttpRequest request()
            {
                return httpRequest;
            }

            @Override
            public Optional<HttpResponse<InputStream>> previousResponse()
            {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers()
            {
                return HttpHeaders.of( responseHeaders, ( k, v ) -> true );
            }

            @Override
            public InputStream body()
            {
                return nullInputStream();
            }

            @Override
            public Optional<SSLSession> sslSession()
            {
                return Optional.empty();
            }

            @Override
            public URI uri()
            {
                return httpRequest.uri();
            }

            @Override
            public HttpClient.Version version()
            {
                return HttpClient.Version.HTTP_1_1;
            }
        };
    }

    @Test
    void shouldConstruct()
    {
        assertDoesNotThrow( () -> new eu.cessda.oaiharvester.HttpClient( new HarvesterConfiguration() ) );
    }

    @Test
    void shouldReturnResponse() throws IOException, InterruptedException
    {
        when(clientMock.send( any( HttpRequest.class), any( HttpResponse.BodyHandler.class ) ))
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 200, Collections.emptyMap() ) );

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should return a successful result
        var inputStream = assertDoesNotThrow( () -> httpClient.getHttpResponse( URI.create( "http://localhost:8080/" ) ) );
        assertNotNull( inputStream );
    }

    @Test
    void shouldRetryIfA500StatusCodeIsReturned() throws IOException, InterruptedException
    {
        when(clientMock.send( any( HttpRequest.class), any( HttpResponse.BodyHandler.class ) ))
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 500, Collections.emptyMap() ))
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 200, Collections.emptyMap() ));

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should return a successful result
        var inputStream = assertDoesNotThrow( () -> httpClient.getHttpResponse( URI.create( "http://localhost:8080/" ) ) );
        assertNotNull( inputStream );
    }

    @Test
    void shouldThrowIf500IsReturnedAndMaxRetriesAreExceeded() throws IOException, InterruptedException
    {
        // Setup variables
        var status = 500;
        var requestURL = URI.create( "http://localhost:8080/" );

        // Setup mocks
        when(clientMock.send( any( HttpRequest.class), any( HttpResponse.BodyHandler.class ) ))
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), status, Collections.emptyMap() ) );

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should throw
        var httpException = assertThrows( HTTPException.class, () -> httpClient.getHttpResponse( requestURL ) );
        assertEquals( requestURL, httpException.getRequestURI() );
        assertEquals( status, httpException.getStatusCode() );
    }

    @Test
    void shouldParseRetryAfterHeaderWith429StatusCode() throws IOException, InterruptedException
    {
        // Setup variables
        var delayInSeconds = 1;
        var responseHeaders = Map.of( "Retry-After", List.of( Integer.toString( delayInSeconds ) ) );

        // Setup mocks
        when(clientMock.send( any( HttpRequest.class), any( HttpResponse.BodyHandler.class ) ))
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 429, responseHeaders ) )
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 200, Collections.emptyMap() ) );

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should return a successful result
        var timeBefore = Instant.now();
        assertDoesNotThrow( () -> httpClient.getHttpResponse( URI.create( "http://localhost:8080/" ) ) );
        var timeAfter = Instant.now();

        // Assert that the appropriate amount of time passed
        assertThat( Duration.between( timeBefore, timeAfter )).isGreaterThanOrEqualTo( Duration.ofSeconds( delayInSeconds ) );
    }

    @Test
    void shouldParseRetryAfterHeaderWithA503StatusCode() throws IOException, InterruptedException
    {
        // Setup variables
        var delay = Duration.ofSeconds( 2 );
        var expectedTimeAfter = ZonedDateTime.now().plus( delay );

        var responseHeaders = Map.of( "Retry-After", List.of(DateTimeFormatter.RFC_1123_DATE_TIME.format( expectedTimeAfter )));

        // Setup mocks
        when(clientMock.send( any( HttpRequest.class), any( HttpResponse.BodyHandler.class ) ))
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 503, responseHeaders ) )
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 200, Collections.emptyMap() ) );

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should return a successful result
        assertDoesNotThrow( () -> httpClient.getHttpResponse( URI.create( "http://localhost:8080/" ) ) );
    }

    @Test
    void shouldIgnoreInvalidRetryHeaders() throws IOException, InterruptedException
    {
        // Setup variables

        var responseHeaders = Map.of( "Retry-After", List.of("invalid"));

        // Setup mocks
        when(clientMock.send( any( HttpRequest.class), any( HttpResponse.BodyHandler.class ) ))
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 429, responseHeaders ) )
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 200, Collections.emptyMap() ) );

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should return a successful result
        assertDoesNotThrow( () -> httpClient.getHttpResponse( URI.create( "http://localhost:8080/" ) ) );
    }

    @Test
    void shouldImmediatelyThrowIfA400StatusIsReturned() throws IOException, InterruptedException
    {
        // Setup variables
        var status = 400;
        var requestURL = URI.create( "http://localhost:8080/" );

        // Setup mocks
        when(clientMock.send( any( HttpRequest.class), any( HttpResponse.BodyHandler.class ) ))
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), status, Collections.emptyMap() ) );

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should throw
        var httpException = assertThrows( HTTPException.class, () -> httpClient.getHttpResponse( requestURL ) );
        assertEquals( requestURL, httpException.getRequestURI() );
        assertEquals( status, httpException.getStatusCode() );

        // Assert that no exceptions were suppressed
        assertThat( httpException.getSuppressed() ).isEmpty();
    }

    @SuppressWarnings( "NewExceptionWithoutArguments" ) // mock exception
    @Test
    void shouldHandleIOExceptions() throws IOException, InterruptedException
    {
        // Setup variables
        var requestURL = URI.create( "http://localhost:8080/" );

        // Setup mocks
        when(clientMock.send( any( HttpRequest.class), any( HttpResponse.BodyHandler.class ) ))
            .then( invocation -> { throw new IOException();} );

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should throw
        var ioException = assertThrows( IOException.class, () -> httpClient.getHttpResponse( requestURL ) );

        // Assert that retries took place
        assertThat( ioException.getSuppressed() ).isNotEmpty();
    }

    @Test
    void shouldHandleInterruptedExceptions() throws IOException, InterruptedException
    {
        // Setup mocks
        when(clientMock.send( any( HttpRequest.class), any( HttpResponse.BodyHandler.class ) ))
            .thenThrow( InterruptedException.class );

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should throw an IOException, and the thread should be interrupted
        assertThrows( IOException.class, () -> httpClient.getHttpResponse( URI.create( "http://localhost:8080/" ) ) );
        assertTrue( Thread.interrupted() );
    }
}
