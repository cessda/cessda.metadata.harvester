package eu.cessda.oaiharvester;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2025 CESSDA ERIC
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


import java.io.Serializable;
import java.net.URI;
import java.util.Objects;
import java.util.Set;

/**
 * A configuration of a remote repository.
 *
 * @param oaiConfiguration   The metadata prefixes to harvest from the repository.
 * @param code             The identifier of the repository. Derived from the repository's domain name if {@code null}.
 * @param name             The friendly name of the remote repository.
 * @param defaultLanguage  The language to treat metadata if unspecified.
 * @param validationProfile the CMV profile to validate against.
 * @param validationGate   The validation gate to use. See <a href="https://cmv.cessda.eu/documentation/constraints.html">the CMV documentation</a> for the definition of valid constraints.
 * @param roles The tools that the harvesting metadata will be consumed by.
 */
public record Repo(
    OAIConfiguration oaiConfiguration,
    String code,
    String name,
    String defaultLanguage,
    URI validationProfile,
    String validationGate,
    Set<String> roles
) implements Serializable
{
    public Repo
    {
        if (code == null)
        {
            code = oaiConfiguration.url.getHost();
        }
        Objects.requireNonNull( oaiConfiguration );
    }

    /**
     * A specific harvesting configuration for a repository.
     *
     * @param url               The base URL of the repository.
     * @param metadataPrefix    the metadata prefix to harvest. This is a mandatory parameter.
     * @param setSpec           the set to harvest, or {@code null} if no specific set should be harvested.
     * @param discoverSets      If {@code true}, the harvester should discover sets in the repository and harvest them individually.<br>
     *                          If {@code false}, the harvester should harvest the set specified by {@link #setSpec},
     *                          or harvest without sets if {@link #setSpec} is {@code null}.
     */
    record OAIConfiguration(
        URI url,
        String metadataPrefix,
        String setSpec,
        boolean discoverSets
    ) implements Serializable {
        OAIConfiguration
        {
            Objects.requireNonNull( url );
            Objects.requireNonNull(metadataPrefix);
            if (setSpec != null && discoverSets)
            {
                throw new IllegalArgumentException("discoverSets cannot be true when a setSpec is specified");
            }
        }
    }
}
