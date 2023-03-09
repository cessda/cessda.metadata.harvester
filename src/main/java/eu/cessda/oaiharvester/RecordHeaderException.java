package eu.cessda.oaiharvester;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2023 CESSDA ERIC
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

    /**
     * Construct a new instance of a {@link RecordHeaderException}.
     * @param repo the repository that failed.
     * @param set the set that was being harvested, set to {@code null} if no sets were being harvested.
     * @param cause the cause of this exception.
     */
    RecordHeaderException( Repo repo, String set, Throwable cause )
    {
        super( generateMessage( repo, set, cause ), cause );
        this.repo = repo;
        this.set = set;
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
}
