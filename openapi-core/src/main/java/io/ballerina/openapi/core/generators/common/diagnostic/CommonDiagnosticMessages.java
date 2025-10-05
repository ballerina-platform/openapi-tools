/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.openapi.core.generators.common.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code CommonDiagnosticMessages} enum class for containing the error message related to openapi to ballerina
 * service generation command.
 *
 * @since 1.3.0
 */
public enum CommonDiagnosticMessages {
    OAS_COMMON_101("OAS_COMMON_101", "Responses with 204 status code cannot have a body.",
            DiagnosticSeverity.WARNING),
    OAS_COMMON_102("OAS_COMMON_102", "Field `%s` in the record `%s` is added with different types. " +
            "Hence the field from the first schema is considered.", DiagnosticSeverity.WARNING),
    OAS_COMMON_201("OAS_COMMON_201", "Invalid reference value : %s\nBallerina " +
            "only supports local reference values.", DiagnosticSeverity.ERROR),
    OAS_COMMON_202("OAS_COMMON_202", "Unsupported OAS data type `%s`", DiagnosticSeverity.ERROR),
    OAS_COMMON_203("OAS_COMMON_203", "Path parameter value cannot be null.", DiagnosticSeverity.ERROR),
    OAS_COMMON_204("OAS_COMMON_204", "unsupported path parameter type found in the parameter `%s`. " +
            "hence the parameter type is set to `string`.", DiagnosticSeverity.WARNING);

    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    CommonDiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
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
