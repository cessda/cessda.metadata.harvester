package eu.cessda.eqb.harvester;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class IOUtilities
{
    private static final Logger log = LoggerFactory.getLogger( IOUtilities.class );

    private final TransformerFactory factory;

    public IOUtilities() throws TransformerConfigurationException
    {
        factory = TransformerFactory.newInstance();
        factory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
    }

    /**
     * Creates the destination directory for this repository.
     * @param destinationDirectory the base directory.
     * @param repositoryDirectory the name of the repository.
     * @throws DirectoryCreationFailedException if the directory cannot be created.
     */
    void createDestinationDirectory( Path destinationDirectory, Path repositoryDirectory ) throws DirectoryCreationFailedException
    {
        var outputDirectory = destinationDirectory.resolve( repositoryDirectory );
        try
        {
            log.debug( "Creating destination directory: {}", outputDirectory );
            Files.createDirectories( outputDirectory );
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
     * @param fdest the {@link Path} to write to.
     */
    void writeDomSource( Source source, Path fdest ) throws IOException, TransformerException
    {
        try ( var fOutputStream = Files.newOutputStream( fdest ) )
        {
            log.trace( "Writing to {}", fdest );
            factory.newTransformer().transform( source, new StreamResult( fOutputStream ) );
        }
    }
}
