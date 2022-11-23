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

package io.ballerina.openapi.core.generators.schema.ballerinatypegenerators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ASTERISK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ELLIPSIS_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.core.GeneratorConstants.OBJECT;
import static io.ballerina.openapi.core.GeneratorConstants.PIPE;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for allOf schemas.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *    schemas:
 *     Dog:
 *       allOf:
 *       - $ref: "#/components/schemas/Pet"
 *       - type: object
 *         properties:
 *           bark:
 *             type: boolean
 *  </pre>
 * Generated Ballerina type for the allOf schema `Dog` :
 * <pre>
 *  public type Dog record {
 *      *Pet;
 *      boolean bark?;
 *  };
 * </pre>
 *
 * @since 1.3.0
 */
public class AllOfRecordTypeGenerator extends RecordTypeGenerator {
    public AllOfRecordTypeGenerator(Schema schema, String typeName) {

        super(schema, typeName);
    }

    /**
     * Generate TypeDescriptorNode for allOf schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {

        assert schema instanceof ComposedSchema;
        ComposedSchema composedSchema = (ComposedSchema) schema;
        List<Schema> allOfSchemas = composedSchema.getAllOf();

        RecordMetadata recordMetadata = getRecordMetadata();
        RecordRestDescriptorNode restDescriptorNode = recordMetadata.getRestDescriptorNode();
        if (allOfSchemas.size() == 1 && allOfSchemas.get(0).get$ref() != null) {
            ReferencedTypeGenerator referencedTypeGenerator = new ReferencedTypeGenerator(allOfSchemas.get(0),
                    typeName);
            return referencedTypeGenerator.generateTypeDescriptorNode();
        } else {
            List<Node> recordFieldList = generateAllOfRecordFields(allOfSchemas);
            restDescriptorNode = getRestDescriptorNodeForAllOf(restDescriptorNode, recordFieldList);
            NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
            return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                    recordMetadata.isOpenRecord() ? createToken(OPEN_BRACE_TOKEN) : createToken(OPEN_BRACE_PIPE_TOKEN),
                    fieldNodes, restDescriptorNode,
                    recordMetadata.isOpenRecord() ? createToken(CLOSE_BRACE_TOKEN) : createToken(CLOSE_BRACE_PIPE_TOKEN));
        }
    }

    private List<Node> generateAllOfRecordFields(List<Schema> allOfSchemas) throws BallerinaOpenApiException {

        List<Node> recordFieldList = new ArrayList<>();
        for (Schema allOfSchema : allOfSchemas) {
            if (allOfSchema.get$ref() != null) {
                Token typeRef = AbstractNodeFactory.createIdentifierToken(GeneratorUtils.getValidName(
                        GeneratorUtils.extractReferenceType(allOfSchema.get$ref()), true));
                TypeReferenceNode recordField = NodeFactory.createTypeReferenceNode(createToken(ASTERISK_TOKEN),
                        typeRef, createToken(SEMICOLON_TOKEN));
                recordFieldList.add(recordField);
            } else if (allOfSchema.getProperties() != null) {
                Map<String, Schema<?>> properties = allOfSchema.getProperties();
                List<String> required = allOfSchema.getRequired();
                recordFieldList.addAll(addRecordFields(required, properties.entrySet(), typeName));
                if (allOfSchema.getAdditionalProperties() != null && allOfSchema.getAdditionalProperties() instanceof Schema) {
                    RecordRestDescriptorNode restDescriptorNode =
                            getRecordRestDescriptorNode((Schema<?>) allOfSchema.getAdditionalProperties());
                    recordFieldList.add(restDescriptorNode);
                }
            } else if (allOfSchema instanceof ComposedSchema) {
                ComposedSchema nestedComposedSchema = (ComposedSchema) allOfSchema;
                if (nestedComposedSchema.getAllOf() != null) {
                    recordFieldList.addAll(generateAllOfRecordFields(nestedComposedSchema.getAllOf()));
                } else {
                    // TODO: Needs to improve the error message. Could not access the schema name at this level.
                    throw new BallerinaOpenApiException(
                            "Unsupported nested OneOf or AnyOf schema is found inside a AllOf schema.");
                }
            }
        }
        return recordFieldList;
    }

    /**
     * This util is to create the union record rest fields.
     * <pre> string|int... <pre/>
     * @return
     */
    private static RecordRestDescriptorNode getRestDescriptorNodeForAllOf(RecordRestDescriptorNode restDescriptorNode,
                                                                          List<Node> recordFieldList) {

        List<RecordRestDescriptorNode> recordRestNodes = new LinkedList<>();
        for (Node node: recordFieldList) {
            if (node instanceof RecordRestDescriptorNode) {
                recordRestNodes.add((RecordRestDescriptorNode) node);
            }
        }
        if (restDescriptorNode != null) {
            recordRestNodes.add(restDescriptorNode);
        }
        if (!recordRestNodes.isEmpty() && recordRestNodes.size() > 1) {
            recordFieldList.removeAll(recordRestNodes);
            StringBuilder stringBuilder = new StringBuilder();
            for (RecordRestDescriptorNode restDescNode: recordRestNodes) {
                stringBuilder.append(restDescNode.typeName().toString());
                stringBuilder.append(PIPE);
            }
            String unionRestNode = stringBuilder.toString();
            restDescriptorNode = NodeFactory.createRecordRestDescriptorNode(
                    createIdentifierToken(unionRestNode.substring(0, unionRestNode.length() - 1)),
                    createToken(ELLIPSIS_TOKEN), createToken(SEMICOLON_TOKEN));
        }
        return restDescriptorNode;
    }
}
