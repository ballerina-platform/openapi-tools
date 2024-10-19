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

package io.ballerina.openapi.core.generators.client.diagnostic;

import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

public class ClientDiagnosticImp implements ClientDiagnostic {
    private final String code;
    private final String message;
    private final DiagnosticSeverity diagnosticSeverity;
//    private final Location location;

    public ClientDiagnosticImp(DiagnosticMessages message, String... args) {
        this.code = message.getCode();
        this.message = String.format(message.getDescription(), (Object[]) args);
        this.diagnosticSeverity = message.getSeverity();
//        this.location = location;
    }

    public ClientDiagnosticImp(Diagnostic diagnostic) {
        this.code = diagnostic.diagnosticInfo().code();
        this.message = diagnostic.message();
        this.diagnosticSeverity = diagnostic.diagnosticInfo().severity();
    }

    @Override
    public DiagnosticSeverity getDiagnosticSeverity() {
        return diagnosticSeverity;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Optional<Location> getLocation() {
        return Optional.empty();
    }

    @Override
    public String getCode() {
        return code;
    }
}
