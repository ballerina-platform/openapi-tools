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
package io.ballerina.openapi.converter.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code DiagnosticMessages} enum class for containing the error message related to ballerina to openapi command.
 *
 * @since 2.0.0
 */
public enum DiagnosticMessages {
    OAS_CONVERTOR_100("OAS_CONVERTOR_100",
            "Generated OpenAPI definition does not contain details for the `default` " +
                    "resource method in the Ballerina service.", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_101("OAS_CONVERTOR_101", "Unexpected value: %s", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_102("OAS_CONVERTOR_102", "Invalid mediaType: %s", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_103("OAS_CONVERTOR_103", "No related status code for: %s", DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_104("OAS_CONVERTOR_104",
            "Generated OpenAPI definition does not contain details for the " +
                    "resource function which has `http:Request` parameters in the Ballerina service.",
            DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_105("OAS_CONVERTOR_105",
            "Generated OpenAPI definition does not contain details for the " +
                    "resource function which has `http:Response` as return type in the Ballerina service.",
            DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_106("OAS_CONVERTOR_106", "Given Ballerina file contains compilation error(s).",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_107("OAS_CONVERTOR_107", "No Ballerina services found with name '%s' to" +
            " generate an OpenAPI specification. These services are available in ballerina file. %s",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_108("OAS_CONVERTOR_108", "Failed to generate OpenAPI definition due to: %s",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_109("OAS_CONVERTOR_109", "OpenAPI contract doesn't exist in the given location:%s",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_110("OAS_CONVERTOR_110", "OpenAPI contract path can not be blank.",
                      DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_111("OAS_CONVERTOR_111", "Unsupported file type. Provide a valid contract " +
            "file in .yaml or .json format.",
            DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_112("OAS_CONVERTOR_112", "Provided OpenAPI contract contains parsing error(s).",
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
