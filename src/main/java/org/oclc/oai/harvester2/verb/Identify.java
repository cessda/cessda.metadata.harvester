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
 * Copyright (C) 2019 - 2023 CESSDA ERIC
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

import eu.cessda.eqb.harvester.HttpClient;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.NoSuchElementException;

/**
 * This class represents an Identify response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public final class Identify extends HarvesterVerb
{
	/**
	 * Client-side Identify verb constructor
	 *
	 * @throws IOException           an I/O error occurred
	 */
	Identify( InputStream is ) throws IOException, SAXException
	{
		super( is );
	}

	public static Identify instance( HttpClient httpClient, URI baseURL) throws IOException, SAXException
	{
		var requestURL = URI.create(baseURL + "?verb=Identify");
		try (var is = httpClient.getHttpResponse( requestURL ))
		{
			return new Identify( is );
		}
	}

	/**
	 * Get the oai:protocolVersion value from the Identify response
	 *
	 * @return the oai:protocolVersion value
	 */
	public String getProtocolVersion()
	{
		try
		{
			if ( SCHEMA_LOCATION_V2_0.equals( getSchemaLocation() ) )
			{
				return getSingleString( "/oai20:OAI-PMH/oai20:Identify/oai20:protocolVersion" );
			}
			else if ( SCHEMA_LOCATION_V1_1_IDENTIFY.equals( getSchemaLocation() ) )
			{
				return getSingleString( "/oai11_Identify:Identify/oai11_Identify:protocolVersion" );
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

}
