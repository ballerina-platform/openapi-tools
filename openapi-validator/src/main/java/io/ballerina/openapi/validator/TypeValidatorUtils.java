/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator;

import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static io.ballerina.openapi.validator.Constants.ARRAY_BRACKETS;
import static io.ballerina.openapi.validator.Constants.BOOLEAN;
import static io.ballerina.openapi.validator.Constants.DECIMAL;
import static io.ballerina.openapi.validator.Constants.DOUBLE;
import static io.ballerina.openapi.validator.Constants.FLOAT;
import static io.ballerina.openapi.validator.Constants.INT;
import static io.ballerina.openapi.validator.Constants.NUMBER;
import static io.ballerina.openapi.validator.Constants.RECORD;
import static io.ballerina.openapi.validator.Constants.SQUARE_BRACKETS;
import static io.ballerina.openapi.validator.Constants.STRING;
import static io.ballerina.openapi.validator.ValidatorUtils.extractReferenceType;
import static io.ballerina.openapi.validator.ValidatorUtils.reportDiagnostic;

/**
 * This util class is used to validate data types with given schema.
 *
 * @since 1.1.0
 */
public class TypeValidatorUtils {

    /**
     * Validate ballerina record against schema.
     *
     */
    public static void validateRecordType(Schema<?> schema, TypeSymbol typeSymbol, String balRecord,
                                          SyntaxNodeAnalysisContext context,
                                          OpenAPI openAPI, String oasName, DiagnosticSeverity severity) {

        if (typeSymbol instanceof RecordTypeSymbol || typeSymbol instanceof TypeReferenceTypeSymbol) {
//            List<String> recordFields = new ArrayList<>(); //Comment due to enable later implementation.
            Map<String, Schema> properties = schema.getProperties();
            if (schema instanceof ObjectSchema) {
                properties = schema.getProperties();
            }
            if (typeSymbol instanceof TypeReferenceTypeSymbol) {
                typeSymbol = ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor();
            }
            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeSymbol;
            Map<String, RecordFieldSymbol> fieldSymbolList = recordTypeSymbol.fieldDescriptors();
            for (Map.Entry<String, RecordFieldSymbol> field : fieldSymbolList.entrySet()) {
                boolean isFieldExist = false;
                for (Map.Entry<String, Schema> property : properties.entrySet()) {
                    if (field.getKey().trim().equals(property.getKey().trim())) {
                        isFieldExist = true;
                        String fieldType = field.getValue().typeDescriptor().signature();
                        if (field.getValue().typeDescriptor() instanceof TypeReferenceTypeSymbol) {
                            TypeReferenceTypeSymbol typeRef =
                                    (TypeReferenceTypeSymbol) field.getValue().typeDescriptor();
                            fieldType = typeRef.definition().getName().get();
                        }

                        Schema schemaValue = property.getValue();
                        String oas = schemaValue.getType();
                        if (oas != null && oas.equals(NUMBER)) {
                            oas = DOUBLE;
                            if (schemaValue.getFormat() != null &&
                                    schemaValue.getFormat().equals(FLOAT)) {
                                oas = FLOAT;
                            }
                        }
                        Optional<String> oasType = convertOpenAPITypeToBallerina(oas);
                        if (schemaValue instanceof ArraySchema) {
                            ArraySchema arraySchema = (ArraySchema) schemaValue;
                            validateArrayTypeMismatch(balRecord, context, field, arraySchema, severity);
                        } else if (schemaValue.get$ref() != null) {
                            Schema componentSchema = openAPI.getComponents().getSchemas()
                                    .get(extractReferenceType(schemaValue.get$ref()).orElse(null));
                            validateRecordType(componentSchema, field.getValue().typeDescriptor(), fieldType, context
                                    , openAPI, extractReferenceType(schemaValue.get$ref()).orElse(null), severity);
                        } else if (schemaValue instanceof ObjectSchema) {
                            // Todo: inline record validation ex: record {|int id; string name;|}
                        } else if (oasType.isEmpty() || !fieldType.equals(oasType.get())) {
                            // type mismatch field
                            reportDiagnostic(context, CompilationError.TYPE_MISMATCH_FIELD,
                                    field.getValue().getLocation().orElse(null), severity, oas, fieldType,
                                    field.getKey(), balRecord);
                        }
                        break;
                    }
                }
                if (!isFieldExist) {
                    // Undocumented field.
                    reportDiagnostic(context, CompilationError.UNDEFINED_BRECORD_FIELD,
                            field.getValue().getLocation().orElse(null), severity,
                            field.getKey(), balRecord, oasName);
//                    recordFields.add(field.getKey());
                }
            }
//            if (recordFields.size() == fieldSymbolList.size()) {
//                //TODO : undocumented records
//            }
        }
    }

    /**
     * This function is used to validate type for array fields.
     */
    private static void validateArrayTypeMismatch(String balRecord, SyntaxNodeAnalysisContext context,
                                                  Map.Entry<String, RecordFieldSymbol> field, ArraySchema arraySchema,
                                                  DiagnosticSeverity severity) {
        Optional<String> oasType;
        String messageOasType;
        String balFieldType = field.getValue().typeDescriptor().signature();
        Schema<?> arraySchemaItems = arraySchema.getItems();
        ArraySchema traverseNestedArraySchema;
        String array = SQUARE_BRACKETS;
        StringBuilder arrayBuilder = new StringBuilder();
        arrayBuilder.append(array);
        String oasArrayItems = arraySchema.getItems().getType();

        if (arraySchemaItems instanceof ArraySchema) {
            traverseNestedArraySchema = (ArraySchema) arraySchemaItems;
            arrayBuilder.append(array);
            oasArrayItems = traverseNestedArraySchema.getItems().getType();
            while (traverseNestedArraySchema.getItems() instanceof ArraySchema) {
                Schema<?> traversSchemaNestedArraySchemaType = traverseNestedArraySchema.getItems();
                if (traversSchemaNestedArraySchemaType instanceof ArraySchema) {
                    traverseNestedArraySchema = (ArraySchema) traversSchemaNestedArraySchemaType;
                    arrayBuilder.append(array);
                } else {
                    Optional<String> oasItems = convertOpenAPITypeToBallerina(
                            traversSchemaNestedArraySchemaType.getType().trim());
                    if (oasItems.isEmpty()) {
                        oasArrayItems = traversSchemaNestedArraySchemaType.getType().trim();
                    } else {
                        oasArrayItems = oasItems.get();
                    }
                }
            }
        }

        oasType = Optional.of(convertOpenAPITypeToBallerina(oasArrayItems).orElse(oasArrayItems)
                + arrayBuilder.toString());
        messageOasType = oasArrayItems + arrayBuilder.toString();
        if (!balFieldType.equals(oasType.get())) {
            // type mismatch error
            reportDiagnostic(context, CompilationError.TYPE_MISMATCH_FIELD, field.getValue().getLocation().orElse(null),
                    severity, messageOasType, balFieldType, field.getKey(), balRecord);
        }
    }


    /**
     * Method for convert openApi type to ballerina type.
     *
     * @param type OpenApi parameter types
     * @return ballerina type
     */
    public static Optional<String> convertOpenAPITypeToBallerina(String type) {
        if (type == null) {
            return Optional.empty();
        }
        switch (type) {
            case Constants.INTEGER:
                return Optional.of(INT);
            case STRING:
                return Optional.of(STRING);
            case BOOLEAN:
                return Optional.of(BOOLEAN);
            case Constants.ARRAY:
                return Optional.of(ARRAY_BRACKETS);
            case Constants.OBJECT:
                return Optional.of(RECORD);
            case DOUBLE:
            case NUMBER:
                return Optional.of(DECIMAL);
            case FLOAT:
                return Optional.of(FLOAT);
            default:
                return Optional.empty();
        }
    }

    /**
     * Method for convert ballerina TYPE_DEC type to ballerina type.
     *
     * @param type OpenApi parameter types
     * @return ballerina type
     */
    public static Optional<String> convertBallerinaType(SyntaxKind type) {
        switch (type) {
            case INT_TYPE_DESC:
                return Optional.of(INT);
            case STRING_TYPE_DESC:
                return Optional.of(STRING);
            case BOOLEAN_TYPE_DESC:
                return Optional.of(BOOLEAN);
            case ARRAY_TYPE_DESC:
                return Optional.of(ARRAY_BRACKETS);
            case RECORD_TYPE_DESC:
                return Optional.of(RECORD);
            case DECIMAL_TYPE_DESC:
                return Optional.of(DECIMAL);
            case FLOAT_TYPE_DESC:
                return Optional.of(FLOAT);
            default:
                return Optional.empty();
        }
    }

    /**
     * This util is to validate OpenAPI Object Schema against to Ballerina record.
     */
    public static void validateObjectSchema(ObjectSchema objectSchema, TypeSymbol typeSymbol,
                                          SyntaxNodeAnalysisContext context, String balRecord, Location parentLocation,
                                            DiagnosticSeverity severity) {
        //when union type typeSymbol didn't give location currently
        Location location;
        if (typeSymbol.getLocation().isEmpty()) {
            location = parentLocation;
        } else {
            location = typeSymbol.getLocation().get();
        }
        if (typeSymbol instanceof RecordTypeSymbol || typeSymbol instanceof TypeReferenceTypeSymbol) {
            if (typeSymbol instanceof TypeReferenceTypeSymbol) {
                typeSymbol = ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor();
            }

            RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeSymbol;
            Map<String, Schema> properties = objectSchema.getProperties();
            Map<String, RecordFieldSymbol> recordFieldSymbolMap = recordTypeSymbol.fieldDescriptors();
            AtomicInteger numberOfMissingFields = new AtomicInteger();
            properties.forEach((key, value)-> {
                AtomicBoolean isPropertyExist = new AtomicBoolean(false);
                recordFieldSymbolMap.forEach((fieldName, fieldValue) -> {
                    if (key.equals(fieldName.trim())) {
                        isPropertyExist.set(true);
                    }
                });
                if (!isPropertyExist.get()) {
                    // Missing field message;
                    numberOfMissingFields.addAndGet(1);
                    reportDiagnostic(context, CompilationError.UNIMPLEMENTED_OAS_PROPERTY,
                            location, severity, key, balRecord);
                }
            });
//            if (numberOfMissingFields.get() == properties.size()) {
//                // TODO: error message for unimplemented record
//            }
        }
    }
}
