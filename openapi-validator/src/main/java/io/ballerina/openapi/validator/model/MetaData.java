/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator.model;

import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Purpose of this class is to store all the metadata details that required to validation and generate diagnostic.
 *
 * @since 1.1.0
 */
public class MetaData {
    private final SyntaxNodeAnalysisContext context;
    private final OpenAPI openAPI;
    private final String path;
    private final String method;
    private final DiagnosticSeverity severity;
    private final Location location;

    public MetaData(SyntaxNodeAnalysisContext context, OpenAPI openAPI, String path, String method,
                    DiagnosticSeverity severity, Location location) {
        this.context = context;
        this.openAPI = openAPI;
        this.path = path;
        this.method = method;
        this.severity = severity;
        this.location = location;
    }

    public SyntaxNodeAnalysisContext getContext() {
        return context;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public DiagnosticSeverity getSeverity() {
        return severity;
    }

    public Location getLocation() {
        return location;
    }
}
