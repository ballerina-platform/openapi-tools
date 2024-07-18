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
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;

import static io.ballerina.openapi.service.mapper.Constants.EMPTY;
import static io.ballerina.openapi.service.mapper.Constants.PATH;

/**
 * This {@link PathExampleMapper} class represents the examples mapper for path parameters.
 *
 * @since 2.1.0
 */
public class PathExampleMapper extends ParameterExampleMapper {

    public PathExampleMapper(PathParameterSymbol parameter, Parameter parameterSchema, SemanticModel semanticModel,
                             List<OpenAPIMapperDiagnostic> diagnostics) {
        super(parameter.annotAttachments(), parameterSchema, semanticModel, diagnostics,
                parameter.getName().orElse(EMPTY), parameter.getLocation().orElse(null), PATH);
    }
}
