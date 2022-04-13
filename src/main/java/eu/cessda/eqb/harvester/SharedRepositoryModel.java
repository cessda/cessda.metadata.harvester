package eu.cessda.eqb.harvester;

import java.net.URI;

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
public record SharedRepositoryModel(
    String code,
    String name,
    URI url,
    String setSpec,
    String metadataPrefix,
    String ddiVersion,
    URI profile,
    String validationGate,
    String defaultLanguage
)
{
}
