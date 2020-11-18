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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.XPathAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * HarvesterVerb is the parent class for each of the OAI verbs.
 *
 * @author Jefffrey A. Young, OCLC Online Computer Library Center
 */
public abstract class HarvesterVerb
{

	// Constants used by the HTTP client
	private static final String RETRY_AFTER = "Retry-After";
	private static final String LOCATION = "Location";

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
		catch (ParserConfigurationException e)
		{
			throw new IllegalStateException( e );
		}
	}

	// Instance variables
	private final Document doc;
	private final String schemaLocation;
	private final String requestURL;

	/**
	 * Mock object creator (for unit testing purposes)
	 */
	public HarvesterVerb()
	{
		doc = null;
		requestURL = null;
		schemaLocation = null;
	}

	/**
	 * Performs the OAI request
	 *
	 * @param requestURL
	 * @throws IOException
	 * @throws SAXException
	 * @throws TransformerException
	 */
	public HarvesterVerb( String requestURL ) throws IOException, SAXException, TransformerException
	{
		this( requestURL, 2 );
	}

	/**
	 * Performs the OAI request
	 *
	 * @param requestURL
	 * @throws IOException
	 * @throws SAXException
	 * @throws TransformerException
	 */
	public HarvesterVerb( String requestURL, Integer timeout ) throws IOException, SAXException, TransformerException
	{
		this.requestURL = requestURL;

		try ( InputStream in = getHttpResponse( new URL( requestURL ), timeout ) )
		{
			doc = factory.newDocumentBuilder().parse( in );
		}
		catch (ParserConfigurationException e)
		{
			throw new IllegalStateException( e );
		}

		this.schemaLocation = getSingleString( "/*/@xsi:schemaLocation" );
	}

	private static InputStream getHttpResponse( URL requestURL, Integer timeout ) throws IOException
	{
		HttpURLConnection con;
		int responseCode;
		int retries = 0;
		do
		{
			con = (HttpURLConnection) requestURL.openConnection();
			con.setRequestProperty( "User-Agent", "OAIHarvester/2.0" );
			con.setRequestProperty( "Accept-Encoding", "compress, gzip, identify" );

			// TK added default timeout for dataverses taking too long to respond / stall
			log.trace( "Timeout : {} seconds", timeout );
			con.setConnectTimeout( timeout * 1000 );
			con.setReadTimeout( timeout * 1000 );

			responseCode = con.getResponseCode();
			log.trace( "responseCode={}", responseCode );

			if ( responseCode == 302 )
			{
				if ( log.isInfoEnabled() )
					log.info( con.getHeaderFields().toString() );
				con.getHeaderFields().get( LOCATION ).forEach( log::info );
				return getHttpResponse( new URL( con.getHeaderFields().get( LOCATION ).get( 0 ) ), timeout );
			}

			if ( responseCode == HttpURLConnection.HTTP_UNAVAILABLE )
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
					log.debug( "Server response: Retry-After={}", retrySeconds );
					try
					{
						Thread.sleep( retrySeconds * 1000 );
					}
					catch (InterruptedException ex)
					{
						Thread.currentThread().interrupt();
						return new ByteArrayInputStream( new byte[0] );
					}
				}
			}
		} while (responseCode == HttpURLConnection.HTTP_UNAVAILABLE && retries++ < 3);
		return decodeHttpInputStream( con );
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
	 * @throws TransformerException
	 */
	public NodeList getErrors() throws TransformerException
	{
		if ( SCHEMA_LOCATION_V2_0.equals( getSchemaLocation() ) )
		{
			return getNodeList( "/oai20:OAI-PMH/oai20:error" );
		}
		else
		{
			return null;
		}
	}

	/**
	 * Get the OAI request URL for this response
	 *
	 * @return the OAI request URL as a String
	 */
	public String getRequestURL()
	{
		return requestURL;
	}

	private static InputStream decodeHttpInputStream( HttpURLConnection con ) throws IOException
	{
		String contentEncoding = con.getHeaderField( "Content-Encoding" );
		log.trace( "contentEncoding={}", contentEncoding );

		if ( contentEncoding == null )
		{
			contentEncoding = "";
		}

		switch (contentEncoding)
		{
		case "compress":
			ZipInputStream zis = new ZipInputStream( con.getInputStream() );
			zis.getNextEntry();
			return zis;
		case "gzip":
			return new GZIPInputStream( con.getInputStream() );
		case "deflate":
			return new InflaterInputStream( con.getInputStream() );
		default:
			return con.getInputStream();
		}
	}

	/**
	 * Get the String value for the given XPath location in the response DOM
	 *
	 * @param xpath
	 * @return a String containing the value of the XPath location.
	 * @throws TransformerException
	 */
	public String getSingleString( String xpath ) throws TransformerException
	{
		return getSingleString( getDocument(), xpath );
	}

	public String getSingleString( Node node, String xpath ) throws TransformerException
	{
		return XPathAPI.eval( node, xpath, namespaceElement ).str();
	}

	/**
	 * Get a NodeList containing the nodes in the response DOM for the specified xpath
	 *
	 * @param xpath
	 * @return the NodeList for the xpath into the response DOM
	 * @throws TransformerException
	 */
	public NodeList getNodeList( String xpath ) throws TransformerException
	{
		return XPathAPI.selectNodeList( getDocument(), xpath, namespaceElement );
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
