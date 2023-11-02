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
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;

import static io.ballerina.openapi.service.utils.MapperCommonUtils.getTypeName;

public final class TypeSchemaGenerator {

    public static Schema getTypeSchema(TypeSymbol typeSymbol, Map<String, Schema> components,
                                       SemanticModel semanticModel) {
        return switch (typeSymbol.typeKind()) {
            case MAP -> MapTypeMapper.getSchema((MapTypeSymbol) typeSymbol, components, semanticModel);
            case ARRAY -> ArrayTypeMapper.getSchema((ArrayTypeSymbol) typeSymbol, components, semanticModel);
            case TYPE_REFERENCE -> ReferenceTypeMapper.getSchema((TypeReferenceTypeSymbol) typeSymbol,
                    components, semanticModel);
            case RECORD -> RecordTypeMapper.getSchema((RecordTypeSymbol) typeSymbol, components, semanticModel);
            case INTERSECTION -> ReadOnlyTypeMapper.getSchema((IntersectionTypeSymbol) typeSymbol,
                    components, semanticModel);
            case UNION -> UnionTypeMapper.getSchema((UnionTypeSymbol) typeSymbol, components, semanticModel);
            case TABLE -> TableTypeMapper.getSchema((TableTypeSymbol) typeSymbol, components, semanticModel);
            case TUPLE -> TupleTypeMapper.getSchema((TupleTypeSymbol) typeSymbol, components, semanticModel);
            case ERROR -> ErrorTypeMapper.getSchema((ErrorTypeSymbol) typeSymbol, components, semanticModel);
            default -> SimpleTypeMapper.getTypeSchema(typeSymbol);
        };
    }

    public static Map<String, Schema> createComponentMapping(TypeReferenceTypeSymbol typeSymbol,
                                                             Map<String, Schema> components,
                                                             SemanticModel semanticModel) {
        if (components.containsKey(getTypeName(typeSymbol))) {
            return components;
        }
        TypeSymbol referredType = typeSymbol.typeDescriptor();
        TypeMapper mapper = switch (referredType.typeKind()) {
            case TYPE_REFERENCE -> new ReferenceTypeMapper(typeSymbol, semanticModel);
            case MAP -> new MapTypeMapper(typeSymbol, semanticModel);
            case RECORD -> new RecordTypeMapper(typeSymbol, semanticModel);
            case INTERSECTION -> new ReadOnlyTypeMapper(typeSymbol, semanticModel);
            case ARRAY -> new ArrayTypeMapper(typeSymbol, semanticModel);
            case UNION -> new UnionTypeMapper(typeSymbol, semanticModel);
            case TABLE -> new TableTypeMapper(typeSymbol, semanticModel);
            case TUPLE -> new TupleTypeMapper(typeSymbol, semanticModel);
            case ERROR -> new ErrorTypeMapper(typeSymbol, semanticModel);
            default -> new SimpleTypeMapper(typeSymbol, semanticModel);
        };
        components.put(getTypeName(typeSymbol), null);
        mapper.addToComponents(components);
        return components;
    }
}
