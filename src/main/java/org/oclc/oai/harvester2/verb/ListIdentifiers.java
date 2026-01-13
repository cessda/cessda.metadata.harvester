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

import eu.cessda.oaiharvester.HttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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
	 * Client-side ListIdentifiers verb constructor
	 *
	 * @throws SAXException the xml response is bad
	 * @throws IOException  an I/O error occurred
	 */
	ListIdentifiers( InputSource is ) throws IOException, SAXException
	{
		super( is );
	}

	/**
	 * Construct a new instance of {@link ListIdentifiers} using a resumption token.
	 * @param baseURL the URL of the repository.
	 * @param resumptionToken the resumption token.
	 * @throws IOException if an IO error occurs.
	 * @throws SAXException if an error occurs when parsing the XML.
	 */
	public static ListIdentifiers instance( HttpClient httpClient, URI baseURL, String resumptionToken ) throws IOException, SAXException
	{
        Objects.requireNonNull( resumptionToken, "resumptionToken cannot be null" );
		var requestURL = getRequestURL( baseURL, resumptionToken );
		try (var httpResponse = httpClient.getHttpResponse( requestURL ))
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
     * @param baseURL        the URL of the repository.
     * @param metadataPrefix the metadata prefix to use.
     * @param set            the set to harvest.
     * @param from           the date to harvest from. Set to {@code null} to harvest from the beginning.
     * @param until          to date to harvest to. Set to {@code null} for no limit.
     * @throws IOException   if an IO error occurs.
     * @throws SAXException  if an error occurs when parsing the XML.
     */
	public static ListIdentifiers instance( HttpClient httpClient, URI baseURL, String metadataPrefix, String set, LocalDate from, LocalDate until )
			throws IOException, SAXException
	{
		var requestURL = getRequestURL( baseURL, from, until, set, metadataPrefix );
		try (var httpResponse = httpClient.getHttpResponse( requestURL ))
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
	 * Returns a list of identifiers found in the response. The returned list is unmodifiable.
     *
     * @throws java.time.format.DateTimeParseException if the datestamp element is not valid.
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

	/**
	 * Construct the query portion of the http request (resumptionToken version)
	 *
	 * @param baseURL the base URL of the OAI-PMH repository.
	 * @param resumptionToken the resumption token.
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
}
