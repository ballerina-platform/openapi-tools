/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.core.generators.schema;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.GeneratorConstants;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.document.DocCommentsGenerator;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.AllOfRecordTypeGenerator;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.AnyDataTypeGenerator;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.ArrayTypeGenerator;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.PrimitiveTypeGenerator;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.RecordTypeGenerator;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.ReferencedTypeGenerator;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.TypeGenerator;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.UnionTypeGenerator;
import io.ballerina.openapi.core.generators.schema.model.GeneratorMetaData;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
import static io.ballerina.compiler.syntax.tree.SyntaxKind.AT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.MAPPING_CONSTRUCTOR;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.core.GeneratorConstants.BALLERINA;
import static io.ballerina.openapi.core.GeneratorConstants.CONSTRAINT;

/**
 * Contains util functions needed for schema generation.
 *
 * @since 1.3.0
 */
public class TypeGeneratorUtils {
    private static final List<String> primitiveTypeList =
            new ArrayList<>(Arrays.asList(GeneratorConstants.INTEGER, GeneratorConstants.NUMBER,
                    GeneratorConstants.STRING, GeneratorConstants.BOOLEAN));

    /**
     * Get SchemaType object relevant to the schema given.
     *
     * @param schemaValue Schema object
     * @param typeName    parameter name
     * @return Relevant SchemaType object
     */
    public static TypeGenerator getTypeGenerator(Schema<?> schemaValue, String typeName, String parentName) {

        if (schemaValue.get$ref() != null) {
            return new ReferencedTypeGenerator(schemaValue, typeName);
        } else if (schemaValue instanceof ComposedSchema) {
            ComposedSchema composedSchema = (ComposedSchema) schemaValue;
            if (composedSchema.getAllOf() != null) {
                return new AllOfRecordTypeGenerator(schemaValue, typeName);
            } else {
                return new UnionTypeGenerator(schemaValue, typeName);
            }
        } else if ((schemaValue.getType() != null && schemaValue.getType().equals(GeneratorConstants.OBJECT)) ||
                schemaValue instanceof ObjectSchema || schemaValue.getProperties() != null ||
                schemaValue instanceof MapSchema) {
            return new RecordTypeGenerator(schemaValue, typeName);
        } else if (schemaValue instanceof ArraySchema) {
            return new ArrayTypeGenerator(schemaValue, typeName, parentName);
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
     * @param schema           Schema of the property
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
     * This function returns record field list with its relevant imports as set of string.
     * Ex: If the record field has a constraint annotation node, then the type.bal needs its constraint imports
     * therefore this function makes sure to add relevant imports to the type.bal.
     */
    public static ImmutablePair<List<Node>, Set<String>> updateRecordFieldListWithImports(
            List<String> required, List<Node> recordFieldList, Map.Entry<String, Schema<?>> field,
            Schema<?> fieldSchema, NodeList<Node> schemaDocNodes, IdentifierToken fieldName,
            TypeDescriptorNode fieldTypeName) {

        return updateRecordFieldListWithImports(required, recordFieldList, field, fieldSchema, schemaDocNodes,
                fieldName,
                fieldTypeName, System.err);
    }

    public static ImmutablePair<List<Node>, Set<String>> updateRecordFieldListWithImports(
            List<String> required, List<Node> recordFieldList, Map.Entry<String, Schema<?>> field,
            Schema<?> fieldSchema, NodeList<Node> schemaDocNodes, IdentifierToken fieldName,
            TypeDescriptorNode fieldTypeName, PrintStream outStream) {

        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(schemaDocNodes);
        Set<String> imports = new HashSet<>();
        //Generate constraint annotation.
        AnnotationNode constraintNode = generateConstraintNode(fieldSchema);
        MetadataNode metadataNode;
        boolean isConstraintSupport =
                constraintNode != null && fieldSchema.getNullable() != null && fieldSchema.getNullable() ||
                        (fieldSchema instanceof ComposedSchema && ((fieldSchema).getOneOf() != null ||
                                (fieldSchema).getAnyOf() != null));
        boolean nullable = GeneratorMetaData.getInstance().isNullable();
        if (nullable) {
            constraintNode = null;
        } else if (isConstraintSupport) {
            outStream.printf("WARNING: constraints in the OpenAPI contract will be ignored for the " +
                            "field `%s`, as constraints are not supported on Ballerina union types%n",
                    fieldName.toString().trim());
            constraintNode = null;
        }
        if (constraintNode == null) {
            metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        } else {
            metadataNode = createMetadataNode(documentationNode, createNodeList(constraintNode));
        }

        if (required != null) {
            setRequiredFields(required, recordFieldList, field, fieldSchema, fieldName, fieldTypeName, metadataNode);
        } else {
            RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(metadataNode, null,
                    fieldTypeName, fieldName, createToken(QUESTION_MARK_TOKEN), createToken(SEMICOLON_TOKEN));
            recordFieldList.add(recordFieldNode);
        }
        if (constraintNode != null) {
            ImportDeclarationNode constraintImport = GeneratorUtils.getImportDeclarationNode(BALLERINA, CONSTRAINT);
            constraintImport.toSourceCode();
            imports.add(constraintImport.toSourceCode());
        }
        return new ImmutablePair<>(recordFieldList, imports);
    }

    private static void setRequiredFields(List<String> required, List<Node> recordFieldList,
                                          Map.Entry<String, Schema<?>> field,
                                          Schema<?> fieldSchema,
                                          IdentifierToken fieldName,
                                          TypeDescriptorNode fieldTypeName,
                                          MetadataNode metadataNode) {

        if (!required.contains(field.getKey().trim())) {
            if (fieldSchema.getDefault() != null) {
                RecordFieldWithDefaultValueNode defaultNode =
                        getRecordFieldWithDefaultValueNode(fieldSchema, fieldName, fieldTypeName, metadataNode);
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
    }

    private static RecordFieldWithDefaultValueNode getRecordFieldWithDefaultValueNode(Schema<?> fieldSchema,
                                                                                      IdentifierToken fieldName,
                                                                                      TypeDescriptorNode fieldTypeName,
                                                                                      MetadataNode metadataNode) {

        Token defaultValueToken;
        String defaultValue = fieldSchema.getDefault().toString().trim();
        if (fieldSchema instanceof StringSchema) {
            if (defaultValue.equals("\"")) {
                defaultValueToken = AbstractNodeFactory.createIdentifierToken("\"" + "\\" +
                        fieldSchema.getDefault().toString() + "\"");
            } else {
                defaultValueToken = AbstractNodeFactory.createIdentifierToken("\"" +
                        fieldSchema.getDefault().toString() + "\"");
            }
        } else if (!defaultValue.matches("^[0-9]*$") && !defaultValue.matches("^(\\d*\\.)?\\d+$")
                && !(defaultValue.startsWith("[") && defaultValue.endsWith("]")) &&
                !(fieldSchema instanceof BooleanSchema)) {
            //This regex was added due to avoid adding quotes for default values which are numbers and array values.
            //Ex: default: 123
            defaultValueToken = AbstractNodeFactory.createIdentifierToken("\"" +
                    fieldSchema.getDefault().toString() + "\"");
        } else {
            defaultValueToken = AbstractNodeFactory.createIdentifierToken(fieldSchema.getDefault().toString());
        }
        ExpressionNode expressionNode = createRequiredExpressionNode(defaultValueToken);
        return NodeFactory.createRecordFieldWithDefaultValueNode
                (metadataNode, null, fieldTypeName, fieldName, createToken(EQUAL_TOKEN),
                        expressionNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * This util is to set the constraint validation for given data type in the record field and user define type.
     *
     * @param fieldSchema Schema for data type
     * @return {@link MetadataNode}
     */
    public static AnnotationNode generateConstraintNode(Schema<?> fieldSchema) {

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
        String annotBody = GeneratorConstants.OPEN_BRACE + String.join(GeneratorConstants.COMMA, fields) +
                GeneratorConstants.CLOSE_BRACE;
        AnnotationNode annotationNode;
        if (fieldSchema instanceof NumberSchema) {
            if (fieldSchema.getFormat() != null && fieldSchema.getFormat().equals(GeneratorConstants.FLOAT)) {
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
    private static AnnotationNode generateStringConstraint(StringSchema stringSchema) {

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
    private static AnnotationNode generateArrayConstraint(ArraySchema arraySchema) {

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
        boolean isInt = numberSchema instanceof IntegerSchema;
        if (numberSchema.getMinimum() != null &&
                BigDecimal.ZERO.compareTo(numberSchema.getMinimum()) != 0
                && numberSchema.getExclusiveMinimum() == null) {
            String value = numberSchema.getMinimum().toString();
            String fieldRef = GeneratorConstants.MINIMUM + GeneratorConstants.COLON +
                    (isInt ? numberSchema.getMinimum().intValue() : value);
            fields.add(fieldRef);
        }
        if (numberSchema.getMaximum() != null &&
                BigDecimal.ZERO.compareTo(numberSchema.getMaximum()) != 0
                && numberSchema.getExclusiveMaximum() == null) {
            String value = numberSchema.getMaximum().toString();
            String fieldRef = GeneratorConstants.MAXIMUM + GeneratorConstants.COLON +
                    (isInt ? numberSchema.getMaximum().intValue() : value);
            fields.add(fieldRef);
        }
        if (numberSchema.getExclusiveMinimum() != null &&
                numberSchema.getExclusiveMinimum() && numberSchema.getMinimum() != null &&
                BigDecimal.ZERO.compareTo(numberSchema.getMinimum()) != 0) {
            String value = numberSchema.getMinimum().toString();
            String fieldRef = GeneratorConstants.EXCLUSIVE_MIN + GeneratorConstants.COLON +
                    (isInt ? numberSchema.getMinimum().intValue() : value);
            fields.add(fieldRef);
        }
        if (numberSchema.getExclusiveMaximum() != null &&
                numberSchema.getExclusiveMaximum() &&
                numberSchema.getMaximum() != null && BigDecimal.ZERO.compareTo(numberSchema.getMaximum()) != 0) {
            String value = numberSchema.getMaximum().toString();
            String fieldRef = GeneratorConstants.EXCLUSIVE_MAX + GeneratorConstants.COLON +
                    (isInt ? numberSchema.getMaximum().intValue() : value);
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
            String componentName = GeneratorUtils.getValidName(split[split.length - 1], true);
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
            String typeName = GeneratorUtils.getValidName(GeneratorUtils.extractReferenceType(
                    schemaValue.get$ref()), true);
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
}
