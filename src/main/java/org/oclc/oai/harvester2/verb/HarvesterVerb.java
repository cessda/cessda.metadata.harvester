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
 * Copyright (C) 2019 - 2023 CESSDA ERIC
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
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
public abstract sealed class HarvesterVerb permits GetRecord,
    Identify, ListIdentifiers, ListMetadataFormats, ListRecords, ListSets {
    /* Primary OAI namespaces */
    protected static final String OAI_2_0_NAMESPACE
        = "http://www.openarchives.org/OAI/2.0/";

    /**
     * A formatter that supports all the date formats returned by
     * OAI-PMH repositories.
     */
    protected static final DateTimeFormatter OAI_DATE_TIME_FORMATTER
        = new DateTimeFormatterBuilder().append(ISO_LOCAL_DATE)
                                        .optionalStart()
                                        .appendLiteral('T')
                                        .append(ISO_LOCAL_TIME)
                                        .optionalStart()
                                        .appendOffsetId()
                                        .toFormatter();

    // Error handler to suppress DocumentBuilder outputting directly to stderr
    private static final ErrorHandler HANDLER = new ErrorHandler() {
        @Override
        public void warning(SAXParseException exception) {
            // do nothing
        }

        @Override
        public void error(SAXParseException exception)
                throws SAXParseException {
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception)
                throws SAXParseException {
            throw exception;
        }
    };

    private static final DocumentBuilderFactory factory;
    private static final ThreadLocal<DocumentBuilder> documentBuilder;
    private static final ThreadLocal<Transformer> identityTransformer;

    static {
        /* Configure Document Builder Factory */
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        documentBuilder = ThreadLocal.withInitial(() -> {
            try {
                var documentBuilder = factory.newDocumentBuilder();
                documentBuilder.setErrorHandler(HANDLER);
                return documentBuilder;
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException(e);
            }
        });

        identityTransformer = ThreadLocal.withInitial(() -> {
            try {
                var transformer = TransformerFactory.newInstance()
                    .newTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                                              "yes");
                return transformer;
            } catch (TransformerConfigurationException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    // Instance variables
    private final Document doc;

    /**
     * Instance a {@link HarvesterVerb} from an {@link InputStream}.
     *
     * @param in the input stream representing the source document.
     * @throws IOException  if an IO error occurs.
     * @throws SAXException if an error occurs when parsing the stream.
     */
    protected HarvesterVerb(InputStream in) throws IOException, SAXException {
        doc = documentBuilder.get().parse(in);
    }

    /**
     * Construct a {@link RecordHeader} from a header node.
     *
     * @param headerNode the node to convert.
     * @throws DateTimeParseException if the datestamp element is not valid.
     */
    protected RecordHeader getRecordHeader(Node headerNode) {
        String identifier = null;
        TemporalAccessor datestamp = null;
        var sets = new HashSet<String>();

        // Get the status attribute on the header node
        RecordHeader.Status status = null;
        var namedItem = (Attr) headerNode.getAttributes().getNamedItem(
            "status");
        if (namedItem != null) {
            status = RecordHeader.Status.valueOf(namedItem.getValue());
        }

        var childNodes = headerNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            var node = childNodes.item(i);

            var localName = node.getLocalName();

            if ("identifier".equals(localName)) {
                identifier = node.getTextContent().trim();
            } else if ("datestamp".equals(localName)) {
                // NSD returns invalid ISO dates such as
                // 2020-09-02T15:12:07+0000. This corrects the dates
                // before parsing by replacing +0000 with Z.
                var datestampString = node.getTextContent().trim().replace(
                    "+0000", "Z");
                datestamp = OAI_DATE_TIME_FORMATTER.parseBest(
                    datestampString,
                    OffsetDateTime::from,
                    LocalDateTime::from, LocalDate::from);
            } else if ("setSpec".equals(localName)) {
                sets.add(node.getTextContent().trim());
            }
        }

        return new RecordHeader(identifier, datestamp, sets, status);
    }

    /**
     * Get the OAI response as a DOM object.
     *
     * @return the DOM for the OAI response
     */
    public Document getDocument() {
        return doc;
    }

    /**
     * Get the OAI errors.
     *
     * @return a NodeList of /oai:OAI-PMH/oai:error elements
     */
    public List<OAIError> getErrors() {
        var elements = doc.getElementsByTagNameNS(OAI_2_0_NAMESPACE, "error");

        var errorList = new ArrayList<OAIError>(elements.getLength());

        for (int i = 0; i < elements.getLength(); i++) {
            var errorElement = elements.item(i);

            var codeString = ((Attr) errorElement.getAttributes().getNamedItem(
                "code")).getValue();
            var errorCode = OAIError.Code.valueOf(codeString);

            // Check if the error has free text
            var errorText = errorElement.getTextContent().trim();
            if (!errorText.isEmpty()) {
                errorList.add(new OAIError(errorCode, errorText));
            } else {
                errorList.add(new OAIError(errorCode));
            }
        }

        return errorList;
    }

    public String toString() {
        try (var sw = new StringWriter()) {
            identityTransformer.get().transform(new DOMSource(doc),
                                                new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException | IOException e) {
            return e.toString();
        }
    }

}
