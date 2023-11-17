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

package io.ballerina.openapi.service.mapper.type;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.openapi.service.mapper.type.ComponentMapper.createComponentMapping;

public class ReferenceTypeMapper extends TypeMapper {

    public ReferenceTypeMapper(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel,
                               List<OpenAPIMapperDiagnostic> diagnostics) {
        super(typeSymbol, semanticModel, diagnostics);
    }

    @Override
    public Schema getReferenceTypeSchema(Map<String, Schema> components) {
        TypeReferenceTypeSymbol referredType = (TypeReferenceTypeSymbol) typeSymbol.typeDescriptor();
        Schema schema = getSchema(referredType, components, semanticModel, diagnostics);
        if (Objects.nonNull(schema)) {
            schema.description(description);
        }
        return schema;
    }

    public static Schema getSchema(TypeReferenceTypeSymbol typeSymbol, Map<String, Schema> components,
                                   SemanticModel semanticModel, List<OpenAPIMapperDiagnostic> diagnostics) {
        if (!components.containsKey(MapperCommonUtils.getTypeName(typeSymbol))) {
            createComponentMapping(typeSymbol, components, semanticModel, diagnostics);
            if (!components.containsKey(MapperCommonUtils.getTypeName(typeSymbol)) || Objects.isNull(
                    components.get(MapperCommonUtils.getTypeName(typeSymbol)))) {
                return null;
            }
        }
        return new ObjectSchema().$ref(MapperCommonUtils.getTypeName(typeSymbol));
    }

    public static TypeSymbol getReferredType(TypeSymbol typeSymbol) {
        if (typeSymbol.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
            return getReferredType(((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor());
        }
        if (typeSymbol.typeKind().equals(TypeDescKind.INTERSECTION)) {
            TypeSymbol effectiveType = ReadOnlyTypeMapper.getEffectiveType((IntersectionTypeSymbol) typeSymbol);
            if (Objects.isNull(effectiveType)) {
                return null;
            }
            return getReferredType(effectiveType);
        }
        return typeSymbol;
    }
}
