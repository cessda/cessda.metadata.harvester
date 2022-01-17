package eu.cessda.eqb.harvester;

import java.io.IOException;
import java.io.Serial;
import java.net.URI;

/**
 * Exception for HTTP responses that return a failure status code (i.e. 400+). The status code and
 * the request URI are stored for logging purposes.
 */
class HTTPException extends IOException
{
    @Serial
    private static final long serialVersionUID = 7340278405480735554L;

    private final URI requestURI;
    private final int statusCode;

    /**
     * Constructs a {@link HTTPException} with the specified status code and request URI.
     *
     * @param requestURI the URI requested from the server.
     * @param statusCode           the status code of the external response that caused this exception.
     */
    public HTTPException( URI requestURI, int statusCode ) {
        super(requestURI + ": Server returned " + statusCode);
        this.requestURI = requestURI;
        this.statusCode = statusCode;
    }

    public URI getRequestURI()
    {
        return requestURI;
    }

    public int getStatusCode()
    {
        return statusCode;
    }
}
