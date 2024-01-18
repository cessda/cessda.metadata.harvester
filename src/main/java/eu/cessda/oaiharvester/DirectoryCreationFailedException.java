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


import java.io.Serial;
import java.nio.file.Path;

/**
 * Thrown when creating the output directory fails.
 */
class DirectoryCreationFailedException extends Exception
{
    @Serial
    private static final long serialVersionUID = 1L;

    private final String directory;

    /**
     * Constructs a {@link DirectoryCreationFailedException} with the specified directory and cause.
     *
     * @param directory the directory that could not be created.
     * @param cause     the exception that caused this exception.
     */
    DirectoryCreationFailedException( Path directory, Throwable cause )
    {
        super( "Creating " + directory + " failed", cause );
        this.directory = directory.toString();
    }

    Path getDirectory()
    {
        return Path.of(directory);
    }
}
