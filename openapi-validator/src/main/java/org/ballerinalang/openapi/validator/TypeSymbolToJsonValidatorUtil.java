package org.ballerinalang.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
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
    public static List<ValidationError> validate(Schema<?> schema, TypeSymbol typeSymbol,
                                                 SyntaxTree syntaxTree, SemanticModel semanticModel)
            throws OpenApiValidatorException {

        List<ValidationError> validationErrorList = new ArrayList<>();
        if (typeSymbol instanceof RecordTypeSymbol) {
            Map<String, Schema> properties = schema.getProperties();
            if (schema instanceof ObjectSchema) {
                properties = ((ObjectSchema) schema).getProperties();
            }

            List<FieldSymbol> fieldSymbolList = ((RecordTypeSymbol) typeSymbol).fieldDescriptors();
            for (FieldSymbol fieldSymbol : fieldSymbolList) {
                boolean isExist = false;
                for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                    if (fieldSymbol.name().equals(entry.getKey())) {
                        isExist = true;
                        if (!fieldSymbol.typeDescriptor().typeKind().getName()
                                .equals(TypeSymbolToJsonValidatorUtil.convertOpenAPITypeToBallerina(entry.getValue()
                                        .getType())) && (!(entry.getValue() instanceof ObjectSchema))) {
                            TypeMismatch validationError = new TypeMismatch(fieldSymbol.name(),
                                    convertTypeToEnum(entry.getValue().getType()),
                                    convertTypeToEnum(fieldSymbol.typeDescriptor().typeKind().getName()),
                                    fieldSymbol.signature());
                            validationErrorList.add(validationError);
                        } else if ((entry.getValue() instanceof ObjectSchema) && (fieldSymbol.typeDescriptor() instanceof TypeReferenceTypeSymbol)) {
                            // Handle the nested record type
//                             DocumentationNode reference = (DocumentationNode) fieldSymbol.documentation().get();
//                             fieldSymbol.typeDescriptor().langLibMethods().
                            TypeSymbol refRecordType[] = null;
                            List<ValidationError> nestedValidationError;
                            Optional<Symbol> symbol = semanticModel.symbol(syntaxTree.filePath(),
                                    LinePosition.from(fieldSymbol.location().lineRange().startLine().line(),
                                            fieldSymbol.location().lineRange().startLine().offset()));
                            symbol.ifPresent(symbol1 -> {
                                refRecordType[0] = ((TypeReferenceTypeSymbol) symbol1).typeDescriptor();
                            });
                            nestedValidationError = validate(entry.getValue(), refRecordType[0], syntaxTree,
                                    semanticModel);
                            validationErrorList.addAll(nestedValidationError);

                        } else {
                            // Handle array type mismatching.

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
            //Check given type is record or not

            return validationErrorList;
        }

        return validationErrorList;
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


