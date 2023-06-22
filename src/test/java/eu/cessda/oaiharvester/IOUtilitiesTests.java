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


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.oclc.oai.harvester2.verb.RecordHeader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class IOUtilitiesTests
{
    @Test
    void shouldCreateOutputDirectory( @TempDir Path tempDir ) throws DirectoryCreationFailedException
    {
        var dirToCreate = Path.of( String.valueOf( new Random().nextInt() ) );

        // Assert that the directory initially does not exist
        assertThat( tempDir.resolve( dirToCreate ) ).doesNotExist();

        // Create the directory
        var createdDirectory = IOUtilities.createDestinationDirectory( tempDir, dirToCreate );

        // Assert that the directory has been created
        assertThat( tempDir.resolve( dirToCreate ) ).exists();

        // Assert that the returned path is equal to the expected path
        assertThat( createdDirectory ).isEqualTo( tempDir.resolve( dirToCreate ) );
    }

    @Test
    void shouldDeleteOrphanedRecords(@TempDir Path tempDir) throws IOException
    {
        // Generate random file names
        var fileNameIntArray = new Random().ints(5).toArray();

        // Select files to be kept
        var filesToKeep = Arrays.stream(fileNameIntArray).limit( 3 ).toArray();

        // Create all the files
        for (var fileName : fileNameIntArray)
        {
            Files.createFile( tempDir.resolve( fileName + ".xml" ) );
        }

        // Delete orphaned records
        var mockRecordHeaders = Arrays.stream( filesToKeep )
            .mapToObj( i -> new RecordHeader( String.valueOf( i ), LocalDate.now(), Collections.emptySet(), null ) )
            .toList();
        IOUtilities.deleteOrphanedRecords( new Repo( null, "TEST", null, null, false, null, null), mockRecordHeaders, tempDir );

        // Check that the directory is in the expected state
        for ( var file : fileNameIntArray )
        {
            if ( Arrays.stream( filesToKeep ).anyMatch( fileToKeep -> fileToKeep == file ))
            {
                // Verify kept files are still present
                assertThat( tempDir ).isDirectoryContaining( path -> path.getFileName().toString().equals( file + ".xml" ) );
            }
            else
            {
                // Verify orphaned records are deleted
                assertThat( tempDir ).isDirectoryNotContaining( path -> path.getFileName().toString().equals( file + ".xml" ));
            }
        }
    }
}
