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

package io.ballerina.openapi.generators.schema.ballerinatypegenerators;

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
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.schema.TypeGeneratorUtils;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesisedTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.openapi.generators.GeneratorConstants.MAX_ARRAY_LENGTH;
import static io.ballerina.openapi.generators.GeneratorConstants.SPECIAL_CHARACTER_REGEX;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;
import static io.ballerina.openapi.generators.GeneratorUtils.isAvailableConstraint;
import static io.ballerina.openapi.generators.schema.TypeGeneratorUtils.getNullableType;

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
 * @since 2.0.0
 */
public class ArrayTypeGenerator extends TypeGenerator {
    private String parentType = null;
    private TypeDefinitionNode arrayItemWithConstraint = null;
    public ArrayTypeGenerator(Schema schema, String typeName, String parentType) {
        super(schema, typeName);
        this.parentType = parentType;
    }

    public TypeDefinitionNode getArrayItemWithConstraint() {
        return arrayItemWithConstraint;
    }

    /**
     * Generate TypeDescriptorNode for array type schemas. If array type is not given, type will be `AnyData`
     * public type StringArray string[];
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        assert schema instanceof ArraySchema;
        ArraySchema arraySchema = (ArraySchema) schema;
        Schema<?> items = arraySchema.getItems();
        boolean isConstraintsAvailable = isAvailableConstraint(items);
        TypeGenerator typeGenerator;
        if (isConstraintsAvailable) {
            String normalizedTypeName = typeName.replaceAll(SPECIAL_CHARACTER_REGEX, "").trim();
            typeName = getValidName(
                    parentType != null ?
                            parentType + "-" + normalizedTypeName + "-Items-" + items.getType() :
                            normalizedTypeName + "-Items-" + items.getType(),
                    true);
            typeGenerator = TypeGeneratorUtils.getTypeGenerator(items, typeName, null);
            List<AnnotationNode> typeAnnotations = new ArrayList<>();
            AnnotationNode constraintNode = TypeGeneratorUtils.generateConstraintNode(items);
            if (constraintNode != null) {
                typeAnnotations.add(constraintNode);
            }
            arrayItemWithConstraint = typeGenerator.generateTypeDefinitionNode(
                    createIdentifierToken(typeName),
                    new ArrayList<>(),
                    typeAnnotations);
        } else {
            typeGenerator = TypeGeneratorUtils.getTypeGenerator(items, typeName, null);
        }

        TypeDescriptorNode typeDescriptorNode;
        if (typeGenerator instanceof PrimitiveTypeGenerator && isConstraintsAvailable) {
            typeDescriptorNode = NodeParser.parseTypeDescriptor(typeName);
        } else {
            typeDescriptorNode = typeGenerator.generateTypeDescriptorNode();
        }

        if (typeGenerator instanceof UnionTypeGenerator) {
            typeDescriptorNode = createParenthesisedTypeDescriptorNode(
                    createToken(OPEN_PAREN_TOKEN), typeDescriptorNode, createToken(CLOSE_PAREN_TOKEN));
        }
        if (typeDescriptorNode instanceof OptionalTypeDescriptorNode) {
            Node node = ((OptionalTypeDescriptorNode) typeDescriptorNode).typeDescriptor();
            typeDescriptorNode = (TypeDescriptorNode) node;
        }

        if (arraySchema.getMaxItems() != null) {
            if (arraySchema.getMaxItems() > MAX_ARRAY_LENGTH) {
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
        ArrayTypeDescriptorNode arrayTypeDescriptorNode = createArrayTypeDescriptorNode(typeDescriptorNode
                , arrayDimensions);

        return getNullableType(arraySchema, arrayTypeDescriptorNode);
    }
}
