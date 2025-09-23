package eu.cessda.oaiharvester;

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


import org.oclc.oai.harvester2.verb.OAIError;
import org.oclc.oai.harvester2.verb.RecordHeader;

import java.io.Serial;
import java.util.List;
import java.util.Optional;

/**
 * Represents when an error has occurred retrieving record headers from a repository.
 */
public class RecordHeaderException extends Exception
{
    @Serial
    private static final long serialVersionUID = 5629333665262898326L;

    private final Repo repo;
    private final String set;
    private final List<RecordHeader> headers;

    /**
     * Construct a new instance of a {@link RecordHeaderException}.
     * @param repo the repository that failed.
     * @param set the set that was being harvested, set to {@code null} if no sets were being harvested.
     * @param headers the list of headers discovered so far.
     * @param cause the cause of this exception.
     */
    RecordHeaderException( Repo repo, String set, List<RecordHeader> headers, Throwable cause )
    {
        super( generateMessage( repo, set, cause ), cause );
        this.repo = repo;
        this.set = set;
        this.headers = headers;
    }

    /**
     * Construct a new instance of a {@link RecordHeaderException}.
     * @param repo the repository that failed.
     * @param set the set that was being harvested, set to {@code null} if no sets were being harvested.
     * @param headers the list of headers discovered so far.
     * @param cause the cause of this exception.
     */
    RecordHeaderException( Repo repo, String set, List<RecordHeader> headers, List<OAIError> errors )
    {
        super( generateMessage( repo, set, errors ) );
        this.repo = repo;
        this.set = set;
        this.headers = headers;
    }

    /**
     * Generate the message for this exception.
     */
    private static String generateMessage( Repo repo, String set, Throwable cause )
    {
        if (set != null)
        {
            return repo.code() + ": " + set + ": Fetching identifiers failed: " + cause;
        }
        else
        {
            return repo.code() + ": Fetching identifiers failed: " + cause;
        }
    }

    /**
     * Generate the message for this exception.
     */
    private static String generateMessage( Repo repo, String set, List<OAIError> errors )
    {
        if (set != null)
        {
            return repo.code() + ": " + set + ": OAI-PMH errors: " + errors;
        }
        else
        {
            return repo.code() + ": OAI-PMH errors: " + errors;
        }
    }

    /**
     * Get the repository that failed.
     */
    public Repo getRepo()
    {
        return repo;
    }

    /**
     * Get the set that was being harvested.
     */
    public Optional<String> getSet()
    {
        return Optional.ofNullable(set);
    }

    /**
     * Get the list of record headers.
     */
    public List<RecordHeader> getHeaders()
    {
        return headers;
    }
}
