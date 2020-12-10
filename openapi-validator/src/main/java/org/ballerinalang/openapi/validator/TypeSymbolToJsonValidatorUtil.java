package org.ballerinalang.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.FieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LinePosition;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import org.ballerinalang.openapi.validator.error.MissingFieldInJsonSchema;
import org.ballerinalang.openapi.validator.error.TypeMismatch;
import org.ballerinalang.openapi.validator.error.ValidationError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TypeSymbolToJsonValidatorUtil {
    private Project project;

    public void init(Project project) {
        this.project = project;
    }
    public static List<ValidationError> validate(Schema<?> schema, TypeSymbol typeSymbol,
                                                 SyntaxTree syntaxTree, SemanticModel semanticModel)
            throws OpenApiValidatorException {

        List<ValidationError> validationErrorList = new ArrayList<>();
        //Check given type is record or not
        if (typeSymbol instanceof RecordTypeSymbol) {
            Map<String, Schema> properties = schema.getProperties();
            if (schema instanceof ObjectSchema) {
                properties = ((ObjectSchema) schema).getProperties();
            }
            List<ValidationError> validationError = validateRecordType((RecordTypeSymbol) typeSymbol, syntaxTree,
                    semanticModel, properties);
            validationErrorList.addAll(validationError);
        }
//        else if () {
//
//        } else if () {
//
//        }

        return validationErrorList;
    }

    private static List<ValidationError> validateRecordType(RecordTypeSymbol typeSymbol, SyntaxTree syntaxTree,
                                                            SemanticModel semanticModel,
                                                            Map<String, Schema> properties)
            throws OpenApiValidatorException {
        List<ValidationError> validationErrorList = new ArrayList<>();
        List<FieldSymbol> fieldSymbolList = typeSymbol.fieldDescriptors();
        for (FieldSymbol fieldSymbol : fieldSymbolList) {
            boolean isExist = false;
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                if (fieldSymbol.name().equals(entry.getKey())) {
                    isExist = true;
                    if (!fieldSymbol.typeDescriptor().typeKind().getName()
                            .equals(TypeSymbolToJsonValidatorUtil.convertOpenAPITypeToBallerina(entry.getValue()
                                    .getType())) && (!(entry.getValue() instanceof ObjectSchema)) && (!(fieldSymbol.typeDescriptor() instanceof ArrayTypeSymbol))) {
                        TypeMismatch validationError = new TypeMismatch(fieldSymbol.name(),
                                convertTypeToEnum(entry.getValue().getType()),
                                convertTypeToEnum(fieldSymbol.typeDescriptor().typeKind().getName()),
                                fieldSymbol.signature());
                        validationErrorList.add(validationError);
                    } else if ((entry.getValue() instanceof ObjectSchema) && (fieldSymbol.typeDescriptor() instanceof TypeReferenceTypeSymbol)) {
                        // Handle the nested record type
                        TypeSymbol refRecordType = null;
                        List<ValidationError> nestedValidationError;
                        Optional<Symbol> symbol = semanticModel.symbol(syntaxTree.filePath(),
                                LinePosition.from(fieldSymbol.location().lineRange().startLine().line(),
                                        fieldSymbol.location().lineRange().startLine().offset()));
                        fieldSymbol.typeDescriptor();
                        if (symbol != null && symbol.isPresent()) {
                            Symbol symbol1 = symbol.get();
                            if (symbol1 instanceof TypeReferenceTypeSymbol) {
                                refRecordType = ((TypeReferenceTypeSymbol) symbol1).typeDescriptor();
                            } else if (symbol1 instanceof VariableSymbol) {
                                VariableSymbol variableSymbol = (VariableSymbol) symbol1;
                                if (variableSymbol.typeDescriptor() != null) {
                                    Symbol variable = variableSymbol.typeDescriptor();
                                    if ( variable instanceof TypeReferenceTypeSymbol) {
                                        if (((TypeReferenceTypeSymbol) variable).typeDescriptor() != null) {
                                            refRecordType = ((TypeReferenceTypeSymbol)variable).typeDescriptor();
                                        }
                                    } else {
                                        refRecordType = variableSymbol.typeDescriptor();
                                    }
                                }
                            }
                        }
                        nestedValidationError = validate(entry.getValue(), refRecordType, syntaxTree,
                                semanticModel);
                        validationErrorList.addAll(nestedValidationError);

                    } else if((fieldSymbol.typeDescriptor() instanceof ArrayTypeSymbol) && ((entry.getValue().getType()).equals("array"))) {
                        // Handle array type mismatching.
                        validateArrayType(validationErrorList, fieldSymbol, entry, syntaxTree, semanticModel);
                    }
                }
            }
            // Handle missing record file against to schema
            if (!isExist) {
                MissingFieldInJsonSchema validationError =
                        new MissingFieldInJsonSchema(fieldSymbol.name(),
                                convertTypeToEnum(fieldSymbol.typeDescriptor().typeKind().getName()),
                                fieldSymbol.signature());
                validationErrorList.add(validationError);
            }

        }

        return validationErrorList;
    }

    private static void validateArrayType(List<ValidationError> validationErrorList, FieldSymbol fieldSymbol,
                                      Map.Entry<String, Schema> entry, SyntaxTree syntaxTree, SemanticModel semanticModel)
            throws OpenApiValidatorException {

        ArrayTypeSymbol arraySymbol = null;
        ArraySchema arraySchema = new ArraySchema();
        if (entry.getValue() instanceof ArraySchema) {
            arraySchema = (ArraySchema) entry.getValue();
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
            Schema  arraySchemaItems = arraySchema.getItems();

            if ((itemTypeSymbol instanceof ArrayTypeSymbol) && (arraySchemaItems instanceof ArraySchema)) {
                traverseNestedArraySymbol = (ArrayTypeSymbol) itemTypeSymbol;
                traverseNestedArraySchema = (ArraySchema) arraySchemaItems;
                 while ((traverseNestedArraySymbol.memberTypeDescriptor() instanceof ArrayTypeSymbol) && ( traverseNestedArraySchema.getItems() instanceof ArraySchema)) {
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
                            recordRefSymbol.typeDescriptor(), syntaxTree, semanticModel);
                    validationErrorList.addAll(recordItemErrors);

                } else if (!traverseNestedArraySymbol.memberTypeDescriptor().typeKind().getName().equals(
                        TypeSymbolToJsonValidatorUtil.convertOpenAPITypeToBallerina(traverseNestedArraySchema.getItems().getType()))){
                    TypeMismatch typeMismatch = new TypeMismatch(fieldSymbol.name(),
                            convertTypeToEnum(traverseNestedArraySchema.getItems().getType()),
                            convertTypeToEnum(traverseNestedArraySymbol.memberTypeDescriptor().typeKind().getName()));
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
    public static String convertOpenAPITypeToBallerina(String type) {
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
    public static String convertEnumTypetoString(Constants.Type type) {
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


