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
 * Copyright (C) 2019 - 2024 CESSDA ERIC
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
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

/**
 * This class represents an GetRecord response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public final class GetRecord extends HarvesterVerb
{
    /**
	 * Construct a new {@link GetRecord} from an {@link InputStream}.
	 * @param in the input stream to parse.
	 * @throws IOException if an IO error occurs when reading the input stream.
	 * @throws SAXException if an error occurs when parsing the XML.
	 */
	GetRecord( InputSource in ) throws IOException, SAXException
	{
		super(in);
	}

    /**
     * Query an OAI-PMH repository for a record using the GetRecord verb.
     *
     * @param httpClient     the HTTP client to use.
     * @param baseURL        the baseURL of the server to be queried.
     * @param identifier     the record identifier.
     * @param metadataPrefix the metadata prefix of the record to retrieve.
     * @throws IOException  if an IO error occurred.
     * @throws SAXException if the XML could not be parsed.
     */
    public static GetRecord instance( HttpClient httpClient, URI baseURL, String identifier, String metadataPrefix ) throws IOException, SAXException
    {
        var requestURL = getRequestURL( baseURL, identifier, metadataPrefix );

        try ( var httpResponse = httpClient.getHttpResponse( requestURL ) )
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
     * @throws IOException if an IO error occurred.
     */
    public static InputStream asStream( HttpClient httpClient, URI baseURL, String identifier, String metadataPrefix ) throws IOException
    {
        var requestURL = getRequestURL( baseURL, identifier, metadataPrefix );
        return httpClient.getHttpResponse( requestURL );
    }

    private static URI getRequestURL( URI baseURL, String identifier, String metadataPrefix )
    {
        return URI.create( baseURL + "?verb=GetRecord"
            + "&identifier=" + identifier
            + "&metadataPrefix=" + metadataPrefix
        );
    }

    /**
     * Get the oai:record header.
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
