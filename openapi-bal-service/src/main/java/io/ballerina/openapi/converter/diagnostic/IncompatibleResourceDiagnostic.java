/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.converter.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

/**
 * {@code IncompatibleResourceError} represents the error that OAS not compatible with ballerina implementation.
 *
 * @since 2.0.0
 */
public class IncompatibleResourceDiagnostic extends OpenAPIConverterDiagnostic {
    private final String code;
    private final String message;
    private final Location location;
    private final DiagnosticSeverity severity;

    public IncompatibleResourceDiagnostic(String code, String message,
                                          Location location, DiagnosticSeverity severity) {
        this.code = code;
        this.message = message;
        this.location = location;
        this.severity = severity;
    }

    public String getCode() {
        return code;
    }

    public DiagnosticSeverity getSeverity() {
        return severity;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Location getLocation() {
        return location;
    }
}
