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
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.openapi.converter.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.converter.diagnostic.IncompatibleResourceDiagnostic;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.converter.Constants.DOUBLE;
import static io.ballerina.openapi.converter.Constants.FLOAT;

/**
 * This util class for processing the mapping in between ballerina record and openAPI object schema.
 *
 * @since 2.0.0
 */
public class OpenAPIComponentMapper {
    private final Components components;
    private final List<OpenAPIConverterDiagnostic> diagnostics;


    public OpenAPIComponentMapper(Components components) {
         this.components = components;
         this.diagnostics = new ArrayList<>();
    }

    public List<OpenAPIConverterDiagnostic> getDiagnostics() {
        return diagnostics;
    }
    /**
     * This function for doing the mapping with ballerina record to object schema.
     *
     * @param schema         Map of current schemas
     * @param typeSymbol     Record Name as a TypeSymbol
     */
    public void createComponentSchema(Map<String, Schema> schema, TypeSymbol typeSymbol) {
        if (schema == null) {
            schema = new HashMap<>();
        }
        // Getting main record description
        String componentName = ConverterCommonUtils.unescapeIdentifier(typeSymbol.getName().orElseThrow().trim());
        Map<String, String> apiDocs = getRecordFieldsAPIDocsMap((TypeReferenceTypeSymbol) typeSymbol, componentName);
        String typeDoc = null;
        if (apiDocs.size() > 0) {
            typeDoc = apiDocs.get(typeSymbol.getName().get());
        }
        TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeSymbol;
        TypeSymbol type = typeRef.typeDescriptor();
        // Handle record type request body
        switch (type.typeKind()) {
            case RECORD:
                // Handle typeInclusions with allOf type binding
                handleRecordTypeSymbol((RecordTypeSymbol) type, schema, componentName, apiDocs);
                break;
            case INTERSECTION:
                List<TypeSymbol> typeSymbols = ((IntersectionTypeSymbol) type).memberTypeDescriptors();
                for (TypeSymbol symbol: typeSymbols) {
                    if (!(symbol instanceof RecordTypeSymbol)) {
                        continue;
                    }
                    handleRecordTypeSymbol((RecordTypeSymbol) symbol, schema, componentName, apiDocs);
                }
                break;
            case STRING:
                schema.put(componentName, new StringSchema().description(typeDoc));
                components.setSchemas(schema);
                break;
            case INT:
                schema.put(componentName, new IntegerSchema().description(typeDoc));
                components.setSchemas(schema);
                break;
            case DECIMAL:
                schema.put(componentName, new NumberSchema().format(DOUBLE).description(typeDoc));
                components.setSchemas(schema);
                break;
            case FLOAT:
                schema.put(componentName, new NumberSchema().format(FLOAT).description(typeDoc));
                components.setSchemas(schema);
                break;
            case ARRAY:
                ArraySchema arraySchema = mapArrayToArraySchema(schema, type, componentName);
                schema.put(componentName, arraySchema.description(typeDoc));
                components.setSchemas(schema);
                break;
            case UNION:
                Schema unionSchema = handleUnionType((UnionTypeSymbol) type, new Schema<>(), componentName);
                schema.put(componentName, unionSchema.description(typeDoc));
                components.setSchemas(schema);
                break;
            case MAP:
                MapTypeSymbol mapTypeSymbol = (MapTypeSymbol) type;
                TypeDescKind typeDescKind = mapTypeSymbol.typeParam().typeKind();
                Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(typeDescKind.getName());
                schema.put(componentName, new ObjectSchema().additionalProperties(openApiSchema).description(typeDoc));
                Map<String, Schema> schemas = components.getSchemas();
                if (schemas != null) {
                    schemas.putAll(schema);
                } else {
                    components.setSchemas(schema);
                }
                break;
            default:
                // Diagnostic for currently unsupported data types.
                DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_114;
                IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage,
                        typeRef.getLocation().get(), type.typeKind().getName());
                diagnostics.add(error);
                break;
        }
    }

    private void handleRecordTypeSymbol(RecordTypeSymbol recordTypeSymbol, Map<String, Schema> schema,
                                        String componentName, Map<String, String> apiDocs) {
        // Handle typeInclusions with allOf type binding
        List<TypeSymbol> typeInclusions = recordTypeSymbol.typeInclusions();
        Map<String, RecordFieldSymbol> rfields = recordTypeSymbol.fieldDescriptors();
        HashSet<String> unionKeys = new HashSet<>(rfields.keySet());
        if (typeInclusions.isEmpty()) {
            generateObjectSchemaFromRecordFields(schema, componentName, rfields, apiDocs);
        } else {
            mapTypeInclusionToAllOfSchema(schema, componentName, typeInclusions, rfields, unionKeys, apiDocs);
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
            apiDocs.put(componentName, description.get().trim());
        }
        // Record field apidoc mapping
        TypeReferenceTypeSymbol recordTypeReference = typeSymbol;
        TypeDefinitionSymbol recordTypeDefinitionSymbol = (TypeDefinitionSymbol) ((recordTypeReference).definition());
        if (recordTypeDefinitionSymbol.typeDescriptor() instanceof RecordTypeSymbol) {
            RecordTypeSymbol recordType = (RecordTypeSymbol) recordTypeDefinitionSymbol.typeDescriptor();
            Map<String, RecordFieldSymbol> recordFieldSymbols = recordType.fieldDescriptors();
            for (Map.Entry<String , RecordFieldSymbol> fields: recordFieldSymbols.entrySet()) {
                Optional<Documentation> fieldDoc = ((Documentable) fields.getValue()).documentation();
                if (fieldDoc.isPresent() && fieldDoc.get().description().isPresent()) {
                    apiDocs.put(ConverterCommonUtils.unescapeIdentifier(fields.getKey()),
                            fieldDoc.get().description().get());
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
            referenceSchema.set$ref(ConverterCommonUtils.unescapeIdentifier(typeInclusionName));
            allOfSchemaList.add(referenceSchema);
            if (typeInclusion.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) typeInclusion;
                if (typeRecord.typeDescriptor() instanceof RecordTypeSymbol) {
                    RecordTypeSymbol typeInclusionRecord = (RecordTypeSymbol) typeRecord.typeDescriptor();
                    Map<String, RecordFieldSymbol> tInFields = typeInclusionRecord.fieldDescriptors();
                    unionKeys.addAll(tInFields.keySet());
                    unionKeys.removeAll(tInFields.keySet());
                    generateObjectSchemaFromRecordFields(schema, typeInclusionName, tInFields, apiDocs);
                    // Update the schema value
                    schema = this.components.getSchemas();
                }
            }
        }
        Map<String, RecordFieldSymbol> filteredField = new LinkedHashMap<>();
        rfields.forEach((key1, value) -> unionKeys.stream().filter(key ->
                ConverterCommonUtils.unescapeIdentifier(key1.trim()).
                        equals(ConverterCommonUtils.unescapeIdentifier(key))).forEach(key ->
                filteredField.put(ConverterCommonUtils.unescapeIdentifier(key1), value)));
        ObjectSchema objectSchema = generateObjectSchemaFromRecordFields(schema, null, filteredField, apiDocs);
        allOfSchemaList.add(objectSchema);
        allOfSchema.setAllOf(allOfSchemaList);
        if (schema != null && !schema.containsKey(componentName)) {
            // Set properties for the schema
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
            String fieldName = ConverterCommonUtils.unescapeIdentifier(field.getKey().trim());
            if (!field.getValue().isOptional()) {
                required.add(fieldName);
            }
            String type = field.getValue().typeDescriptor().typeKind().toString().toLowerCase(Locale.ENGLISH);
            Schema property = ConverterCommonUtils.getOpenApiSchema(type);
            if (field.getValue().typeDescriptor().typeKind() == TypeDescKind.TYPE_REFERENCE) {
                TypeReferenceTypeSymbol typeReference = (TypeReferenceTypeSymbol) field.getValue().typeDescriptor();
                property = handleTypeReference(schema, typeReference, property, isSameRecord(componentName,
                        typeReference));
                schema = components.getSchemas();
            } else if (field.getValue().typeDescriptor().typeKind() == TypeDescKind.UNION) {
                property = handleUnionType((UnionTypeSymbol) field.getValue().typeDescriptor(), property,
                        componentName);
                schema = components.getSchemas();
            }
            if (property instanceof ArraySchema) {
                Boolean nullable = property.getNullable();
                property = mapArrayToArraySchema(schema, field.getValue().typeDescriptor(), componentName);
                property.setNullable(nullable);
                schema = components.getSchemas();
            }
            // Add API documentation for record field
            if (apiDocs.containsKey(fieldName)) {
                property.setDescription(apiDocs.get(fieldName));
            }
            schemaProperties.put(fieldName, property);
        }
        componentSchema.setProperties(schemaProperties);
        componentSchema.setRequired(required);
        if (componentName != null && schema != null && !schema.containsKey(componentName)) {
            // Set properties for the schema
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
                                       Schema property, boolean isCyclicRecord) {
        if (typeReferenceSymbol.definition().kind() == SymbolKind.ENUM) {
            EnumSymbol enumSymbol = (EnumSymbol) typeReferenceSymbol.definition();
            property = mapEnumValues(enumSymbol);
        } else {
            property.set$ref(ConverterCommonUtils.unescapeIdentifier(
                    typeReferenceSymbol.getName().orElseThrow().trim()));
            if (!isCyclicRecord) {
                createComponentSchema(schema, typeReferenceSymbol);
            }
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
    private Schema handleUnionType(UnionTypeSymbol unionType, Schema property, String parentComponentName) {
        List<TypeSymbol> unionTypes = unionType.memberTypeDescriptors();
        List<Schema> properties = new ArrayList<>();
        boolean nullable = false;
        for (TypeSymbol union: unionTypes) {
            if (union.typeKind() == TypeDescKind.NIL) {
                nullable = true;
            } else if (union.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                property = ConverterCommonUtils.getOpenApiSchema(union.typeKind().getName().trim());
                TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) union;
                property = handleTypeReference(this.components.getSchemas(), typeReferenceTypeSymbol, property,
                        isSameRecord(parentComponentName, typeReferenceTypeSymbol));
                properties.add(property);
                // TODO: uncomment after fixing ballerina lang union type handling issue
            } else if (union.typeKind() == TypeDescKind.UNION) {
                property = handleUnionType((UnionTypeSymbol) union, property, parentComponentName);
                properties.add(property);
            } else if (union.typeKind() == TypeDescKind.ARRAY) {
                property = mapArrayToArraySchema(components.getSchemas(), union, parentComponentName);
                properties.add(property);
            } else if (union.typeKind() == TypeDescKind.MAP) {
                MapTypeSymbol mapTypeSymbol = (MapTypeSymbol) union;
                TypeDescKind typeDescKind = mapTypeSymbol.typeParam().typeKind();
                Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(typeDescKind.getName());
                property = new ObjectSchema().additionalProperties(openApiSchema);
                properties.add(property);
                Map<String, Schema> schemas = components.getSchemas();
                if (schemas != null) {
                    schemas.put(parentComponentName, property);;
                } else {
                    Map<String, Schema> schema = new HashMap<>();
                    schema.put(parentComponentName, property);
                    components.setSchemas(schema);
                }
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

    private boolean isSameRecord(String parentComponentName, TypeReferenceTypeSymbol typeReferenceTypeSymbol) {
        if (parentComponentName == null) {
            return false;
        }
        return typeReferenceTypeSymbol.getName().isPresent() &&
                parentComponentName.equals(typeReferenceTypeSymbol.getName().get().trim());
    }

    /**
     * This function generate oneOf composed schema for record fields.
     */
    private Schema generateOneOfSchema(Schema property, List<Schema> properties) {
        boolean isTypeReference = properties.size() == 1 && properties.get(0).get$ref() == null;
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
                // TODO: this temp fix will remove after fixing this issue in ballerina lang:
                // https://github.com/ballerina-platform/ballerina-lang/issues/35783
                enums.add(enumMember.typeDescriptor().signature().replaceAll("\"", ""));
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
    private ArraySchema mapArrayToArraySchema(Map<String, Schema> schema, TypeSymbol symbol,
                                       String componentName) {
        ArraySchema property = new ArraySchema();
        int arrayDimensions = 0;
        while (symbol instanceof ArrayTypeSymbol) {
            arrayDimensions = arrayDimensions + 1;
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            symbol = arrayTypeSymbol.memberTypeDescriptor();
        }
        // Handle record fields have reference record array type (ex: Tag[] tags)
        Schema symbolProperty  = ConverterCommonUtils.getOpenApiSchema(symbol.typeKind().getName());
        // Handle record fields have union type array (ex: string[]? name)
        if (symbol.typeKind() == TypeDescKind.UNION) {
            symbolProperty = getSchemaForUnionType((UnionTypeSymbol) symbol, symbolProperty, componentName, schema);
        }
        // Set the record model to the definition
        if (symbol.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
            symbolProperty = getSchemaForTypeReferenceSymbol(symbol, symbolProperty, componentName, schema);
        }
        // Handle nested array type
        if (arrayDimensions > 1) {
            property.setItems(handleArray(arrayDimensions - 1, symbolProperty, new ArraySchema()));
        } else {
            property.setItems(symbolProperty);
        }
        return property;
    }

    /**
     * This function is used to map union type of BUNION type (ex: string[]? name).
     *
     * TODO: Map for different array type unions (ex:float|int[] ids, float|int[]? ids)
     * `string[]? name` here it takes union member types as array and nil,fix should do with array type and map to
     * oneOf OAS.
     */
    private Schema getSchemaForUnionType(UnionTypeSymbol symbol, Schema symbolProperty, String componentName,
                                         Map<String, Schema> schema) {
        List<TypeSymbol> typeSymbols = symbol.userSpecifiedMemberTypes();
        for (TypeSymbol typeSymbol: typeSymbols) {
            if (typeSymbol.typeKind() == TypeDescKind.ARRAY) {
                TypeSymbol arrayType = ((ArrayTypeSymbol) typeSymbol).memberTypeDescriptor();
                // Set the record model to the definition
                if (arrayType.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                    symbolProperty = getSchemaForTypeReferenceSymbol(arrayType, symbolProperty, componentName, schema);
                } else {
                    symbolProperty = ConverterCommonUtils.getOpenApiSchema(arrayType.typeKind().getName());
                }
            } else if (typeSymbol.typeKind() != TypeDescKind.NIL) {
                symbolProperty = ConverterCommonUtils.getOpenApiSchema(typeSymbol.typeKind().getName());
            }
        }
        return symbolProperty;
    }

    /**
     * This util function is to handle the type reference symbol is record type or enum type.
     */
    private Schema getSchemaForTypeReferenceSymbol(TypeSymbol arrayType, Schema symbolProperty, String componentName,
                                                   Map<String, Schema> schema) {

        if (((TypeReferenceTypeSymbol) arrayType).definition().kind() == SymbolKind.ENUM) {
            TypeReferenceTypeSymbol typeRefEnum = (TypeReferenceTypeSymbol) arrayType;
            EnumSymbol enumSymbol = (EnumSymbol) typeRefEnum.definition();
            symbolProperty = mapEnumValues(enumSymbol);
        } else {
            symbolProperty.set$ref(ConverterCommonUtils.unescapeIdentifier(
                    arrayType.getName().orElseThrow().trim()));
            TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) arrayType;
            if (!isSameRecord(componentName, typeRecord)) {
                createComponentSchema(schema, typeRecord);
            }
        }
        return symbolProperty;
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
