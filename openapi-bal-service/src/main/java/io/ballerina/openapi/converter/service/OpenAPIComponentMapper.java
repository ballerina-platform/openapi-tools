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
import io.ballerina.compiler.api.symbols.ReadonlyTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.IntersectionTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.converter.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.converter.diagnostic.ExceptionDiagnostic;
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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.converter.Constants.DOUBLE;
import static io.ballerina.openapi.converter.Constants.FLOAT;
import static io.ballerina.openapi.converter.Constants.HTTP;
import static io.ballerina.openapi.converter.Constants.HTTP_CODES;
import static io.ballerina.openapi.converter.Constants.REGEX_INTERPOLATION_PATTERN;
import static io.ballerina.openapi.converter.Constants.DATE_CONSTRAINT_ANNOTATION;

/**
 * This util class for processing the mapping in between ballerina record and openAPI object schema.
 *
 * @since 2.0.0
 */
public class OpenAPIComponentMapper {
    private final Components components;
    private final List<OpenAPIConverterDiagnostic> diagnostics;
    private final HashSet<String> visitedTypeDefinitionNames = new HashSet<>();
    private final Set<TypeDefinitionNode> typeDefinitionNodes;

    public OpenAPIComponentMapper(Components components, ModuleMemberVisitor moduleMemberVisitor) {
         this.components = components;
         this.diagnostics = new ArrayList<>();
         this.typeDefinitionNodes = moduleMemberVisitor.getTypeDefinitionNodes();
    }

    public List<OpenAPIConverterDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    /**
     * This function for doing the mapping with ballerina record to object schema.
     *
     * @param schema     Map of current schemas
     * @param typeSymbol Record Name as a TypeSymbol
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
        if (type.typeKind() == TypeDescKind.INTERSECTION) {
            type = excludeReadonlyIfPresent(type);
        }

        //Access module part node for finding the node to given typeSymbol.
        ConstraintAnnotation.ConstraintAnnotationBuilder constraintBuilder =
                new ConstraintAnnotation.ConstraintAnnotationBuilder();
        ((TypeReferenceTypeSymbol) typeSymbol).definition().getName().ifPresent(name -> {
            for (TypeDefinitionNode typeDefinitionNode : typeDefinitionNodes) {
                if (typeDefinitionNode.typeName().text().equals(name) && typeDefinitionNode.metadata().isPresent()) {
                        extractedConstraintAnnotation(typeDefinitionNode.metadata().get(), constraintBuilder);
                }
            }
        });
        ConstraintAnnotation constraintAnnot = constraintBuilder.build();

        switch (type.typeKind()) {
            case RECORD:
                // Handle typeInclusions with allOf type binding
                handleRecordTypeSymbol((RecordTypeSymbol) type, schema, componentName, apiDocs);
                break;
            case TYPE_REFERENCE:
                schema.put(componentName, new ObjectSchema().$ref(ConverterCommonUtils.unescapeIdentifier(
                        type.getName().orElseThrow().trim())));
                components.setSchemas(schema);
                TypeReferenceTypeSymbol referredType = (TypeReferenceTypeSymbol) type;
                if (!visitedTypeDefinitionNames.contains(componentName)) {
                    visitedTypeDefinitionNames.add(componentName);
                    createComponentSchema(schema, referredType);
                }
                break;
            case STRING:
                Schema stringSchema = new StringSchema().description(typeDoc);
                setConstraintValueToSchema(constraintAnnot, stringSchema);
                schema.put(componentName, stringSchema);
                components.setSchemas(schema);
                break;
            case JSON:
            case XML:
                schema.put(componentName, new ObjectSchema().description(typeDoc));
                components.setSchemas(schema);
                break;
            case INT:
                Schema intSchema = new IntegerSchema().description(typeDoc);
                setConstraintValueToSchema(constraintAnnot, intSchema);
                schema.put(componentName, intSchema);
                components.setSchemas(schema);
                break;
            case DECIMAL:
                Schema decimalSchema = new NumberSchema().format(DOUBLE).description(typeDoc);
                setConstraintValueToSchema(constraintAnnot, decimalSchema);
                schema.put(componentName, decimalSchema);
                components.setSchemas(schema);
                break;
            case FLOAT:
                Schema floatSchema = new NumberSchema().format(FLOAT).description(typeDoc);
                setConstraintValueToSchema(constraintAnnot, floatSchema);
                schema.put(componentName, floatSchema);
                components.setSchemas(schema);
                break;
            case ARRAY:
            case TUPLE:
                ArraySchema arraySchema = mapArrayToArraySchema(schema, type, componentName);
                setConstraintValueToSchema(constraintAnnot, arraySchema.description(typeDoc));
                schema.put(componentName, arraySchema);
                components.setSchemas(schema);
                break;
            case UNION:
                if (!visitedTypeDefinitionNames.contains(componentName)) {
                    visitedTypeDefinitionNames.add(componentName);
                    if (typeRef.definition() instanceof EnumSymbol) {
                        EnumSymbol enumSymbol = (EnumSymbol) typeRef.definition();
                        Schema enumSchema = mapEnumValues(enumSymbol);
                        schema.put(componentName, enumSchema.description(typeDoc));
                    } else {
                        Schema unionSchema = handleUnionType((UnionTypeSymbol) type, new Schema<>(), componentName);
                        schema.put(componentName, unionSchema.description(typeDoc));
                    }
                    if (components.getSchemas() != null) {
                        schema.putAll(components.getSchemas());
                    }
                    components.setSchemas(schema);
                }
                break;
            case MAP:
                MapTypeSymbol mapTypeSymbol = (MapTypeSymbol) type;
                TypeSymbol typeParam = mapTypeSymbol.typeParam();

                if (typeParam.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                    TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) typeParam;
                    schema.put(componentName, new ObjectSchema().additionalProperties(new ObjectSchema()
                            .$ref(ConverterCommonUtils.unescapeIdentifier(
                                    typeReferenceTypeSymbol.getName().orElseThrow().trim()))));
                    createComponentSchema(schema, typeReferenceTypeSymbol);
                }

                if (!schema.containsKey(componentName)) {
                    TypeDescKind typeDescKind = mapTypeSymbol.typeParam().typeKind();
                    Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(typeDescKind.getName());
                    schema.put(componentName, new ObjectSchema().additionalProperties(
                            openApiSchema.getType() == null ? true : openApiSchema).description(typeDoc));
                }

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

    /**
     * Remove readonly from the type symbol.
     *
     * @param typeSymbol TypeSymbol
     * @return typeSymbol without readonly
     */
    public TypeSymbol excludeReadonlyIfPresent(TypeSymbol typeSymbol) {
        List<TypeSymbol> typeSymbols = ((IntersectionTypeSymbol) typeSymbol).memberTypeDescriptors();
        for (TypeSymbol symbol : typeSymbols) {
            if (!(symbol instanceof ReadonlyTypeSymbol)) {
                typeSymbol = symbol;
                break;
            }
        }
        return typeSymbol;
    }

    private void handleRecordTypeSymbol(RecordTypeSymbol recordTypeSymbol, Map<String, Schema> schema,
                                        String componentName, Map<String, String> apiDocs) {
        // Handle typeInclusions with allOf type binding
        visitedTypeDefinitionNames.add(componentName);
        List<TypeSymbol> typeInclusions = recordTypeSymbol.typeInclusions();
        Map<String, RecordFieldSymbol> rfields = recordTypeSymbol.fieldDescriptors();
        if (typeInclusions.isEmpty()) {
            generateObjectSchemaFromRecordFields(schema, componentName, rfields, apiDocs);
        } else {
            mapTypeInclusionToAllOfSchema(schema, componentName, recordTypeSymbol, apiDocs);
        }
    }

    /**
     * Creating API docs related to given record fields.
     */
    private Map<String, String> getRecordFieldsAPIDocsMap(TypeReferenceTypeSymbol typeSymbol, String componentName) {
        Map<String, String> apiDocs = new LinkedHashMap<>();
        Symbol recordSymbol = typeSymbol.definition();
        Optional<Documentation> documentation = ((Documentable) recordSymbol).documentation();
        if (documentation.isPresent() && documentation.get().description().isPresent()) {
            Optional<String> description = (documentation.get().description());
            apiDocs.put(componentName, description.get().trim());
        }
        // Record field apidoc mapping
        if (((typeSymbol).definition() instanceof TypeDefinitionSymbol)) {
            TypeDefinitionSymbol recordTypeDefinitionSymbol = (TypeDefinitionSymbol) ((typeSymbol).definition());
            if (recordTypeDefinitionSymbol.typeDescriptor() instanceof RecordTypeSymbol) {
                RecordTypeSymbol recordType = (RecordTypeSymbol) recordTypeDefinitionSymbol.typeDescriptor();
                Map<String, RecordFieldSymbol> recordFieldSymbols = recordType.fieldDescriptors();
                for (Map.Entry<String, RecordFieldSymbol> fields : recordFieldSymbols.entrySet()) {
                    Optional<Documentation> fieldDoc = ((Documentable) fields.getValue()).documentation();
                    if (fieldDoc.isPresent() && fieldDoc.get().description().isPresent()) {
                        apiDocs.put(ConverterCommonUtils.unescapeIdentifier(fields.getKey()),
                                fieldDoc.get().description().get());
                    }
                }
            }
        }
        return apiDocs;
    }

    /**
     * This function is to map the ballerina typeInclusion to OAS allOf composedSchema.
     */
    private void mapTypeInclusionToAllOfSchema(Map<String, Schema> schema, String componentName,
                                               RecordTypeSymbol recordTypeSymbol, Map<String, String> apiDocs) {

        List<TypeSymbol> typeInclusions = recordTypeSymbol.typeInclusions();
        Map<String, RecordFieldSymbol> recordFields = recordTypeSymbol.fieldDescriptors();
        HashSet<String> recordFieldNames = new HashSet<>(recordFields.keySet());
        // Map to allOF need to check the status code inclusion there
        ComposedSchema allOfSchema = new ComposedSchema();
        // Set schema
        List<Schema> allOfSchemaList = new ArrayList<>();
        for (TypeSymbol typeInclusion : typeInclusions) {
            Schema<?> referenceSchema = new Schema();
            String typeInclusionName = typeInclusion.getName().orElseThrow();
            referenceSchema.set$ref(ConverterCommonUtils.unescapeIdentifier(typeInclusionName));
            allOfSchemaList.add(referenceSchema);
            if (typeInclusion.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) typeInclusion;
                if (typeRecord.typeDescriptor() instanceof RecordTypeSymbol &&
                        !isSameRecord(typeInclusionName, typeRecord)) {
                    RecordTypeSymbol typeInclusionRecord = (RecordTypeSymbol) typeRecord.typeDescriptor();
                    Map<String, RecordFieldSymbol> tInFields = typeInclusionRecord.fieldDescriptors();
                    recordFieldNames.addAll(tInFields.keySet());
                    recordFieldNames.removeAll(tInFields.keySet());
                    generateObjectSchemaFromRecordFields(schema, typeInclusionName, tInFields, apiDocs);
                    // Update the schema value
                    schema = this.components.getSchemas();
                }
            }
        }
        Map<String, RecordFieldSymbol> filteredField = new LinkedHashMap<>();
        recordFields.forEach((key1, value) -> recordFieldNames.stream().filter(key ->
                ConverterCommonUtils.unescapeIdentifier(key1.trim()).
                        equals(ConverterCommonUtils.unescapeIdentifier(key))).forEach(key ->
                filteredField.put(ConverterCommonUtils.unescapeIdentifier(key1), value)));
        ObjectSchema objectSchema = generateObjectSchemaFromRecordFields(schema, null, filteredField, apiDocs);
        allOfSchemaList.add(objectSchema);
        allOfSchema.setAllOf(allOfSchemaList);
        if (schema != null && !schema.containsKey(componentName)) {
            // Set properties for the schema
            schema.put(componentName, allOfSchema);
            if (this.components.getSchemas() != null) {
                schema.putAll(this.components.getSchemas());
            }
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
                                                              String componentName,
                                                              Map<String, RecordFieldSymbol> rfields,
                                                              Map<String, String> apiDocs) {
        ObjectSchema componentSchema = new ObjectSchema();
        List<String> required = new ArrayList<>();
        componentSchema.setDescription(apiDocs.get(componentName));
        Map<String, Schema> schemaProperties = new LinkedHashMap<>();
        RecordTypeDescriptorNode recordNode = null;
        // Get the recordNode type descriptor node from given recordNode type symbol
        for (TypeDefinitionNode typeDefinitionNode : typeDefinitionNodes) {
            if (typeDefinitionNode.typeName().text().equals(componentName)) {
                if (typeDefinitionNode.typeDescriptor().kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
                    recordNode = (RecordTypeDescriptorNode) typeDefinitionNode.typeDescriptor();
                } else if (typeDefinitionNode.typeDescriptor().kind().equals(SyntaxKind.INTERSECTION_TYPE_DESC)) {
                    IntersectionTypeDescriptorNode intersecNode =
                            (IntersectionTypeDescriptorNode) typeDefinitionNode.typeDescriptor();
                    Node leftTypeDesc = intersecNode.leftTypeDesc();
                    Node rightTypeDesc = intersecNode.rightTypeDesc();
                    if (leftTypeDesc.kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
                        recordNode = (RecordTypeDescriptorNode) leftTypeDesc;
                    }
                    if (rightTypeDesc.kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
                        recordNode = (RecordTypeDescriptorNode) rightTypeDesc;
                    }
                }
            }
        }

        for (Map.Entry<String, RecordFieldSymbol> field : rfields.entrySet()) {
            ConstraintAnnotation.ConstraintAnnotationBuilder constraintBuilder =
                    new ConstraintAnnotation.ConstraintAnnotationBuilder();
            if (recordNode != null) {
                for (Node node : recordNode.fields()) {
                    if (node instanceof RecordFieldNode) {
                        RecordFieldNode fieldNode = (RecordFieldNode) node;
                        if (fieldNode.fieldName().toString().equals(field.getKey())) {
                            Optional<MetadataNode> metadata = fieldNode.metadata();
                            metadata.ifPresent(metadataNode -> extractedConstraintAnnotation(metadataNode,
                                    constraintBuilder));
                        }
                    }
                }
            }
            ConstraintAnnotation constraintAnnot = constraintBuilder.build();
            String fieldName = ConverterCommonUtils.unescapeIdentifier(field.getKey().trim());
            if (!field.getValue().isOptional()) {
                required.add(fieldName);
            }
            TypeDescKind fieldTypeKind = field.getValue().typeDescriptor().typeKind();
            String type = fieldTypeKind.toString().toLowerCase(Locale.ENGLISH);

            if (fieldTypeKind == TypeDescKind.INTERSECTION) {
                TypeSymbol readOnlyExcludedType = excludeReadonlyIfPresent(field.getValue().typeDescriptor());
                type = readOnlyExcludedType.typeKind().toString().toLowerCase(Locale.ENGLISH);
            }

            Schema property = ConverterCommonUtils.getOpenApiSchema(type);

            if (fieldTypeKind == TypeDescKind.TYPE_REFERENCE) {
                TypeReferenceTypeSymbol typeReference = (TypeReferenceTypeSymbol) field.getValue().typeDescriptor();
                property = handleTypeReference(schema, typeReference, property,
                        isSameRecord(ConverterCommonUtils.unescapeIdentifier(
                                        typeReference.definition().getName().get()), typeReference));
                schema = components.getSchemas();
            } else if (fieldTypeKind == TypeDescKind.UNION) {
                property = handleUnionType((UnionTypeSymbol) field.getValue().typeDescriptor(), property,
                        componentName);
                schema = components.getSchemas();
            } else if (fieldTypeKind == TypeDescKind.MAP) {
                MapTypeSymbol mapTypeSymbol = (MapTypeSymbol) field.getValue().typeDescriptor();
                property = handleMapType(schema, componentName, property, mapTypeSymbol);
                schema = components.getSchemas();
            }
            if (property instanceof ArraySchema && !(((ArraySchema) property).getItems() instanceof ComposedSchema)) {
                Boolean nullable = property.getNullable();
                property = mapArrayToArraySchema(schema, field.getValue().typeDescriptor(), componentName);
                property.setNullable(nullable);
                schema = components.getSchemas();
            }
            // Add API documentation for recordNode field
            if (apiDocs.containsKey(fieldName)) {
                property.setDescription(apiDocs.get(fieldName));
            }
            // Assign value to property - string, number, array
            setConstraintValueToSchema(constraintAnnot, property);
            schemaProperties.put(fieldName, property);
        }
        componentSchema.setProperties(schemaProperties);
        componentSchema.setRequired(required);
        if (componentName != null && schema != null && !schema.containsKey(componentName)) {
            // Set properties for the schema
            schema.put(componentName, componentSchema);
            if (this.components.getSchemas() != null) {
                schema.putAll(this.components.getSchemas());
            }
            this.components.setSchemas(schema);
        } else if (schema == null && componentName != null) {
            schema = new LinkedHashMap<>();
            schema.put(componentName, componentSchema);
            this.components.setSchemas(schema);
        }
        visitedTypeDefinitionNames.add(componentName);
        return componentSchema;
    }

    private Schema handleMapType(Map<String, Schema> schema, String componentName, Schema property,
                                 MapTypeSymbol mapTypeSymbol) {

        TypeDescKind typeDescKind = mapTypeSymbol.typeParam().typeKind();
        if (typeDescKind == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeReference = (TypeReferenceTypeSymbol) mapTypeSymbol.typeParam();
            Schema reference = handleTypeReference(schema, typeReference, new Schema<>(),
                    isSameRecord(componentName, typeReference));
            property = property.additionalProperties(reference);
        } else if (typeDescKind == TypeDescKind.ARRAY) {
            ArraySchema arraySchema = mapArrayToArraySchema(schema, mapTypeSymbol.typeParam(), componentName);
            property = property.additionalProperties(arraySchema);
        } else {
            Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(typeDescKind.getName());
            property = property.additionalProperties(openApiSchema.getType() == null ? true : openApiSchema);
        }
        return property;
    }

    /**
     * This function is used to handle the field datatype has TypeReference(ex: Record or Enum).
     */
    private Schema<?> handleTypeReference(Map<String, Schema> schema, TypeReferenceTypeSymbol typeReferenceSymbol,
                                          Schema<?> property, boolean isCyclicRecord) {
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
     * This function is used to generate schema when field has union type as data type.
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
        for (TypeSymbol union : unionTypes) {
            if (union.typeKind() == TypeDescKind.INTERSECTION) {
                union = excludeReadonlyIfPresent(union);
            }
            if (union.typeKind() == TypeDescKind.NIL) {
                nullable = true;
            } else if (union.typeKind() == TypeDescKind.TYPE_REFERENCE) {
                if (union.getModule().isPresent() && union.getModule().get().id().modulePrefix().equals(HTTP) &&
                        union.getName().isPresent() && HTTP_CODES.containsKey(union.getName().get())) {
                    continue;
                }
                property = ConverterCommonUtils.getOpenApiSchema(union.typeKind().getName().trim());
                TypeReferenceTypeSymbol typeReferenceTypeSymbol = (TypeReferenceTypeSymbol) union;
                property = handleTypeReference(components.getSchemas(), typeReferenceTypeSymbol, property,
                        isSameRecord(parentComponentName, typeReferenceTypeSymbol));
                visitedTypeDefinitionNames.add(typeReferenceTypeSymbol.getName().get());
                properties.add(property);
            } else if (union.typeKind() == TypeDescKind.UNION) {
                property = handleUnionType((UnionTypeSymbol) union, property, parentComponentName);
                properties.add(property);
            } else if (union.typeKind() == TypeDescKind.ARRAY || union.typeKind() == TypeDescKind.TUPLE) {
                property = mapArrayToArraySchema(components.getSchemas(), union, parentComponentName);
                properties.add(property);
            } else if (union.typeKind() == TypeDescKind.MAP) {
                MapTypeSymbol mapTypeSymbol = (MapTypeSymbol) union;
                TypeDescKind typeDescKind = mapTypeSymbol.typeParam().typeKind();
                Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(typeDescKind.getName());
                property = new ObjectSchema().additionalProperties(openApiSchema);
                properties.add(property);
                if (components.getSchemas() != null) {
                    Map<String, Schema> schemas = components.getSchemas();
                    schemas.put(parentComponentName, property);
                    components.setSchemas(schemas);
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
        return visitedTypeDefinitionNames.contains(typeReferenceTypeSymbol.getName().get().trim());
    }

    /**
     * This function generates oneOf composed schema for record fields.
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
                String signatureValue = enumMember.typeDescriptor().signature();
                if (signatureValue.startsWith("\"") && signatureValue.endsWith("\"")) {
                    signatureValue = signatureValue.substring(1, signatureValue.length() - 1);
                }
                enums.add(signatureValue);
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
        visitedTypeDefinitionNames.add(componentName);
        ArraySchema property = new ArraySchema();
        int arrayDimensions = 0;
        while (symbol instanceof ArrayTypeSymbol) {
            arrayDimensions = arrayDimensions + 1;
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            symbol = arrayTypeSymbol.memberTypeDescriptor();
        }
        // Handle record fields have reference record array type (ex: Tag[] tags)
        Schema<?> symbolProperty = ConverterCommonUtils.getOpenApiSchema(symbol.typeKind().getName());
        // Handle record fields have union type array (ex: string[]? name)
        if (symbol.typeKind() == TypeDescKind.UNION) {
            symbolProperty = getSchemaForUnionType((UnionTypeSymbol) symbol, symbolProperty, componentName, schema);
        }
        // Set the record model to the definition
        if (symbol.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
            symbolProperty = getSchemaForTypeReferenceSymbol(symbol, symbolProperty, componentName, schema);
        }
        // Handle record fields have union type array (ex: map<string>[] name)
        if (symbol.typeKind() == TypeDescKind.MAP) {
            MapTypeSymbol mapTypeSymbol = (MapTypeSymbol) symbol;
            symbolProperty = handleMapType(schema, componentName, symbolProperty, mapTypeSymbol);

            schema = components.getSchemas();
        }

        // Handle the tuple type
        if (symbol.typeKind().equals(TypeDescKind.TUPLE)) {
            // Add all the schema related to typeSymbols into the list. Then the list can be mapped into oneOf
            // type.
            TupleTypeSymbol tuple = (TupleTypeSymbol) symbol;
            List<Schema> arrayItems = new ArrayList<>();
            for (TypeSymbol typeSymbol : tuple.memberTypeDescriptors()) {
                Schema<?> openApiSchema = ConverterCommonUtils.getOpenApiSchema(typeSymbol.signature());
                // Handle type_reference type
                if (typeSymbol instanceof TypeReferenceTypeSymbol) {
                    openApiSchema.set$ref(typeSymbol.signature());
                    createComponentSchema(schema, typeSymbol);
                }
                arrayItems.add(openApiSchema);
            }
            symbolProperty = new ComposedSchema().oneOf(arrayItems);
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
     * <p>
     * TODO: Map for different array type unions (ex:float|int[] ids, float|int[]? ids)
     * `string[]? name` here it takes union member types as array and nil,fix should do with array type and map to
     * oneOf OAS.
     */
    private Schema getSchemaForUnionType(UnionTypeSymbol symbol, Schema symbolProperty, String componentName,
                                         Map<String, Schema> schema) {
        List<TypeSymbol> typeSymbols = symbol.userSpecifiedMemberTypes();
        for (TypeSymbol typeSymbol : typeSymbols) {
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
     * This util function is used to handle the type reference symbol is record type or enum type.
     */
    private Schema getSchemaForTypeReferenceSymbol(TypeSymbol referenceType, Schema symbolProperty,
                                                   String componentName, Map<String, Schema> schema) {

        if (((TypeReferenceTypeSymbol) referenceType).definition().kind() == SymbolKind.ENUM) {
            TypeReferenceTypeSymbol typeRefEnum = (TypeReferenceTypeSymbol) referenceType;
            EnumSymbol enumSymbol = (EnumSymbol) typeRefEnum.definition();
            symbolProperty = mapEnumValues(enumSymbol);
        } else {
            symbolProperty.set$ref(ConverterCommonUtils.unescapeIdentifier(
                    referenceType.getName().orElseThrow().trim()));
            TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) referenceType;
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
            arrayProperty.setItems(handleArray(arrayDimensions - 1, property, narray));
        } else if (arrayDimensions == 1) {
            arrayProperty.setItems(property);
        }
        return arrayProperty;
    }

    /**
     * This util is used to set the integer constraint values for relevant schema field.
     */
    private void setIntegerConstraintValuesToSchema(ConstraintAnnotation constraintAnnot, IntegerSchema properties) {
        BigDecimal minimum = null;
        BigDecimal maximum = null;
        if (constraintAnnot.getMinValue().isPresent()) {
            minimum = BigDecimal.valueOf(Integer.parseInt(constraintAnnot.getMinValue().get()));
        } else if (constraintAnnot.getMinValueExclusive().isPresent()) {
            minimum = BigDecimal.valueOf(Integer.parseInt(constraintAnnot.getMinValueExclusive().get()));
            properties.setExclusiveMinimum(true);
        }

        if (constraintAnnot.getMaxValue().isPresent()) {
            maximum = BigDecimal.valueOf(Integer.parseInt(constraintAnnot.getMaxValue().get()));
        } else if (constraintAnnot.getMaxValueExclusive().isPresent()) {
            maximum = BigDecimal.valueOf(Integer.parseInt(constraintAnnot.getMaxValueExclusive().get()));
            properties.setExclusiveMaximum(true);
        }
        properties.setMinimum(minimum);
        properties.setMaximum(maximum);
    }

    /**
     * This util is used to set the number (float, double) constraint values for relevant schema field.
     */
    private void setNumberConstraintValuesToSchema(ConstraintAnnotation constraintAnnot, NumberSchema properties)
                                                        throws ParseException {
        BigDecimal minimum = null;
        BigDecimal maximum = null;
        if (constraintAnnot.getMinValue().isPresent()) {
            minimum = BigDecimal.valueOf((NumberFormat.getInstance()
                    .parse(constraintAnnot.getMinValue().get()).doubleValue()));
        } else if (constraintAnnot.getMinValueExclusive().isPresent()) {
            minimum = BigDecimal.valueOf((NumberFormat.getInstance()
                    .parse(constraintAnnot.getMinValueExclusive().get()).doubleValue()));
            properties.setExclusiveMinimum(true);
        }

        if (constraintAnnot.getMaxValue().isPresent()) {
            maximum = BigDecimal.valueOf((NumberFormat.getInstance()
                    .parse(constraintAnnot.getMaxValue().get()).doubleValue()));
        } else if (constraintAnnot.getMaxValueExclusive().isPresent()) {
            maximum = BigDecimal.valueOf((NumberFormat.getInstance()
                    .parse(constraintAnnot.getMaxValueExclusive().get()).doubleValue()));
            properties.setExclusiveMaximum(true);
        }
        properties.setMinimum(minimum);
        properties.setMaximum(maximum);
    }

    /**
     * This util is used to set the string constraint values for relevant schema field.
     */
    private void setStringConstraintValuesToSchema(ConstraintAnnotation constraintAnnot, StringSchema properties) {
        if (constraintAnnot.getLength().isPresent()) {
            properties.setMinLength(Integer.valueOf(constraintAnnot.getLength().get()));
            properties.setMaxLength(Integer.valueOf(constraintAnnot.getLength().get()));
        } else {
            properties.setMaxLength(constraintAnnot.getMaxLength().isPresent() ?
                    Integer.valueOf(constraintAnnot.getMaxLength().get()) : null);
            properties.setMinLength(constraintAnnot.getMinLength().isPresent() ?
                    Integer.valueOf(constraintAnnot.getMinLength().get()) : null);
        }

        if (constraintAnnot.getPattern().isPresent()) {
            String regexPattern = constraintAnnot.getPattern().get();
            properties.setPattern(regexPattern);
        }
    }

    /**
     * This util is used to set the array constraint values for relevant schema field.
     */
    private void setArrayConstraintValuesToSchema(ConstraintAnnotation constraintAnnot, ArraySchema properties) {
        if (constraintAnnot.getLength().isPresent()) {
            properties.setMinItems(Integer.valueOf(constraintAnnot.getLength().get()));
            properties.setMaxItems(Integer.valueOf(constraintAnnot.getLength().get()));
        } else {
            properties.setMaxItems(constraintAnnot.getMaxLength().isPresent() ?
                    Integer.valueOf(constraintAnnot.getMaxLength().get()) : null);
            properties.setMinItems(constraintAnnot.getMinLength().isPresent() ?
                    Integer.valueOf(constraintAnnot.getMinLength().get()) : null);
        }
    }

    /**
     * This util is used to set the constraint values for relevant schema field.
     */
    private void setConstraintValueToSchema(ConstraintAnnotation constraintAnnot, Schema properties) {
        try {
            //Ballerina currently supports only Int, Number (Float, Decimal), String & Array constraints,
            //with plans to extend constraint support in the future.
            if (properties instanceof ArraySchema arraySchema) {
                setArrayConstraintValuesToSchema(constraintAnnot, arraySchema);
            } else if (properties instanceof StringSchema stringSchema) {
                setStringConstraintValuesToSchema(constraintAnnot, stringSchema);
            } else if (properties instanceof IntegerSchema integerSchema) {
                setIntegerConstraintValuesToSchema(constraintAnnot, integerSchema);
            } else if (properties instanceof NumberSchema numberSchema) {
                setNumberConstraintValuesToSchema(constraintAnnot, numberSchema);
            }
        } catch (ParseException parseException) {
            DiagnosticMessages error = DiagnosticMessages.OAS_CONVERTOR_114;
            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(error.getCode(),
                    error.getDescription(), null, parseException.getMessage());
            diagnostics.add(diagnostic);
        }
    }

    /**
     * This util is used to extract the annotation values in `@constraint` and store it in builder.
     */
    private void extractedConstraintAnnotation(MetadataNode metadata,
                                               ConstraintAnnotation.ConstraintAnnotationBuilder constraintBuilder) {
        NodeList<AnnotationNode> annotations = metadata.annotations();
        annotations.stream()
                .filter(this::isConstraintAnnotation)
                .filter(annotation -> annotation.annotValue().isPresent())
                .forEach(annotation -> {
                    if (isDateConstraint(annotation)) {
                        DiagnosticMessages errorMsg = DiagnosticMessages.OAS_CONVERTOR_120;
                        IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMsg,
                                annotation.location(), annotation.toString());
                        diagnostics.add(error);
                        return;
                    }
                    MappingConstructorExpressionNode annotationValue = annotation.annotValue().get();
                    annotationValue.fields().stream()
                            .filter(field -> SyntaxKind.SPECIFIC_FIELD.equals(field.kind()))
                            .forEach(field -> {
                                String name = ((SpecificFieldNode) field).fieldName().toString().trim();
                                processConstraintAnnotation((SpecificFieldNode) field, name, constraintBuilder);
                            });
                });
    }

    /**
     * This util is used to check whether an annotation is a constraint annotation.
     */
    private boolean isConstraintAnnotation(AnnotationNode annotation) {
        if (annotation.annotReference() instanceof QualifiedNameReferenceNode qualifiedNameRef) {
            return qualifiedNameRef.modulePrefix().text().equals("constraint");
        }
        return false;
    }

    /*
     * This util is used to check whether an annotation is a constraint:Date annotation.
     * Currently, we don't have support for mapping Date constraints to OAS hence we skip them.
     * {@link <a href="https://github.com/ballerina-platform/ballerina-standard-library/issues/5049">...</a>}
     * Once the above improvement is completed this method should be removed!
     */
    private boolean isDateConstraint(AnnotationNode annotation) {
        return annotation.annotReference().toString().trim().equals(DATE_CONSTRAINT_ANNOTATION);
    }

    /**
     * This util is used to process the content of a constraint annotation.
     */
    private void processConstraintAnnotation(SpecificFieldNode specificFieldNode, String fieldName,
                                             ConstraintAnnotation.ConstraintAnnotationBuilder constraintBuilder) {
        specificFieldNode.valueExpr()
                .flatMap(this::extractFieldValue)
                .ifPresent(fieldValue -> fillConstraintValue(constraintBuilder, fieldName, fieldValue));
    }

    private Optional<String> extractFieldValue(ExpressionNode exprNode) {
        SyntaxKind syntaxKind = exprNode.kind();
        switch (syntaxKind) {
            case NUMERIC_LITERAL:
                return Optional.of(exprNode.toString().trim());
            case REGEX_TEMPLATE_EXPRESSION:
                String regexContent = ((TemplateExpressionNode) exprNode).content().get(0).toString();
                if (regexContent.matches(REGEX_INTERPOLATION_PATTERN)) {
                    return Optional.of(regexContent);
                } else {
                    DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_119;
                    IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage,
                            exprNode.location(), regexContent);
                    diagnostics.add(error);
                    return Optional.empty();
                }
            case MAPPING_CONSTRUCTOR:
                return ((MappingConstructorExpressionNode) exprNode).fields().stream()
                    .filter(fieldNode -> ((SpecificFieldNode) fieldNode).fieldName().toString().trim().equals("value"))
                    .findFirst()
                    .flatMap(node -> ((SpecificFieldNode) node).valueExpr()
                           .flatMap(this::extractFieldValue));
            default:
                DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_118;
                IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage,
                        exprNode.location(), exprNode.toString());
                diagnostics.add(error);
                return Optional.empty();
        }
    }

    /**
     * This util is used to build the constraint builder with available constraint annotation field value.
     */
    private void fillConstraintValue(ConstraintAnnotation.ConstraintAnnotationBuilder constraintBuilder,
                                     String name, String constraintValue) {
        switch (name) {
            case "minValue":
                constraintBuilder.withMinValue(constraintValue);
                break;
            case "maxValue":
                constraintBuilder.withMaxValue(constraintValue);
                break;
            case "minValueExclusive":
                constraintBuilder.withMinValueExclusive(constraintValue);
                break;
            case "maxValueExclusive":
                constraintBuilder.withMaxValueExclusive(constraintValue);
                break;
            case "length":
                constraintBuilder.withLength(constraintValue);
                break;
            case "maxLength":
                constraintBuilder.withMaxLength(constraintValue);
                break;
            case "minLength":
                constraintBuilder.withMinLength(constraintValue);
                break;
            case "pattern":
                constraintBuilder.withPattern(constraintValue);
                break;
            default:
                break;
        }
    }
}
