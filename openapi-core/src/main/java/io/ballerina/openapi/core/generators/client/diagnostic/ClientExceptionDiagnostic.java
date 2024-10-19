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

import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

public class ClientExceptionDiagnostic implements ClientDiagnostic {
    private final String code;
    private final String message;
    private final DiagnosticSeverity diagnosticSeverity;
    private final Location location;

    public ClientExceptionDiagnostic(String code, String message, Location location, String... args) {
        this.code = code;
        this.message = String.format(message, (Object[]) args);
        this.diagnosticSeverity = DiagnosticSeverity.ERROR;
        this.location = location;
    }

    @Override
    public String getCode() {
        return code;
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
        return Optional.ofNullable(location);
    }
}
