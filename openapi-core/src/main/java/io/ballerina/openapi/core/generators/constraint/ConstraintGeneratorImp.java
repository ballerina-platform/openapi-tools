package io.ballerina.openapi.core.generators.constraint;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.internal.regexp.RegExpFactory;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.AT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.MAPPING_CONSTRUCTOR;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DECIMAL;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.FLOAT;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.INT;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.INT_SIGNED32;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.STRING;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isArraySchema;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isComposedSchema;

public class ConstraintGeneratorImp implements ConstraintGenerator {
    OpenAPI openAPI;
    HashMap<String, TypeDefinitionNode> typeDefinitions;
    boolean isConstraint = false;
    List<Diagnostic> diagnostics = new ArrayList<>();

    public ConstraintGeneratorImp(OpenAPI openAPI, HashMap<String, TypeDefinitionNode> typeDefinitions) {
        this.openAPI = openAPI;
        this.typeDefinitions = typeDefinitions;
    }

    boolean getIsConstraint() {
        return isConstraint;
    }

    @Override
    public ConstraintResult updateTypeDefinitionsWithConstraints() {
        if (openAPI.getComponents().getSchemas() == null) {
            return new ConstraintResult(typeDefinitions, isConstraint, diagnostics);
        }
        openAPI.getComponents().getSchemas().forEach((key, value) -> {
            if (typeDefinitions.containsKey(key) && GeneratorUtils.hasConstraints(value)) {
                key = GeneratorUtils.getValidName(key, true);
                if (typeDefinitions.containsKey(key)) {
                    TypeDefinitionNode typeDefinitionNode = typeDefinitions.get(key);
                    //modify the typeDefinitionNode with constraints
                    if (typeDefinitionNode.typeDescriptor().kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
                        Map properties = value.getProperties();
                        RecordTypeDescriptorNode record = (RecordTypeDescriptorNode) typeDefinitionNode.typeDescriptor();
                        NodeList<Node> fields = record.fields();
                        List<Node> recordFields = new ArrayList<>();
                        for (Node field : fields) {
                            // todo fix by checking field node type (RecordFieldNode, RecordFieldWithType)
                            RecordFieldNode recordFieldNode = (RecordFieldNode) field;
                            String fieldName = recordFieldNode.fieldName().text();
                            //todo remove this replacement with new lang changes
                            fieldName = fieldName.replaceAll("^'", "");
                            Schema<?> fieldSchema = (Schema<?>) properties.get(fieldName);
                            if (hasConstraints(fieldSchema)) {
                                //modify the record field with constraints
                                AnnotationNode constraintNode = null;
                                try {
                                    constraintNode = generateConstraintNode(fieldName, fieldSchema);
                                } catch (BallerinaOpenApiException e) {
                                    //todo diagnostic
                                }
                                MetadataNode metadataNode;
                                boolean isConstraintSupport =
                                        constraintNode != null && fieldSchema.getNullable() != null && fieldSchema.getNullable() ||
                                                (fieldSchema.getOneOf() != null ||
                                                        fieldSchema.getAnyOf() != null);
                                boolean nullable = GeneratorMetaData.getInstance().isNullable();
                                if (nullable) {
                                    constraintNode = null;
                                } else if (isConstraintSupport) {
                                    //todo diagnostic
//                                    outStream.printf("WARNING: constraints in the OpenAPI contract will be ignored for the " +
//                                                    "field `%s`, as constraints are not supported on Ballerina union types%n",
//                                            fieldName.toString().trim());
                                    constraintNode = null;
                                }
                                if (constraintNode == null) {
                                    metadataNode = createMetadataNode(null, createEmptyNodeList());
                                } else {
                                    isConstraint = true;
                                    metadataNode = createMetadataNode(null, createNodeList(constraintNode));
                                }
                                recordFieldNode = recordFieldNode.modify(
                                        metadataNode,
                                        recordFieldNode.readonlyKeyword().orElse(null),
                                        recordFieldNode.typeName(),
                                        recordFieldNode.fieldName(),
                                        recordFieldNode.questionMarkToken().orElse(null),
                                        recordFieldNode.semicolonToken()
                                );

                            }
                            recordFields.add(recordFieldNode);

                            //This is special scenario for array schema,
                            //when the items has constraints then we define separate type for it.
                            if (fieldSchema instanceof ArraySchema arraySchema) {
                                updateConstraintWithArrayItems(key, fieldName, arraySchema);
                            } else if (fieldSchema.getOneOf() != null) {
                                //todo handle object schema
                                List oneOf = fieldSchema.getOneOf();
                                for (Schema schema : (List<Schema>) oneOf) {

                                }

                            }
                        }
                        RecordTypeDescriptorNode updatedRecord = record.modify(
                                record.recordKeyword(),
                                record.bodyStartDelimiter(),
                                createNodeList(recordFields),
                                record.recordRestDescriptor().orElse(null),
                                record.bodyEndDelimiter()
                        );
                        typeDefinitionNode = typeDefinitionNode.modify(
                                typeDefinitionNode.metadata().orElse(null),
                                typeDefinitionNode.visibilityQualifier().orElse(null),
                                typeDefinitionNode.typeKeyword(),
                                typeDefinitionNode.typeName(),
                                updatedRecord,
                                typeDefinitionNode.semicolonToken()
                        );
                    } else {
                        if (hasConstraints(value)) {
                            //modify the record field with constraints
                            AnnotationNode constraintNode = null;
                            try {
                                constraintNode = generateConstraintNode(key, value);
                            } catch (BallerinaOpenApiException e) {
                                //todo diagnostic
                            }
                            MetadataNode metadataNode;
                            boolean isConstraintSupport =
                                    constraintNode != null && value.getNullable() != null && value.getNullable() ||
                                            ((value.getOneOf() != null ||
                                                    value.getAnyOf() != null));
                            boolean nullable = GeneratorMetaData.getInstance().isNullable();
                            if (nullable) {
                                constraintNode = null;
                            } else if (isConstraintSupport) {
                                //todo diagnostic
//                                    outStream.printf("WARNING: constraints in the OpenAPI contract will be ignored for the " +
//                                                    "field `%s`, as constraints are not supported on Ballerina union types%n",
//                                            fieldName.toString().trim());
                                constraintNode = null;
                            }
                            if (constraintNode == null) {
                                metadataNode = createMetadataNode(null, createEmptyNodeList());
                            } else {
                                isConstraint = true;
                                metadataNode = createMetadataNode(null, createNodeList(constraintNode));
                            }
                            typeDefinitionNode = typeDefinitionNode.modify(
                                    metadataNode,
                                    typeDefinitionNode.visibilityQualifier().orElse(null),
                                    typeDefinitionNode.typeKeyword(),
                                    typeDefinitionNode.typeName(),
                                    typeDefinitionNode.typeDescriptor(),
                                    typeDefinitionNode.semicolonToken()
                            );

                            if (value instanceof ArraySchema arraySchema) {
                                String normalizedTypeName = key.replaceAll(GeneratorConstants.SPECIAL_CHARACTER_REGEX, "").trim();
                                updateConstraintWithArrayItems("", normalizedTypeName, arraySchema);
                            }
                        }
                    }
                    typeDefinitions.put(key, typeDefinitionNode);
                }
            }
        });
        return new ConstraintResult(typeDefinitions, isConstraint, diagnostics);
    }

    private void updateConstraintWithArrayItems(String key, String fieldName, ArraySchema arraySchema) {
        Schema<?> itemSchema = arraySchema.getItems();
        if (hasConstraints(itemSchema)) {
            String normalizedTypeName = fieldName.replaceAll(GeneratorConstants.SPECIAL_CHARACTER_REGEX,
                    "").trim();
            String itemTypeName = getValidName(key + "-" + normalizedTypeName + "-items-" +
                    itemSchema.getType(), true);
            if (typeDefinitions.containsKey(itemTypeName)) {
                TypeDefinitionNode itemTypeDefNode = typeDefinitions.get(itemTypeName);
                if (hasConstraints(itemSchema)) {
                    //modify the record field with constraints
                    AnnotationNode constraintNode = null;
                    try {
                        constraintNode = generateConstraintNode(itemTypeName, itemSchema);
                    } catch (BallerinaOpenApiException e) {
                        //todo diagnostic
                    }
                    MetadataNode metadataNode;
                    boolean isConstraintSupport =
                            constraintNode != null && itemSchema.getNullable() != null && itemSchema.getNullable() ||
                                    ((itemSchema.getOneOf() != null ||
                                            itemSchema.getAnyOf() != null));
                    boolean nullable = GeneratorMetaData.getInstance().isNullable();
                    if (nullable) {
                        constraintNode = null;
                    } else if (isConstraintSupport) {
                        //todo diagnostic
//                                    outStream.printf("WARNING: constraints in the OpenAPI contract will be ignored for the " +
//                                                    "field `%s`, as constraints are not supported on Ballerina union types%n",
//                                            fieldName.toString().trim());
                        constraintNode = null;
                    }
                    if (constraintNode == null) {
                        metadataNode = createMetadataNode(null, createEmptyNodeList());
                    } else {
                        isConstraint = true;
                        metadataNode = createMetadataNode(null, createNodeList(constraintNode));
                    }
                    itemTypeDefNode = itemTypeDefNode.modify(
                            metadataNode,
                            itemTypeDefNode.visibilityQualifier().orElse(null),
                            itemTypeDefNode.typeKeyword(),
                            itemTypeDefNode.typeName(),
                            itemTypeDefNode.typeDescriptor(),
                            itemTypeDefNode.semicolonToken()
                    );
                    typeDefinitions.put(itemTypeName, itemTypeDefNode);
                }
            }
        }
    }

    @Override
    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    /**
     * This util is to check if the given schema contains any constraints.
     */
    public static boolean hasConstraints(Schema<?> value) {

        if (value.getProperties() != null) {
            boolean constraintExists = value.getProperties().values().stream()
                    .anyMatch(GeneratorUtils::hasConstraints);
            if (constraintExists) {
                return true;
            }
        } else if (isComposedSchema(value)) {
            List<Schema> allOf = value.getAllOf();
            List<Schema> oneOf = value.getOneOf();
            List<Schema> anyOf = value.getAnyOf();
            boolean constraintExists = false;
            if (allOf != null) {
                constraintExists = allOf.stream().anyMatch(GeneratorUtils::hasConstraints);
            } else if (oneOf != null) {
                constraintExists = oneOf.stream().anyMatch(GeneratorUtils::hasConstraints);
            } else if (anyOf != null) {
                constraintExists = anyOf.stream().anyMatch(GeneratorUtils::hasConstraints);
            }
            if (constraintExists) {
                return true;
            }

        } else if (isArraySchema(value)) {
            if (!isConstraintExists(value)) {
                return isConstraintExists(value.getItems());
            }
        }
        return isConstraintExists(value);
    }

    private static boolean isConstraintExists(Schema<?> propertyValue) {

        return propertyValue.getMaximum() != null ||
                propertyValue.getMinimum() != null ||
                propertyValue.getMaxLength() != null ||
                propertyValue.getMinLength() != null ||
                propertyValue.getMaxItems() != null ||
                propertyValue.getMinItems() != null ||
                propertyValue.getExclusiveMinimum() != null ||
                propertyValue.getExclusiveMinimumValue() != null ||
                propertyValue.getExclusiveMaximum() != null ||
                propertyValue.getExclusiveMaximumValue() != null ||
                propertyValue.getPattern() != null;
    }

    /**
     * This util is to set the constraint validation for given data type in the record field and user define type.
     *
     * @param fieldSchema Schema for data type
     * @return {@link MetadataNode}
     */
    public static AnnotationNode generateConstraintNode(String typeName, Schema<?> fieldSchema)
            throws BallerinaOpenApiException {
        if (isConstraintAllowed(typeName, fieldSchema)) {
            String ballerinaType = convertOpenAPITypeToBallerina(fieldSchema, true);
            // For openAPI field schemas having 'string' type, constraints generation will be skipped when
            // the counterpart Ballerina type is non-string (e.g. for string schemas with format 'binary' or 'byte',
            // the counterpart ballerina type is 'byte[]', hence any string constraints cannot be applied)
            if (ballerinaType.equals(STRING)) {
                // Attributes : maxLength, minLength
                return generateStringConstraint(fieldSchema);
            } else if (ballerinaType.equals(DECIMAL) || ballerinaType.equals(FLOAT) || ballerinaType.equals(INT) ||
                    ballerinaType.equals(INT_SIGNED32)) {
                // Attribute : minimum, maximum, exclusiveMinimum, exclusiveMaximum
                return generateNumberConstraint(fieldSchema);
            } else if (GeneratorUtils.isArraySchema(fieldSchema)) {
                // Attributes: maxItems, minItems
                return generateArrayConstraint(fieldSchema);
            }
            // Ignore Object, Map and Composed schemas.
            return null;
        }
        return null;
    }

    public static boolean isConstraintAllowed(String typeName, Schema schema) {

        boolean isConstraintNotAllowed = schema.getNullable() != null && schema.getNullable() ||
                (schema.getOneOf() != null || schema.getAnyOf() != null) || getOpenAPIType(schema) == null;
        boolean nullable = GeneratorMetaData.getInstance().isNullable();
        if (nullable) {
            return false;
        } else if (isConstraintNotAllowed) {
            //todo diagnostic
//            OUT_STREAM.printf("WARNING: constraints in the OpenAPI contract will be ignored for the " +
//                            "type `%s`, as constraints are not supported on Ballerina union types%n",
//                    typeName.trim());
            return false;
        }
        return true;
    }


    /**
     * Generate constraint for numbers : int, float, decimal.
     */
    private static AnnotationNode generateNumberConstraint(Schema<?> fieldSchema) {

        List<String> fields = getNumberAnnotFields(fieldSchema);
        if (fields.isEmpty()) {
            return null;
        }
        String annotBody = GeneratorConstants.OPEN_BRACE + String.join(GeneratorConstants.COMMA, fields) +
                GeneratorConstants.CLOSE_BRACE;
        AnnotationNode annotationNode;
        if (GeneratorUtils.isNumberSchema(fieldSchema)) {
            if (fieldSchema.getFormat() != null && fieldSchema.getFormat().equals(FLOAT)) {
                annotationNode = createAnnotationNode(GeneratorConstants.CONSTRAINT_FLOAT, annotBody);
            } else {
                annotationNode = createAnnotationNode(GeneratorConstants.CONSTRAINT_NUMBER, annotBody);
            }
        } else {
            annotationNode = createAnnotationNode(GeneratorConstants.CONSTRAINT_INT, annotBody);
        }
        return annotationNode;
    }

    /**
     * Generate constraint for string.
     */
    private static AnnotationNode generateStringConstraint(Schema<?> stringSchema) {

        List<String> fields = getStringAnnotFields(stringSchema);
        if (fields.isEmpty()) {
            return null;
        }
        String annotBody = GeneratorConstants.OPEN_BRACE + String.join(GeneratorConstants.COMMA, fields) +
                GeneratorConstants.CLOSE_BRACE;
        return createAnnotationNode(GeneratorConstants.CONSTRAINT_STRING, annotBody);
    }

    /**
     * Generate constraint for array.
     */
    private static AnnotationNode generateArrayConstraint(Schema arraySchema) {

        List<String> fields = getArrayAnnotFields(arraySchema);
        if (fields.isEmpty()) {
            return null;
        }
        String annotBody = GeneratorConstants.OPEN_BRACE + String.join(GeneratorConstants.COMMA, fields) +
                GeneratorConstants.CLOSE_BRACE;
        return createAnnotationNode(GeneratorConstants.CONSTRAINT_ARRAY, annotBody);
    }


    private static List<String> getNumberAnnotFields(Schema<?> numberSchema) {

        List<String> fields = new ArrayList<>();
        boolean isInt = GeneratorUtils.isIntegerSchema(numberSchema);
        if (numberSchema.getMinimum() != null && numberSchema.getExclusiveMinimum() == null) {
            String value = numberSchema.getMinimum().toString();
            String fieldRef = GeneratorConstants.MINIMUM + GeneratorConstants.COLON +
                    (isInt ? numberSchema.getMinimum().intValue() : value);
            fields.add(fieldRef);
        }
        if (numberSchema.getMaximum() != null && numberSchema.getExclusiveMaximum() == null) {
            String value = numberSchema.getMaximum().toString();
            String fieldRef = GeneratorConstants.MAXIMUM + GeneratorConstants.COLON +
                    (isInt ? numberSchema.getMaximum().intValue() : value);
            fields.add(fieldRef);
        }
        if (numberSchema.getExclusiveMinimum() != null &&
                numberSchema.getExclusiveMinimum() && numberSchema.getMinimum() != null) {
            String value = numberSchema.getMinimum().toString();
            String fieldRef = GeneratorConstants.EXCLUSIVE_MIN + GeneratorConstants.COLON +
                    (isInt ? numberSchema.getMinimum().intValue() : value);
            fields.add(fieldRef);
        }
        if (numberSchema.getMinimum() == null && numberSchema.getExclusiveMinimumValue() != null) {
            String value = numberSchema.getExclusiveMinimumValue().toString();
            String fieldRef = GeneratorConstants.EXCLUSIVE_MIN + GeneratorConstants.COLON +
                    (isInt ? numberSchema.getExclusiveMinimumValue().intValue() : value);
            fields.add(fieldRef);
        }
        if (numberSchema.getExclusiveMaximum() != null &&
                numberSchema.getExclusiveMaximum() && numberSchema.getMaximum() != null) {
            String value = numberSchema.getMaximum().toString();
            String fieldRef = GeneratorConstants.EXCLUSIVE_MAX + GeneratorConstants.COLON +
                    (isInt ? numberSchema.getMaximum().intValue() : value);
            fields.add(fieldRef);
        }
        if (numberSchema.getMaximum() == null && numberSchema.getExclusiveMaximumValue() != null) {
            String value = numberSchema.getExclusiveMaximumValue().toString();
            String fieldRef = GeneratorConstants.EXCLUSIVE_MAX + GeneratorConstants.COLON +
                    (isInt ? numberSchema.getExclusiveMaximumValue().intValue() : value);
            fields.add(fieldRef);
        }

        //TODO: This will be enable once constraint package gives this support.
//        if (numberSchema.getMultipleOf() != null) {
//            String value = numberSchema.getMultipleOf().toString();
//            String fieldRef = "multipleOf:" + value;
//            fields.add(fieldRef);
//        }
        return fields;
    }

    private static List<String> getStringAnnotFields(Schema stringSchema) {

        List<String> fields = new ArrayList<>();
        if (stringSchema.getMaxLength() != null && stringSchema.getMaxLength() != 0) {
            String value = stringSchema.getMaxLength().toString();
            String fieldRef = GeneratorConstants.MAX_LENGTH + GeneratorConstants.COLON + value;
            fields.add(fieldRef);
        }
        if (stringSchema.getMinLength() != null && stringSchema.getMinLength() != 0) {
            String value = stringSchema.getMinLength().toString();
            String fieldRef = GeneratorConstants.MIN_LENGTH + GeneratorConstants.COLON + value;
            fields.add(fieldRef);
        }
        if (stringSchema.getPattern() != null) {
            String value = stringSchema.getPattern();
            // This is to check whether the pattern is valid or not.
            // TODO: This temp fix will be removed with available with the new Regex API.
            // https://github.com/ballerina-platform/ballerina-lang/issues/40328
            // https://github.com/ballerina-platform/ballerina-lang/issues/40318
            try {
                Pattern.compile(value, Pattern.UNICODE_CHARACTER_CLASS);
                // Ballerina parser
                RegExpFactory.parse(value);
                String fieldRef = String.format("pattern: re`%s`", value);
                fields.add(fieldRef);
            } catch (BError err) {
                //TODO
                //This handle a case which Ballerina doesn't support
//                OUT_STREAM.printf("WARNING: skipped generation for unsupported pattern in ballerina: %s %n", value);
            } catch (Exception e) {
                // This try catch is to check whether the pattern is valid or not. Swagger parser doesn't provide any
                // error for invalid patterns. Therefore, we need to check it within code. (ex: syntax errors)
//                OUT_STREAM.printf("WARNING: skipped generation for non-ECMA flavoured pattern: %s %n", value);
            }
        }
        return fields;
    }

    private static List<String> getArrayAnnotFields(Schema arraySchema) {

        List<String> fields = new ArrayList<>();
        if (arraySchema.getMaxItems() != null && arraySchema.getMaxItems() != 0) {
            String value = arraySchema.getMaxItems().toString();
            String fieldRef = GeneratorConstants.MAX_LENGTH + GeneratorConstants.COLON + value;
            fields.add(fieldRef);
        }
        if (arraySchema.getMinItems() != null && arraySchema.getMinItems() != 0) {
            String value = arraySchema.getMinItems().toString();
            String fieldRef = GeneratorConstants.MIN_LENGTH + GeneratorConstants.COLON + value;
            fields.add(fieldRef);
        }
        return fields;
    }

    /**
     * This util create any annotation node by providing annotation reference and annotation body content.
     *
     * @param annotationReference Annotation reference value
     * @param annotFields         Annotation body content fields with single string
     * @return {@link AnnotationNode}
     */
    private static AnnotationNode createAnnotationNode(String annotationReference, String annotFields) {

        MappingConstructorExpressionNode annotationBody = null;
        SimpleNameReferenceNode annotReference = createSimpleNameReferenceNode(
                createIdentifierToken(annotationReference));
        ExpressionNode expressionNode = NodeParser.parseExpression(annotFields);
        if (expressionNode.kind() == MAPPING_CONSTRUCTOR) {
            annotationBody = (MappingConstructorExpressionNode) expressionNode;
        }
        return NodeFactory.createAnnotationNode(
                createToken(AT_TOKEN),
                annotReference,
                annotationBody);
    }
}
