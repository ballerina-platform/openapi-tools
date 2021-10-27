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
package io.ballerina.openapi.converter.error;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * This {@code ErrorMessages} enum class for containing the error message related to ballerina to openapi command.
 */
public enum ErrorMessages {
    OAS_CONVERTOR_100("OAS_CONVERTOR_100",
            "Generated OpenAPI definition does not contain details for the `default` " +
            "resource method in the Ballerina service.", DiagnosticSeverity.WARNING),
    OAS_CONVERTOR_101("OAS_CONVERTOR_101", "Unexpected value: " , DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_102("OAS_CONVERTOR_102", "Invalid mediaType : " , DiagnosticSeverity.ERROR),
    OAS_CONVERTOR_103("OAS_CONVERTOR_103", "No related status code for : " , DiagnosticSeverity.ERROR);


    private final String code;
    private String description;
    private final DiagnosticSeverity severity;

    ErrorMessages(String code, String description, DiagnosticSeverity severity) {
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
