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

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * This class represents an GetRecord response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public final class GetRecord extends HarvesterVerb
{
    /**
	 * Construct a new {@link GetRecord} instance from an {@link InputSource}.
     *
	 * @param is the input source to parse.
	 * @throws IOException if an IO error occurs when reading the input stream.
	 * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
	 */
	GetRecord( InputSource is ) throws IOException, OAIException
	{
		super(is);
	}

    /**
     * Query an OAI-PMH repository for a record using the GetRecord verb.
     *
     * @param httpClient     the HTTP client to use.
     * @param baseURL        the baseURL of the server to be queried.
     * @param identifier     the record identifier.
     * @param metadataPrefix the metadata prefix of the record to retrieve.
     * @return a GetRecord response.
     * @throws IOException  if an IO error occurred.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     * @throws InterruptedException if the operation was interrupted.
     */
    public static GetRecord instance( HttpClient httpClient, URI baseURL, String identifier, String metadataPrefix ) throws IOException, OAIException, InterruptedException
    {
        var requestURL = getRequestURL( baseURL, identifier, metadataPrefix );
        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();

        try ( var httpResponse = httpClient.send( httpRequest, HttpResponse.BodyHandlers.ofInputStream() ).body() )
        {
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
            return new GetRecord( inputSource );
        }
    }

    /**
     * Get an OAI-PMH GetRecord request as an input stream.
     *
     * @param httpClient     the HTTP client to use.
     * @param baseURL        the baseURL of the server to be queried.
     * @param identifier     the record identifier.
     * @param metadataPrefix the metadata prefix of the record to retrieve.
     * @return an input stream of the GetRecord response.
     * @throws IOException if an IO error occurred.
     * @throws InterruptedException if the operation was interrupted.
     */
    public static InputStream asStream( HttpClient httpClient, URI baseURL, String identifier, String metadataPrefix ) throws IOException, InterruptedException
    {
        var requestURL = getRequestURL( baseURL, identifier, metadataPrefix );

        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();

        return httpClient.send( httpRequest, bodyHandler ).body();
    }

    private static URI getRequestURL( URI baseURL, String identifier, String metadataPrefix )
    {
        return URI.create( baseURL + "?verb=GetRecord"
            + "&identifier=" + identifier
            + "&metadataPrefix=" + metadataPrefix
        );
    }

    /**
     * Determine whether a record should be harvested.
     * <p>
     * This method checks whether the datestamps in the OAI-PMH header differ and returns {@code true} if they do.
     *
     * @param newRecordHeader an input stream representing the record that would be harvested
     * @param oldRecord an input stream representing the already harvested record
     * @return {@code true} if the timestamps differ or if an IO error occurs loading
     * @throws XMLStreamException if an error occurs parsing the XML.
     */
    public static boolean shouldHarvest( RecordHeader newRecordHeader, InputStream oldRecord ) throws XMLStreamException
    {
        // Try opening existing file
        var previousRecordReader = xmlInputFactory.createXMLStreamReader( oldRecord );
        var previousDateStamp = getDateStamp( previousRecordReader );

        // Harvest if the datestamp is not equal
        return !newRecordHeader.datestamp().equals( previousDateStamp );
    }

    /**
     * Get the oai:record header.
     *
     * @return the header.
     */
    public RecordHeader getHeader()
    {
        var recordHeader = getDocument().getElementsByTagNameNS( OAI_2_0_NAMESPACE, "header" );
        var headerNode = recordHeader.item( 0 );
        return getRecordHeader( headerNode );
    }

    /**
	 * Gets the metadata of the OAI-PMH response.
	 *
	 * @return the metadata section of the document, or an empty optional if metadata was not returned.
	 */
	public Optional<Element> getMetadata()
	{
		var metadataElements = getDocument().getElementsByTagNameNS( OAI_2_0_NAMESPACE, "metadata" );

		// If a record is deleted, then the metadata section will not be present
        for ( int i = 0; i < metadataElements.getLength(); i++ )
        {
            var metadataNodes = metadataElements.item( i ).getChildNodes();
			for ( int j = 0; j < metadataNodes.getLength(); j++ )
			{
			    // Select the first element within the metadata element
                var node = metadataNodes.item( j );
                if ( node instanceof Element element )
                {
					return Optional.of( element );
				}
            }
        }
        return Optional.empty();
	}
}
