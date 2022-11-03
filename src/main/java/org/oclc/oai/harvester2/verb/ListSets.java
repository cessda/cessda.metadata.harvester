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
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents an ListSets response on either the server or on the
 * client
 *
 * @author Jeffrey A. Young, OCLC Online Computer Library Center
 */
public final class ListSets extends HarvesterVerb implements Resumable {
    /**
     * Client-side ListSets verb constructor.
     *
     * @param is the input stream to construct from.
     * @throws SAXException if an error occurs parsing the XML.
     * @throws IOException  if an I/O error occurred.
     */
    ListSets(InputStream is) throws IOException, SAXException {
        super(is);
    }

    public static ListSets instance(HttpClient httpClient, URI baseURL) throws IOException, SAXException {
        var requestURL = getRequestURL(baseURL);
        try (var is = httpClient.getHttpResponse(requestURL)) {
            return new ListSets(is);
        }
    }

    public static ListSets instance(HttpClient httpClient, URI baseURL, String resumptionToken)
            throws IOException, SAXException {
        var requestURL = getRequestURL(baseURL, resumptionToken);
        try (var is = httpClient.getHttpResponse(requestURL)) {
            return new ListSets(is);
        }
    }

    /**
     * Returns a list of sets found in the response. The returned list is
     * unmodifiable.
     */
    public List<String> getSets() {
        var nl = getDocument().getElementsByTagNameNS(OAI_2_0_NAMESPACE, "setSpec");
        var sets = new ArrayList<String>(nl.getLength());
        for (int i = 0; i < nl.getLength(); i++) {
            sets.add(nl.item(i).getTextContent());
        }
        return Collections.unmodifiableList(sets);
    }

    /**
     * Generate a ListSets URI for the given baseURL
     *
     * @param baseURL the base URL of the OAI-PMH repository.
     */
    private static URI getRequestURL(URI baseURL) {
        return URI.create(baseURL + "?verb=ListSets");
    }

    /**
     * Construct the query portion of the http request (resumptionToken version)
     *
     * @param baseURL         the base URL of the OAI-PMH repository.
     * @param resumptionToken the resumption token.
     */
    private static URI getRequestURL(URI baseURL, String resumptionToken) {
        return URI.create(baseURL + "?verb=ListSets"
                + "&resumptionToken=" + URLEncoder.encode(resumptionToken, StandardCharsets.UTF_8));
    }
}
