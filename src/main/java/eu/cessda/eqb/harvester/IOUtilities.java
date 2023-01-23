package eu.cessda.eqb.harvester;

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


import com.fasterxml.jackson.databind.ObjectMapper;
import org.oclc.oai.harvester2.verb.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.XMLConstants;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class IOUtilities
{
    private static final Logger log = LoggerFactory.getLogger( IOUtilities.class );

    @SuppressWarnings( "java:S5164" ) // This application is not a server application.
    private final ThreadLocal<Transformer> transformerThreadLocal = ThreadLocal.withInitial( () -> {
        try
        {
            var factory = TransformerFactory.newInstance();
            factory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
            return factory.newTransformer();
        }
        catch ( TransformerConfigurationException e )
        {
            throw new IllegalStateException(e);
        }
    } );

    /**
     * Creates the destination directory for this repository.
     * @param destinationDirectory the base directory.
     * @param repositoryDirectory the name of the repository.
     * @throws DirectoryCreationFailedException if the directory cannot be created.
     * @return the created directory.
     */
    static Path createDestinationDirectory( Path destinationDirectory, Path repositoryDirectory ) throws DirectoryCreationFailedException
    {
        var outputDirectory = destinationDirectory.resolve( repositoryDirectory );
        try
        {
            log.debug( "Creating destination directory: {}", outputDirectory );
            return Files.createDirectories( outputDirectory );
        }
        catch ( IOException e )
        {
            throw new DirectoryCreationFailedException( outputDirectory, e );
        }
    }

    /**
     * Create metadata describing the repository in the given directory. The filename created is metadata.json.
     * @param directory the directory to write to.
     * @param repository the repository to write metadata for.
     * @param metadataFormat the metadata format for this harvest.
     */
    static void createMetadata( Path directory, Repo repository, Repo.MetadataFormat metadataFormat ) throws MetadataCreationFailedException
    {
        var metadata = new PipelineMetadata(
            repository.code(),
            repository.name(),
            repository.url(),
            metadataFormat.setSpec(),
            metadataFormat.metadataPrefix(),
            metadataFormat.ddiVersion(),
            metadataFormat.validationProfile(),
            repository.validationGate(),
            repository.defaultLanguage()
        );

        try (var outputStream = Files.newOutputStream( directory.resolve( "pipeline.json" ) ) ) {
            var objectMapper = new ObjectMapper();
            objectMapper.writeValue( outputStream, metadata );
        } catch ( IOException e ) {
            throw new MetadataCreationFailedException(e);
        }
    }

    /**
     * Writes the given {@link Source} to the specified {@link Path}.
     * @throws IOException if an IO error occurs while writing the file.
     * @throws TransformerException if an unrecoverable error occurs whilst writing the source.
     * @param source the XML source.
     * @param destination the {@link Path} to write to.
     */
    void writeDomSource( Source source, Path destination ) throws IOException, TransformerException
    {
        var transformer = transformerThreadLocal.get();

        try ( var fOutputStream = Files.newOutputStream( destination ) )
        {
            log.trace( "Writing to {}", destination );
            transformer.transform( source, new StreamResult( fOutputStream ) );
        }
        finally
        {
            // Always reset the transformer before returning.
            transformer.reset();
        }
    }

    /**
     * Remove any records that are present in the destination directory, but are not declared in the repository.
     * @param repo the source repository.
     * @param recordHeaders the list of records harvested from the repository.
     * @param destinationPath the destination path.
     * @return the number of files that were deleted.
     */
    @SuppressWarnings( "java:S1141" )
    static int deleteOrphanedRecords( Repo repo, Collection<RecordHeader> recordHeaders, Path destinationPath )
    {
        // Track the amount of files that were deleted
        int filesDeleted = 0;

        try ( var stream = Files.newDirectoryStream( destinationPath, "*.xml" ) )
        {
            // Collect encountered identifiers to a HashSet, this will be used for comparisons
            var recordIdentifiers = new HashSet<String>( (int) (recordHeaders.size() / 0.75F), 0.75F );
            for ( var recordHeader : recordHeaders )
            {
                var identifier = recordHeader.identifier();
                recordIdentifiers.add( URLEncoder.encode( identifier, UTF_8 ) + ".xml" );
            }

            // Select records not discovered by the repository
            for ( var fileName : stream )
            {
                if ( !recordIdentifiers.contains( fileName.getFileName().toString() ) )
                {
                    try
                    {
                        // Delete the records.
                        Files.delete( fileName );
                        filesDeleted++;
                        log.debug( "{}: Deleted {}", repo.code(), fileName );
                    }
                    catch ( IOException e )
                    {
                        log.warn( "{}: Couldn't delete {}: {}", repo.code(), fileName, e.toString() );
                    }
                }
            }
        }
        catch ( DirectoryIteratorException | IOException e )
        {
            log.warn( "{}: Couldn't clean up: {}", repo.code(), e.toString() );
        }

        return filesDeleted;
    }
}
