/*-
 * #%L
 * CESSDA Euro Question Bank: Metadata Harvester
 * %%
 * Copyright (C) 2020 CESSDA ERIC
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

import eu.cessda.eqb.harvester.HttpClient;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents an ListIdentifiers response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class ListIdentifiers extends HarvesterVerb implements Resumable
{
	/**
	 * Client-side ListIdentifiers verb constructor
	 *
	 * @param baseURL the baseURL of the server to be queried
	 * @throws SAXException the xml response is bad
	 * @throws IOException  an I/O error occurred
	 */
	public ListIdentifiers( HttpClient httpClient, URI baseURL, String from, String until, String set, String metadataPrefix, Duration timeout )
			throws IOException, SAXException
	{
		super( httpClient, getRequestURL( baseURL, from, until, set, metadataPrefix ), timeout );
	}

	/**
	 * Client-side ListIdentifiers verb constructor (resumptionToken version)
	 *
	 * @param baseURL
	 * @param resumptionToken
	 * @throws IOException
	 * @throws SAXException
	 */
	public ListIdentifiers( HttpClient httpClient, URI baseURL, String resumptionToken, Duration timeout ) throws IOException, SAXException
	{
		super( httpClient, getRequestURL( baseURL, resumptionToken ), timeout );
	}

	/**
	 * Construct the query portion of the http request
	 *
	 * @return a String containing the query portion of the http request
	 */
	private static URI getRequestURL( URI baseURL, String from, String until, String set, String metadataPrefix )
	{

		StringBuilder requestURL = new StringBuilder( baseURL.toString() );
		requestURL.append( "?verb=ListIdentifiers" );
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
	 * Returns a list of identifiers found in the response. The returned list is unmodifiable.
	 */
	public List<String> getIdentifiers()
	{
		var identifiersIDs = getDocument().getElementsByTagNameNS( OAI_2_0_NAMESPACE, "identifier" );
		var records = new ArrayList<String>(identifiersIDs.getLength());
		for ( int i = 0; i < identifiersIDs.getLength(); i++ )
		{
			var identifier = identifiersIDs.item( i ).getTextContent();
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
		return URI.create(baseURL + "?verb=ListIdentifiers"
				+ "&resumptionToken=" + URLEncoder.encode( resumptionToken, StandardCharsets.UTF_8 )
		);
	}
}
