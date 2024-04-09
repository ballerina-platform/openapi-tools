/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.resourcepath.util.PathSegment;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.parameters.PathParameter;

import java.util.Map;

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.unescapeIdentifier;

/**
 * This {@link PathParameterMapper} class represents the path parameter mapper.
 * This class provides functionality for mapping the Ballerina path parameter to OpenAPI path parameter.
 *
 * @since 1.9.0
 */
public class PathParameterMapper extends AbstractParameterMapper {

    private final TypeSymbol type;
    private final String name;
    private final String description;
    private final TypeMapper typeMapper;
    private final Location location;
    private final PathSegment.Kind pathSegmentKind;

    public PathParameterMapper(PathParameterSymbol pathParameterSymbol, Map<String, String> apiDocs,
                               OperationInventory operationInventory, TypeMapper typeMapper) {
        super(operationInventory);
        this.location = pathParameterSymbol.getLocation().orElse(null);
        this.pathSegmentKind = pathParameterSymbol.pathSegmentKind();
        this.type = pathParameterSymbol.typeDescriptor();
        this.typeMapper = typeMapper;
        this.name = unescapeIdentifier(pathParameterSymbol.getName().get());
        this.description = apiDocs.get(pathParameterSymbol.getName().get());
    }

    @Override
    public PathParameter getParameterSchema() throws ParameterMapperException {
        if (pathSegmentKind.equals(PathSegment.Kind.PATH_REST_PARAMETER)) {
            ExceptionDiagnostic error = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_125, location, name);
            throw new ParameterMapperException(error);
        }
        PathParameter pathParameter = new PathParameter();
        pathParameter.setName(name);
        pathParameter.setRequired(true);
        pathParameter.setSchema(typeMapper.getTypeSchema(type));
        pathParameter.setDescription(description);
        return pathParameter;
    }
}
