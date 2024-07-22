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
package io.ballerina.openapi.service.mapper.example.parameter;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;

import static io.ballerina.openapi.service.mapper.Constants.EMPTY;

/**
 * This {@link DefaultParamExampleMapper} class represents the examples mapper for query and header parameters.
 *
 * @since 2.1.0
 */
public class DefaultParamExampleMapper extends ParameterExampleMapper {

    public DefaultParamExampleMapper(String paramTypeName, ParameterSymbol parameterSymbol, Parameter parameterSchema,
                                     SemanticModel semanticModel, List<OpenAPIMapperDiagnostic> diagnostics) {
        super(parameterSymbol.annotAttachments(), parameterSchema, semanticModel, diagnostics,
                parameterSymbol.getName().orElse(EMPTY), parameterSymbol.getLocation().orElse(null),
                paramTypeName, parameterSymbol.typeDescriptor());
    }
}
