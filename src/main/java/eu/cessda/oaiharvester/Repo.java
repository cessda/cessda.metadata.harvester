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
 * @param metadataPrefixes The metadata prefixes to harvest from the repository.
 * @param code The identifier of the repository.
 * @param name The friendly name of the remote repository.
 * @param url The base URL of the repository.
 * @param discoverSets If true, the repository should be queried for the sets contained.
 * @param defaultLanguage The default language to be used in the UI.
 * @param validationGate The validation gate to use.
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
    record MetadataFormat(String setSpec, String metadataPrefix, String ddiVersion, URI validationProfile) implements Serializable {}
}
