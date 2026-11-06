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

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.io.InputStream.nullInputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings( { "unchecked", "resource" } ) // HttpResponse.BodyHandler.class cannot be cast to generic type
class HttpClientTests
{
    private final HttpClient clientMock = mock( HttpClient.class );

    static HttpResponse<InputStream> getHttpResponse( HttpRequest httpRequest )
    {
        return getHttpResponse( httpRequest, 200 );
    }

    static HttpResponse<InputStream> getHttpResponse( HttpRequest httpRequest, int status )
    {
        return getHttpResponse( httpRequest, status, Collections.emptyMap() );
    }

    static HttpResponse<InputStream> getHttpResponse( HttpRequest httpRequest, InputStream body)
    {
        return getHttpResponse( httpRequest, 200, Collections.emptyMap(), body );
    }

    static HttpResponse<InputStream> getHttpResponse( HttpRequest httpRequest,  int status, Map<String, List<String>> responseHeaders )
    {
        return getHttpResponse( httpRequest, status, responseHeaders, nullInputStream() );
    }

    static HttpResponse<InputStream> getHttpResponse(HttpRequest httpRequest,  int status, Map<String, List<String>> responseHeaders, InputStream body )
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
                return HttpHeaders.of( responseHeaders, ( _, _ ) -> true );
            }

            @Override
            public InputStream body()
            {
                return body;
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
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ) ) );

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should return a successful result
        var httpRequest = HttpRequest.newBuilder(URI.create( "http://localhost:8080/")).build();
        var httpResponse = assertDoesNotThrow( () -> httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ) );
        assertThat( httpResponse ).isNotNull().extracting( HttpResponse::statusCode ).isEqualTo( 200 );
    }

    @Test
    void shouldRetryIfA500StatusCodeIsReturned() throws IOException, InterruptedException
    {
        when(clientMock.send( any( HttpRequest.class), any( HttpResponse.BodyHandler.class ) ))
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 500 ))
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ) ));

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should return a successful result
        var httpRequest = HttpRequest.newBuilder(URI.create( "http://localhost:8080/")).build();
        var httpResponse = assertDoesNotThrow( () -> httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ) );
        assertThat( httpResponse ).extracting( HttpResponse::statusCode ).isEqualTo( 200 );
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
        var httpRequest = HttpRequest.newBuilder(URI.create( "http://localhost:8080/")).build();
        var httpException = assertThatExceptionOfType( HTTPException.class ).isThrownBy( () -> httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ) ) ;
        httpException.extracting( HTTPException::getRequestURI ).isEqualTo( requestURL );
        httpException.extracting( HTTPException::getStatusCode ).isEqualTo( status );
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

        // Build the request
        var httpRequest = HttpRequest.newBuilder(URI.create( "http://localhost:8080/")).build();

        // The client should return a successful result, time the response
        var timeBefore = Instant.now();
        assertDoesNotThrow( () -> httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ) );
        var timeAfter = Instant.now();

        // Assert that the appropriate amount of time passed
        assertThat( Duration.between( timeBefore, timeAfter )).isGreaterThanOrEqualTo( Duration.ofSeconds( delayInSeconds ) );
    }

    @Test
    void shouldParseRetryAfterHeaderWithA503StatusCode() throws IOException, InterruptedException
    {
        // Setup variables
        var delay = Duration.ofSeconds( 2 );

        // RFC_1123_DATE_TIME only has second precision and will round down
        // Make sure that we don't create a delay of less than 2 seconds
        // by using a second ticking clock that will also round down
        var expectedTimeAfter = ZonedDateTime.now( Clock.tickSeconds( ZoneId.systemDefault() ) ).plus( delay );

        var responseHeaders = Map.of( "Retry-After", List.of(DateTimeFormatter.RFC_1123_DATE_TIME.format( expectedTimeAfter )));

        // Setup mocks
        when(clientMock.send( any( HttpRequest.class), any( HttpResponse.BodyHandler.class ) ))
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 503, responseHeaders ) )
            .thenAnswer( invocation -> getHttpResponse( invocation.getArgument( 0 ), 200, Collections.emptyMap() ) );

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // Build the request
        var httpRequest = HttpRequest.newBuilder(URI.create( "http://localhost:8080/")).build();

        // The client should return a successful result, time the response
        assertDoesNotThrow( () -> httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ) );

        // Assert that the appropriate amount of time passed
        assertThat( expectedTimeAfter ).isInThePast();
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

        // The client should return a successful result, ignoring the invalid header
        var httpRequest = HttpRequest.newBuilder(URI.create( "http://localhost:8080/")).build();
        assertDoesNotThrow( () -> httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ) );
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
        var httpRequest = HttpRequest.newBuilder(URI.create( "http://localhost:8080/")).build();
        var httpException = assertThrows( HTTPException.class, () -> httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ) );
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
            .then( _ -> { throw new IOException();} );

        var httpClient = new eu.cessda.oaiharvester.HttpClient( clientMock );

        // The client should throw
        var httpRequest = HttpRequest.newBuilder(URI.create( "http://localhost:8080/")).build();
        var ioException = assertThrows( IOException.class, () -> httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ) );

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

        // The client should throw an InterruptedException, and the thread should not be interrupted
        var httpRequest = HttpRequest.newBuilder(URI.create( "http://localhost:8080/")).build();
        assertThrows( InterruptedException.class, () -> httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ) );
        assertFalse( Thread.interrupted() );
    }
}
