package org.oclc.oai.harvester2.verb;

import java.util.Objects;

/**
 * An OAI-PMH set description. It contains the set's spec, name
 * and an optional description.
 *
 * @param spec        the unique identifier of the set.
 * @param name        the friendly name of the set.
 * @param description optional community-specific information which can be XML encoded, or {@code null} if there is no description.
 * @see <a href="https://www.openarchives.org/OAI/openarchivesprotocol.html#Set">https://www.openarchives.org/OAI/openarchivesprotocol.html#Set</a>
 */
public record Set(String spec, String name, String description) {
    /**
     * Constructs a new instance of a {@link Set}
     *
     * @param spec        the unique identifier of the set.
     * @param name        the friendly name of the set.
     * @param description optional community-specific information which can be XML encoded, or {@code null} if there is no description.
     */
    public Set(String spec, String name, String description) {
        this.spec = Objects.requireNonNull(spec);
        this.name = Objects.requireNonNull(name);
        this.description = description;
    }
}
