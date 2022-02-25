/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator.error;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code DiagnosticMessages} enum class for containing the error message related to openapi validator plugin.
 *
 * @since 2.0.0
 */
public enum DiagnosticMessages {
    OAS_VALIDATOR_001("OAS_VALIDATOR_001", "OpenAPI contract doesn't exist in the given location: %s",
            DiagnosticSeverity.ERROR),
    OAS_VALIDATOR_002("OAS_VALIDATOR_002", "Invalid file type. Provide either a .yaml or .json file.",
            DiagnosticSeverity.ERROR),
    OAS_VALIDATOR_003("OAS_VALIDATOR_003", "Contract file is not existed in the given path should be applied.",
            DiagnosticSeverity.ERROR),
    OAS_VALIDATOR_004("OAS_VALIDATOR_004", "Given OpenAPI contract file path is an empty string.",
            DiagnosticSeverity.ERROR),
    OAS_VALIDATOR_005("OAS_VALIDATOR_005", "Contract file is not existed in the given path should be applied.",
            DiagnosticSeverity.ERROR);


    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    DiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
        this.code = code;
        this.description = description;
        this.severity = severity;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiagnosticSeverity getSeverity() {
        return severity;
    }
}
