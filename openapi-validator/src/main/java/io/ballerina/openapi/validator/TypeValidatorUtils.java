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
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.validator.Constants.ARRAY_BRACKETS;
import static io.ballerina.openapi.validator.Constants.BOOLEAN;
import static io.ballerina.openapi.validator.Constants.DECIMAL;
import static io.ballerina.openapi.validator.Constants.FLOAT;
import static io.ballerina.openapi.validator.Constants.INT;
import static io.ballerina.openapi.validator.Constants.NUMBER;
import static io.ballerina.openapi.validator.Constants.RECORD;
import static io.ballerina.openapi.validator.Constants.STRING;
import static io.ballerina.openapi.validator.ValidatorUtils.extractReferenceType;
import static io.ballerina.openapi.validator.ValidatorUtils.updateContext;

/**
 * This util class is used to validate data types with given schema.
 *
 * @since 2201.1.0
 */
public class TypeValidatorUtils {

    /**
     * Validate ballerina record against schema
     * @param schema
     * @param typeSymbol
     * @param balRecord
     * @param context
     * @param openAPI
     * @param oasName
     */
    public static void validateRecordType(Schema<?> schema, TypeSymbol typeSymbol, String balRecord,
                                          SyntaxNodeAnalysisContext context,
                                          OpenAPI openAPI, String oasName) {

        if (typeSymbol instanceof RecordTypeSymbol || typeSymbol instanceof TypeReferenceTypeSymbol) {
            List<String> recordFields = new ArrayList<>();
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
                        Optional<String> oasType = convertOpenAPITypeToBallerina(property.getValue().getType());
                        if (property.getValue() instanceof ArraySchema) {
                            validateArrayTypeMismatch(balRecord, context, field, property);
                        } else if (property.getValue().get$ref() != null) {
                            Schema componentSchema = openAPI.getComponents().getSchemas()
                                    .get(extractReferenceType(property.getValue().get$ref()).orElse(null));
                            validateRecordType(componentSchema, field.getValue().typeDescriptor(), fieldType, context
                                    , openAPI, extractReferenceType(property.getValue().get$ref()).orElse(null));
                        } else if (property.getValue() instanceof ObjectSchema) {
                            // Todo: inline record validation ex: record {|int id; string name;|}
                        } else if (oasType.isEmpty() || !fieldType.equals(oasType.get())) {
                            // type mismatch field
                            updateContext(context, CompilationError.TYPE_MISMATCH_FIELD,
                                    field.getValue().getLocation().orElse(null), fieldType,
                                    property.getValue().getType(), field.getKey(), balRecord);
                        }
                        break;
                    }
                }
                if (!isFieldExist) {
                    // Undocumented field.
                    updateContext(context, CompilationError.UNDOCUMENTED_BRECORD_FIELD,
                            field.getValue().getLocation().orElse(null), field.getKey(), balRecord, oasName);
                    recordFields.add(field.getKey());
                }
            }
            if (recordFields.size() == fieldSymbolList.size()) {
                //TODO : undocumented records
            }
        }
    }

    /**
     * This function is used to validate type for array fields.
     * @param balRecord
     * @param context
     * @param field
     * @param property
     * @return
     */
    private static Optional<String> validateArrayTypeMismatch(String balRecord, SyntaxNodeAnalysisContext context,
                                              Map.Entry<String, RecordFieldSymbol> field,
                                              Map.Entry<String, Schema> property) {

        Optional<String> oasType;
        String messageOasType;
        String balFieldType = field.getValue().typeDescriptor().signature();

        ArraySchema arraySchema = (ArraySchema) property.getValue();
        Schema<?> arraySchemaItems = arraySchema.getItems();
        ArraySchema traverseNestedArraySchema;
        String array = "[]";
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

        oasType = Optional.of(convertOpenAPITypeToBallerina(oasArrayItems).orElse(oasArrayItems) + arrayBuilder.toString());
        messageOasType = oasArrayItems + arrayBuilder.toString();
        // type mismatch error
        updateContext(context, CompilationError.TYPE_MISMATCH_FIELD, field.getValue().getLocation().orElse(null),
                balFieldType, messageOasType, field.getKey(), balRecord);
        return oasType;
    }

    // todo: OpenAPI-to-Ballerina schema

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
            case DECIMAL:
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
}
