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

package io.ballerina.openapi.generators.openapi;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * This util class for processing the mapping in between ballerina record and openAPI object schema.
 */
public class OpenAPIComponentMapper {
    private Components components;
    private SemanticModel semanticModel;
    private ConverterUtils converterUtils = new ConverterUtils();

    public OpenAPIComponentMapper(Components components, SemanticModel semanticModel) {

        this.components = components;
        this.semanticModel = semanticModel;
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
        Schema componentSchema = new Schema();
        componentSchema.setType("object");
        Map<String, Schema> schemaProperties = new HashMap<>();
        if (typeSymbol instanceof TypeReferenceTypeSymbol) {
            TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeSymbol;
            // Handle record type request body
            if (typeRef.typeDescriptor() instanceof RecordTypeSymbol) {
                //Handel typeInclusions
                RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeRef.typeDescriptor();
                List<TypeSymbol> typeSymbols = recordTypeSymbol.typeInclusions();
                if (!typeSymbols.isEmpty()) {
                    // Generate
                }

                Map<String, RecordFieldSymbol> rfields = recordTypeSymbol.fieldDescriptors();
                for (Map.Entry<String, RecordFieldSymbol> field: rfields.entrySet()) {
                    String type = field.getValue().typeDescriptor().typeKind().toString().toLowerCase(Locale.ENGLISH);
                    Schema property = converterUtils.getOpenApiSchema(type);
                    if (type.equals(Constants.TYPE_REFERENCE) && property.get$ref().
                            equals("#/components/schemas/true")) {
                        property.set$ref(field.getValue().typeDescriptor().getName().orElseThrow().trim());
                        Optional<TypeSymbol> recordSymbol = semanticModel.type(field.getValue().location().lineRange());
                        TypeSymbol recordVariable =  recordSymbol.orElseThrow();
                        if (recordVariable.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                            TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) recordVariable;
                            handleRecordNode(recordNode, schema, typeRecord);
                            schema = components.getSchemas();
                        }
                    }
                    if (property instanceof ArraySchema) {
                        setArraySchema(recordNode, schema, field.getValue(), (ArraySchema) property);
                        schema = components.getSchemas();
                    }
                    schemaProperties.put(field.getKey(), property);
                }
                componentSchema.setProperties(schemaProperties);
            }
        }
        if (schema != null && !schema.containsKey(componentName)) {
            //Set properties for the schema
            schema.put(componentName, componentSchema);
            this.components.setSchemas(schema);
        } else if (schema == null) {
            schema = new HashMap<>();
            schema.put(componentName, componentSchema);
            this.components.setSchemas(schema);
        }
    }

    /**
     * Generate arraySchema for ballerina record  as array type.
     */
    private void setArraySchema(SimpleNameReferenceNode recordNode, Map<String, Schema> schema,
                                  RecordFieldSymbol field, ArraySchema property) {

        TypeSymbol symbol = field.typeDescriptor();
        int arrayDimensions = 0;
        while (symbol instanceof ArrayTypeSymbol) {
            arrayDimensions = arrayDimensions + 1;
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            symbol = arrayTypeSymbol.memberTypeDescriptor();
        }
        //handle record field has nested record array type ex: Tag[] tags
        Schema symbolProperty  = converterUtils.getOpenApiSchema(symbol.typeKind().getName());
        if (symbolProperty.get$ref() != null && symbolProperty.get$ref().equals("#/components/schemas/true")) {
            symbolProperty.set$ref(symbol.getName().orElseThrow().trim());
            //Set the record model to the definition
            if (symbol.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) symbol;
                handleRecordNode(recordNode, schema, typeRecord);
            }
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
