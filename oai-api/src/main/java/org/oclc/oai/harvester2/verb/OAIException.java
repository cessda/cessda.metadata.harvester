package org.oclc.oai.harvester2.verb;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exception raised when attempting to call a method on an OAI-PMH verb
 * when the OAI-PMH endpoint returned an error.
 */
public class OAIException extends Exception
{
    @Serial
    private static final long serialVersionUID = -8958642908046620477L;

    private final ArrayList<OAIError> oaiErrors;

    OAIException( List<OAIError> oaiErrors )
    {
        super(getMessage(oaiErrors));
        this.oaiErrors = new ArrayList<>( oaiErrors );
    }

    OAIException(Throwable cause)
    {
        super(cause);
        this.oaiErrors = new ArrayList<>(0);
    }

    private static String getMessage(List<OAIError> oaiErrors)
    {
        if (oaiErrors.size() == 1)
        {
            return oaiErrors.get( 0 ).toString();
        }
        else
        {
            return "Multiple OAI-PMH errors: " + oaiErrors;
        }
    }

    /**
     * Gets the list of OAIErrors that caused this exception.
     * @return the list of errors.
     */
    public List<OAIError> getErrors()
    {
        return Collections.unmodifiableList( oaiErrors );
    }
}
