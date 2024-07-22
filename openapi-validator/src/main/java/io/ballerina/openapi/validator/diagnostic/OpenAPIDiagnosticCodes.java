/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
package io.ballerina.openapi.validator.diagnostic;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import static io.ballerina.tools.diagnostics.DiagnosticSeverity.ERROR;

/**
 * Diagnostic codes for OpenAPI compiler plugin.
 *
 * @since 2.1.0
 */
public enum OpenAPIDiagnosticCodes {

    OPENAPI_100("OPENAPI_100", "OpenAPI example annotation is not supported on non-anydata types", ERROR),
    OPENAPI_101("OPENAPI_101", "OpenAPI examples annotation is not supported on non-anydata types", ERROR),
    OPENAPI_102("OPENAPI_102", "OpenAPI example and OpenAPI examples annotations cannot be used together", ERROR),
    OPENAPI_103("OPENAPI_103", "OpenAPI example(s) annotaiton is not supported on rest parameter", ERROR);

    private final String code;
    private final String message;
    private final DiagnosticSeverity severity;

    OpenAPIDiagnosticCodes(String code, String message, DiagnosticSeverity severity) {
        this.code = code;
        this.message = message;
        this.severity = severity;
    }

    public Diagnostic getDiagnosticCode(Location location) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code, message, severity);
        return DiagnosticFactory.createDiagnostic(diagnosticInfo, location);
    }

    public Diagnostic getDiagnosticCode(Location location, Object... args) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(code, message, severity);
        return DiagnosticFactory.createDiagnostic(diagnosticInfo, location, args);
    }
}
