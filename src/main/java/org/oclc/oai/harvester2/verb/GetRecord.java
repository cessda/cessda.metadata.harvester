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
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * This class represents an GetRecord response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class GetRecord extends HarvesterVerb
{
	GetRecord( InputStream in ) throws IOException, SAXException
	{
		super(in);
	}

	/**
	 * Client-side GetRecord verb constructor
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
	public static GetRecord instance( URI baseURL, String identifier, String metadataPrefix, Duration timeout ) throws IOException, SAXException
	{
		var requestURL = getRequestURL( baseURL, identifier, metadataPrefix );
		try (var in = HttpClient.getHttpResponse( requestURL.toURL(), timeout ))
		{
			return new GetRecord( in );
		}
	}

	/**
	 * Get the oai:identifier from the oai:header
	 *
	 * @return identifier
	 */
	public String getIdentifier()
	{
		try
		{
			if ( SCHEMA_LOCATION_V2_0.equals( getSchemaLocation() ) )
			{
				return getSingleString( "/oai20:OAI-PMH/oai20:GetRecord/oai20:record/oai20:header/oai20:identifier" );
			}
			else if ( SCHEMA_LOCATION_V1_1_GET_RECORD.equals( getSchemaLocation() ) )
			{
				return getSingleString( "/oai11_GetRecord:GetRecord/oai11_GetRecord:record/oai11_GetRecord:header/oai11_GetRecord:identifier" );
			}
			else
			{
				throw new NoSuchElementException( getSchemaLocation() );
			}
		}
		catch ( TransformerException e )
		{
			throw new IllegalStateException( e );
		}
	}

	/**
	 * Gets the metadata of the OAI-PMH response.
	 *
	 * @return the metadata section of the document, or an empty optional if metadata was not returned.
	 */
	public Optional<Node> getMetadata()
	{
		var metadataElements = getDocument().getElementsByTagNameNS( OAI_2_0_NAMESPACE, "metadata" );

		// If a record is deleted, then the metadata section will not be present
		if (metadataElements.getLength() > 0)
		{
			var metadataChildNodes = metadataElements.item( 0 ).getChildNodes();
			return Optional.ofNullable( metadataChildNodes.item( 0 ) );
		}

		return Optional.empty();
	}

	/**
	 * Construct the query portion of the http request
	 *
	 * @return a String containing the query portion of the http request
	 */
	private static URI getRequestURL( URI baseURL, String identifier, String metadataPrefix )
	{
		return URI.create(baseURL + "?verb=GetRecord"
				+ "&identifier=" + identifier
				+ "&metadataPrefix=" + metadataPrefix
		);
	}
}
