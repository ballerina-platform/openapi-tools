/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.generators.service;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code ServiceDiagnosticMessages} enum class for containing the error message related to openapi to ballerina
 * service generation command.
 *
 * @since 2.0.0
 */
public enum ServiceDiagnosticMessages {
    OAS_SERVICE_100("OAS_SERVICE_100", "Query parameters with nested array types are not " +
            "supported in Ballerina.", DiagnosticSeverity.ERROR),
    OAS_SERVICE_101("OAS_SERVICE_101",
            "Query parameters with no array item type can not be mapped to Ballerina resource" +
                    " query parameters.", DiagnosticSeverity.ERROR),
    OAS_SERVICE_102("OAS_SERVICE_102",
            "Query parameters with type '%s' can not be mapped to the Ballerina query parameters.",
            DiagnosticSeverity.ERROR),
    OAS_SERVICE_103("OAS_SERVICE_103", "Header '%s' with array item type: '%s' is not " +
            "supported in Ballerina.", DiagnosticSeverity.ERROR),
    OAS_SERVICE_104("OAS_SERVICE_104",
            "Header '%s' with no array item type can not be mapped as a valid Ballerina" +
                    " header parameter.", DiagnosticSeverity.ERROR),
    OAS_SERVICE_105("OAS_SERVICE_105",
            "Header '%s' with type '%s' can not be mapped as a valid Ballerina header parameter.",
            DiagnosticSeverity.ERROR),
    OAS_SERVICE_106("OAS_SERVICE_106",
            "Header '%s' with no header type can not be mapped to the Ballerina headers.",
            DiagnosticSeverity.ERROR);

    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    ServiceDiagnosticMessages(String code, String description, DiagnosticSeverity severity) {
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
