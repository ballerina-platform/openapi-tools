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

package io.ballerina.openapi.converter.service;

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.utils.ConverterUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This util class for processing the mapping in between ballerina record and openAPI object schema.
 *
 * @since 2.0.0
 */
public class OpenAPIComponentMapper {
    private final Components components;

    public OpenAPIComponentMapper(Components components) {
         this.components = components;
    }

    /**
     * This function for doing the mapping with ballerina record to object schema.
     *
     * @param recordNode     Record Node
     * @param schema         Map of current schemas
     * @param typeSymbol     Record Name as a TypeSymbol
     */
    public void handleRecordNode(SimpleNameReferenceNode recordNode, Map<String, Schema> schema,
                                 TypeSymbol typeSymbol) {
        String componentName = typeSymbol.getName().orElseThrow().trim();
        if (typeSymbol instanceof TypeReferenceTypeSymbol) {
            TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeSymbol;
            // Handle record type request body
            if (typeRef.typeDescriptor() instanceof RecordTypeSymbol) {
                // Handel typeInclusions with allOf type binding
                RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeRef.typeDescriptor();
                List<TypeSymbol> typeInclusions = recordTypeSymbol.typeInclusions();
                Map<String, RecordFieldSymbol> rfields = recordTypeSymbol.fieldDescriptors();
                HashSet<String> unionKeys = new HashSet(rfields.keySet());
                if (typeInclusions.isEmpty()) {
                    // Handle like object
                    mapReferenceNodeToObjectSchema(recordNode, schema, componentName, rfields);
                } else {
                    mapTypeInclusionToAllOfSchema(recordNode, schema, componentName, typeInclusions, rfields,
                            unionKeys);
                }
            }
        }
    }

    /**
     * This function is to map the ballerina typeInclusion to OAS allOf composedSchema.
     */
    private void mapTypeInclusionToAllOfSchema(SimpleNameReferenceNode recordNode, Map<String, Schema> schema,
                                               String componentName, List<TypeSymbol> typeInclusions, Map<String,
            RecordFieldSymbol> rfields, HashSet<String> unionKeys) {

        // Map to allOF need to check the status code inclusion there
        ComposedSchema allOfSchema = new ComposedSchema();
        // Set schema
        List<Schema> allOfSchemaList = new ArrayList<>();
        for (TypeSymbol typeInclusion: typeInclusions) {
            Schema<?> referenceSchema = new Schema();
            String typeInclusionName = typeInclusion.getName().orElseThrow();
            referenceSchema.set$ref(typeInclusionName);
            allOfSchemaList.add(referenceSchema);
            if (typeInclusion.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) typeInclusion;
                if (typeRecord.typeDescriptor() instanceof RecordTypeSymbol) {
                    RecordTypeSymbol typeInclusionRecord = (RecordTypeSymbol) typeRecord.typeDescriptor();
                    Map<String, RecordFieldSymbol> tInFields = typeInclusionRecord.fieldDescriptors();
                    unionKeys.addAll(tInFields.keySet());
                    unionKeys.removeAll(tInFields.keySet());
                    mapReferenceNodeToObjectSchema(recordNode, schema, typeInclusionName, tInFields);
                    //Update the schema value
                    schema = this.components.getSchemas();
                }
            }
        }
        Map<String, RecordFieldSymbol> filteredField =
                rfields.entrySet().stream().filter(field -> unionKeys.stream().allMatch(key ->
                        field.getKey().equals(key))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        ObjectSchema objectSchema = mapReferenceNodeToObjectSchema(recordNode, schema, null, filteredField);
        allOfSchemaList.add(objectSchema);
        allOfSchema.setAllOf(allOfSchemaList);
        if (schema != null && !schema.containsKey(componentName)) {
            //Set properties for the schema
            schema.put(componentName, allOfSchema);
            this.components.setSchemas(schema);
        } else if (schema == null) {
            schema = new HashMap<>();
            schema.put(componentName, allOfSchema);
            this.components.setSchemas(schema);
        }
    }

    /**
     * This function is to map ballerina record type symbol to OAS objectSchema.
     */
    private ObjectSchema mapReferenceNodeToObjectSchema(SimpleNameReferenceNode recordNode, Map<String, Schema> schema,
                                                        String componentName, Map<String, RecordFieldSymbol> rfields) {
        ObjectSchema componentSchema = new ObjectSchema();
        Map<String, Schema> schemaProperties = new HashMap<>();
        for (Map.Entry<String, RecordFieldSymbol> field: rfields.entrySet()) {
            String type = field.getValue().typeDescriptor().typeKind().toString().toLowerCase(Locale.ENGLISH);
            Schema property = ConverterUtils.getOpenApiSchema(type);
            if (type.equals(Constants.TYPE_REFERENCE)) {
                property.set$ref(field.getValue().typeDescriptor().getName().orElseThrow().trim());
                TypeSymbol recordVariable =  field.getValue().typeDescriptor();
                TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) recordVariable;
                handleRecordNode(recordNode, schema, typeRecord);
                schema = components.getSchemas();
            }
            if (property instanceof ArraySchema) {
                mapArrayToArraySchema(recordNode, schema, field.getValue(), (ArraySchema) property);
                schema = components.getSchemas();
            }
            schemaProperties.put(field.getKey(), property);
        }
        componentSchema.setProperties(schemaProperties);
        if (componentName != null && schema != null && !schema.containsKey(componentName)) {
            //Set properties for the schema
            schema.put(componentName, componentSchema);
            this.components.setSchemas(schema);
        } else if (schema == null && componentName != null) {
            schema = new HashMap<>();
            schema.put(componentName, componentSchema);
            this.components.setSchemas(schema);
        }
        return componentSchema;
    }

    /**
     * Generate arraySchema for ballerina record  as array type.
     */
    private void mapArrayToArraySchema(SimpleNameReferenceNode recordNode, Map<String, Schema> schema,
                                       RecordFieldSymbol field, ArraySchema property) {

        TypeSymbol symbol = field.typeDescriptor();
        int arrayDimensions = 0;
        while (symbol instanceof ArrayTypeSymbol) {
            arrayDimensions = arrayDimensions + 1;
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            symbol = arrayTypeSymbol.memberTypeDescriptor();
        }
        //handle record field has nested record array type ex: Tag[] tags
        Schema symbolProperty  = ConverterUtils.getOpenApiSchema(symbol.typeKind().getName());
        //Set the record model to the definition
        if (symbol.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
            symbolProperty.set$ref(symbol.getName().orElseThrow().trim());
            TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) symbol;
            handleRecordNode(recordNode, schema, typeRecord);
        }
        //Handle nested array type
        if (arrayDimensions > 1) {
            property.setItems(handleArray(arrayDimensions - 1, symbolProperty, new ArraySchema()));
        } else {
            property.setItems(symbolProperty);
        }
    }

    /**
     * Handle nested array.
     */
    private ArraySchema handleArray(int arrayDimensions, Schema property, ArraySchema arrayProperty) {
        if (arrayDimensions > 1) {
            ArraySchema narray = new ArraySchema();
            arrayProperty.setItems(handleArray(arrayDimensions - 1, property,  narray));
        } else if (arrayDimensions == 1) {
            arrayProperty.setItems(property);
        }
        return arrayProperty;
    }

}
