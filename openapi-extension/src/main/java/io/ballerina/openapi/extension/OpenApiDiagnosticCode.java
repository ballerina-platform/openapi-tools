/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.openapi.extension;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

/**
 * {@code OpenApiDiagnosticCode} is used to hold open-api doc generation related diagnostic codes.
 */
public enum OpenApiDiagnosticCode {
    OPENAPI_100("OPENAPI_100", "could not create resource directory", DiagnosticSeverity.WARNING),
    OPENAPI_101("OPENAPI_101", "could not find the provided contract file",
            DiagnosticSeverity.WARNING),
    OPENAPI_102("OPENAPI_102", "error occurred when generating open-api doc : {0}",
            DiagnosticSeverity.WARNING),
    OPENAPI_103("OPENAPI_103", "unknown error occurred when generating open-api doc : {0}",
            DiagnosticSeverity.WARNING),
    OPENAPI_104("OPENAPI_104", "error occurred while packing generated resources : {0}",
            DiagnosticSeverity.WARNING),
    OPENAPI_105("OPENAPI_105", "unknown error occurred while packing generated resources : {0}",
            DiagnosticSeverity.WARNING),
    OPENAPI_106("OPENAPI_106", "error occurred while cleaning-up generated resources : {0}",
            DiagnosticSeverity.WARNING),
    OPENAPI_107("OPENAPI_107", "generated open-api definition is empty",
            DiagnosticSeverity.WARNING);

    private final String code;
    private final String description;
    private final DiagnosticSeverity severity;

    OpenApiDiagnosticCode(String code, String description, DiagnosticSeverity severity) {
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
