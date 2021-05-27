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
import org.apache.xpath.XPathAPI;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.time.Duration;

/**
 * HarvesterVerb is the parent class for each of the OAI verbs.
 *
 * @author Jefffrey A. Young, OCLC Online Computer Library Center
 */
public abstract class HarvesterVerb
{
	/* Primary OAI namespaces */
	protected static final String OAI_2_0_NAMESPACE = "http://www.openarchives.org/OAI/2.0/";
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
			namespaceElement.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:oai20", OAI_2_0_NAMESPACE );
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
	protected HarvesterVerb( HttpClient httpClient, String requestURL ) throws IOException, SAXException
	{
		this(httpClient, requestURL, Duration.ofSeconds( 10 ) );
	}

	/**
	 * Performs the OAI request
	 *
	 * @param requestURL
	 * @throws IOException
	 * @throws SAXException
	 */
	protected HarvesterVerb( HttpClient httpClient, String requestURL, Duration timeout ) throws IOException, SAXException
	{
		this.requestURL = URI.create( requestURL );

		try ( var in = httpClient.getHttpResponse( this.requestURL.toURL(), timeout ) )
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
		return doc.getElementsByTagNameNS( OAI_2_0_NAMESPACE, "error" );
	}

	/**
	 * Get the OAI request URL for this response.
	 */
	public URI getRequestURL()
	{
		return requestURL;
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
		return XPathAPI.eval( doc, xpath, namespaceElement ).str();
	}

	public String toString()
	{
		try ( var sw = new StringWriter() )
		{
			var idTransformer = xformFactory.newTransformer();
			idTransformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
			idTransformer.transform( new DOMSource( doc ), new StreamResult( sw ) );
			return sw.toString();
		}
		catch (TransformerException | IOException e)
		{
			return e.toString();
		}
	}
}
