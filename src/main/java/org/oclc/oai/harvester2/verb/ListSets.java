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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * This class represents an ListSets response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class ListSets extends HarvesterVerb implements Resumable
{
	/**
	 * Client-side ListSets verb constructor
	 *
	 * @param baseURL the baseURL of the server to be queried
	 * @throws MalformedURLException the baseURL is bad
	 * @throws IOException           an I/O error occurred
	 */
	public ListSets( HttpClient httpClient, URI baseURL ) throws IOException, SAXException
	{
		super( httpClient, getRequestURL( baseURL ));
	}

	/**
	 * Client-side ListSets verb constructor
	 *
	 * @param baseURL the baseURL of the server to be queried
	 * @throws MalformedURLException the baseURL is bad
	 * @throws IOException           an I/O error occurred
	 */
	public ListSets( HttpClient httpClient, URI baseURL, String resumptionToken ) throws IOException, SAXException
	{
		super( httpClient, getRequestURL( baseURL, resumptionToken ) );
	}

	/**
	 * Generate a ListSets request for the given baseURL
	 *
	 * @param baseURL
	 * @return
	 */
	private static URI getRequestURL( URI baseURL )
	{
		return URI.create(baseURL + "?verb=ListSets");
	}

	/**
	 * Construct the query portion of the http request (resumptionToken version)
	 *
	 * @param baseURL
	 * @param resumptionToken
	 * @return
	 */
	private static URI getRequestURL( URI baseURL, String resumptionToken )
	{
		return URI.create(baseURL + "?verb=ListRecords"
				+ "&resumptionToken=" + URLEncoder.encode( resumptionToken, StandardCharsets.UTF_8 )
		);
	}
}
