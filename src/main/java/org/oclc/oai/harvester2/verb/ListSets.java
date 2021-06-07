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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Duration;

/**
 * This class represents an ListSets response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public class ListSets extends HarvesterVerb implements Resumable
{

	private static final Logger log = LoggerFactory.getLogger( ListSets.class );

	/**
	 * Client-side ListSets verb constructor
	 *
	 * @param baseURL the baseURL of the server to be queried
	 * @throws MalformedURLException the baseURL is bad
	 * @throws IOException           an I/O error occurred
	 */
	public ListSets( HttpClient httpClient, String baseURL, Duration timeout ) throws IOException, SAXException
	{
		super( httpClient, getRequestURL( baseURL ), timeout );
	}

	/**
	 * Generate a ListSets request for the given baseURL
	 *
	 * @param baseURL
	 * @return
	 */
	private static String getRequestURL( String baseURL )
	{
		StringBuilder requestURL = new StringBuilder( baseURL );
		if ( baseURL.contains( "?" ) )
		{
			requestURL.append( "&verb=ListSets" );
		}
		else
		{
			requestURL.append( "?verb=ListSets" );
		}
		log.info( "get Sets: {}", requestURL );
		return requestURL.toString();
	}
}
