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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
     * Construct a new {@link ListMetadataFormats} instance from an {@link InputSource}.
     *
     * @param is the input source to parse.
     * @throws IOException if an IO error occurs when reading the input stream.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error..
     */
    ListMetadataFormats( InputSource is ) throws IOException, OAIException
    {
        super( is );
    }

    /**
     * Construct a new {@link ListMetadataFormats} instance from an OAI-PMH repository URL.
     *
     * @param httpClient     the HTTP client to use.
     * @param baseURL the URL of the OAI-PMH repository.
     * @return a {@link ListMetadataFormats} instance.
     * @throws IOException  if an IO error occurs.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     */
    public static ListMetadataFormats instance( HttpClient httpClient, URI baseURL ) throws IOException, OAIException
    {
        var requestURL = getRequestURL( baseURL, null );
        InputStream result;

        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();

        try
        {
            result = httpClient.send( httpRequest, bodyHandler ).body();
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
            throw new IOException( e );
        }
        try ( var httpResponse = result )
        {
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
            return new ListMetadataFormats( inputSource );
        }
    }

    /**
     * Construct a new {@link ListMetadataFormats} instance for a specific record identifier in an OAI-PMH repository
     *
     * @param httpClient     the HTTP client to use.
     * @param baseURL the URL of the OAI-PMH repository.
     * @param identifier the record identifier.
     * @return the {@link ListMetadataFormats} instance.
     * @throws IOException if an IO error occurs.
     * @throws OAIException if an error occurs when parsing the stream, or the endpoint returns an error.
     */
    public static ListMetadataFormats instance( HttpClient httpClient, URI baseURL, String identifier ) throws IOException, OAIException
    {
        Objects.requireNonNull( identifier, "identifier cannot be null" );
        var requestURL = getRequestURL( baseURL, identifier );
        InputStream result;

        var httpRequest = HttpRequest.newBuilder( requestURL ).GET().build();
        var bodyHandler = HttpResponse.BodyHandlers.ofInputStream();

        try
        {
            result = httpClient.send( httpRequest, bodyHandler ).body();
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
            throw new IOException( e );
        }
        try ( var httpResponse = result )
        {
            var inputSource = new InputSource();
            inputSource.setSystemId( requestURL.toASCIIString() );
            inputSource.setByteStream( httpResponse );
            return new ListMetadataFormats( inputSource );
        }
    }

    /**
     * Construct a {@link URI} with the query portion of the http request appended.
     *
     * @return the {@link URI}.
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
     * @return a list of metadata formats.
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

    /**
     * An OAI-PMH MetadataFormat response. All fields are required.
     *
     * @param metadataPrefix    the metadata prefix.
     * @param schema            the XML schema of the metadata format.
     * @param metadataNamespace the XML namespace of the metadata format.
     * @see <a href="https://www.openarchives.org/OAI/openarchivesprotocol.html#ListMetadataFormats">MetadataFormat specification</a>
     */
    public record MetadataFormat( String metadataPrefix, URI schema, URI metadataNamespace )
    {
        /**
         * Construct a new {@link MetadataFormat} instance.
         *
         * @param metadataPrefix    the metadata prefix.
         * @param schema            the XML schema of the metadata format.
         * @param metadataNamespace the XML namespace of the metadata format.
         */
        public MetadataFormat( String metadataPrefix, URI schema, URI metadataNamespace )
        {
            this.metadataPrefix = Objects.requireNonNull( metadataPrefix );
            this.schema = Objects.requireNonNull( schema );
            this.metadataNamespace = Objects.requireNonNull( metadataNamespace );
        }
    }
}
