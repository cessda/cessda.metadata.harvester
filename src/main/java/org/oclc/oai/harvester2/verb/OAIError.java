package org.oclc.oai.harvester2.verb;

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


import java.util.Objects;
import java.util.Optional;

public class OAIError {
    /**
     * The OAI-PMH error code.
     */
    private final Code code;
    /**
     * The OAI-PMH error message.
     */
    private final String message;

    OAIError(Code code) {
        this.code = code;
        this.message = null;
    }

    OAIError(Code code, String message) {
        this.code = code;
        this.message = message;
    }

    public Code getCode() {
        return code;
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OAIError oaiError = (OAIError) o;
        return code == oaiError.code
            && Objects.equals(message, oaiError.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message);
    }

    @Override
    public String toString() {
        if (message != null) {
            return code + ": " + message;
        } else {
            return code.toString();
        }
    }

    /**
     * OAI-PMH error codes.
     */
    @SuppressWarnings({"unused", "java:S115"})
    public enum Code {
        badArgument,
        badResumptionToken,
        badVerb,
        cannotDisseminateFormat,
        idDoesNotExist,
        noRecordsMatch,
        noMetadataFormats,
        noSetHierarchy
    }
}
