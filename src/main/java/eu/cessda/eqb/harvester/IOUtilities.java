package eu.cessda.eqb.harvester;

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
import java.util.stream.Collectors;

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
     * @param records the list of records harvested from the repository.
     * @param destinationPath the destination path.
     */
    @SuppressWarnings( "java:S1141" )
    static void deleteOrphanedRecords( Repo repo, Collection<RecordHeader> records, Path destinationPath )
    {
        try ( var stream = Files.newDirectoryStream( destinationPath ) )
        {
            // Collect encountered identifiers to a HashSet, this will be used for comparisons
            var recordIdentifiers = records.stream().map( RecordHeader::identifier )
                .map( identifier -> URLEncoder.encode( identifier, UTF_8 ) + ".xml" )
                .collect( Collectors.toCollection( HashSet::new ) );

            // Select records not discovered by the repository
            for ( var fileName : stream )
            {
                if ( !recordIdentifiers.contains( fileName.getFileName().toString() ) )
                {
                    try
                    {
                        // Delete the records.
                        Files.delete( fileName );
                        log.debug( "{}: Deleted {}", repo.getCode(), fileName );
                    }
                    catch ( IOException e )
                    {
                        log.warn( "{}: Couldn't delete {}: {}", repo.getCode(), fileName, e.toString() );
                    }
                }
            }
        }
        catch ( DirectoryIteratorException | IOException e )
        {
            log.warn( "{}: Couldn't clean up: {}", repo.getCode(), e.toString() );
        }
    }
}
