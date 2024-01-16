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
package io.ballerina.openapi.service.mapper.diagnostic;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

/**
 * This {@code IncompatibleResourceDiagnostic} represents the diagnostic that OAS not compatible with ballerina
 * implementation.
 *
 * @since 2.0.0
 */
public class IncompatibleResourceDiagnostic implements OpenAPIMapperDiagnostic {
    private final String code;
    private final String message;
    private final Location location;
    private final DiagnosticSeverity severity;

    public IncompatibleResourceDiagnostic(DiagnosticMessages details, Location location, String... args) {
        this.code = details.getCode();
        this.message = generateDescription(details, args);
        this.location = location;
        this.severity = details.getSeverity();
    }

    public IncompatibleResourceDiagnostic(DiagnosticMessages details, Location location) {
        this(details, location, details.getDescription());
    }
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public DiagnosticSeverity getDiagnosticSeverity() {
        return severity;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }

    /**
     *  This method is to create message description with args values.
     */
    private static String generateDescription(DiagnosticMessages details, String[] args) {
        return String.format(details.getDescription(), (Object[]) args);
    }
}
