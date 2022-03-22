package eu.cessda.eqb.harvester;

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
)
{
    record MetadataFormat(String setSpec, String metadataPrefix, String ddiVersion, URI validationProfile) {}
}
