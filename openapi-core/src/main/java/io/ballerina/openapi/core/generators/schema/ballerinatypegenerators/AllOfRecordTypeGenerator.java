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
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.schema.model.GeneratorMetaData;
import io.ballerina.openapi.core.generators.schema.model.RecordMetadata;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ASTERISK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ELLIPSIS_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.core.generators.schema.TypeGeneratorUtils.getTypeGenerator;

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
    private final List<Schema<?>> restSchemas = new LinkedList<>();

    public AllOfRecordTypeGenerator(Schema schema, String typeName) {
        super(schema, typeName);
    }

    /**
     * Generates TypeDescriptorNode for allOf schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        // This assertion is always `true` because this type generator receive ComposedSchema during the upper level
        // filtering as input. Has to use this assertion statement instead of `if` condition, because to avoid
        // unreachable else statement.
        List<Schema<?>> allOfSchemas = schema.getAllOf();

        RecordMetadata recordMetadata = getRecordMetadata();
        RecordRestDescriptorNode restDescriptorNode = recordMetadata.getRestDescriptorNode();
        if (allOfSchemas.size() == 1 && allOfSchemas.get(0).get$ref() != null) {
            ReferencedTypeGenerator referencedTypeGenerator = new ReferencedTypeGenerator(allOfSchemas.get(0),
                    typeName);
            return referencedTypeGenerator.generateTypeDescriptorNode();
        } else {
            ImmutablePair<List<Node>, List<Schema<?>>> recordFlist = generateAllOfRecordFields(allOfSchemas);
            List<Node> recordFieldList = recordFlist.getLeft();
            List<Schema<?>> validSchemas = recordFlist.getRight();
            if (validSchemas.isEmpty()) {
                AnyDataTypeGenerator anyDataTypeGenerator = new AnyDataTypeGenerator(schema, typeName);
                return anyDataTypeGenerator.generateTypeDescriptorNode();
            } else if (validSchemas.size() == 1) {
                TypeGenerator typeGenerator = getTypeGenerator(validSchemas.get(0), typeName, null);
                return typeGenerator.generateTypeDescriptorNode();
            } else {
                addAdditionalSchemas(schema);
                restDescriptorNode =
                        restSchemas.size() > 1 ? getRestDescriptorNodeForAllOf(restSchemas) : restDescriptorNode;

                NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
                return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                        recordMetadata.isOpenRecord() ?
                                createToken(OPEN_BRACE_TOKEN) : createToken(OPEN_BRACE_PIPE_TOKEN),
                        fieldNodes, restDescriptorNode,
                        recordMetadata.isOpenRecord() ? createToken(CLOSE_BRACE_TOKEN) :
                                createToken(CLOSE_BRACE_PIPE_TOKEN));
            }
        }
    }

    private ImmutablePair<List<Node>, List<Schema<?>>> generateAllOfRecordFields(List<Schema<?>> allOfSchemas)
            throws BallerinaOpenApiException {

        List<Node> recordFieldList = new ArrayList<>();
        List<Schema<?>> validSchemas = new ArrayList<>();

        for (Schema allOfSchema : allOfSchemas) {
            if (allOfSchema.get$ref() != null) {
                String extractedSchemaName = GeneratorUtils.extractReferenceType(allOfSchema.get$ref());
                String modifiedSchemaName = GeneratorUtils.getValidName(extractedSchemaName, true);
                Token typeRef = AbstractNodeFactory.createIdentifierToken(modifiedSchemaName);
                TypeReferenceNode recordField = NodeFactory.createTypeReferenceNode(createToken(ASTERISK_TOKEN),
                        typeRef, createToken(SEMICOLON_TOKEN));
                // check whether given reference schema has additional fields.
                OpenAPI openAPI = GeneratorMetaData.getInstance().getOpenAPI();
                Schema<?> refSchema = openAPI.getComponents().getSchemas().get(modifiedSchemaName);
                addAdditionalSchemas(refSchema);

                recordFieldList.add(recordField);
            } else if (allOfSchema.getProperties() != null) {
                Map<String, Schema<?>> properties = allOfSchema.getProperties();
                List<String> required = allOfSchema.getRequired();
                recordFieldList.addAll(addRecordFields(required, properties.entrySet(), typeName));
                addAdditionalSchemas(allOfSchema);
            } else if (GeneratorUtils.isComposedSchema(allOfSchema)) {
                if (allOfSchema.getAllOf() != null) {
                    ImmutablePair<List<Node>, List<Schema<?>>> immutablePair =
                            generateAllOfRecordFields(allOfSchema.getAllOf());
                    List<Node> recordAllFields = (List<Node>) immutablePair.getLeft();
                    recordFieldList.addAll(recordAllFields);
                } else {
                    // TODO: Needs to improve the error message. Could not access the schema name at this level.
                    throw new BallerinaOpenApiException(
                            "Unsupported nested OneOf or AnyOf schema is found inside a AllOf schema.");
                }
            }
            if (allOfSchema.getType() != null || allOfSchema.getProperties() != null || allOfSchema.get$ref() != null
                    || allOfSchema.getAllOf() != null) {
                validSchemas.add(allOfSchema);
            }
        }
        return ImmutablePair.of(recordFieldList, validSchemas);
    }

    /**
     * This util is to create the union record rest fields, when given allOf schema has multiple additional fields.
     * Note: This scenario only happens with AllOf scenarios since it maps with type inclusions.
     *
     * ex: string|int...
     * @return
     */
    private RecordRestDescriptorNode getRestDescriptorNodeForAllOf(List<Schema<?>> restSchemas)
            throws BallerinaOpenApiException {
        TypeDescriptorNode unionType = getUnionType(restSchemas);
        return NodeFactory.createRecordRestDescriptorNode(unionType, createToken(ELLIPSIS_TOKEN),
                createToken(SEMICOLON_TOKEN));
    }

    /**
     * Creates the UnionType done for a given schema list.
     *
     * @param schemas  List of schemas included in additional fields.
     * @return Union type
     * @throws BallerinaOpenApiException when unsupported combination of schemas found
     */
    private TypeDescriptorNode getUnionType(List<Schema<?>> schemas) throws BallerinaOpenApiException {

        // TODO: this has issue with generating union type with `string?|int?...
        // this will be tracked via https://github.com/ballerina-platform/openapi-tools/issues/810
        List<TypeDescriptorNode> typeDescriptorNodes = new ArrayList<>();
        for (Schema schema : schemas) {
            TypeGenerator typeGenerator = getTypeGenerator(schema, null, null);
            TypeDescriptorNode typeDescriptorNode = typeGenerator.generateTypeDescriptorNode();
            imports.addAll(typeGenerator.getImports());
            typeDescriptorNodes.add(typeDescriptorNode);
            // error for rest field unhandled constraint support
            if (GeneratorUtils.hasConstraints(schema)) {
                // use printStream for echo the error, because current openapi to ballerina implementation won't
                // handle diagnostic message.
                OUT_STREAM.println("WARNING: constraints in the OpenAPI contract will be ignored for the " +
                        "additionalProperties field, as constraints are not supported on Ballerina rest record " +
                        "field.");
            }
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

    private void addAdditionalSchemas(Schema<?> refSchema) {
        if (refSchema.getAdditionalProperties() != null && refSchema.getAdditionalProperties() instanceof Schema) {
            restSchemas.add((Schema<?>) refSchema.getAdditionalProperties());
        }
    }
}
