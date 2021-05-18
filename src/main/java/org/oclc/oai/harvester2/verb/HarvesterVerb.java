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

import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

/**
 * HarvesterVerb is the parent class for each of the OAI verbs.
 *
 * @author Jefffrey A. Young, OCLC Online Computer Library Center
 */
public abstract class HarvesterVerb
{

	// Constants used by the HTTP client
	private static final String RETRY_AFTER = "Retry-After";

	// Logger
	private static final Logger log = LoggerFactory.getLogger( HarvesterVerb.class );

	/* Primary OAI namespaces */
	public static final String SCHEMA_LOCATION_V2_0 = "http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd";
	public static final String SCHEMA_LOCATION_V1_1_GET_RECORD = "http://www.openarchives.org/OAI/1.1/OAI_GetRecord http://www.openarchives.org/OAI/1.1/OAI_GetRecord.xsd";
	public static final String SCHEMA_LOCATION_V1_1_IDENTIFY = "http://www.openarchives.org/OAI/1.1/OAI_Identify http://www.openarchives.org/OAI/1.1/OAI_Identify.xsd";
	public static final String SCHEMA_LOCATION_V1_1_LIST_IDENTIFIERS = "http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers.xsd";
	public static final String SCHEMA_LOCATION_V1_1_LIST_METADATA_FORMATS = "http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats.xsd";
	public static final String SCHEMA_LOCATION_V1_1_LIST_RECORDS = "http://www.openarchives.org/OAI/1.1/OAI_ListRecords http://www.openarchives.org/OAI/1.1/OAI_ListRecords.xsd";
	public static final String SCHEMA_LOCATION_V1_1_LIST_SETS = "http://www.openarchives.org/OAI/1.1/OAI_ListSets http://www.openarchives.org/OAI/1.1/OAI_ListSets.xsd";

	private static final Element namespaceElement;
	private static final DocumentBuilderFactory factory;
	private static final TransformerFactory xformFactory = TransformerFactory.newInstance();

	/**
	 * A node list that is always empty.
	 */
	public static final NodeList EMPTY_NODE_LIST = new NodeList()
	{
		@Override
		public Node item( int index )
		{
			return null;
		}

		@Override
		public int getLength()
		{
			return 0;
		}
	};

	static
	{
		try
		{
			/* Load DOM Document */
			factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware( true );
			DOMImplementation impl = factory.newDocumentBuilder().getDOMImplementation();
			Document namespaceHolder = impl.createDocument( "http://www.oclc.org/research/software/oai/harvester",
					"harvester:namespaceHolder", null );
			namespaceElement = namespaceHolder.getDocumentElement();
			namespaceElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:harvester",
					"http://www.oclc.org/research/software/oai/harvester" );
			namespaceElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi",
					"http://www.w3.org/2001/XMLSchema-instance" );
			namespaceElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:oai20",
					"http://www.openarchives.org/OAI/2.0/" );
			namespaceElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:oai11_GetRecord",
					"http://www.openarchives.org/OAI/1.1/OAI_GetRecord" );
			namespaceElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:oai11_Identify",
					"http://www.openarchives.org/OAI/1.1/OAI_Identify" );
			namespaceElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:oai11_ListIdentifiers",
					"http://www.openarchives.org/OAI/1.1/OAI_ListIdentifiers" );
			namespaceElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:oai11_ListMetadataFormats",
					"http://www.openarchives.org/OAI/1.1/OAI_ListMetadataFormats" );
			namespaceElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:oai11_ListRecords",
					"http://www.openarchives.org/OAI/1.1/OAI_ListRecords" );
			namespaceElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:oai11_ListSets",
					"http://www.openarchives.org/OAI/1.1/OAI_ListSets" );
		}
		catch ( ParserConfigurationException e )
		{
			throw new IllegalStateException( e );
		}
	}

	// Instance variables
	private final Document doc;
	private final String schemaLocation;
	private final URI requestURL;

	/**
	 * Performs the OAI request with the default timeout of 10 seconds.
	 *
	 * @param requestURL the URL to request
	 * @throws IOException
	 * @throws SAXException
	 */
	protected HarvesterVerb( String requestURL ) throws IOException, SAXException
	{
		this( requestURL, Duration.ofSeconds( 10 ) );
	}

	/**
	 * Performs the OAI request
	 *
	 * @param requestURL
	 * @throws IOException
	 * @throws SAXException
	 */
	protected HarvesterVerb( String requestURL, Duration timeout ) throws IOException, SAXException
	{
		this.requestURL = URI.create( requestURL );

		try ( InputStream in = getHttpResponse( this.requestURL.toURL(), timeout ) )
		{
			doc = factory.newDocumentBuilder().parse( in );
		}
		catch ( ParserConfigurationException e )
		{
			throw new IllegalStateException( e );
		}

		String schemaLocationTemp;
		try
		{
			schemaLocationTemp = getSingleString( "/*/@xsi:schemaLocation" );
		}
		catch ( TransformerException e )
		{
			schemaLocationTemp = "";
		}
		this.schemaLocation = schemaLocationTemp;
	}

	private static InputStream getHttpResponse( URL requestURL, Duration timeout ) throws IOException
	{
		HttpURLConnection con;
		int responseCode;
		int retries = 0;

		while ( true )
		{
			con = (HttpURLConnection) requestURL.openConnection();
			con.setRequestProperty( "User-Agent", "OAIHarvester/2.0" );
			con.setRequestProperty( "Accept-Encoding", "compress, gzip, identity" );

			// TK added default timeout for dataverses taking too long to respond / stall
			log.trace( "Timeout : {}", timeout );
			con.setConnectTimeout( (int) timeout.toMillis() );
			con.setReadTimeout( (int) timeout.toMillis() );

			responseCode = con.getResponseCode();
			log.trace( "responseCode={}", responseCode );

			if ( responseCode == HTTP_UNAVAILABLE && retries++ < 3 )
			{
				long retrySeconds = con.getHeaderFieldInt( RETRY_AFTER, -1 );
				if ( retrySeconds == -1 )
				{
					long now = Instant.now().toEpochMilli();
					long retryDate = con.getHeaderFieldDate( RETRY_AFTER, now );
					retrySeconds = retryDate - now;
				}
				if ( retrySeconds > 0 )
				{
					log.debug( "Server response: Retry-After={}", con.getHeaderField( RETRY_AFTER ) );
					try
					{
						TimeUnit.SECONDS.sleep( retrySeconds );
					}
					catch ( InterruptedException ex )
					{
						Thread.currentThread().interrupt();
						return InputStream.nullInputStream();
					}
				}
			}
			else
			{
				break;
			}
		}

		if ( responseCode >= 400 )
		{
			throw handleHTTPResponseErrors( con, responseCode );
		}

		return decodeHttpInputStream( con );
	}

	private static IOException handleHTTPResponseErrors( HttpURLConnection con, int responseCode )
	{
		IOException exception;
		try ( var stream = decodeHttpInputStream( con ) )
		{
			exception = new IOException( String.format( "Server returned %d, body: %s",
					responseCode,
					new String( stream.readAllBytes(), StandardCharsets.UTF_8 )
			) );
		}
		catch ( IOException e )
		{
			// Make sure the response code is not lost if an IO error occurs
			exception = new IOException( String.format( "Server returned %d", responseCode ), e );
		}
		return exception;
	}

	/**
	 * Get the OAI response as a DOM object
	 *
	 * @return the DOM for the OAI response
	 */
	public Document getDocument()
	{
		return doc;
	}

	/**
	 * Get the xsi:schemaLocation for the OAI response
	 * 
	 * @return the xsi:schemaLocation value
	 */
	public String getSchemaLocation()
	{
		return schemaLocation;
	}

	/**
	 * Get the OAI errors
	 *
	 * @return a NodeList of /oai:OAI-PMH/oai:error elements
	 */
	public NodeList getErrors()
	{
		try
		{
			if ( SCHEMA_LOCATION_V2_0.equals( getSchemaLocation() ) )
			{
				return XPathAPI.selectNodeList( getDocument(), "/oai20:OAI-PMH/oai20:error", namespaceElement );
			}
			else
			{
				return EMPTY_NODE_LIST;
			}
		}
		catch ( TransformerException e )
		{
			throw new IllegalStateException( e );
		}
	}

	/**
	 * Get the OAI request URL for this response
	 *
	 * @return the OAI request URL as a String
	 */
	public URI getRequestURL()
	{
		return requestURL;
	}

	private static InputStream decodeHttpInputStream( HttpURLConnection con ) throws IOException
	{
		String contentEncoding = con.getHeaderField( "Content-Encoding" );
		log.trace( "contentEncoding={}", contentEncoding );

		final InputStream inputStream;
		if ( con.getResponseCode() < 400 )
		{
			inputStream = con.getInputStream();
		}
		else
		{
			inputStream = con.getErrorStream();
		}

		if ( contentEncoding == null )
		{
			return inputStream;
		}

		switch ( contentEncoding )
		{
			case "compress":
				ZipInputStream zis = new ZipInputStream( inputStream );
				zis.getNextEntry();
				return zis;
			case "gzip":
				return new GZIPInputStream( inputStream );
			case "deflate":
				return new InflaterInputStream( inputStream );
			default:
				return inputStream;
		}
	}

	/**
	 * Get the String value for the given XPath location in the response DOM
	 *
	 * @param xpath the XPath to evaluate.
	 * @return a String containing the value of the XPath location.
	 * @throws TransformerException if an error occurs getting the XPath location
	 */
	public String getSingleString( String xpath ) throws TransformerException
	{
		return XPathAPI.eval( getDocument(), xpath, namespaceElement ).str();
	}

	public String toString()
	{
		try ( StringWriter sw = new StringWriter() )
		{
			Result output = new StreamResult( sw );
			Transformer idTransformer = xformFactory.newTransformer();
			idTransformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
			idTransformer.transform( new DOMSource( getDocument() ), output );
			return sw.toString();
		}
		catch (TransformerException | IOException e)
		{
			return e.toString();
		}
	}
}
