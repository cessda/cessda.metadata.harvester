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
 * Copyright (C) 2019 - 2025 CESSDA ERIC
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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
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
	 * Client-side ListRecords verb constructor
	 *
	 * @throws SAXException
	 *             the xml response is bad
	 * @throws IOException
	 *             an I/O error occurred
	 */
	ListRecords( InputSource is ) throws IOException, SAXException
	{
		super( is );
	}

	public static ListRecords instance( HttpClient httpClient, URI baseURL, String resumptionToken ) throws IOException, SAXException
	{
		var requestURL = getRequestURL( baseURL, resumptionToken );
		try (var httpResponse = httpClient.getHttpResponse( requestURL ))
		{
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
			return new ListRecords( inputSource );
		}
	}

	public static ListRecords instance( HttpClient httpClient, URI baseURL, LocalDate from, LocalDate until, String set, String metadataPrefix )
			throws IOException, SAXException
	{
		var requestURL = getRequestURL( baseURL, from, until, set, metadataPrefix );
		try (var httpResponse = httpClient.getHttpResponse( requestURL ))
		{
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
			return new ListRecords( inputSource );
		}
	}

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
		requestURL.append( "&oaiConfiguration=" ).append( metadataPrefix );
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
