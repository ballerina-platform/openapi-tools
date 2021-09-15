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
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.EnumSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
     * @param schema         Map of current schemas
     * @param typeSymbol     Record Name as a TypeSymbol
     */
    public void createComponentSchema(Map<String, Schema> schema, TypeSymbol typeSymbol) {
        // Getting main record description
        String componentName = typeSymbol.getName().orElseThrow().trim();
        Map<String, String> apiDocs = getRecordFieldsAPIDocsMap((TypeReferenceTypeSymbol) typeSymbol, componentName);
        TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeSymbol;
        // Handle record type request body
        if (typeRef.typeDescriptor() instanceof RecordTypeSymbol) {
            // Handle typeInclusions with allOf type binding
            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeRef.typeDescriptor();
            List<TypeSymbol> typeInclusions = recordTypeSymbol.typeInclusions();
            Map<String, RecordFieldSymbol> rfields = recordTypeSymbol.fieldDescriptors();
            HashSet<String> unionKeys = new HashSet<>(rfields.keySet());
            if (typeInclusions.isEmpty()) {
                // Handle like object
                generateObjectSchemaFromRecordFields(schema, componentName, rfields, apiDocs);
            } else {
                mapTypeInclusionToAllOfSchema(schema, componentName, typeInclusions, rfields, unionKeys, apiDocs);
            }
        }
    }

    /**
     * Creating API docs related to given record fields.
     */
    private Map<String, String> getRecordFieldsAPIDocsMap(TypeReferenceTypeSymbol typeSymbol, String componentName) {

        Map<String, String> apiDocs =  new LinkedHashMap<>();
        Symbol recordSymbol = typeSymbol.definition();
        Optional<Documentation> documentation = ((Documentable) recordSymbol).documentation();
        if (documentation.isPresent() && documentation.get().description().isPresent()) {
            Optional<String> description = (documentation.get().description());
            apiDocs.put(componentName, description.get());
        }
        // record field apidoc
        TypeReferenceTypeSymbol recordTypeReference = typeSymbol;
        TypeDefinitionSymbol recordTypeDefinitionSymbol = (TypeDefinitionSymbol) ((recordTypeReference).definition());
        if (recordTypeDefinitionSymbol.typeDescriptor() instanceof RecordTypeSymbol) {
            RecordTypeSymbol recordType = (RecordTypeSymbol) recordTypeDefinitionSymbol.typeDescriptor();
            Map<String, RecordFieldSymbol> recordFieldSymbols = recordType.fieldDescriptors();
            for (Map.Entry<String , RecordFieldSymbol> fields: recordFieldSymbols.entrySet()) {
                Optional<Documentation> fieldDoc = ((Documentable) fields.getValue()).documentation();
                if (fieldDoc.isPresent() && fieldDoc.get().description().isPresent()) {
                    apiDocs.put(fields.getKey(), fieldDoc.get().description().get());
                }
            }
        }
        return apiDocs;
    }

    /**
     * This function is to map the ballerina typeInclusion to OAS allOf composedSchema.
     */
    private void mapTypeInclusionToAllOfSchema(Map<String, Schema> schema,
                                               String componentName, List<TypeSymbol> typeInclusions, Map<String,
            RecordFieldSymbol> rfields, HashSet<String> unionKeys, Map<String, String> apiDocs) {

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
                    generateObjectSchemaFromRecordFields(schema, typeInclusionName, tInFields, apiDocs);
                    //Update the schema value
                    schema = this.components.getSchemas();
                }
            }
        }
        Map<String, RecordFieldSymbol> filteredField = new LinkedHashMap<>();
        rfields.forEach((key1, value) -> unionKeys.stream().filter(key -> key1.trim().equals(key)).forEach(key ->
                filteredField.put(key1, value)));
        ObjectSchema objectSchema = generateObjectSchemaFromRecordFields(schema, null, filteredField, apiDocs);
        allOfSchemaList.add(objectSchema);
        allOfSchema.setAllOf(allOfSchemaList);
        if (schema != null && !schema.containsKey(componentName)) {
            //Set properties for the schema
            schema.put(componentName, allOfSchema);
            this.components.setSchemas(schema);
        } else if (schema == null) {
            schema = new LinkedHashMap<>();
            schema.put(componentName, allOfSchema);
            this.components.setSchemas(schema);
        }
    }

    /**
     * This function is to map ballerina record type symbol to OAS objectSchema.
     */
    private ObjectSchema generateObjectSchemaFromRecordFields(Map<String, Schema> schema,
                                                              String componentName, Map<String,
                                                              RecordFieldSymbol> rfields,
                                                              Map<String, String> apiDocs) {
        ObjectSchema componentSchema = new ObjectSchema();
        List<String> required = new ArrayList<>();
        componentSchema.setDescription(apiDocs.get(componentName));
        Map<String, Schema> schemaProperties = new LinkedHashMap<>();
        for (Map.Entry<String, RecordFieldSymbol> field: rfields.entrySet()) {
            if (!field.getValue().isOptional()) {
                required.add(field.getKey().trim());
            }
            String type = field.getValue().typeDescriptor().typeKind().toString().toLowerCase(Locale.ENGLISH);
            Schema property = ConverterCommonUtils.getOpenApiSchema(type);
            if (field.getValue().typeDescriptor().typeKind() == TypeDescKind.TYPE_REFERENCE) {
                TypeReferenceTypeSymbol typeReference = (TypeReferenceTypeSymbol) field.getValue().typeDescriptor();
                property = handleTypeReference(schema, typeReference, property);
                schema = components.getSchemas();
            } else if (field.getValue().typeDescriptor().typeKind() == TypeDescKind.UNION) {
                property = handleUnionType((UnionTypeSymbol) field.getValue().typeDescriptor(), property);
                schema = components.getSchemas();
            }
            if (property instanceof ArraySchema) {
                mapArrayToArraySchema(schema, field.getValue(), (ArraySchema) property);
                schema = components.getSchemas();
            }
            // Add API documentation for record field
            if (apiDocs.containsKey(field.getKey().trim())) {
                property.setDescription(apiDocs.get(field.getKey().trim()));
            }
            schemaProperties.put(field.getKey(), property);
        }
        componentSchema.setProperties(schemaProperties);
        componentSchema.setRequired(required);
        if (componentName != null && schema != null && !schema.containsKey(componentName)) {
            //Set properties for the schema
            schema.put(componentName, componentSchema);
            this.components.setSchemas(schema);
        } else if (schema == null && componentName != null) {
            schema = new LinkedHashMap<>();
            schema.put(componentName, componentSchema);
            this.components.setSchemas(schema);
        }
        return componentSchema;
    }

    /**
     * This function uses to handle the field datatype has TypeReference(ex: Record or Enum).
     */
    private Schema handleTypeReference(Map<String, Schema> schema, TypeReferenceTypeSymbol typeReferenceSymbol,
                                       Schema property) {
        if (typeReferenceSymbol.definition().kind() == SymbolKind.ENUM) {
            EnumSymbol enumSymbol = (EnumSymbol) typeReferenceSymbol.definition();
            property = mapEnumValues(enumSymbol);
        } else {
            property.set$ref(typeReferenceSymbol.getName().orElseThrow().trim());
            createComponentSchema(schema, typeReferenceSymbol);
        }
        return property;
    }

    /**
     * This function uses to generate schema when field has union type as data type.
     * <pre>
     *     type Pet record {
     *         Dog|Cat type;
     *     };
     * </pre>
     */
    private Schema handleUnionType(UnionTypeSymbol unionType, Schema property) {
        List<TypeSymbol> unionTypes = unionType.userSpecifiedMemberTypes();
        //Set array size to 4 by assuming union type can have max 4 types.
        List<Schema> properties = new ArrayList<>(4);
        boolean nullable = false;
        for (TypeSymbol union: unionTypes) {
            if (union.typeKind() == TypeDescKind.NIL) {
                nullable = true;
            } else if (union.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                property = ConverterCommonUtils.getOpenApiSchema(union.typeKind().getName().trim());
                TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) union;
                property = handleTypeReference(this.components.getSchemas(), typeReferenceTypeSymbol, property);
                properties.add(property);
                // commented due to known issue in ballerina lang union type handling
//            } else if (union.typeKind() == TypeDescKind.UNION) {
//                property = handleUnionType((UnionTypeSymbol) union, property, properties);
//                properties.add(property);
            } else {
                property = ConverterCommonUtils.getOpenApiSchema(union.typeKind().getName().trim());
                properties.add(property);
            }
        }

        property = generateOneOfSchema(property, properties);

        if (nullable) {
            property.setNullable(true);
        }
        return property;
    }

    /**
     * This function generate oneOf composed schema for record fields.
     */
    private Schema generateOneOfSchema(Schema property, List<Schema> properties) {
        boolean isTypeReference = (properties.size() == 1) && (properties.get(0).get$ref() == null);
        if ((properties.size() == 1) && (properties.get(0).get$ref() == null)) {
            isTypeReference = true;
        }
        if (!isTypeReference) {
            ComposedSchema oneOf = new ComposedSchema();
            oneOf.setOneOf(properties);
            property = oneOf;
        }
        return property;
    }

    private Schema mapEnumValues(EnumSymbol enumSymbol) {

        Schema property;
        property = new StringSchema();
        List<String> enums = new ArrayList<>();
        List<ConstantSymbol> enumMembers = enumSymbol.members();
        for (ConstantSymbol enumMember : enumMembers) {
            if (enumMember.typeDescriptor().typeKind() == TypeDescKind.SINGLETON) {
                enums.add(enumMember.typeDescriptor().signature());
            } else {
                enums.add(enumMember.constValue().toString().trim());
            }
        }
        property.setEnum(enums);
        return property;
    }

    /**
     * Generate arraySchema for ballerina record  as array type.
     */
    private void mapArrayToArraySchema(Map<String, Schema> schema, RecordFieldSymbol field, ArraySchema property) {

        TypeSymbol symbol = field.typeDescriptor();
        int arrayDimensions = 0;
        while (symbol instanceof ArrayTypeSymbol) {
            arrayDimensions = arrayDimensions + 1;
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            symbol = arrayTypeSymbol.memberTypeDescriptor();
        }
        //handle record field has nested record array type ex: Tag[] tags
        Schema symbolProperty  = ConverterCommonUtils.getOpenApiSchema(symbol.typeKind().getName());
        //Set the record model to the definition
        if (symbol.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
            if (((TypeReferenceTypeSymbol) symbol).definition().kind() == SymbolKind.ENUM) {
                TypeReferenceTypeSymbol typeRefEnum = (TypeReferenceTypeSymbol) symbol;
                EnumSymbol enumSymbol = (EnumSymbol) typeRefEnum.definition();
                symbolProperty = mapEnumValues(enumSymbol);
            } else {
                symbolProperty.set$ref(symbol.getName().orElseThrow().trim());
                TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) symbol;
                createComponentSchema(schema, typeRecord);
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
