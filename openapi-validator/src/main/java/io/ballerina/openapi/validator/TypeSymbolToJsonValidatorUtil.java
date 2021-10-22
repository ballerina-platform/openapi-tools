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

package io.ballerina.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FloatTypeSymbol;
import io.ballerina.compiler.api.symbols.IntTypeSymbol;
import io.ballerina.compiler.api.symbols.NilTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.StringTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.validator.error.MissingFieldInBallerinaType;
import io.ballerina.openapi.validator.error.MissingFieldInJsonSchema;
import io.ballerina.openapi.validator.error.TypeMismatch;
import io.ballerina.openapi.validator.error.ValidationError;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.validator.ErrorMessages.couldNotFindLocation;

/**
 * This util use for comparing the TypeSymbol with given openAPI schema.
 */
public class TypeSymbolToJsonValidatorUtil {

    public static List<ValidationError> validate(Schema<?> schema, TypeSymbol typeSymbol,
                                                 SyntaxTree syntaxTree, SemanticModel semanticModel,
                                                 String componentName, Location location)
            throws OpenApiValidatorException {
        String paramName = "";
        String schemaType = "";
        String ballerinaType = "";
        List<ValidationError> validationErrorList = new ArrayList<>();
        boolean isExitType = false;
        //Check given type is a record or not
        if (typeSymbol instanceof RecordTypeSymbol || typeSymbol instanceof TypeReferenceTypeSymbol) {
            Map<String, Schema> properties = schema.getProperties();
            if (schema instanceof ObjectSchema) {
                properties = schema.getProperties();
            }
            if (typeSymbol instanceof TypeReferenceTypeSymbol) {
                typeSymbol = ((TypeReferenceTypeSymbol) typeSymbol).typeDescriptor();
            }
            isExitType = true;
            List<ValidationError> validationError = validateRecordType((RecordTypeSymbol) typeSymbol, syntaxTree,
                    semanticModel, properties, componentName, location);
            validationErrorList.addAll(validationError);
        } else if (typeSymbol instanceof StringTypeSymbol || typeSymbol instanceof IntTypeSymbol
                || typeSymbol instanceof FloatTypeSymbol) {
            if (!componentName.isBlank()) {
                paramName = componentName;
            } else {
                paramName = "simpleParam";
            }

            if (typeSymbol.typeKind().getName().equals(convertOpenAPITypeToBallerina(schema.getType()))) {
                // type mismatch
                isExitType = true;
            } else {
                schemaType = schema.getType();
                ballerinaType = typeSymbol.typeKind().getName();
            }
        } else if ((typeSymbol instanceof ArrayTypeSymbol) && (schema instanceof  ArraySchema)) {
            if (!componentName.isBlank()) {
                paramName = componentName;
            } else {
                paramName = "array";
            }
            if (((ArrayTypeSymbol) typeSymbol).memberTypeDescriptor().typeKind().getName().toLowerCase(Locale.ENGLISH)
                    .equals(convertOpenAPITypeToBallerina(((ArraySchema) schema).getItems().getType()))) {
                isExitType = true;
            } else if ((((ArraySchema) schema).getItems() instanceof ObjectSchema) &&
                    (((ArrayTypeSymbol) typeSymbol).memberTypeDescriptor() instanceof TypeReferenceTypeSymbol)) {
                TypeSymbol recordType = null;
                isExitType = true;
                String finalParamName = paramName;
                Optional<TypeSymbol> symbol = semanticModel.typeOf(((ArrayTypeSymbol) typeSymbol)
                        .memberTypeDescriptor().getLocation().orElseThrow(() -> new OpenApiValidatorException(
                                couldNotFindLocation(finalParamName))).lineRange());
                if (symbol.isPresent()) {
                    recordType = ((TypeDefinitionSymbol) symbol.get()).typeDescriptor();
                }
                List<ValidationError> recordValidationError = validate(((ArraySchema) schema).getItems(),
                        recordType, syntaxTree, semanticModel, componentName, location);
                validationErrorList.addAll(recordValidationError);

            } else if ((((ArraySchema) schema).getItems() instanceof ArraySchema) &&
                    (((ArrayTypeSymbol) typeSymbol).memberTypeDescriptor() instanceof ArrayTypeSymbol)) {
                // handle nested array
                isExitType = true;

                TypeSymbol bArrayTypeSymbol = ((ArrayTypeSymbol) typeSymbol).memberTypeDescriptor();
                Schema arraySchemaItems = ((ArraySchema) schema).getItems();

                if ((bArrayTypeSymbol instanceof ArrayTypeSymbol) && (arraySchemaItems instanceof ArraySchema)) {
                    ArrayTypeSymbol traverseNestedArray = (ArrayTypeSymbol) bArrayTypeSymbol;
                    ArraySchema traversSchemaNestedArray = (ArraySchema) arraySchemaItems;

                    while ((traverseNestedArray.memberTypeDescriptor() instanceof ArrayTypeSymbol) &&
                            (traversSchemaNestedArray.getItems() instanceof ArraySchema)) {
                        Schema<?> traversSchemaNestedArraySchemaType = traversSchemaNestedArray.getItems();
                        if (traversSchemaNestedArraySchemaType instanceof ArraySchema) {
                            traversSchemaNestedArray = (ArraySchema) traversSchemaNestedArraySchemaType;
                        }
                        TypeSymbol traverseNestedArrayBType = traverseNestedArray.memberTypeDescriptor();
                        if (traverseNestedArrayBType instanceof ArrayTypeSymbol) {
                            traverseNestedArray = (ArrayTypeSymbol) traverseNestedArrayBType;
                        }
                    }
                    List<ValidationError> arrayErrors = validate(traversSchemaNestedArray,
                            traverseNestedArray, syntaxTree, semanticModel, componentName, location);

                    validationErrorList.addAll(arrayErrors);
                }
            } else {
                schemaType = ((ArraySchema) schema).getItems().getType();
                ballerinaType = ((ArrayTypeSymbol) typeSymbol).memberTypeDescriptor().typeKind().getName()
                                .toLowerCase(Locale.ENGLISH);
            }

        } else if (typeSymbol instanceof UnionTypeSymbol) {
            List<TypeSymbol> typeSymbols = ((UnionTypeSymbol) typeSymbol).memberTypeDescriptors();
            if (!typeSymbols.isEmpty()) {
                isExitType = true;
                List<ValidationError> error = new ArrayList<>();
                for (TypeSymbol symbol: typeSymbols) {
                    if (!(symbol instanceof NilTypeSymbol)) {
                        error = validate(schema, symbol, syntaxTree, semanticModel,
                                componentName, location);
                        break;
                    }
                }
                if (!error.isEmpty()) {
                    validationErrorList.addAll(error);
                }
            }
        }

        if (!isExitType) {
            if (schema == null) {
                throw new AssertionError();
            }
            TypeMismatch typeMismatch = new TypeMismatch(paramName, convertTypeToEnum(schemaType),
                    convertTypeToEnum(ballerinaType), location);
            validationErrorList.add(typeMismatch);
        }
        return validationErrorList;
    }

    private static List<ValidationError> validateRecordType(RecordTypeSymbol recordTypeSymbol, SyntaxTree syntaxTree,
                                                            SemanticModel semanticModel,
                                                            Map<String, Schema> properties, String componentName,
                                                            Location location)
            throws OpenApiValidatorException {
        List<ValidationError> validationErrorList = new ArrayList<>();
        Map<String, RecordFieldSymbol> fieldSymbolList = recordTypeSymbol.fieldDescriptors();
        for (Map.Entry<String, RecordFieldSymbol> fieldSymbol : fieldSymbolList.entrySet()) {
            boolean isExist = false;
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                if (fieldSymbol.getValue().getName().orElseThrow().equals(entry.getKey())) {
                    isExist = true;
                    if (!fieldSymbol.getValue().typeDescriptor().typeKind().getName()
                            .equals(TypeSymbolToJsonValidatorUtil.convertOpenAPITypeToBallerina(entry.getValue()
                                    .getType())) && (!(entry.getValue() instanceof ObjectSchema)) &&
                            (!(fieldSymbol.getValue().typeDescriptor() instanceof ArrayTypeSymbol))) {

                        TypeMismatch validationError = new TypeMismatch(fieldSymbol.getValue().getName().orElseThrow(),
                                convertTypeToEnum(entry.getValue().getType()),
                                convertTypeToEnum(fieldSymbol.getValue().typeDescriptor().typeKind().getName()),
                                componentName,
                                fieldSymbol.getValue().getLocation().orElseThrow(() -> new OpenApiValidatorException(
                                        couldNotFindLocation(fieldSymbol.getValue().getName().get()))));
                        validationErrorList.add(validationError);

                    } else if ((entry.getValue() instanceof ObjectSchema) && (fieldSymbol.getValue().typeDescriptor()
                            instanceof TypeReferenceTypeSymbol)) {
                        // Handle the nested record type
                        TypeSymbol refRecordType = null;
                        List<ValidationError> nestedValidationError;
                        Optional<TypeSymbol> symbol = semanticModel.typeOf(
                                fieldSymbol.getValue().getLocation().orElseThrow(() -> new OpenApiValidatorException(
                                        couldNotFindLocation(fieldSymbol.getValue().getName().get()))).lineRange());

                        if (symbol != null && symbol.isPresent()) {
                            Symbol symbol1 = symbol.get();
                            if (symbol1 instanceof TypeReferenceTypeSymbol) {
                                refRecordType = ((TypeReferenceTypeSymbol) symbol1).typeDescriptor();
                            } else if (symbol1 instanceof VariableSymbol) {
                                VariableSymbol variableSymbol = (VariableSymbol) symbol1;
                                if (variableSymbol.typeDescriptor() != null) {
                                    Symbol variable = variableSymbol.typeDescriptor();
                                    if (variable instanceof TypeReferenceTypeSymbol) {
                                        if (((TypeReferenceTypeSymbol) variable).typeDescriptor() != null) {
                                            refRecordType = ((TypeReferenceTypeSymbol) variable).typeDescriptor();
                                        }
                                    } else {
                                        refRecordType = variableSymbol.typeDescriptor();
                                    }
                                }
                            }
                        }
                        nestedValidationError = validate(entry.getValue(), refRecordType, syntaxTree,
                                semanticModel, componentName, location);
                        validationErrorList.addAll(nestedValidationError);

                    } else if ((fieldSymbol.getValue().typeDescriptor() instanceof ArrayTypeSymbol) &&
                            ((entry.getValue().getType()).equals("array"))) {
                        // Handle array type mismatching.
                        validateArrayType(validationErrorList, fieldSymbol.getValue(), entry, syntaxTree, semanticModel,
                                componentName, location);
                    }
                }
            }
            // Handle missing record file against to schema
            if (!isExist) {
                MissingFieldInJsonSchema validationError =
                        new MissingFieldInJsonSchema(fieldSymbol.getValue().getName().orElseThrow(),
                                convertTypeToEnum(fieldSymbol.getValue().typeDescriptor().typeKind().getName()),
                                componentName, location);
                validationErrorList.add(validationError);
            }
        }
        // Find missing fields in BallerinaType
        for (Map.Entry<String, Schema> entry : properties.entrySet()) {
            boolean isExist = false;
            for (Map.Entry<String, RecordFieldSymbol> field : fieldSymbolList.entrySet()) {
                if (field.getValue().getName().orElseThrow().equals(entry.getKey())) {
                    isExist = true;
                }
            }
            if (!isExist) {
                MissingFieldInBallerinaType validationError = new MissingFieldInBallerinaType(entry.getKey(),
                        convertTypeToEnum(entry.getValue().getType()), componentName);
                validationErrorList.add(validationError);
            }
        }
        return validationErrorList;
    }

    private static void validateArrayType(List<ValidationError> validationErrorList, RecordFieldSymbol fieldSymbol,
                                      Map.Entry<String, Schema> entry, SyntaxTree syntaxTree,
                                          SemanticModel semanticModel, String componentName, Location location)
            throws OpenApiValidatorException {

        ArrayTypeSymbol arraySymbol = null;
        ArraySchema arraySchema = new ArraySchema();
        Schema schema = entry.getValue();
        if (schema instanceof ArraySchema) {
            arraySchema = (ArraySchema) schema;
        }
        if (fieldSymbol.typeDescriptor() instanceof ArrayTypeSymbol) {
            arraySymbol = (ArrayTypeSymbol) fieldSymbol.typeDescriptor();
        }
        //traverse nested array
        ArrayTypeSymbol traverseNestedArraySymbol = arraySymbol;
        ArraySchema traverseNestedArraySchema = arraySchema;

        if (arraySymbol != null) {
            //check array item type
            TypeSymbol itemTypeSymbol = arraySymbol.memberTypeDescriptor();
            Schema arraySchemaItems = arraySchema.getItems();

            if ((itemTypeSymbol instanceof ArrayTypeSymbol) && (arraySchemaItems instanceof ArraySchema)) {
                traverseNestedArraySymbol = (ArrayTypeSymbol) itemTypeSymbol;
                traverseNestedArraySchema = (ArraySchema) arraySchemaItems;
                 while ((traverseNestedArraySymbol.memberTypeDescriptor() instanceof ArrayTypeSymbol) &&
                         (traverseNestedArraySchema.getItems() instanceof ArraySchema)) {
                     Schema<?> traversSchemaNestedArraySchemaType = traverseNestedArraySchema.getItems();
                     if (traversSchemaNestedArraySchemaType instanceof ArraySchema) {
                         traverseNestedArraySchema = (ArraySchema) traversSchemaNestedArraySchemaType;
                     }
                     TypeSymbol traverseNestedArrayTypeSymbol = traverseNestedArraySymbol.memberTypeDescriptor();
                     if (traverseNestedArrayTypeSymbol instanceof ArrayTypeSymbol) {
                         traverseNestedArraySymbol = (ArrayTypeSymbol) traverseNestedArrayTypeSymbol;
                     }
                 }
            }
            //when item type is record
            if (traverseNestedArraySchema.getItems() != null) {
                if ((traverseNestedArraySchema.getItems() instanceof ObjectSchema) &&
                        (traverseNestedArraySymbol.memberTypeDescriptor() instanceof TypeReferenceTypeSymbol)) {

                    Schema recordSchema = traverseNestedArraySchema.getItems();
                    TypeReferenceTypeSymbol recordRefSymbol =
                            (TypeReferenceTypeSymbol) traverseNestedArraySymbol.memberTypeDescriptor();
                    List<ValidationError> recordItemErrors = TypeSymbolToJsonValidatorUtil.validate(recordSchema,
                            recordRefSymbol.typeDescriptor(), syntaxTree, semanticModel, componentName, location);
                    validationErrorList.addAll(recordItemErrors);

                } else if (!traverseNestedArraySymbol.memberTypeDescriptor().typeKind().getName().equals(
                        TypeSymbolToJsonValidatorUtil.convertOpenAPITypeToBallerina(
                                traverseNestedArraySchema.getItems().getType()))) {
                    TypeMismatch typeMismatch = new TypeMismatch(fieldSymbol.getName().orElseThrow(),
                            convertTypeToEnum(traverseNestedArraySchema.getItems().getType()),
                            convertTypeToEnum(traverseNestedArraySymbol.memberTypeDescriptor().typeKind().getName()),
                            location);
                    validationErrorList.add(typeMismatch);
                }
            }
        }
    }

    /**
     * Method for convert string type to constant enum type.
     * @param type  input type
     * @return enum type
     */
    public static Constants.Type convertTypeToEnum(String type) {
        Constants.Type convertedType;
        switch (type) {
            case Constants.INTEGER:
                convertedType = Constants.Type.INTEGER;
                break;
            case Constants.INT:
                convertedType = Constants.Type.INT;
                break;
            case Constants.STRING:
                convertedType = Constants.Type.STRING;
                break;
            case Constants.BOOLEAN:
                convertedType = Constants.Type.BOOLEAN;
                break;
            case Constants.ARRAY:
            case "[]":
                convertedType = Constants.Type.ARRAY;
                break;
            case Constants.OBJECT:
                convertedType = Constants.Type.OBJECT;
                break;
            case Constants.RECORD:
                convertedType = Constants.Type.RECORD;
                break;
            case Constants.NUMBER:
                convertedType = Constants.Type.NUMBER;
                break;
            case  Constants.DECIMAL:
                convertedType = Constants.Type.DECIMAL;
                break;
            default:
                convertedType = Constants.Type.ANYDATA;
        }
        return convertedType;
    }

    /**
     * Method for convert openApi type to ballerina type.
     * @param type  OpenApi parameter types
     * @return ballerina type
     */
    private static String convertOpenAPITypeToBallerina(String type) {
        String convertedType;
        switch (type) {
            case Constants.INTEGER:
                convertedType = "int";
                break;
            case Constants.STRING:
                convertedType = "string";
                break;
            case Constants.BOOLEAN:
                convertedType = "boolean";
                break;
            case Constants.ARRAY:
                convertedType = "[]";
                break;
            case Constants.OBJECT:
                convertedType = "record";
                break;
            case Constants.DECIMAL:
                convertedType = "decimal";
                break;
            default:
                convertedType = "";
        }
        return convertedType;
    }

    /**
     * Convert enum type to string type.
     * @param type  type of parameter
     * @return enum type
     */
    public static String convertEnumTypeToString(Constants.Type type) {
        String convertedType;
        switch (type) {
            case INT:
                convertedType = "int";
                break;
            case INTEGER:
                convertedType = "integer";
                break;
            case STRING:
                convertedType = "string";
                break;
            case BOOLEAN:
                convertedType = "boolean";
                break;
            case ARRAY:
                convertedType = "array";
                break;
            case OBJECT:
                convertedType = "object";
                break;
            case RECORD:
                convertedType = "record";
                break;
            case DECIMAL:
                convertedType = "decimal";
                break;
            default:
                convertedType = "";
        }
        return convertedType;
    }
}


