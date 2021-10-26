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
package io.ballerina.openapi.converter.service;

import io.ballerina.openapi.converter.error.OpenAPIConverterError;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;

/**
 * {@code OAS} is used to contain OpenAPI definition and error list.
 *
 * @since 2.0.0
 */
public class OAS {
    private OpenAPI definition;
    private List<OpenAPIConverterError> errors;

    public OAS(OpenAPI definition, List<OpenAPIConverterError> errors) {
        this.definition = definition;
        this.errors = errors;
    }

    public OpenAPI getDefinition() {
        return definition;
    }

    public List<OpenAPIConverterError> getErrors() {
        return errors;
    }

    public void setErrors(List<OpenAPIConverterError> errors) {
        this.errors = errors;
    }
}
