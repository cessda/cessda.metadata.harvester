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


import javax.net.ssl.SSLSession;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Optional;

/**
 * mock data for Record headers.
 *
 * @author moses AT doraventures DOT com
 */
public final class RecordHeadersMock
{

    public static final URL GET_LIST_IDENTIFIERS_XML_RESUMPTION_EMPTY =
        RecordHeadersMock.class.getResource( "/ListIdentifiersResumptionEmpty.xml" );
    public static final URL GET_LIST_IDENTIFIERS_XML_RESUMPTION_TOKEN_NOT_MOCKED_FOR_INVALID =
        RecordHeadersMock.class.getResource( "/ListIdentifiersResumptionTokenNotMockedForInvalid.xml" );
    public static final URL GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION =
        RecordHeadersMock.class.getResource( "/ListIdentifiersWithResumption.xml" );
    public static final URL GET_LIST_IDENTIFIERS_XML_WITH_RESUMPTION_LAST_LIST =
        RecordHeadersMock.class.getResource( "/ListIdentifiersWithResumptionLastList.xml" );
    public static final URL GET_LIST_IDENTIFIERS_XML_WITH_CANNOT_DISSEMINATE_FORMAT_ERROR =
        RecordHeadersMock.class.getResource( "/ListIdentifiersWithCannotDisseminateFormatError.xml" );
    public static final URL GET_LIST_SETS_XML =
        RecordHeadersMock.class.getResource( "/ListSets.xml" );
    public static final URL GET_LIST_SETS_XML_LAST_LIST =
        RecordHeadersMock.class.getResource( "/ListSetsLastList.xml" );

    static HttpResponse<InputStream> buildResponse(HttpRequest httpRequest, InputStream inputStream)
    {
        return new HttpResponse<>()
        {
            @Override
            public int statusCode()
            {
                return 200;
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
                return HttpHeaders.of( Collections.emptyMap(), ( k, v ) -> true );
            }

            @Override
            public InputStream body()
            {
                return inputStream;
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
                return httpRequest.version().orElse( HttpClient.Version.HTTP_1_1 );
            }
        };
    }
}
