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

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * This class represents an ListRecords response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public final class ListRecords extends HarvesterVerb implements Resumable
{
    /**
     * Construct a new {@link ListRecords} instance from an {@link InputSource}.
     *
     * @param is the input source to parse.
     * @throws IOException if an IO error occurs when reading the input stream.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     */
	ListRecords( InputSource is ) throws IOException, OAIException
	{
		super( is );
	}

    /**
     * Query an OAI-PMH repository for a list of records using a resumption token.
     *
     * @param httpClient     the HTTP client to use.
     * @param baseURL        the baseURL of the server to be queried.
     * @param resumptionToken the resumption token.
     * @return the ListRecords response.
     * @throws IOException  if an IO error occurred.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     * @throws InterruptedException if the operation was interrupted.
     */
	public static ListRecords instance( HttpClient httpClient, URI baseURL, String resumptionToken ) throws IOException, OAIException, InterruptedException
    {
		var requestURL = getRequestURL( baseURL, resumptionToken );

        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();

        try (var httpResponse = httpClient.send( httpRequest, bodyHandler ).body() )
		{
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
			return new ListRecords( inputSource );
		}
	}

    /**
     * Query an OAI-PMH repository for a list of records using the ListRecords verb.
     *
     * @param httpClient     the HTTP client to use.
     * @param baseURL        the URL of the repository.
     * @param metadataPrefix the metadata prefix to use.
     * @param set            the set to harvest. Set to {@code null} to disable set based harvesting.
     * @param from           the date to harvest from. Set to {@code null} to harvest from the beginning.
     * @param until          to date to harvest to. Set to {@code null} for no limit.
     * @return the ListRecords response.
     * @throws IOException   if an IO error occurs.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     * @throws InterruptedException if the operation is interrupted.
     */
	public static ListRecords instance( HttpClient httpClient, URI baseURL, LocalDate from, LocalDate until, String set, String metadataPrefix )
        throws IOException, OAIException, InterruptedException
    {
		var requestURL = getRequestURL( baseURL, from, until, set, metadataPrefix );

        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();

        try (var httpResponse = httpClient.send( httpRequest, bodyHandler ).body() )
		{
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
			return new ListRecords( inputSource );
		}
	}

    /**
     * Return a list of records in the response.
     *
     * @return the records.
     */
	public NodeList getRecords()
    {
        return getDocument().getElementsByTagNameNS( OAI_2_0_NAMESPACE, "record" );
    }

	/**
	 * Construct the query portion of the http request
	 *
	 * @return a {@link String} containing the query portion of the http request
	 */
	private static URI getRequestURL( URI baseURL, LocalDate from, LocalDate until, String set, String metadataPrefix )
	{
		var requestURL = new StringBuilder( baseURL.toString() );
		requestURL.append( "?verb=ListRecords" );
		if ( from != null )
		{
			requestURL.append( "&from=" ).append( from );
		}
		if ( until != null )
		{
			requestURL.append( "&until=" ).append( until );
		}
		if ( set != null )
		{
			requestURL.append( "&set=" ).append( set );
		}
		requestURL.append( "&metadataPrefix=" ).append( metadataPrefix );
		return URI.create(requestURL.toString());
	}

	/**
	 * Construct the query portion of the http request (resumptionToken version)
	 */
	private static URI getRequestURL( URI baseURL, String resumptionToken )
	{
		return URI.create(baseURL + "?verb=ListRecords"
				+ "&resumptionToken=" + URLEncoder.encode( resumptionToken, StandardCharsets.UTF_8 )
		);
	}
}
