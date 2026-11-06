package org.oclc.oai.harvester2.verb;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2026 CESSDA ERIC
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
