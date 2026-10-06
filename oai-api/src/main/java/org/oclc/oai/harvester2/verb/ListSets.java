/*
 Copyright 2006 OCLC, Online Computer Library Center
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

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

import org.xml.sax.InputSource;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents an ListSets response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public final class ListSets extends HarvesterVerb implements Resumable
{
    /**
     /**
     * Construct a new {@link ListSets} instance from an {@link InputSource}.
     *
     * @param is the input source to parse.
     * @throws IOException if an IO error occurs when reading the input stream.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     */
    ListSets( InputSource is ) throws IOException, OAIException
    {
        super( is );
    }

    /**
     * Construct a new instance of {@link ListIdentifiers}.
     *
     * @param httpClient     the HTTP client to use.
     * @param baseURL        the URL of the repository.
     * @return the ListSets response.
     * @throws IOException   if an IO error occurs.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     * @throws InterruptedException if the operation is interrupted.
     */
    public static ListSets instance( HttpClient httpClient, URI baseURL ) throws IOException, OAIException, InterruptedException
    {
        var requestURL = getRequestURL( baseURL );

        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();

        try ( var httpResponse = httpClient.send( httpRequest, bodyHandler ).body() )
        {
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
            return new ListSets( inputSource );
        }
    }

    /**
     * Construct a new instance of {@link ListSets} using a resumption token.
     *
     * @param httpClient     the HTTP client to use.
     * @param baseURL the URL of the repository.
     * @param resumptionToken the resumption token.
     * @return the ListSets response.
     * @throws IOException if an IO error occurs.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     * @throws InterruptedException if the operation is interrupted.
     */
    public static ListSets instance( HttpClient httpClient, URI baseURL, String resumptionToken ) throws IOException, OAIException, InterruptedException
    {
        var requestURL = getRequestURL( baseURL, resumptionToken );

        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();

        try ( var httpResponse = httpClient.send( httpRequest, bodyHandler ).body() )
        {
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
            return new ListSets( inputSource );
        }
    }

    /**
     * Generate a ListSets URI for the given baseURL
     *
     * @param baseURL the base URL of the OAI-PMH repository.
     */
    private static URI getRequestURL( URI baseURL )
    {
        return URI.create( baseURL + "?verb=ListSets" );
    }

    /**
     * Construct the query portion of the http request (resumptionToken version)
     *
     * @param baseURL         the base URL of the OAI-PMH repository.
     * @param resumptionToken the resumption token.
     */
    private static URI getRequestURL( URI baseURL, String resumptionToken )
    {
        return URI.create( baseURL + "?verb=ListSets"
            + "&resumptionToken=" + URLEncoder.encode( resumptionToken, StandardCharsets.UTF_8 )
        );
    }

    /**
     * Returns a list of sets found in the response. The returned list is unmodifiable.
     *
     * @return the list.
     */
    public List<String> getSets()
    {
        var nl = getDocument().getElementsByTagNameNS( OAI_2_0_NAMESPACE, "setSpec" );
        var sets = new ArrayList<String>( nl.getLength() );
        for ( int i = 0; i < nl.getLength(); i++ )
        {
            sets.add( nl.item( i ).getTextContent().trim() );
        }
        return Collections.unmodifiableList( sets );
    }
}
