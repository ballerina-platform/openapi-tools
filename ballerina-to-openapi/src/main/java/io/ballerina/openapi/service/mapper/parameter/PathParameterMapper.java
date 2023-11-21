// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.AdditionalData;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.type.ComponentMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.unescapeIdentifier;

public class PathParameterMapper implements ParameterMapper {

    private final TypeSymbol type;
    private final String name;
    private final String description;
    private final SemanticModel semanticModel;
    private final List<OpenAPIMapperDiagnostic> diagnostics;

    public PathParameterMapper(PathParameterSymbol parameterSymbol, Map<String, String> apiDocs,
                               SemanticModel semanticModel, List<OpenAPIMapperDiagnostic> diagnostics) {
        this.type = parameterSymbol.typeDescriptor();
        this.name = unescapeIdentifier(parameterSymbol.getName().get());
        this.description = apiDocs.get(name);
        this.semanticModel = semanticModel;
        this.diagnostics = diagnostics;
    }

    @Override
    public Parameter getParameterSchema(Components components) {
        PathParameter pathParameter = new PathParameter();
        pathParameter.setName(name);
        pathParameter.setRequired(true);
        Map<String, Schema> componentSchemas = components.getSchemas();
        if (Objects.isNull(componentSchemas)) {
            componentSchemas = new HashMap<>();
        }
        AdditionalData additionalData = new AdditionalData(semanticModel, null, diagnostics);
        pathParameter.setSchema(ComponentMapper.getTypeSchema(type, componentSchemas, additionalData));
        if (!componentSchemas.isEmpty()) {
            components.setSchemas(componentSchemas);
        }
        pathParameter.setDescription(description);
        return pathParameter;
    }
}
