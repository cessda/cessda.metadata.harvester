package eu.cessda.eqb.harvester;

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
