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

import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.IncompatibleResourceDiagnostic;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationDTO;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.parameters.PathParameter;

import java.util.Map;

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.removeStartingSingleQuote;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.unescapeIdentifier;

public class PathParameterMapper extends AbstractParameterMapper {

    private final TypeSymbol type;
    private final String name;
    private final String description;
    private final AdditionalData additionalData;
    private final Components components;
    private final Location location;
    private final PathSegment.Kind pathSegmentKind;

    public PathParameterMapper(PathParameterSymbol pathParameterSymbol, Components components,
                               Map<String, String> apiDocs, OperationDTO operationDTO, AdditionalData additionalData) {
        super(operationDTO);
        this.location = pathParameterSymbol.getLocation().orElse(null);
        this.pathSegmentKind = pathParameterSymbol.pathSegmentKind();
        this.type = pathParameterSymbol.typeDescriptor();
        this.components = components;
        this.name = unescapeIdentifier(pathParameterSymbol.getName().get());
        this.description = apiDocs.get(removeStartingSingleQuote(pathParameterSymbol.getName().get()));
        this.additionalData = additionalData;
    }

    @Override
    public PathParameter getParameterSchema() {
        if (pathSegmentKind.equals(PathSegment.Kind.PATH_REST_PARAMETER)) {
            DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_125;
            IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage, location, name);
            additionalData.diagnostics().add(error);
            return null;
        }
        PathParameter pathParameter = new PathParameter();
        pathParameter.setName(name);
        pathParameter.setRequired(true);
        pathParameter.setSchema(TypeMapper.getTypeSchema(type, components, additionalData));
        pathParameter.setDescription(description);
        return pathParameter;
    }
}
