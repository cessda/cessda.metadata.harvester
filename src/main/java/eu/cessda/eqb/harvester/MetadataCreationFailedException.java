package eu.cessda.eqb.harvester;

import java.io.Serial;

class MetadataCreationFailedException extends Exception {
    @Serial
    private static final long serialVersionUID = -2002365319483072484L;

    MetadataCreationFailedException(Throwable e) {
        super("Failed to create pipeline.json: " + e.toString(), e);
    }
}
