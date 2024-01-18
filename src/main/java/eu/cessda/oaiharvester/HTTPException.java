package eu.cessda.oaiharvester;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2024 CESSDA ERIC
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
