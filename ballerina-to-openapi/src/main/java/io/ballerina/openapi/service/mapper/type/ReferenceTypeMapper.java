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

import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.AdditionalData;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Objects;

import static io.ballerina.openapi.service.mapper.type.TypeMapper.createComponentMapping;

public class ReferenceTypeMapper extends AbstractTypeMapper {

    public ReferenceTypeMapper(TypeReferenceTypeSymbol typeSymbol, AdditionalData additionalData) {
        super(typeSymbol, additionalData);
    }

    @Override
    public Schema getReferenceSchema(OpenAPI openAPI) {
        TypeReferenceTypeSymbol referredType = (TypeReferenceTypeSymbol) typeSymbol.typeDescriptor();
        Schema schema = getSchema(referredType, openAPI, additionalData);
        if (Objects.nonNull(schema)) {
            schema.description(description);
        }
        return schema;
    }

    public static Schema getSchema(TypeReferenceTypeSymbol typeSymbol, OpenAPI openAPI,
                                   AdditionalData additionalData) {
        if (isBuiltInSubTypes(typeSymbol)) {
            return SimpleTypeMapper.getTypeSchema(typeSymbol.typeDescriptor(), additionalData);
        }
        if (!AbstractTypeMapper.hasMapping(openAPI, typeSymbol)) {
            createComponentMapping(typeSymbol, openAPI, additionalData);
            if (!AbstractTypeMapper.hasFullMapping(openAPI, typeSymbol)) {
                return null;
            }
        }
        return new ObjectSchema().$ref(MapperCommonUtils.getTypeName(typeSymbol));
    }

    public static boolean isBuiltInSubTypes(TypeReferenceTypeSymbol typeSymbol) {
        TypeSymbol referredType = typeSymbol.typeDescriptor();
        return switch (referredType.typeKind()) {
            case INT_SIGNED8, INT_SIGNED16, INT_SIGNED32, INT_UNSIGNED8, INT_UNSIGNED16, INT_UNSIGNED32,
                    XML_COMMENT, XML_ELEMENT, XML_PROCESSING_INSTRUCTION, XML_TEXT,
                    STRING_CHAR->
                    true;
            default -> false;
        };
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
