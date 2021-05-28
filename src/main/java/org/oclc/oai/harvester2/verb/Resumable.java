package org.oclc.oai.harvester2.verb;

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
            var token = resumptionToken.item( 0 ).getTextContent();
            if (!token.isEmpty())
            {
                return Optional.of( token );
            }
        }
        return Optional.empty();
    }
}
