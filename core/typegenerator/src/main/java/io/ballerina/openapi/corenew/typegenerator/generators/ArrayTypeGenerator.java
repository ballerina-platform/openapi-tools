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

package io.ballerina.openapi.corenew.typegenerator.generators;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.corenew.typegenerator.GeneratorConstants;
import io.ballerina.openapi.corenew.typegenerator.GeneratorUtils;
import io.ballerina.openapi.corenew.typegenerator.TypeGeneratorUtils;
import io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException;
import io.ballerina.openapi.corenew.typegenerator.model.GeneratorMetaData;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesisedTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.extractReferenceType;

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
    private String parentType = null;

    public ArrayTypeGenerator(Schema schema, String typeName, String parentType) {
        super(schema, typeName);
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
            AnnotationNode constraintNode = TypeGeneratorUtils.generateConstraintNode(typeName, items);
            if (constraintNode != null) {
                typeAnnotations.add(constraintNode);
            }
            typeName = GeneratorUtils.getValidName(
                    parentType != null ?
                            parentType + "-" + normalizedTypeName + "-Items-" + GeneratorUtils.getOpenAPIType(items) :
                            normalizedTypeName + "-Items-" + GeneratorUtils.getOpenAPIType(items),
                    true);
            typeGenerator = TypeGeneratorUtils.getTypeGenerator(items, typeName, null);
            TypeDefinitionNode arrayItemWithConstraint = typeGenerator.generateTypeDefinitionNode(
                    createIdentifierToken(typeName),
                    typeAnnotations);
            imports.addAll(typeGenerator.getImports());
            typeDefinitionNodeList.add(arrayItemWithConstraint);
        } else {
            typeGenerator = TypeGeneratorUtils.getTypeGenerator(items, typeName, null);
        }

        TypeDescriptorNode typeDescriptorNode;
        typeDefinitionNodeList.addAll(typeGenerator.getTypeDefinitionNodeList());
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
    public static Optional<TypeDescriptorNode> getTypeDescNodeForArraySchema(Schema schema)
            throws BallerinaOpenApiException {
        TypeDescriptorNode member;
        String schemaType = GeneratorUtils.getOpenAPIType(schema.getItems());
        if (schema.getItems().get$ref() != null) {
            member = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(GeneratorUtils.getValidName(
                            extractReferenceType(schema.getItems().get$ref()), true)));
        } else if (schemaType != null && (schemaType.equals(GeneratorConstants.INTEGER) || schemaType.equals(GeneratorConstants.NUMBER) ||
                schemaType.equals(GeneratorConstants.BOOLEAN) || schemaType.equals(GeneratorConstants.STRING))) {
            member = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(
                    GeneratorUtils.convertOpenAPITypeToBallerina(schema.getItems())));
        } else {
            return Optional.empty();
        }
        ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        NodeList<ArrayDimensionNode> nodeList = createNodeList(dimensionNode);
        return Optional.ofNullable(createArrayTypeDescriptorNode(member, nodeList));
    }
}