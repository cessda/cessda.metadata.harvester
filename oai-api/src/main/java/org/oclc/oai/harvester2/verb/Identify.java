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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * This class represents an Identify response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public final class Identify extends HarvesterVerb
{
	/**
	 * Construct a new {@link Identify} instance from an {@link InputSource}.
	 *
     * @param is the input source to parse.
	 * @throws IOException if an I/O error occurred
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
	 */
	Identify( InputSource is ) throws IOException, OAIException
	{
		super( is );
	}

    /**
     * Construct a new {@link Identify} instance from an OAI-PMH repository URL.
     *
     * @param httpClient the HTTP client used to get the OAI-PMH response.
     * @param baseURL the URL of the OAI-PMH repository.
     * @return an Identify response.
     * @throws IOException  if an IO error occurred.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     * @throws InterruptedException if the operation was interrupted.
     */
	public static Identify instance( HttpClient httpClient, URI baseURL ) throws IOException, OAIException, InterruptedException
    {
		var requestURL = URI.create(baseURL + "?verb=Identify");
        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();

        try ( var httpResponse =  httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ).body() )
        {
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
            return new Identify( inputSource );
        }
	}
}
