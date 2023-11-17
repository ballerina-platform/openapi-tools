/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
import io.ballerina.openapi.service.diagnostic.OpenAPIMapperDiagnostic;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.openapi.service.utils.MapperCommonUtils.getTypeName;

/**
 * This util class for processing the mapping in between ballerina record and openAPI object schema.
 *
 * @since 2.0.0
 */
public class ComponentMapper {

    private final Components components;
    private final List<OpenAPIMapperDiagnostic> diagnostics;
    private final SemanticModel semanticModel;

    public ComponentMapper(Components components, SemanticModel semanticModel) {
        this.components = components;
        this.diagnostics = new ArrayList<>();
        this.semanticModel = semanticModel;
    }

    public static Schema getTypeSchema(TypeSymbol typeSymbol, Map<String, Schema> components,
                                       SemanticModel semanticModel, List<OpenAPIMapperDiagnostic> diagnostics) {
        return switch (typeSymbol.typeKind()) {
            case MAP -> MapTypeMapper.getSchema((MapTypeSymbol) typeSymbol, components, semanticModel, diagnostics);
            case ARRAY ->
                    ArrayTypeMapper.getSchema((ArrayTypeSymbol) typeSymbol, components, semanticModel, diagnostics);
            case TYPE_REFERENCE -> ReferenceTypeMapper.getSchema((TypeReferenceTypeSymbol) typeSymbol, components,
                    semanticModel, diagnostics);
            case RECORD -> RecordTypeMapper.getSchema((RecordTypeSymbol) typeSymbol, components, semanticModel,
                    diagnostics);
            case INTERSECTION ->
                    ReadOnlyTypeMapper.getSchema((IntersectionTypeSymbol) typeSymbol, components, semanticModel,
                            diagnostics);
            case UNION ->
                    UnionTypeMapper.getSchema((UnionTypeSymbol) typeSymbol, components, semanticModel, diagnostics);
            case TABLE ->
                    TableTypeMapper.getSchema((TableTypeSymbol) typeSymbol, components, semanticModel, diagnostics);
            case TUPLE ->
                    TupleTypeMapper.getSchema((TupleTypeSymbol) typeSymbol, components, semanticModel, diagnostics);
            case ERROR ->
                    ErrorTypeMapper.getSchema((ErrorTypeSymbol) typeSymbol, components, semanticModel, diagnostics);
            default -> SimpleTypeMapper.getTypeSchema(typeSymbol, diagnostics);
        };
    }

    public static Map<String, Schema> createComponentMapping(TypeReferenceTypeSymbol typeSymbol,
                                                             Map<String, Schema> components,
                                                             SemanticModel semanticModel,
                                                             List<OpenAPIMapperDiagnostic> diagnostics) {
        if (components.containsKey(getTypeName(typeSymbol))) {
            return components;
        }
        TypeSymbol referredType = typeSymbol.typeDescriptor();
        TypeMapper mapper = switch (referredType.typeKind()) {
            case TYPE_REFERENCE -> new ReferenceTypeMapper(typeSymbol, semanticModel, diagnostics);
            case MAP -> new MapTypeMapper(typeSymbol, semanticModel, diagnostics);
            case RECORD -> new RecordTypeMapper(typeSymbol, semanticModel, diagnostics);
            case INTERSECTION -> new ReadOnlyTypeMapper(typeSymbol, semanticModel, diagnostics);
            case ARRAY -> new ArrayTypeMapper(typeSymbol, semanticModel, diagnostics);
            case UNION -> new UnionTypeMapper(typeSymbol, semanticModel, diagnostics);
            case TABLE -> new TableTypeMapper(typeSymbol, semanticModel, diagnostics);
            case TUPLE -> new TupleTypeMapper(typeSymbol, semanticModel, diagnostics);
            case ERROR -> new ErrorTypeMapper(typeSymbol, semanticModel, diagnostics);
            default -> new SimpleTypeMapper(typeSymbol, semanticModel, diagnostics);
        };
        components.put(getTypeName(typeSymbol), null);
        mapper.addToComponents(components);
        return components;
    }

    public List<OpenAPIMapperDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    /**
      * This function for doing the mapping with ballerina record to object schema.
      *
      * @param typeSymbol Record Name as a TypeSymbol
      */
    public void createComponentsSchema(TypeSymbol typeSymbol) {
        Map<String, Schema> schema = components.getSchemas();
        if (schema == null) {
            schema = new HashMap<>();
        }
        Map<String, Schema> componentsSchema = createComponentMapping((TypeReferenceTypeSymbol) typeSymbol, schema,
                semanticModel, diagnostics);
        components.setSchemas(componentsSchema);
    }
}
