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
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This class represents an ListIdentifiers response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public final class ListIdentifiers extends HarvesterVerb implements Resumable
{
	/**
     * Construct a new {@link ListIdentifiers} instance from an {@link InputSource}.
	 *
     * @param is the input source to parse.
     * @throws IOException if an IO error occurs when reading the input stream.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
	 */
	ListIdentifiers( InputSource is ) throws IOException, OAIException
	{
		super( is );
	}

	/**
	 * Construct a new instance of {@link ListIdentifiers} using a resumption token.
     *
     * @param httpClient     the HTTP client to use.
	 * @param baseURL the URL of the repository.
	 * @param resumptionToken the resumption token.
     * @return a {@link ListIdentifiers} instance.
	 * @throws IOException if an IO error occurs.
	 * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     * @throws InterruptedException if the operation is interrupted.
	 */
	public static ListIdentifiers instance( HttpClient httpClient, URI baseURL, String resumptionToken ) throws IOException, OAIException, InterruptedException
    {
        Objects.requireNonNull( resumptionToken, "resumptionToken cannot be null" );

		var requestURL = getRequestURL( baseURL, resumptionToken );
        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();

        try (var httpResponse = httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ).body() )
		{
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
			return new ListIdentifiers( inputSource );
		}
	}

	/**
     * Construct a new instance of {@link ListIdentifiers}.
     *
     * @param httpClient     the HTTP client to use.
     * @param baseURL        the URL of the repository.
     * @param metadataPrefix the metadata prefix to use.
     * @param set            the set to harvest. Set to {@code null} to disable set based harvesting.
     * @param from           the date to harvest from. Set to {@code null} to harvest from the beginning.
     * @param until          to date to harvest to. Set to {@code null} for no limit.
     * @return a {@link ListIdentifiers} instance.
     * @throws IOException   if an IO error occurs.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     * @throws InterruptedException if the operation is interrupted.
     */
	public static ListIdentifiers instance( HttpClient httpClient, URI baseURL, String metadataPrefix, String set, LocalDate from, LocalDate until )
        throws IOException, OAIException, InterruptedException
    {
		var requestURL = getRequestURL( baseURL, from, until, set, metadataPrefix );
        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();

        try (var httpResponse = httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ).body() )
		{
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
			return new ListIdentifiers( inputSource );
		}
	}

	/**
	 * Construct the query portion of the http request
	 *
	 * @return a String containing the query portion of the http request
	 */
	private static URI getRequestURL( URI baseURL, LocalDate from, LocalDate until, String set, String metadataPrefix )
	{
        // Validate required parameters
        Objects.requireNonNull( baseURL, "baseURL must not be null" );
        Objects.requireNonNull( metadataPrefix, "metadataPrefix must not be null" );

        // Construct the request URL
		StringBuilder requestURL = new StringBuilder( baseURL.toString() );
		requestURL.append( "?verb=ListIdentifiers" );
        requestURL.append( "&metadataPrefix=" ).append( metadataPrefix );
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
		return URI.create(requestURL.toString());
	}

	/**
	 * Construct the query portion of the http request (resumptionToken version)
	 *
	 * @param baseURL the base URL of the OAI-PMH repository.
	 * @param resumptionToken the resumption token.
     * @return a URI with a resumption token appended.
	 */
	private static URI getRequestURL( URI baseURL, String resumptionToken )
	{
        var listIdentifierURL = baseURL + "?verb=ListIdentifiers&resumptionToken=";
        try
        {
            // Attempt to create the URL using the resumption token directly
            return new URI( listIdentifierURL + resumptionToken );
        }
        catch ( URISyntaxException e )
        {
            // Fall back to encoding the resumption token
            return URI.create( listIdentifierURL + URLEncoder.encode( resumptionToken, StandardCharsets.UTF_8 ) );
        }
	}

	/**
	 * Returns a list of identifiers found in the response. The returned list is unmodifiable.
     *
     * @return a list of record headers.
     * @throws DateTimeParseException if the datestamp element is not valid.
	 */
	public List<RecordHeader> getIdentifiers()
	{
		var recordHeaders = getDocument().getElementsByTagNameNS( OAI_2_0_NAMESPACE, "header" );
		var records = new ArrayList<RecordHeader>(recordHeaders.getLength());
		for ( int i = 0; i < recordHeaders.getLength(); i++ )
		{
			var identifier = getRecordHeader( recordHeaders.item( i ) );
			records.add(identifier);
		}
		return Collections.unmodifiableList(records);
	}
}
