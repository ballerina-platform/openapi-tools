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

package io.ballerina.openapi.core.typegenerator.generators;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.typegenerator.GeneratorConstants;
import io.ballerina.openapi.core.typegenerator.GeneratorUtils;
import io.ballerina.openapi.core.typegenerator.TypeGenerationDiagnosticMessages;
import io.ballerina.openapi.core.typegenerator.TypeGeneratorUtils;
import io.ballerina.openapi.core.typegenerator.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.typegenerator.model.GeneratorMetaData;
import io.ballerina.openapi.core.typegenerator.model.TypeDescriptorReturnType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesisedTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for array schemas.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *     Pets:
 *       type: array
 *       items:
 *         $ref: "#/components/schemas/Pet"
 *  </pre>
 * Generated Ballerina type for the schema `Pet` :
 * <pre>
 *      public type Pets Pet[];
 * </pre>
 *
 * @since 1.3.0
 */
public class ArrayTypeGenerator extends TypeGenerator {
    private String parentType;
    private static final List<String> queryParamSupportedTypes =
            new ArrayList<>(Arrays.asList(GeneratorConstants.INTEGER, GeneratorConstants.NUMBER,
                    GeneratorConstants.STRING, GeneratorConstants.BOOLEAN, GeneratorConstants.OBJECT));

    public ArrayTypeGenerator(Schema schema, String typeName, String parentType, HashMap<String, TypeDefinitionNode> subTypesMap, HashMap<String, NameReferenceNode> pregeneratedTypeMap) {
        super(schema, typeName, subTypesMap, pregeneratedTypeMap);
        this.parentType = parentType;
    }

    /**
     * Generate TypeDescriptorNode for array type schemas. If array type is not given, type will be `AnyData`
     * public type StringArray string[];
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {

        Schema<?> items = schema.getItems();
        boolean isConstraintsAvailable =
                !GeneratorMetaData.getInstance().isNullable() && GeneratorUtils.hasConstraints(items) && typeName != null;
        TypeGenerator typeGenerator;
        if (isConstraintsAvailable) {
            String normalizedTypeName = typeName.replaceAll(GeneratorConstants.SPECIAL_CHARACTER_REGEX, "").trim();
            List<AnnotationNode> typeAnnotations = new ArrayList<>();
            AnnotationNode constraintNode = null;//TypeGeneratorUtils.generateConstraintNode(typeName, items);
            if (constraintNode != null) {
                typeAnnotations.add(constraintNode);
            }
            typeName = GeneratorUtils.getValidName(
                    parentType != null ?
                            parentType + "-" + normalizedTypeName + "-Items-" + GeneratorUtils.getOpenAPIType(items) :
                            normalizedTypeName + "-Items-" + GeneratorUtils.getOpenAPIType(items),
                    true);
            typeGenerator = TypeGeneratorUtils.getTypeGenerator(items, typeName, null, subTypesMap, pregeneratedTypeMap);
            if (!pregeneratedTypeMap.containsKey(typeName)) {
                pregeneratedTypeMap.put(typeName, createSimpleNameReferenceNode(createIdentifierToken(typeName)));
                TypeDefinitionNode arrayItemWithConstraint = typeGenerator.generateTypeDefinitionNode(
                        createIdentifierToken(typeName),
                        typeAnnotations);
                imports.addAll(typeGenerator.getImports());
                subTypesMap.put(typeName, arrayItemWithConstraint);
            }
        } else {
            typeGenerator = TypeGeneratorUtils.getTypeGenerator(items, typeName, null, subTypesMap, pregeneratedTypeMap);
        }

        TypeDescriptorNode typeDescriptorNode;
        if ((typeGenerator instanceof PrimitiveTypeGenerator ||
                typeGenerator instanceof ArrayTypeGenerator) && isConstraintsAvailable) {
            typeDescriptorNode = NodeParser.parseTypeDescriptor(typeName);
        } else {
            typeDescriptorNode = typeGenerator.generateTypeDescriptorNode();
        }

        if (typeGenerator instanceof UnionTypeGenerator || (items.getEnum() != null && items.getEnum().size() > 0)) {
            typeDescriptorNode = createParenthesisedTypeDescriptorNode(
                    createToken(OPEN_PAREN_TOKEN), typeDescriptorNode, createToken(CLOSE_PAREN_TOKEN));
        }
        if (typeDescriptorNode instanceof OptionalTypeDescriptorNode) {
            Node node = ((OptionalTypeDescriptorNode) typeDescriptorNode).typeDescriptor();
            typeDescriptorNode = (TypeDescriptorNode) node;
        }

        if (schema.getMaxItems() != null) {
            if (schema.getMaxItems() > GeneratorConstants.MAX_ARRAY_LENGTH) {
                throw new BallerinaOpenApiException("Maximum item count defined in the definition exceeds the " +
                        "maximum ballerina array length.");
            }
        }
        NodeList<ArrayDimensionNode> arrayDimensions = NodeFactory.createEmptyNodeList();
        if (typeDescriptorNode.kind() == SyntaxKind.ARRAY_TYPE_DESC) {
            ArrayTypeDescriptorNode innerArrayType = (ArrayTypeDescriptorNode) typeDescriptorNode;
            arrayDimensions = innerArrayType.dimensions();
            typeDescriptorNode = innerArrayType.memberTypeDesc();
        }

        ArrayDimensionNode arrayDimension = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        arrayDimensions = arrayDimensions.add(arrayDimension);
        ArrayTypeDescriptorNode arrayTypeDescriptorNode = createArrayTypeDescriptorNode(typeDescriptorNode,
                arrayDimensions);
        imports.addAll(typeGenerator.getImports());
        return TypeGeneratorUtils.getNullableType(schema, arrayTypeDescriptorNode);
    }

    /**
     * Generate {@code TypeDescriptorNode} for ArraySchema in OAS.
     */
    public Optional<TypeDescriptorNode> getTypeDescNodeForArraySchema(Schema schema, HashMap<String, TypeDefinitionNode> subTypesMap)
            throws BallerinaOpenApiException {
        TypeDescriptorNode member;
        String schemaType = GeneratorUtils.getOpenAPIType(schema.getItems());
        if (schema.getItems().get$ref() != null) {
            String typeName = GeneratorUtils.getValidName(GeneratorUtils.extractReferenceType(schema.getItems().get$ref()), true);
            String validTypeName = GeneratorUtils.escapeIdentifier(typeName);
            TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(
                    GeneratorMetaData.getInstance().getOpenAPI().getComponents().getSchemas().get(typeName), validTypeName, null, subTypesMap, pregeneratedTypeMap);
            if (!pregeneratedTypeMap.containsKey(validTypeName)) {
                pregeneratedTypeMap.put(validTypeName, createSimpleNameReferenceNode(createIdentifierToken(validTypeName)));
                TypeDefinitionNode typeDefinitionNode = typeGenerator.generateTypeDefinitionNode(
                        createIdentifierToken(validTypeName), new ArrayList<>());
                subTypesMap.put(validTypeName, typeDefinitionNode);
            }
            member = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(GeneratorUtils.escapeIdentifier(GeneratorUtils.getValidName(
                            GeneratorUtils.extractReferenceType(schema.getItems().get$ref()), true))));
        } else if (schemaType != null && (schemaType.equals(GeneratorConstants.INTEGER) || schemaType.equals(GeneratorConstants.NUMBER) ||
                schemaType.equals(GeneratorConstants.BOOLEAN) || schemaType.equals(GeneratorConstants.STRING))) {
            member = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(
                    GeneratorUtils.convertOpenAPITypeToBallerina(schema.getItems())));
        } else {
            return Optional.empty();
        }
        return Optional.ofNullable(getArrayTypeDescriptorNodeFromTypeDescriptorNode(member));
    }

    public static TypeDescriptorReturnType getArrayTypeDescriptorNode(Schema<?> items) throws BallerinaOpenApiException {
        HashMap<String, TypeDefinitionNode> subtypesMap = new HashMap<>();
        ArrayTypeDescriptorNode typeDescriptorNode =
                getArrayTypeDescriptorNode(GeneratorMetaData.getInstance().getOpenAPI(), items, subtypesMap, new HashMap<>());
        return new TypeDescriptorReturnType(Optional.of(typeDescriptorNode), subtypesMap);
    }

    private static ArrayTypeDescriptorNode getArrayTypeDescriptorNode(OpenAPI openAPI, Schema<?> items, HashMap<String, TypeDefinitionNode> subTypesMap, HashMap<String, NameReferenceNode> pregeneratedTypeMap) throws BallerinaOpenApiException {
        String arrayName;
        if (items.get$ref() != null) {
            String referenceType = GeneratorUtils.extractReferenceType(items.get$ref());
            String type = GeneratorUtils.getValidName(referenceType, true);
            Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type);
            TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(refSchema, type, null, subTypesMap, pregeneratedTypeMap);
            if (!pregeneratedTypeMap.containsKey(type)) {
                pregeneratedTypeMap.put(type, createSimpleNameReferenceNode(createIdentifierToken(type)));
                TypeDefinitionNode typeDefinitionNode = typeGenerator.generateTypeDefinitionNode(
                        createIdentifierToken(type), new ArrayList<>());
                subTypesMap.put(type, typeDefinitionNode);
            }
            if (queryParamSupportedTypes.contains(GeneratorUtils.getOpenAPIType(refSchema))) {
                arrayName = type;
            } else {
                TypeGenerationDiagnosticMessages messages = TypeGenerationDiagnosticMessages.OAS_SERVICE_102;
                throw new BallerinaOpenApiException(String.format(messages.getDescription(), type));
            }
        } else {
            arrayName = GeneratorUtils.convertOpenAPITypeToBallerina(items);
            if (items.getEnum() != null && !items.getEnum().isEmpty()) {
                arrayName = OPEN_PAREN_TOKEN.stringValue() + arrayName + CLOSE_PAREN_TOKEN.stringValue();
            }
        }
        Token arrayNameToken = createIdentifierToken(arrayName, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        BuiltinSimpleNameReferenceNode memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, arrayNameToken);
        ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        NodeList<ArrayDimensionNode> nodeList = createNodeList(dimensionNode);

        if (items.getNullable() != null && items.getNullable() && items.getEnum() == null) {
            // generate -> int?[]
            OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(memberTypeDesc,
                    createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            return createArrayTypeDescriptorNode(optionalNode, nodeList);
        }
        // generate -> int[]
        return createArrayTypeDescriptorNode(memberTypeDesc, nodeList);
    }

    public static ArrayTypeDescriptorNode getArrayTypeDescriptorNodeFromTypeDescriptorNode(TypeDescriptorNode typeDescriptorNode) {
        ArrayDimensionNode arrayDimensionNode = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        NodeList<ArrayDimensionNode> nodeList = createNodeList(arrayDimensionNode);
        return createArrayTypeDescriptorNode(typeDescriptorNode, nodeList);
    }
}
