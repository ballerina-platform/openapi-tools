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

package io.ballerina.openapi.core.generators.constraint;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code ConstraintDiagnosticMessages} enum class for containing the diagnostic messages
 * related to openapi to ballerina constraint generation command.
 *
 * @since 1.9.0
 */
public enum ConstraintDiagnosticMessages {
    OAS_CONSTRAINT_101("OAS_CONSTRAINT_101",
            "constraints in the OpenAPI contract will be ignored for the " +
                    "field `%s`, as constraints are not supported on Ballerina union types.",
            DiagnosticSeverity.WARNING),
    OAS_CONSTRAINT_102("OAS_CONSTRAINT_102", "skipped generation for unsupported pattern" +
            " in ballerina: %s", DiagnosticSeverity.WARNING),
    OAS_CONSTRAINT_103("OAS_CONSTRAINT_103", "skipped generation for non-ECMA flavoured" +
            " pattern: %s", DiagnosticSeverity.WARNING);
    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    ConstraintDiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
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
