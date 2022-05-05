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
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
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
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.generators.GeneratorConstants.BOOLEAN;
import static io.ballerina.openapi.generators.GeneratorConstants.INTEGER;
import static io.ballerina.openapi.generators.GeneratorConstants.NUMBER;
import static io.ballerina.openapi.generators.GeneratorConstants.OBJECT;
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
            List<Node> schemaDoc = getFieldApiDocs(field.getValue());
            NodeList<Node> schemaDocNodes = createNodeList(schemaDoc);
            IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(fieldNameStr);
            TypeDescriptorNode fieldTypeName = getTypeGenerator(field.getValue(), fieldNameStr)
                    .generateTypeDescriptorNode();
            MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(schemaDocNodes);
            MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
            if (required != null) {
                if (!required.contains(field.getKey().trim())) {
                    if (field.getValue().getDefault() != null) {
                        Token defaultValue;
                        if ((field.getValue().getType()).equals(STRING)) {
                            defaultValue = AbstractNodeFactory.createIdentifierToken("\"" +
                                    field.getValue().getDefault().toString() + "\"");
                        } else {
                            defaultValue = AbstractNodeFactory.createIdentifierToken
                                    (field.getValue().getDefault().toString());
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
