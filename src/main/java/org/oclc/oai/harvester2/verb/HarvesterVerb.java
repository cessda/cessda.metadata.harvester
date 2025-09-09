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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

/**
 * HarvesterVerb is the parent class for each of the OAI verbs.
 *
 * @author Jefffrey A. Young, OCLC Online Computer Library Center
 */
public abstract sealed class HarvesterVerb permits GetRecord, Identify, ListIdentifiers, ListMetadataFormats, ListRecords, ListSets
{
    private static final Logger log = LoggerFactory.getLogger( HarvesterVerb.class );

    /* Primary OAI namespaces */
    public static final String OAI_2_0_NAMESPACE = "http://www.openarchives.org/OAI/2.0/";

    /**
     * A formatter that supports all the date formats returned by OAI-PMH repositories.
     */
    protected static final DateTimeFormatter OAI_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .append( ISO_LOCAL_DATE )
        .optionalStart()
        .appendLiteral( 'T' )
        .append( ISO_LOCAL_TIME )
        .optionalStart()
        .appendOffsetId()
        .toFormatter();

    // Error handler to suppress DocumentBuilder outputting directly to stderr
    private static final ErrorHandler HANDLER = new ErrorHandler()
    {
        @Override
        public void warning( SAXParseException exception )
        {
            // Log SAX warnings as debug messages
            if (log.isDebugEnabled())
            {
                log.debug( exception.toString() );
            }
        }

        @Override
        public void error( SAXParseException exception ) throws SAXParseException
        {
            throw exception;
        }

        @Override
        public void fatalError( SAXParseException exception ) throws SAXParseException
        {
            throw exception;
        }
    };

    private static final ThreadLocal<DocumentBuilder> documentBuilder = ThreadLocal.withInitial( () ->
    {
        /* Configure Document Builder Factory */
        var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );

        try
        {
            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler( HANDLER );
            return builder;
        }
        catch ( ParserConfigurationException e )
        {
            throw new IllegalStateException( e );
        }
    } );
    private static final ThreadLocal<Transformer> identityTransformer = ThreadLocal.withInitial( () ->
    {
        try
        {
            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
            return transformer;
        }
        catch ( TransformerConfigurationException e )
        {
            throw new IllegalStateException( e );
        }
    } );
    protected static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

    // Instance variables
    private final Document doc;

    /**
     * Instance a {@link HarvesterVerb} from an {@link InputSource}
     *
     * @param in the input stream representing the source document.
     * @throws IOException  if an IO error occurs.
     * @throws SAXException if an error occurs when parsing the stream.
     */
    protected HarvesterVerb( InputSource in ) throws IOException, SAXException
    {
        doc = documentBuilder.get().parse( in );
    }

    /**
     * Gets the text content of the OAI-PMH {@code <datestamp>} element. This method will read the document
     * until either the {@code <datestamp>} is found or the end of the {@code <header>} element is reached.
     *
     * @param reader the source document reader.
     * @return a {@link TemporalAccessor} representing the {@code <datestamp>} element,
     *         or {@code null} if the datestamp element is not present
     * @throws DateTimeParseException if the datestamp element is not valid.
     * @throws XMLStreamException if the OAI-PMH document is malformed.
     */
    protected static TemporalAccessor getDateStamp( XMLStreamReader reader ) throws XMLStreamException
    {
        while ( reader.hasNext() )
        {
            var event = reader.next();

            if ( event == XMLStreamConstants.START_ELEMENT &&
                reader.getName().equals( new QName( OAI_2_0_NAMESPACE, "datestamp" ) ) )
            {
                return OAI_DATE_TIME_FORMATTER.parseBest( reader.getElementText(), OffsetDateTime::from, LocalDateTime::from, LocalDate::from);
            }

            // Stop reading at the end of the <header> element
            if (event == XMLStreamConstants.END_ELEMENT &&
                reader.getName().equals( new QName( OAI_2_0_NAMESPACE, "header" ) ) )
            {
                break;
            }
        }

        return null;
    }

    /**
     * Construct a {@link RecordHeader} from a header node
     *
     * @param headerNode the node to convert.
     * @throws DateTimeParseException if the datestamp element is not valid.
     */
    protected RecordHeader getRecordHeader( Node headerNode )
    {
        String identifier = null;
        TemporalAccessor datestamp = null;
        var sets = new HashSet<String>();

        // Get the status attribute on the header node
        RecordHeader.Status status = null;
        var namedItem = (Attr) headerNode.getAttributes().getNamedItem( "status" );
        if ( namedItem != null )
        {
            status = RecordHeader.Status.valueOf( namedItem.getValue() );
        }

        var childNodes = headerNode.getChildNodes();

        for ( int i = 0; i < childNodes.getLength(); i++ )
        {
            var node = childNodes.item( i );
            var localName = node.getLocalName();

            if ( "identifier".equals( localName ) )
            {
                identifier = node.getTextContent().trim();
            }
            else if ( "datestamp".equals( localName ) )
            {
                var datestampString = node.getTextContent().trim();
                datestamp = OAI_DATE_TIME_FORMATTER.parseBest( datestampString, OffsetDateTime::from, LocalDateTime::from, LocalDate::from );
            }
            else if ( "setSpec".equals( localName ) )
            {
                sets.add( node.getTextContent().trim() );
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
     * Get the OAI errors
     *
     * @return a NodeList of /oai:OAI-PMH/oai:error elements
     */
    public List<OAIError> getErrors()
    {
        var elements = doc.getElementsByTagNameNS( OAI_2_0_NAMESPACE, "error" );

        var errorList = new ArrayList<OAIError>( elements.getLength() );

        for ( int i = 0; i < elements.getLength(); i++ )
        {
            var errorElement = elements.item( i );

            var codeString = ( (Attr) errorElement.getAttributes().getNamedItem( "code" ) ).getValue();
            var errorCode = OAIError.Code.valueOf( codeString );

            // Check if the error has free text
            var errorText = errorElement.getTextContent().trim();
            if ( !errorText.isEmpty() )
            {
                errorList.add( new OAIError( errorCode, errorText ) );
            }
            else
            {
                errorList.add( new OAIError( errorCode ) );
            }
        }

        return errorList;
    }

    public String toString()
    {
        try
        {
            var stringWriter = new StringWriter();
            identityTransformer.get().transform( new DOMSource( doc ), new StreamResult( stringWriter ) );
            return stringWriter.toString();
        }
        catch ( TransformerException e )
        {
            return e.toString();
        }
    }
}
