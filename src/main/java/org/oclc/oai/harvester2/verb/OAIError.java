package org.oclc.oai.harvester2.verb;

import java.util.Objects;
import java.util.Optional;

public class OAIError
{
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

    public Code getCode()
    {
        return code;
    }

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
     */
    @SuppressWarnings( {"unused", "java:S115"} )
    public enum Code
    {
        badArgument,
        badResumptionToken,
        badVerb,
        cannotDisseminateFormat,
        idDoesNotExist,
        noRecordsMatch,
        noMetadataFormats,
        noSetHierarchy
    }
}
