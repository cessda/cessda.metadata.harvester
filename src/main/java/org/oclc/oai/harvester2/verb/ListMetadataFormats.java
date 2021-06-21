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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Objects;

/**
 * This class represents an ListMetadataFormats response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class ListMetadataFormats extends HarvesterVerb
{
	/**
	 * Client-side ListMetadataFormats verb constructor
	 *
	 * @throws MalformedURLException
	 *             the baseURL is bad
	 * @throws SAXException
	 *             the xml response is bad
	 * @throws IOException
	 *             an I/O error occurred
	 */
	ListMetadataFormats( InputStream in ) throws IOException, SAXException
	{
		super( in );
	}

	public static ListMetadataFormats instance( URI baseURL ) throws IOException, SAXException
	{
		var requestURL = getRequestURL( baseURL, null );
		try (var in = HttpClient.getHttpResponse( requestURL.toURL() , DEFAULT_TIMEOUT ))
		{
			return new ListMetadataFormats( in );
		}
	}

	public static ListMetadataFormats instance( URI baseURL, String identifier ) throws IOException, SAXException
	{
		Objects.requireNonNull(identifier, "identifier cannot be null");
		var requestURL = getRequestURL( baseURL, identifier );
		try (var in = HttpClient.getHttpResponse( requestURL.toURL() , DEFAULT_TIMEOUT ))
		{
			return new ListMetadataFormats( in );
		}
	}

	/**
	 * Construct the query portion of the http request
	 *
	 * @return a String containing the query portion of the http request
	 */
	private static URI getRequestURL( URI baseURL, String identifier )
	{
		StringBuilder requestURL = new StringBuilder( baseURL.toString() );
		requestURL.append( "?verb=ListMetadataFormats" );

		if ( identifier != null )
		{
			requestURL.append( "&identifier=" ).append( identifier );
		}

		return URI.create(requestURL.toString());
	}
}
