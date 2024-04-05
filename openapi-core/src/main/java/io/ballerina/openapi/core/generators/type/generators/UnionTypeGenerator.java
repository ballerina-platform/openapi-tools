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
package io.ballerina.openapi.core.generators.type.generators;

import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.generators.type.TypeGeneratorUtils;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPTIONAL_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for anyOf and oneOf schemas.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *    schemas:
 *     Employee:
 *       anyOf:
 *       - $ref: "#/components/schemas/InternalEmployee"
 *       - $ref: "#/components/schemas/ExternalEmployee"
 *  </pre>
 * Generated Ballerina type for the anyOf schema `Employee` :
 * <pre>
 *  public type Employee InternalEmployee|ExternalEmployee
 * </pre>
 *
 * @since 1.3.0
 */
public class UnionTypeGenerator extends TypeGenerator {

    public UnionTypeGenerator(Schema<?> schema, String typeName, boolean overrideNullable,
                              HashMap<String, TypeDefinitionNode> subTypesMap,
                              HashMap<String, NameReferenceNode> pregeneratedTypeMap) {
        super(schema, typeName, overrideNullable, subTypesMap, pregeneratedTypeMap);
    }

    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws OASTypeGenException {
        List<Schema> schemas;
        if (schema.getOneOf() != null) {
            schemas = schema.getOneOf();
        } else {
            schemas = schema.getAnyOf();
        }
        TypeDescriptorNode unionTypeDesc = getUnionType(schemas, typeName);
        return TypeGeneratorUtils.getNullableType(schema, unionTypeDesc, overrideNullable);
    }

    /**
     * Creates the UnionType string to generate bal type for a given oneOf or anyOf type schema.
     *
     * @param schemas  List of schemas included in the anyOf or oneOf schema
     * @param typeName This is parameter or variable name that used to populate error message meaningful
     * @return Union type
     * @throws OASTypeGenException when unsupported combination of schemas found
     */
    private TypeDescriptorNode getUnionType(List<Schema> schemas, String typeName) throws OASTypeGenException {
        List<TypeDescriptorNode> typeDescriptorNodes = new ArrayList<>();
        for (Schema<?> schema : schemas) {
            TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(schema, typeName, null,
                    overrideNullable, subTypesMap, pregeneratedTypeMap);
            TypeDescriptorNode typeDescNode = typeGenerator.generateTypeDescriptorNode();
            imports.addAll(typeGenerator.getImports());
            if (typeDescNode instanceof OptionalTypeDescriptorNode && GeneratorMetaData.getInstance().isNullable()) {
                Node internalTypeDesc = ((OptionalTypeDescriptorNode) typeDescNode).typeDescriptor();
                typeDescNode = (TypeDescriptorNode) internalTypeDesc;
            }
            typeDescriptorNodes.add(typeDescNode);
        }

        return createUnionTypeNode(typeDescriptorNodes);
    }

    private TypeDescriptorNode createUnionTypeNode(List<TypeDescriptorNode> typeDescNodes) {
        if (typeDescNodes.isEmpty()) {
            return null;
        } else if (typeDescNodes.size() == 1) {
            return typeDescNodes.get(0);
        }

        // if any of the member subtypes is an optional type descriptor, simplify the resultant union type by
        // extracting out the optional operators from all the member subtypes and, adding only to the last one.
        //
        // i.e: T1?|T2?|...|Tn? <=> T1|T2|...|Tn?
        if (typeDescNodes.stream().anyMatch(node -> node.kind() == OPTIONAL_TYPE_DESC)) {
            typeDescNodes = typeDescNodes.stream().map(node -> {
                if (node instanceof OptionalTypeDescriptorNode) {
                    return (TypeDescriptorNode) ((OptionalTypeDescriptorNode) node).typeDescriptor();
                } else {
                    return node;
                }
            }).collect(Collectors.toList());

            OptionalTypeDescriptorNode optionalTypeDesc = createOptionalTypeDescriptorNode(
                    typeDescNodes.get(typeDescNodes.size() - 1), createToken(QUESTION_MARK_TOKEN));
            typeDescNodes.set(typeDescNodes.size() - 1, optionalTypeDesc);
        }

        UnionTypeDescriptorNode unionTypeDescNode = null;
        TypeDescriptorNode leftTypeDesc = typeDescNodes.get(0);
        for (int i = 1; i < typeDescNodes.size(); i++) {
            TypeDescriptorNode rightTypeDesc = typeDescNodes.get(i);
            unionTypeDescNode = createUnionTypeDescriptorNode(leftTypeDesc, createToken(PIPE_TOKEN), rightTypeDesc);
            leftTypeDesc = unionTypeDescNode;
        }

        return unionTypeDescNode;
    }
}
