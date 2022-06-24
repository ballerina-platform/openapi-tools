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

package io.ballerina.openapi.generators.schema;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.DocCommentsGenerator;
import io.ballerina.openapi.generators.schema.ballerinatypegenerators.AllOfRecordTypeGenerator;
import io.ballerina.openapi.generators.schema.ballerinatypegenerators.AnyDataTypeGenerator;
import io.ballerina.openapi.generators.schema.ballerinatypegenerators.ArrayTypeGenerator;
import io.ballerina.openapi.generators.schema.ballerinatypegenerators.PrimitiveTypeGenerator;
import io.ballerina.openapi.generators.schema.ballerinatypegenerators.RecordTypeGenerator;
import io.ballerina.openapi.generators.schema.ballerinatypegenerators.ReferencedTypeGenerator;
import io.ballerina.openapi.generators.schema.ballerinatypegenerators.TypeGenerator;
import io.ballerina.openapi.generators.schema.ballerinatypegenerators.UnionTypeGenerator;
import io.ballerina.openapi.generators.schema.model.GeneratorMetaData;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.AT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.MAPPING_CONSTRUCTOR;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.generators.GeneratorConstants.BOOLEAN;
import static io.ballerina.openapi.generators.GeneratorConstants.CLOSE_BRACE;
import static io.ballerina.openapi.generators.GeneratorConstants.COLON;
import static io.ballerina.openapi.generators.GeneratorConstants.COMMA;
import static io.ballerina.openapi.generators.GeneratorConstants.CONSTRAINT_ARRAY;
import static io.ballerina.openapi.generators.GeneratorConstants.CONSTRAINT_FLOAT;
import static io.ballerina.openapi.generators.GeneratorConstants.CONSTRAINT_INT;
import static io.ballerina.openapi.generators.GeneratorConstants.CONSTRAINT_NUMBER;
import static io.ballerina.openapi.generators.GeneratorConstants.CONSTRAINT_STRING;
import static io.ballerina.openapi.generators.GeneratorConstants.EXCLUSIVE_MAX;
import static io.ballerina.openapi.generators.GeneratorConstants.EXCLUSIVE_MIN;
import static io.ballerina.openapi.generators.GeneratorConstants.FLOAT;
import static io.ballerina.openapi.generators.GeneratorConstants.INTEGER;
import static io.ballerina.openapi.generators.GeneratorConstants.MAXIMUM;
import static io.ballerina.openapi.generators.GeneratorConstants.MAX_LENGTH;
import static io.ballerina.openapi.generators.GeneratorConstants.MINIMUM;
import static io.ballerina.openapi.generators.GeneratorConstants.MIN_LENGTH;
import static io.ballerina.openapi.generators.GeneratorConstants.NUMBER;
import static io.ballerina.openapi.generators.GeneratorConstants.OBJECT;
import static io.ballerina.openapi.generators.GeneratorConstants.OPEN_BRACE;
import static io.ballerina.openapi.generators.GeneratorConstants.STRING;
import static io.ballerina.openapi.generators.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.generators.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;

/**
 * Contains util functions needed for schema generation.
 *
 * @since 2.0.0
 */
public class TypeGeneratorUtils {
    private static final List<String> primitiveTypeList =
            new ArrayList<>(Arrays.asList(INTEGER, NUMBER, STRING, BOOLEAN));

    /**
     * Get SchemaType object relevant to the schema given.
     *
     * @param schemaValue Schema object
     * @param typeName parameter name
     * @return Relevant SchemaType object
     */
    public static TypeGenerator getTypeGenerator(Schema<?> schemaValue, String typeName) {
        if (schemaValue.get$ref() != null) {
            return new ReferencedTypeGenerator(schemaValue, typeName);
        } else if (schemaValue instanceof ComposedSchema) {
            ComposedSchema composedSchema = (ComposedSchema) schemaValue;
            if (composedSchema.getAllOf() != null) {
                return new AllOfRecordTypeGenerator(schemaValue, typeName);
            } else {
                return new UnionTypeGenerator(schemaValue, typeName);
            }
        } else if ((schemaValue.getType() != null && schemaValue.getType().equals(OBJECT)) ||
                schemaValue instanceof ObjectSchema || schemaValue.getProperties() != null) {
            return new RecordTypeGenerator(schemaValue, typeName);
        } else if (schemaValue instanceof ArraySchema) {
            return new ArrayTypeGenerator(schemaValue, typeName);
        } else if (schemaValue.getType() != null && primitiveTypeList.contains(schemaValue.getType())) {
            return new PrimitiveTypeGenerator(schemaValue, typeName);
        } else { // when schemaValue.type == null
            return new AnyDataTypeGenerator(schemaValue, typeName);
        }
    }

    /**
     * Generate proper type name considering the nullable configurations.
     * Scenario 1 : schema.getNullable() != null && schema.getNullable() == true && nullable == true -> string?
     * Scenario 2 : schema.getNullable() != null && schema.getNullable() == true && nullable == false -> string?
     * Scenario 3 : schema.getNullable() != null && schema.getNullable() == false && nullable == false -> string
     * Scenario 4 : schema.getNullable() != null && schema.getNullable() == false && nullable == true -> string
     * Scenario 5 : schema.getNullable() == null && nullable == true -> string?
     * Scenario 6 : schema.getNullable() == null && nullable == false -> string
     *
     * @param schema Schema of the property
     * @param originalTypeDesc Type name
     * @return Final type of the field
     */
    public static TypeDescriptorNode getNullableType(Schema schema, TypeDescriptorNode originalTypeDesc) {
        TypeDescriptorNode nillableType = originalTypeDesc;
        boolean nullable = GeneratorMetaData.getInstance().isNullable();
        if (schema.getNullable() != null) {
            if (schema.getNullable()) {
                nillableType = createOptionalTypeDescriptorNode(originalTypeDesc, createToken(QUESTION_MARK_TOKEN));
            }
        } else if (nullable) {
            nillableType = createOptionalTypeDescriptorNode(originalTypeDesc, createToken(QUESTION_MARK_TOKEN));
        }
        return nillableType;
    }

    /**
     * This util for generating record field with given schema properties.
     */
    public static List<Node> addRecordFields(List<String> required, Set<Map.Entry<String, Schema>> fields)
            throws BallerinaOpenApiException {
        // TODO: Handle allOf , oneOf, anyOf
        List<Node> recordFieldList = new ArrayList<>();
        for (Map.Entry<String, Schema> field : fields) {
            String fieldNameStr = escapeIdentifier(field.getKey().trim());
            // API doc generations
            Schema fieldSchema = field.getValue();
            List<Node> schemaDoc = getFieldApiDocs(fieldSchema);
            NodeList<Node> schemaDocNodes = createNodeList(schemaDoc);
            IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(fieldNameStr);
            TypeDescriptorNode fieldTypeName = getTypeGenerator(fieldSchema, fieldNameStr)
                    .generateTypeDescriptorNode();
            MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(schemaDocNodes);
            //Generate constraint annotation.
            AnnotationNode constraintNode = addConstraint(fieldSchema);
            MetadataNode metadataNode;
            if (constraintNode == null) {
                metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
            } else {
                metadataNode = createMetadataNode(documentationNode, createNodeList(constraintNode));
            }

            if (required != null) {
                if (!required.contains(field.getKey().trim())) {
                    if (fieldSchema.getDefault() != null) {
                        Token defaultValue;
                        if (fieldSchema instanceof StringSchema) {
                            if (fieldSchema.getDefault().toString().trim().equals("\"")) {
                                defaultValue = AbstractNodeFactory.createIdentifierToken("\"" + "\\" +
                                        fieldSchema.getDefault().toString() + "\"");
                            } else {
                                defaultValue = AbstractNodeFactory.createIdentifierToken("\"" +
                                        fieldSchema.getDefault().toString() + "\"");
                            }
                        } else {
                            defaultValue = AbstractNodeFactory.createIdentifierToken
                                    (fieldSchema.getDefault().toString());
                        }
                        ExpressionNode expressionNode = createRequiredExpressionNode(defaultValue);
                        RecordFieldWithDefaultValueNode defaultNode = NodeFactory.createRecordFieldWithDefaultValueNode
                                (metadataNode, null, fieldTypeName, fieldName, createToken(EQUAL_TOKEN),
                                        expressionNode, createToken(SEMICOLON_TOKEN));
                        recordFieldList.add(defaultNode);
                    } else {
                        RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(metadataNode, null,
                                fieldTypeName, fieldName, createToken(QUESTION_MARK_TOKEN),
                                createToken(SEMICOLON_TOKEN));
                        recordFieldList.add(recordFieldNode);
                    }
                } else {
                    RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(metadataNode, null,
                            fieldTypeName, fieldName, null, createToken(SEMICOLON_TOKEN));
                    recordFieldList.add(recordFieldNode);
                }
            } else {
                RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(metadataNode, null,
                        fieldTypeName, fieldName, createToken(QUESTION_MARK_TOKEN), createToken(SEMICOLON_TOKEN));
                recordFieldList.add(recordFieldNode);
            }
        }
        return recordFieldList;

    }

    /**
     * This util is to set the constraint validation for given data type in the record field and user define type.
     *
     * @param fieldSchema Schema for data type
     * @return {@link MetadataNode}
     */
    public static AnnotationNode addConstraint(Schema<?> fieldSchema) {
        if (fieldSchema instanceof StringSchema) {
            StringSchema stringSchema = (StringSchema) fieldSchema;
            // Attributes : maxLength, minLength
            return generateStringConstraint(stringSchema);
        } else if (fieldSchema instanceof IntegerSchema || fieldSchema instanceof NumberSchema) {
            // Attribute : minimum, maximum, exclusiveMinimum, exclusiveMaximum
            return generateNumberConstraint(fieldSchema);
        } else if (fieldSchema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) fieldSchema;
            // Attributes: maxItems, minItems
            return generateArrayConstraint(arraySchema);
        }
        // Ignore Object, Map and Composed schemas.
        return null;
    }

    /**
     * Generate constraint for numbers : int, float, decimal.
     */
    private static AnnotationNode generateNumberConstraint(Schema<?> fieldSchema) {
        List<String> fields = getNumberAnnotFields(fieldSchema);
        if (fields.isEmpty()) {
            return null;
        }
        String annotBody = OPEN_BRACE + String.join(COMMA, fields) + CLOSE_BRACE;
        AnnotationNode annotationNode = createAnnotationNode(CONSTRAINT_INT, annotBody);

        if (fieldSchema instanceof NumberSchema) {
            if (fieldSchema.getFormat() != null && fieldSchema.getFormat().equals(FLOAT)) {
                annotationNode = createAnnotationNode(CONSTRAINT_FLOAT, annotBody);
            } else {
                annotationNode = createAnnotationNode(CONSTRAINT_NUMBER, annotBody);
            }
        }
        return annotationNode;
    }

    /**
     * Generate constraint for string.
     */
    private static AnnotationNode generateStringConstraint(StringSchema stringSchema) {
        List<String> fields = getStringAnnotFields(stringSchema);
        if (fields.isEmpty()) {
            return null;
        }
        String annotBody = OPEN_BRACE + String.join(COMMA, fields) + CLOSE_BRACE;
        return createAnnotationNode(CONSTRAINT_STRING, annotBody);
    }

    /**
     * Generate constraint for array.
     */
    private static AnnotationNode generateArrayConstraint(ArraySchema arraySchema) {
        List<String> fields = getArrayAnnotFields(arraySchema);
        if (fields.isEmpty()) {
            return null;
        }
        String annotBody = OPEN_BRACE + String.join(COMMA, fields) + CLOSE_BRACE;
        return createAnnotationNode(CONSTRAINT_ARRAY, annotBody);
    }

    private static List<String> getNumberAnnotFields(Schema<?> numberSchema) {
        List<String> fields = new ArrayList<>();
        if (numberSchema.getMinimum() != null && numberSchema.getExclusiveMinimum() == null) {
            String value = numberSchema.getMinimum().toString();
            String fieldRef = MINIMUM + COLON + value;
            fields.add(fieldRef);
        }
        if (numberSchema.getMaximum() != null && numberSchema.getExclusiveMaximum() == null) {
            String value = numberSchema.getMaximum().toString();
            String fieldRef = MAXIMUM + COLON + value;
            fields.add(fieldRef);
        }
        if (numberSchema.getExclusiveMinimum() != null &&
                numberSchema.getExclusiveMinimum() && numberSchema.getMaximum() != null) {
            String value = numberSchema.getMinimum().toString();
            String fieldRef = EXCLUSIVE_MIN + COLON + value;
            fields.add(fieldRef);
        }
        if (numberSchema.getExclusiveMaximum() != null &&
                numberSchema.getExclusiveMaximum() &&
                numberSchema.getMaximum() != null) {
            String value = numberSchema.getMaximum().toString();
            String fieldRef = EXCLUSIVE_MAX + COLON + value;
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

    private static List<String> getStringAnnotFields(StringSchema stringSchema) {
        List<String> fields = new ArrayList<>();
        if (stringSchema.getMaxLength() != null) {
            String value = stringSchema.getMaxLength().toString();
            String fieldRef = MAX_LENGTH + COLON + value;
            fields.add(fieldRef);
        }
        if (stringSchema.getMinLength() != null) {
            String value = stringSchema.getMinLength().toString();
            String fieldRef = MIN_LENGTH + COLON + value;
            fields.add(fieldRef);
        }
        //TODO: This will be enable once constraint package gives this support.
//        if (stringSchema.getPattern() != null) {
//            String value = stringSchema.getPattern();
//            String fieldRef = "pattern:" + "\"" + value + "\"";
//            fields.add(fieldRef);
//        }
        return fields;
    }

    private static List<String> getArrayAnnotFields(ArraySchema arraySchema) {
        List<String> fields = new ArrayList<>();
        if (arraySchema.getMaxItems() != null) {
            String value = arraySchema.getMaxItems().toString();
            String fieldRef = MAX_LENGTH + COLON + value;
            fields.add(fieldRef);
        }
        if (arraySchema.getMinItems() != null) {
            String value = arraySchema.getMinItems().toString();
            String fieldRef = MIN_LENGTH + COLON + value;
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

    /**
     * Creates API documentation for record fields.
     *
     * @param field Schema of the field to generate
     * @return Documentation node list
     */
    public static List<Node> getFieldApiDocs(Schema<?> field) {
        List<Node> schemaDoc = new ArrayList<>();
        if (field.getDescription() != null) {
            schemaDoc.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                    field.getDescription(), false));
        } else if (field.get$ref() != null) {
            String[] split = field.get$ref().trim().split("/");
            String componentName = getValidName(split[split.length - 1], true);
            OpenAPI openAPI = GeneratorMetaData.getInstance().getOpenAPI();
            if (openAPI.getComponents().getSchemas().get(componentName) != null) {
                Schema<?> schema = openAPI.getComponents().getSchemas().get(componentName);
                if (schema.getDescription() != null) {
                    schemaDoc.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                            schema.getDescription(), false));
                }
            }
        }
        return schemaDoc;
    }

    /**
     * Creates record documentation.
     *
     * @param documentation   Documentation node list
     * @param schemaValue     OpenAPI schema
     * @param typeAnnotations Annotation list of the record
     */
    public static void getRecordDocs(List<Node> documentation, Schema schemaValue,
                                     List<AnnotationNode> typeAnnotations) throws BallerinaOpenApiException {
        if (schemaValue.getDescription() != null) {
            documentation.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                    schemaValue.getDescription(), false));
        } else if (schemaValue.get$ref() != null) {
            String typeName = getValidName(extractReferenceType(schemaValue.get$ref()), true);
            Schema<?> refSchema = GeneratorMetaData.getInstance().getOpenAPI().
                    getComponents().getSchemas().get(typeName);
            if (refSchema.getDescription() != null) {
                documentation.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                        refSchema.getDescription(), false));
            }
        }
        if (schemaValue.getDeprecated() != null && schemaValue.getDeprecated()) {
            DocCommentsGenerator.extractDeprecatedAnnotation(schemaValue.getExtensions(),
                    documentation, typeAnnotations);
        }
    }

    /**
     * Creates the UnionType string to generate bal type for a given oneOf or anyOf type schema.
     *
     * @param schemas  List of schemas included in the anyOf or oneOf schema
     * @param typeName This is parameter or variable name that used to populate error message meaningful
     * @return Union type
     * @throws BallerinaOpenApiException when unsupported combination of schemas found
     */
    public static TypeDescriptorNode getUnionType(List<Schema> schemas, String typeName)
            throws BallerinaOpenApiException {
        List<TypeDescriptorNode> typeDescriptorNodes = new ArrayList<>();
        for (Schema schema : schemas) {
            TypeDescriptorNode typeDescriptorNode = getTypeGenerator(schema, typeName).generateTypeDescriptorNode();
            if (typeDescriptorNode instanceof OptionalTypeDescriptorNode &&
                    GeneratorMetaData.getInstance().isNullable()) {
                Node internalTypeDesc = ((OptionalTypeDescriptorNode) typeDescriptorNode).typeDescriptor();
                typeDescriptorNode = (TypeDescriptorNode) internalTypeDesc;
            }
            typeDescriptorNodes.add(typeDescriptorNode);
        }
        if (typeDescriptorNodes.size() > 1) {
            UnionTypeDescriptorNode unionTypeDescriptorNode = null;
            TypeDescriptorNode leftTypeDesc = typeDescriptorNodes.get(0);
            for (int i = 1; i < typeDescriptorNodes.size(); i++) {
                TypeDescriptorNode rightTypeDesc = typeDescriptorNodes.get(i);
                unionTypeDescriptorNode = createUnionTypeDescriptorNode(leftTypeDesc, createToken(PIPE_TOKEN),
                        rightTypeDesc);
                leftTypeDesc = unionTypeDescriptorNode;
            }
            return unionTypeDescriptorNode;
        } else {
            return typeDescriptorNodes.get(0);
        }
    }
}
