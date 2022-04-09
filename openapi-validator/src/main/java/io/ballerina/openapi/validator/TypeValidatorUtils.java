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

import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
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
import static io.ballerina.openapi.validator.ValidatorUtils.getNormalizedPath;
import static io.ballerina.openapi.validator.ValidatorUtils.updateContext;

/**
 * This util class is used to validate data types with given schema.
 *
 * @since 2201.1.0
 */
public class TypeValidatorUtils {

    public static void validateRecordType(Schema<?> schema, TypeSymbol typeSymbol, String balRecord,
                                          SyntaxNodeAnalysisContext context,
                                          OpenAPI openAPI) {

        if (typeSymbol instanceof RecordTypeSymbol || typeSymbol instanceof TypeReferenceTypeSymbol) {
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
                        Optional<String> fieldType = field.getValue().getName();
                        if (fieldType.isEmpty()) {
                            return;
                        }
                        if (!fieldType.get().equals(convertOpenAPITypeToBallerina(property.getValue().getType())) &&
                                (!(property.getValue() instanceof ObjectSchema)) &&
                                (!(field.getValue().typeDescriptor() instanceof ArrayTypeSymbol))) {
                            // typemismatch error
                            updateContext(context, CompilationError.TYPE_MISMATCH_FIELD,
                                    field.getValue().getLocation().orElse(null), fieldType.get(),
                                    property.getValue().getType(), field.getKey(), balRecord);
                        }

                        // array type
                        // record type

                    }

//                    if (fieldSymbol.getValue().getName().orElseThrow().equals(entry.getKey())) {
//                        isExist = true;
//                        Optional<String> convertType = TypeSymbolToJsonValidatorUtil.convertOpenAPITypeToBallerina(
//                                entry.getValue().getType());
//                        if (convertType.isPresent() && !fieldSymbol.getValue().typeDescriptor().typeKind().getName()
//                                .equals(convertType.get()) && (!(entry.getValue() instanceof ObjectSchema)) &&
//                                (!(fieldSymbol.getValue().typeDescriptor() instanceof ArrayTypeSymbol))) {
//
//                            TypeMismatch validationError =
//                                    new TypeMismatch(fieldSymbol.getValue().getName().orElseThrow(),
//                                            convertTypeToEnum(entry.getValue().getType()),
//                                            convertTypeToEnum(
//                                                    fieldSymbol.getValue().typeDescriptor().typeKind().getName()),
//                                            componentName,
//                                            fieldSymbol.getValue().getLocation()
//                                                    .orElseThrow(() -> new OpenApiValidatorException(
//                                                            couldNotFindLocation(
//                                                                    fieldSymbol.getValue().getName().get()))));
//                            validationErrorList.add(validationError);
//
//                        } else if ((entry.getValue() instanceof ObjectSchema) &&
//                                (fieldSymbol.getValue().typeDescriptor()
//                                        instanceof TypeReferenceTypeSymbol)) {
//                            // Handle the nested record type
//                            TypeSymbol refRecordType = null;
//                            List<ValidationError> nestedValidationError;
//                            TypeSymbol symbol = fieldSymbol.getValue().typeDescriptor();
//                            if (symbol instanceof TypeReferenceTypeSymbol) {
//                                refRecordType = ((TypeReferenceTypeSymbol) symbol).typeDescriptor();
//                            } else if (symbol instanceof VariableSymbol) {
//                                VariableSymbol variableSymbol = (VariableSymbol) symbol;
//                                if (variableSymbol.typeDescriptor() != null) {
//                                    Symbol variable = variableSymbol.typeDescriptor();
//                                    if (variable instanceof TypeReferenceTypeSymbol) {
//                                        if (((TypeReferenceTypeSymbol) variable).typeDescriptor() != null) {
//                                            refRecordType = ((TypeReferenceTypeSymbol) variable).typeDescriptor();
//                                        }
//                                    } else {
//                                        refRecordType = variableSymbol.typeDescriptor();
//                                    }
//                                }
//                            }
//                            nestedValidationError = validate(entry.getValue(), refRecordType, syntaxTree,
//                                    semanticModel, componentName, location);
//                            validationErrorList.addAll(nestedValidationError);
//
//                        } else if ((fieldSymbol.getValue().typeDescriptor() instanceof ArrayTypeSymbol) &&
//                                ((entry.getValue().getType()).equals("array"))) {
//                            // Handle array type mismatching.
//                            validateArrayType(validationErrorList, fieldSymbol.getValue(), entry, syntaxTree,
//                                    semanticModel,
//                                    componentName, location);
//                        }
//                    }
//                }
//                // Handle missing record file against to schema
//                if (!isExist) {
////                        MissingFieldInJsonSchema validationError =
////                                new MissingFieldInJsonSchema(fieldSymbol.getValue().getName().orElseThrow(),
////                                        convertTypeToEnum(fieldSymbol.getValue().typeDescriptor().typeKind().getName()),
////                                        componentName, location);
////                        validationErrorList.add(validationError);
//                }
//            }
//            // Find missing fields in BallerinaType
//            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
//                boolean isExist = false;
//                for (Map.Entry<String, RecordFieldSymbol> field : fieldSymbolList.entrySet()) {
//                    if (field.getValue().getName().orElseThrow().equals(entry.getKey())) {
//                        isExist = true;
//                    }
//                }
//                if (!isExist) {
////                        MissingFieldInBallerinaType validationError = new MissingFieldInBallerinaType(entry.getKey(),
////                                convertTypeToEnum(entry.getValue().getType()), componentName);
////                        validationErrorList.add(validationError);
//                }
//            }
////                return validationErrorList;
//        }
                }
            }
        }
}

    /**
     * Method for convert openApi type to ballerina type.
     *
     * @param type OpenApi parameter types
     * @return ballerina type
     */
    public static Optional<String> convertOpenAPITypeToBallerina(String type) {

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
