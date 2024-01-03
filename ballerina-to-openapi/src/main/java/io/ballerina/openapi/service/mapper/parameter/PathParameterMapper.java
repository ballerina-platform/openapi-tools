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
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.service.mapper.AdditionalData;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.IncompatibleResourceDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;

import java.util.List;
import java.util.Map;

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.unescapeIdentifier;

public class PathParameterMapper implements ParameterMapper {

    private final TypeSymbol type;
    private final String name;
    private final String description;
    private final SemanticModel semanticModel;
    private final List<OpenAPIMapperDiagnostic> diagnostics;
    private final OpenAPI openAPI;

    public PathParameterMapper(PathParameterSymbol pathParameterSymbol, OpenAPI openAPI, Map<String, String> apiDocs,
                               SemanticModel semanticModel, List<OpenAPIMapperDiagnostic> diagnostics) {
        this.type = pathParameterSymbol.typeDescriptor();
        this.openAPI = openAPI;
        this.name = unescapeIdentifier(pathParameterSymbol.getName().get());
        this.description = apiDocs.get(pathParameterSymbol.getName().get());
        this.semanticModel = semanticModel;
        this.diagnostics = diagnostics;
    }

    /**
     * Map path parameter data to OAS path parameter.
     */
    public void mapPathParameter(List<Parameter> parameters, Node pathParam) {
        if (!pathParam.kind().equals(SyntaxKind.RESOURCE_PATH_REST_PARAM)) {
            parameters.add(getParameterSchema());
        } else {
            DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_125;
            IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage,
                    pathParam.location(), pathParam.toString());
            diagnostics.add(error);
        }
    }

    @Override
    public Parameter getParameterSchema() {
        PathParameter pathParameter = new PathParameter();
        pathParameter.setName(name);
        pathParameter.setRequired(true);
        AdditionalData additionalData = new AdditionalData(semanticModel, null, diagnostics);
        pathParameter.setSchema(TypeMapper.getTypeSchema(type, openAPI, additionalData));
        pathParameter.setDescription(description);
        return pathParameter;
    }
}