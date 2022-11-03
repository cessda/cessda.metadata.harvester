package eu.cessda.eqb.harvester;

import java.io.Serial;
import java.nio.file.Path;

/**
 * Thrown when creating the output directory fails.
 */
class DirectoryCreationFailedException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Path directory;

    /**
     * Constructs a {@link DirectoryCreationFailedException} with the specified
     * directory and cause.
     *
     * @param directory the directory that could not be created.
     * @param cause     the exception that caused this exception.
     */
    DirectoryCreationFailedException(Path directory, Throwable cause) {
        super("Creating " + directory + " failed", cause);
        this.directory = directory;
    }

    Path getDirectory() {
        return directory;
    }
}
