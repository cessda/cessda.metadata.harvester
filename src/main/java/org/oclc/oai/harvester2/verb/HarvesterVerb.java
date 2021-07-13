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
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
import java.io.InputStream;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_TIME;

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

    /** A formatter that supports all the date formats returned by OAI-PMH repositories. */
    protected static final DateTimeFormatter OAI_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .append( ISO_LOCAL_DATE )
        .appendOptional( new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendLiteral( "T" )
            .append( ISO_OFFSET_TIME )
            .toFormatter()
        ).toFormatter();

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

	/**
	 * Instance a {@link HarvesterVerb} from an {@link InputStream}
	 * @param in the input stream representing the source document.
	 * @throws IOException if an IO error occurs.
	 * @throws SAXException if an error occurs when parsing the stream.
	 */
	protected HarvesterVerb( InputStream in ) throws IOException, SAXException
	{
		try
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
     * Construct a {@link RecordHeader} from a header node
     * @param headerNode the node to convert.
     * @throws DateTimeParseException if the datestamp element is not valid.
     */
    protected static RecordHeader getRecordHeader( Node headerNode )
    {
        String identifier = null;
        TemporalAccessor datestamp = null;
        var sets = new HashSet<String>();
        RecordHeader.Status status = null;

        var childNodes = headerNode.getChildNodes();

        for ( int i = 0; i < childNodes.getLength(); i++ )
        {
            var node = childNodes.item( i );
            switch ( node.getNodeName() ) {
                case "identifier":
                    identifier = node.getTextContent();
                    break;

                case "datestamp":
                    // NSD returns invalid ISO dates such as 2020-09-02T15:12:07+0000.
                    // This corrects the dates before parsing by replacing +0000 with Z.
                    var datestampString = node.getTextContent().replace( "+0000", "Z" );
                    datestamp = OAI_DATE_TIME_FORMATTER.parseBest( datestampString, OffsetDateTime::from, LocalDate::from );
                    break;

                case "setSpec":
                    sets.add( node.getTextContent() );
                    break;

                case "status":
                    status = RecordHeader.Status.valueOf( node.getTextContent() );
                    break;

                default:
                    break;
            }
        }

        return new RecordHeader( identifier, datestamp, sets, status );
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
	public List<OAIError> getErrors()
	{
		var elements = doc.getElementsByTagNameNS( OAI_2_0_NAMESPACE, "error" );

		var errorList = new ArrayList<OAIError>(elements.getLength());

		for ( int i = 0; i < elements.getLength(); i++ )
		{
			var errorElement = elements.item( i );

			var codeString = errorElement.getAttributes().getNamedItem( "code" ).getTextContent();
			var errorCode = OAIError.Code.valueOf( codeString );

			// Check if the error has free text
			if (!errorElement.getTextContent().isEmpty())
			{
				errorList.add( new OAIError( errorCode, errorElement.getTextContent() ) );
			}
			else
			{
				errorList.add( new OAIError( errorCode ) );
			}
		}

		return errorList;
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
