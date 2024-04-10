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

package io.ballerina.openapi.core.generators.service.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.generators.common.exception.InvalidReferenceException;
import io.ballerina.openapi.core.generators.common.exception.UnsupportedOASDataTypeException;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;

public abstract class ParameterGenerator {
    final OpenAPI openAPI;
    boolean isNullableRequired;
    final List<Diagnostic> diagnostics = new ArrayList<>();

    public ParameterGenerator(OASServiceMetadata oasServiceMetadata) {
        this.openAPI = oasServiceMetadata.getOpenAPI();
        this.isNullableRequired = oasServiceMetadata.isNullable();
    }

    public abstract ParameterNode generateParameterNode(Parameter parameter) throws UnsupportedOASDataTypeException,
            InvalidReferenceException;

    public boolean isNullableRequired() {
        return isNullableRequired;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
