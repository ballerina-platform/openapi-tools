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
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.CommonData;
import io.ballerina.openapi.service.mapper.type.ComponentMapper;
import io.ballerina.openapi.service.mapper.type.UnionTypeMapper;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.openapi.service.utils.MapperCommonUtils.unescapeIdentifier;

public class QueryParameterMapper implements ParameterMapper {

    private final TypeSymbol type;
    private final String name;
    private final boolean isRequired;
    private final String description;
    private final boolean treatNilableAsOptional;
    private final SemanticModel semanticModel;
    private final List<OpenAPIMapperDiagnostic> diagnostics;

    public QueryParameterMapper(ParameterSymbol parameterSymbol, Map<String, String> apiDocs,
                                boolean treatNilableAsOptional, SemanticModel semanticModel,
                                List<OpenAPIMapperDiagnostic> diagnostics) {
        this.type = parameterSymbol.typeDescriptor();
        this.name = unescapeIdentifier(parameterSymbol.getName().get());
        this.isRequired = parameterSymbol.paramKind().equals(ParameterKind.REQUIRED);
        this.description = apiDocs.get(name);
        this.treatNilableAsOptional = treatNilableAsOptional;
        this.semanticModel = semanticModel;
        this.diagnostics = diagnostics;
    }

    @Override
    public Parameter getParameterSchema(Components components) {
        QueryParameter queryParameter = new QueryParameter();
        queryParameter.setName(name);
        if (isRequired && (!treatNilableAsOptional || !UnionTypeMapper.hasNilableType(type))) {
            queryParameter.setRequired(true);
        }
        Map<String, Schema> componentSchemas = components.getSchemas();
        if (Objects.isNull(componentSchemas)) {
            componentSchemas = new HashMap<>();
        }
        CommonData commonData = new CommonData(semanticModel, null, diagnostics);
        queryParameter.setSchema(ComponentMapper.getTypeSchema(type, componentSchemas, commonData));
        if (!componentSchemas.isEmpty()) {
            components.setSchemas(componentSchemas);
        }
        queryParameter.setDescription(description);
        return queryParameter;
    }
}
