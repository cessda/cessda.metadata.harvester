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

import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;

import java.util.NoSuchElementException;

/**
 * This class represents an ListRecords response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class ListRecords extends HarvesterVerb
{
	/**
	 * Client-side ListRecords verb constructor
	 *
	 * @param baseURL
	 *            the baseURL of the server to be queried
	 * @throws MalformedURLException
	 *             the baseURL is bad
	 * @throws SAXException
	 *             the xml response is bad
	 * @throws IOException
	 *             an I/O error occurred
	 */
	public ListRecords( String baseURL, String from, String until, String set, String metadataPrefix ) throws IOException, SAXException
	{
		super( getRequestURL( baseURL, from, until, set, metadataPrefix ) );
	}

	/**
	 * Client-side ListRecords verb constructor (resumptionToken version)
	 *
	 * @param baseURL
	 * @param resumptionToken
	 * @throws IOException
	 * @throws SAXException
	 */
	public ListRecords( String baseURL, String resumptionToken ) throws IOException, SAXException
	{
		super( getRequestURL( baseURL, resumptionToken ) );
	}

	/**
	 * Get the oai:resumptionToken from the response
	 *
	 * @return the oai:resumptionToken value
	 */
	public String getResumptionToken()
	{
		try
		{
			String schemaLocation = getSchemaLocation();
			if ( schemaLocation.contains( SCHEMA_LOCATION_V2_0 ) )
			{
				return getSingleString( "/oai20:OAI-PMH/oai20:ListRecords/oai20:resumptionToken" );
			}
			else if ( schemaLocation.contains( SCHEMA_LOCATION_V1_1_LIST_RECORDS ) )
			{
				return getSingleString( "/oai11_ListRecords:ListRecords/oai11_ListRecords:resumptionToken" );
			}
			else
			{
				throw new NoSuchElementException( schemaLocation );
			}
		}
		catch ( TransformerException e )
		{
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Construct the query portion of the http request
	 *
	 * @return a String containing the query portion of the http request
	 */
	private static String getRequestURL(
			String baseURL,
			String from,
			String until,
			String set,
			String metadataPrefix )
	{
		StringBuilder requestURL = new StringBuilder( baseURL );
		requestURL.append( "?verb=ListRecords" );
		if ( from != null )
			requestURL.append( "&from=" ).append( from );
		if ( until != null )
			requestURL.append( "&until=" ).append( until );
		if ( set != null )
			requestURL.append( "&set=" ).append( set );
		requestURL.append( "&metadataPrefix=" ).append( metadataPrefix );
		return requestURL.toString();
	}

	/**
	 * Construct the query portion of the http request (resumptionToken version)
	 *
	 * @param baseURL
	 * @param resumptionToken
	 * @return
	 */
	private static String getRequestURL( String baseURL, String resumptionToken ) throws UnsupportedEncodingException
	{
		return baseURL + "?verb=ListRecords" + "&resumptionToken=" + URLEncoder.encode( resumptionToken, "UTF-8" );
	}
}
