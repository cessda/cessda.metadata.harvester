package org.oclc.oai.harvester2.verb;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2026 CESSDA ERIC
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
