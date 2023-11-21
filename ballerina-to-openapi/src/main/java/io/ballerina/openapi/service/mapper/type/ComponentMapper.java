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
import io.ballerina.openapi.service.mapper.CommonData;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getTypeName;

/**
 * This util class for processing the mapping in between ballerina record and openAPI object schema.
 *
 * @since 2.0.0
 */
public class ComponentMapper {

    private final Components components;
    private final CommonData commonData;

    public ComponentMapper(Components components, SemanticModel semanticModel,
                           ModuleMemberVisitor moduleMemberVisitor) {
        this.components = components;
        this.commonData = new CommonData(semanticModel, moduleMemberVisitor,
                new ArrayList<>());
    }

    public static Schema getTypeSchema(TypeSymbol typeSymbol, Map<String, Schema> components,
                                       CommonData componentMapperData) {
        return switch (typeSymbol.typeKind()) {
            case MAP -> MapTypeMapper.getSchema((MapTypeSymbol) typeSymbol, components, componentMapperData);
            case ARRAY ->
                    ArrayTypeMapper.getSchema((ArrayTypeSymbol) typeSymbol, components, componentMapperData);
            case TYPE_REFERENCE -> ReferenceTypeMapper.getSchema((TypeReferenceTypeSymbol) typeSymbol, components,
                    componentMapperData);
            case RECORD -> RecordTypeMapper.getSchema((RecordTypeSymbol) typeSymbol, components,
                    null, componentMapperData);
            case INTERSECTION ->
                    ReadOnlyTypeMapper.getSchema((IntersectionTypeSymbol) typeSymbol, components, componentMapperData);
            case UNION ->
                    UnionTypeMapper.getSchema((UnionTypeSymbol) typeSymbol, components, componentMapperData);
            case TABLE ->
                    TableTypeMapper.getSchema((TableTypeSymbol) typeSymbol, components, componentMapperData);
            case TUPLE ->
                    TupleTypeMapper.getSchema((TupleTypeSymbol) typeSymbol, components, componentMapperData);
            case ERROR ->
                    ErrorTypeMapper.getSchema((ErrorTypeSymbol) typeSymbol, components, componentMapperData);
            default -> SimpleTypeMapper.getTypeSchema(typeSymbol, componentMapperData);
        };
    }

    public static Map<String, Schema> createComponentMapping(TypeReferenceTypeSymbol typeSymbol,
                                                             Map<String, Schema> components,
                                                             CommonData componentMapperData) {
        if (components.containsKey(getTypeName(typeSymbol))) {
            return components;
        }
        TypeSymbol referredType = typeSymbol.typeDescriptor();
        TypeMapper mapper = switch (referredType.typeKind()) {
            case TYPE_REFERENCE -> new ReferenceTypeMapper(typeSymbol, componentMapperData);
            case MAP -> new MapTypeMapper(typeSymbol, componentMapperData);
            case RECORD -> new RecordTypeMapper(typeSymbol, componentMapperData);
            case INTERSECTION -> new ReadOnlyTypeMapper(typeSymbol, componentMapperData);
            case ARRAY -> new ArrayTypeMapper(typeSymbol, componentMapperData);
            case UNION -> new UnionTypeMapper(typeSymbol, componentMapperData);
            case TABLE -> new TableTypeMapper(typeSymbol, componentMapperData);
            case TUPLE -> new TupleTypeMapper(typeSymbol, componentMapperData);
            case ERROR -> new ErrorTypeMapper(typeSymbol, componentMapperData);
            default -> new SimpleTypeMapper(typeSymbol, componentMapperData);
        };
        components.put(getTypeName(typeSymbol), null);
        mapper.addToComponents(components);
        return components;
    }

    public List<OpenAPIMapperDiagnostic> getDiagnostics() {
        return commonData.diagnostics();
    }

    public void createComponentsSchema(TypeSymbol typeSymbol) {
        Map<String, Schema> schema = components.getSchemas();
        if (schema == null) {
            schema = new HashMap<>();
        }
        Map<String, Schema> componentsSchema = createComponentMapping((TypeReferenceTypeSymbol) typeSymbol, schema,
                commonData);
        components.setSchemas(componentsSchema);
    }

    public ModuleMemberVisitor getModuleMemberVisitor() {
        return commonData.moduleMemberVisitor();
    }
}
