package cessda.eqb;

import java.nio.file.Path;

/**
 * Thrown when creating the output directory fails.
 */
class DirectoryCreationFailedException extends HarvesterFailedException
{
    private static final long serialVersionUID = 1L;
    private final Path directory;

    /**
     * Constructs a {@link DirectoryCreationFailedException} with the specified directory and cause.
     *
     * @param directory the directory that could not be created.
     * @param cause     the exception that caused this exception.
     */
    DirectoryCreationFailedException( Path directory, Throwable cause )
    {
        super( String.format( "Creating %s failed", directory.toAbsolutePath().toString() ), cause );
        this.directory = directory;
    }

    Path getDirectory()
    {
        return directory;
    }
}
