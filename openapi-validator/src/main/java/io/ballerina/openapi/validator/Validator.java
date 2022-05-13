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
package io.ballerina.openapi.validator;

import io.ballerina.openapi.validator.model.Filter;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Abstract class for the validator.
 * 
 * @since 1.1.0
 */
public abstract class Validator {
    SyntaxNodeAnalysisContext context;
    OpenAPI openAPI;
    DiagnosticSeverity severity = DiagnosticSeverity.ERROR;
    public Validator(){}

    public Validator(SyntaxNodeAnalysisContext context, OpenAPI openAPI, DiagnosticSeverity severity) {
        this.context = context;
        this.openAPI = openAPI;
        this.severity = severity;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }


    public void validate() {};

    public void initialize(SyntaxNodeAnalysisContext context, OpenAPI openAPI, Filter filter) {};
}
