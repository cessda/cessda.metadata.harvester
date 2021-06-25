package org.oclc.oai.harvester2.verb;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * An OAI-PMH record header. It contains the unique identifier,
 * as well as properties needed for selective harvesting.
 *
 * @see <a href="http://www.openarchives.org/OAI/openarchivesprotocol.html#header">
 * http://www.openarchives.org/OAI/openarchivesprotocol.html#header</a>
 */
public class RecordHeader
{
    private final String identifier;
    private final TemporalAccessor datestamp;
    private final Set<String> sets;
    private final Status status;

    public RecordHeader( String identifier, TemporalAccessor datestamp, Set<String> sets, Status status )
    {
        this.identifier = Objects.requireNonNull( identifier );
        this.datestamp = Objects.requireNonNull( datestamp );
        this.sets = Objects.requireNonNull( sets );
        this.status = status;
    }

    /**
     * Gets the record identifier.
     */
    public String getIdentifier()
    {
        return identifier;
    }

    /**
     * Gets the datestamp associated with this record.
     * <p>
     * This is typically an instance of a {@link LocalDate} or a {@link OffsetDateTime}.
     */
    public TemporalAccessor getDatestamp()
    {
        return datestamp;
    }

    /**
     * Gets the list of sets that this record can be found in. The returned set is unmodifiable.
     */
    public Set<String> getSets()
    {
        return Collections.unmodifiableSet( sets );
    }

    public Optional<Status> getStatus()
    {
        return Optional.ofNullable( status );
    }


    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        RecordHeader header = (RecordHeader) o;
        return identifier.equals( header.identifier ) &&
            datestamp.equals( header.datestamp ) &&
            sets.equals( header.sets );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( identifier, datestamp, sets );
    }

    @Override
    public String toString()
    {
        return "Header{" +
            "identifier='" + identifier + '\'' +
            ", datestamp=" + datestamp +
            ", sets=" + sets +
            '}';
    }

    public enum Status
    {
        deleted
    }
}
