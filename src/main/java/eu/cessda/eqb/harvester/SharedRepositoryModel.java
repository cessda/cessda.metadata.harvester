package eu.cessda.eqb.harvester;

import java.net.URI;

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
