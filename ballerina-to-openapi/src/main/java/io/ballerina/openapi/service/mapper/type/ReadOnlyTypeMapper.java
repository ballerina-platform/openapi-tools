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
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReadOnlyTypeMapper extends TypeMapper {

    public ReadOnlyTypeMapper(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel) {
        super(typeSymbol, semanticModel);
    }

    @Override
    public Schema getReferenceTypeSchema(Map<String, Schema> components) {
        IntersectionTypeSymbol referredType = (IntersectionTypeSymbol) typeSymbol.typeDescriptor();
        Schema effectiveTypeSchema = getSchema(referredType, components, semanticModel);
        return Objects.nonNull(effectiveTypeSchema) ? effectiveTypeSchema.description(description) : null;
    }

    public static Schema getSchema(IntersectionTypeSymbol typeSymbol, Map<String, Schema> components,
                                   SemanticModel semanticModel) {
        TypeSymbol effectiveType = getEffectiveType(typeSymbol);
        if (Objects.isNull(effectiveType)) {
            return null;
        }
        return TypeSchemaGenerator.getTypeSchema(effectiveType, components, semanticModel);
    }

    public static TypeSymbol getEffectiveType(IntersectionTypeSymbol typeSymbol) {
        List<TypeSymbol> memberTypes = typeSymbol.memberTypeDescriptors();
        if (memberTypes.size() == 2) {
            TypeSymbol firstMember = memberTypes.get(0);
            TypeSymbol secondMember = memberTypes.get(1);
            // Do not map readonly to the schema
            if (firstMember.typeKind().equals(TypeDescKind.READONLY)) {
                return secondMember;
            } else if (secondMember.typeKind().equals(TypeDescKind.READONLY)) {
                return firstMember;
            }
        }
        return null;
    }
}
