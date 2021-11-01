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
package io.ballerina.openapi.converter.service.result;

import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;

/**
 * {@code OAS} is used to contain OpenAPI {@code OpenAPI} in string format and error list.
 *
 * @since 2.0.0
 */
public class OAS extends Result {
    private OpenAPI openAPI;

    /**
     * This constructor is used to store the details that {@code OpenAPI} object and diagnostic list.
     *
     * @param openAPI    - OpenAPI definition with {@code OpenAPI} model.
     * @param diagnostics   - List of Diagnostic found through the generation process.
     */
    public OAS(OpenAPI openAPI, List<OpenAPIConverterDiagnostic> diagnostics) {
        super(diagnostics);
        this.openAPI = openAPI;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }
}
