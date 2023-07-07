package eu.cessda.oaiharvester;

/*-
 * #%L
 * CESSDA OAI-PMH Metadata Harvester
 * %%
 * Copyright (C) 2019 - 2023 CESSDA ERIC
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
import java.util.Set;

/**
 * A configuration of a remote repository.
 *
 * @param metadataPrefixes The metadata prefixes to harvest from the repository.
 * @param code             The identifier of the repository.
 * @param name             The friendly name of the remote repository.
 * @param url              The base URL of the repository.
 * @param discoverSets     If true, the repository should discover sets in the repository and harvest them individually.
 *                         If false, the repository should harvest without using sets.
 *                         If {@link MetadataFormat#setSpec} is not {@code null}, that set will override this setting.
 * @param defaultLanguage  The language to treat metadata if unspecified.
 * @param validationGate   The validation gate to use. See <a href="https://cmv.cessda.eu/documentation/constraints.html">the CMV documentation</a> for the definition of valid constraints.
 */
public record Repo(
    Set<MetadataFormat> metadataPrefixes,
    String code,
    String name,
    URI url,
    boolean discoverSets,
    String defaultLanguage,
    String validationGate
) implements Serializable
{
    /**
     * A specific harvesting configuration for a repository.
     *
     * @param metadataPrefix    the metadata prefix to harvest. This is a mandatory parameter.
     * @param setSpec           the set to harvest, or {@code null} if no specific set should be harvested.
     * @param ddiVersion        the DDI version harvested.
     * @param validationProfile the CMV profile to validate against.
     */
    record MetadataFormat(
        String metadataPrefix,
        String setSpec,
        String ddiVersion,
        URI validationProfile
    ) implements Serializable {
    }
}
