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


import org.w3c.dom.Document;

import java.util.Optional;

import static org.oclc.oai.harvester2.verb.HarvesterVerb.OAI_2_0_NAMESPACE;

/**
 * An OAI-PMH response that supports holding a resumption token.
 */
public interface Resumable
{
    /**
     * Get the OAI response as a DOM object
     *
     * @return the DOM for the OAI response
     */
    Document getDocument();

    /**
     * Get the oai:resumptionToken from the response
     *
     * @return the oai:resumptionToken value, or an empty optional if a resumption token is not present
     */
    default Optional<String> getResumptionToken()
    {
        var resumptionToken = getDocument().getElementsByTagNameNS( OAI_2_0_NAMESPACE, "resumptionToken" );

        if (resumptionToken.getLength() > 0)
        {
            var token = resumptionToken.item( 0 ).getTextContent().trim();
            if (!token.isEmpty())
            {
                return Optional.of( token );
            }
        }
        return Optional.empty();
    }
}
