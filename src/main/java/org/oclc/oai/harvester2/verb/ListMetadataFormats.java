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
import java.io.InputStream;
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
public class ListMetadataFormats extends HarvesterVerb
{
	/**
	 * Client-side ListMetadataFormats verb constructor
	 *
	 * @throws MalformedURLException
	 *             the baseURL is bad
	 * @throws SAXException
	 *             the xml response is bad
	 * @throws IOException
	 *             an I/O error occurred
	 */
	ListMetadataFormats( InputStream in ) throws IOException, SAXException
	{
		super( in );
	}

    /**
     * Construct a new {@link ListMetadataFormats} instance from an OAI-PMH repository URL.
     * @param baseURL the URL of the OAI-PMH repository.
     * @throws IOException if an IO error occurs.
     * @throws SAXException if an error occurs when parsing the XML.
     */
	public static ListMetadataFormats instance( HttpClient httpClient, URI baseURL ) throws IOException, SAXException
	{
		var requestURL = getRequestURL( baseURL, null );
		try (var in = httpClient.getHttpResponse( requestURL ))
		{
			return new ListMetadataFormats( in );
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
		Objects.requireNonNull(identifier, "identifier cannot be null");
		var requestURL = getRequestURL( baseURL, identifier );
		try (var in = httpClient.getHttpResponse( requestURL ))
		{
			return new ListMetadataFormats( in );
		}
	}

    /**
     * Gets a list of metadata formats.
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

                switch ( node.getNodeName() )
                {
                    case "metadataPrefix":
                        metadataPrefix = node.getTextContent();
                        break;

                    case "schema":
                        schema = new URI( node.getTextContent() );
                        break;

                    case "metadataNamespace":
                        metadataNamespace = new URI( node.getTextContent() );
                        break;

                    default:
                        break;
                }
            }

            list.add( new MetadataFormat( metadataPrefix, schema, metadataNamespace ) );
        }

        return list;
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

		return URI.create(requestURL.toString());
	}

	public static class MetadataFormat
    {
	    private final String metadataPrefix;
	    private final URI schema;
	    private final URI metadataNamespace;

        public MetadataFormat( String metadataPrefix, URI schema, URI metadataNamespace )
        {
            this.metadataPrefix = Objects.requireNonNull( metadataPrefix );
            this.schema = Objects.requireNonNull( schema );
            this.metadataNamespace = Objects.requireNonNull( metadataNamespace );
        }

        public String getMetadataPrefix()
        {
            return metadataPrefix;
        }

        public URI getSchema()
        {
            return schema;
        }

        public URI getMetadataNamespace()
        {
            return metadataNamespace;
        }

        @Override
        public boolean equals( Object o )
        {
            if ( this == o ) return true;
            if ( o == null || getClass() != o.getClass() ) return false;
            MetadataFormat that = (MetadataFormat) o;
            return metadataPrefix.equals( that.metadataPrefix ) &&
                schema.equals( that.schema ) &&
                metadataNamespace.equals( that.metadataNamespace );
        }

        @Override
        public int hashCode()
        {
            return Objects.hash( metadataPrefix, schema, metadataNamespace );
        }

        @Override
        public String toString()
        {
            return "MetadataFormat{" +
                "metadataPrefix='" + metadataPrefix + '\'' +
                ", schema=" + schema +
                ", metadataNamespace=" + metadataNamespace +
                '}';
        }
    }
}
