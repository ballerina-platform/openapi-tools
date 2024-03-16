/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.core.generators.type;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code ServiceDiagnosticMessages} enum class for containing the error message related to openapi to ballerina
 * service generation command.
 *
 * @since 1.3.0
 */
public enum TypeGenerationDiagnosticMessages {
    OAS_SERVICE_102("OAS_SERVICE_102",
            "Type '%s' is not a valid query parameter type in Ballerina. " +
                    "The supported types are string, int, float, boolean, decimal, array types of the aforementioned " +
                    "types and map<json>.",
            DiagnosticSeverity.ERROR);

    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    TypeGenerationDiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
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
