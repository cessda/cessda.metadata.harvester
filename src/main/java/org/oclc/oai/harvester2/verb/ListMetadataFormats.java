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
 * Copyright (C) 2019 - 2025 CESSDA ERIC
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents an ListMetadataFormats response on either the server or on the client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public final class ListMetadataFormats extends HarvesterVerb
{
    /**
     * Client-side ListMetadataFormats verb constructor
     *
     * @throws MalformedURLException the baseURL is bad
     * @throws SAXException          the xml response is bad
     * @throws IOException           an I/O error occurred
     */
    ListMetadataFormats( InputSource in ) throws IOException, SAXException
    {
        super( in );
    }

    /**
     * Construct a new {@link ListMetadataFormats} instance from an OAI-PMH repository URL.
     *
     * @param baseURL the URL of the OAI-PMH repository.
     * @throws IOException  if an IO error occurs.
     * @throws SAXException if an error occurs when parsing the XML.
     */
    public static ListMetadataFormats instance( HttpClient httpClient, URI baseURL ) throws IOException, SAXException
    {
        var requestURL = getRequestURL( baseURL, null );
        try ( var httpResponse = httpClient.getHttpResponse( requestURL ) )
        {
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
            return new ListMetadataFormats( inputSource );
        }
    }

    /**
     * Construct a new {@link ListMetadataFormats} instance for a specific record identifier in an OAI-PMH repository
     * @param baseURL the URL of the OAI-PMH repository.
     * @param identifier the record identifier.
     * @throws IOException if an IO error occurs.
     * @throws SAXException if an error occurs when parsing the XML.
     */
    public static ListMetadataFormats instance( HttpClient httpClient, URI baseURL, String identifier ) throws IOException, SAXException
    {
        Objects.requireNonNull( identifier, "identifier cannot be null" );
        var requestURL = getRequestURL( baseURL, identifier );
        try ( var httpResponse = httpClient.getHttpResponse( requestURL ) )
        {
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
            return new ListMetadataFormats( inputSource );
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

        return URI.create( requestURL.toString() );
    }

    /**
     * Gets a list of metadata formats.
     *
     * @throws URISyntaxException if the schema or metadataNamespace elements cannot be parsed as a {@link URI}.
     */
    public List<MetadataFormat> getMetadataFormats() throws URISyntaxException
    {
        var metadataFormats = getDocument().getElementsByTagNameNS( OAI_2_0_NAMESPACE, "metadataFormat" );

        var list = new ArrayList<MetadataFormat>();

        for ( int i = 0; i < metadataFormats.getLength(); i++ )
        {
            var childNodes = metadataFormats.item( i ).getChildNodes();

            String metadataPrefix = null;
            URI schema = null;
            URI metadataNamespace = null;

            for ( int j = 0; j < childNodes.getLength(); j++ )
            {
                var node = childNodes.item( j );
                var localName = node.getLocalName();

                if ( "metadataPrefix".equals( localName ) )
                {
                    metadataPrefix = node.getTextContent().trim();
                }
                else if ( "schema".equals( localName ) )
                {
                    schema = new URI( node.getTextContent().trim() );
                }
                else if ( "metadataNamespace".equals( localName ) )
                {
                    metadataNamespace = new URI( node.getTextContent().trim() );
                }
            }

            list.add( new MetadataFormat( metadataPrefix, schema, metadataNamespace ) );
        }

        return list;
    }

    public record MetadataFormat(String metadataPrefix, URI schema, URI metadataNamespace)
    {
        public MetadataFormat( String metadataPrefix, URI schema, URI metadataNamespace )
        {
            this.metadataPrefix = Objects.requireNonNull( metadataPrefix );
            this.schema = Objects.requireNonNull( schema );
            this.metadataNamespace = Objects.requireNonNull( metadataNamespace );
        }
    }
}
