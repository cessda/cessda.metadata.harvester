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
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents errors returned by OAI-PMH endpoints.
 * <p>
 * Each error element must have a code attribute ({@link OAIError#getCode()}, defined in {@link Code}),
 * and may also have a free text string value ({@link OAIError#getMessage()}) to
 * provide information about the error that is useful to a human reader.
 */
public class OAIError implements Serializable
{
    @Serial
    private static final long serialVersionUID = -5854819258463248260L;

    /**
     * The OAI-PMH error code.
     */
    private final Code code;
    /**
     * The OAI-PMH error message.
     */
    private final String message;

    OAIError( Code code )
    {
        this.code = code;
        this.message = null;
    }

    OAIError( Code code, String message )
    {
        this.code = code;
        this.message = message;
    }

    /**
     * Gets the error code.
     * @return the code.
     */
    public Code getCode()
    {
        return code;
    }

    /**
     * Gets the error message, if present.
     * @return the message, or an empty {@link Optional}.
     */
    public Optional<String> getMessage()
    {
        return Optional.ofNullable( message );
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        OAIError oaiError = (OAIError) o;
        return code == oaiError.code && Objects.equals( message, oaiError.message );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( code, message );
    }

    @Override
    public String toString()
    {
        if ( message != null )
        {
            return code + ": " + message;
        }
        else
        {
            return code.toString();
        }
    }

    /**
     * OAI-PMH error codes
     *
     * @see <a href="https://www.openarchives.org/OAI/openarchivesprotocol.html#ErrorConditions">OAI-PMH error code specifications.</a>
     */
    @SuppressWarnings( {"unused", "java:S115"} )
    public enum Code
    {
        /** The request includes illegal arguments, is missing required arguments, includes a repeated argument, or values for arguments have an illegal syntax. */
        badArgument,
        /** The value of the {@code resumptionToken} argument is invalid or expired. */
        badResumptionToken,
        /** Value of the {@code verb} argument is not a legal OAI-PMH verb, the verb argument is missing, or the {@code verb} argument is repeated.  */
        badVerb,
        /** The metadata format identified by the value given for the {@code metadataPrefix} argument is not supported by the item or by the repository. */
        cannotDisseminateFormat,
        /** The value of the {@code identifier} argument is unknown or illegal in this repository. */
        idDoesNotExist,
        /** The combination of the values of the {@code from}, {@code until}, {@code set} and {@code metadataPrefix} arguments results in an empty list. */
        noRecordsMatch,
        /** There are no metadata formats available for the specified item. */
        noMetadataFormats,
        /** The repository does not support sets. */
        noSetHierarchy
    }
}
