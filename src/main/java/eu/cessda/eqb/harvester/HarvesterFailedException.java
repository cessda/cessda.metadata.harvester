package eu.cessda.eqb.harvester;

/**
 * Base exception thrown when the harvester fails to harvest a repository.
 */
class HarvesterFailedException extends Exception
{
    private static final long serialVersionUID = 1L;

    HarvesterFailedException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
