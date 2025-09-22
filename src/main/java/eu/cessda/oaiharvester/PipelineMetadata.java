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


import java.net.URI;
import java.util.Set;

/**
 * Repository model to be serialised to JSON. This is used by downstream components.
 * @param code the identifier of the repository.
 * @param name the friendly name of the remote repository.
 * @param url the base URL of the repository.
 * @param setSpec the set harvested from the repository.
 * @param metadataPrefix the metadata prefix harvested from the repository.
 * @param ddiVersion the DDI version harvested from the remote repository.
 * @param profile the URL of the CMV profile to validate against.
 * @param validationGate the CMV validation gate to use.
 * @param defaultLanguage the default language to use when a metadata record doesn't specify a language.
 */
public record PipelineMetadata(
    String code,
    String name,
    URI url,
    String setSpec,
    String metadataPrefix,
    URI profile,
    String validationGate,
    String defaultLanguage,
    Set<String> role
)
{
}
