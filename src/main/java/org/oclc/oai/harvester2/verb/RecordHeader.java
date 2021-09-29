package org.oclc.oai.harvester2.verb;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;
import java.util.Set;

/**
 * An OAI-PMH record header. It contains the unique identifier,
 * as well as properties needed for selective harvesting.
 *
 * @see <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#header">
 * http://www.openarchives.org/OAI/openarchivesprotocol.html#header</a>
 *
 * @param identifier the OAI-PMH identifier.
 * @param datestamp the datestamp associated with this record. This is typically an instance of a {@link LocalDate} or a {@link OffsetDateTime}.
 * @param sets the list of sets that this record can be found in. The returned set is unmodifiable.
 * @param status the status, or {@code null} if there is no status.
 */
public record RecordHeader(String identifier, TemporalAccessor datestamp, Set<String> sets, Status status)
{
    /**
     * Construct a new instance of a {@link RecordHeader}.
     *
     * @param identifier the OAI-PMH identifier.
     * @param datestamp  the datestamp.
     * @param sets       the sets.
     * @param status     the status, or {@code null} if there is no status.
     */
    public RecordHeader( String identifier, TemporalAccessor datestamp, Set<String> sets, Status status )
    {
        this.identifier = Objects.requireNonNull( identifier );
        this.datestamp = Objects.requireNonNull( datestamp );
        this.sets = Objects.requireNonNull( sets );
        this.status = status;
    }

    public enum Status
    {
        deleted
    }
}
