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

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.type.TypeGeneratorUtils;
import io.ballerina.openapi.core.generators.type.diagnostic.TypeGeneratorDiagnostic;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.ballerina.openapi.core.generators.type.model.RecordMetadata;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ASTERISK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ELLIPSIS_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.core.generators.type.diagnostic.TypeGenerationDiagnosticMessages.OAS_TYPE_102;

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

    public AllOfRecordTypeGenerator(Schema schema, String typeName, boolean overrideNullable,
                                    HashMap<String, TypeDefinitionNode> subTypesMap,
                                    HashMap<String, NameReferenceNode> pregeneratedTypeMap) {
        super(schema, typeName, overrideNullable, subTypesMap, pregeneratedTypeMap);
    }

    /**
     * Generates TypeDescriptorNode for allOf schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws OASTypeGenException {
        // This assertion is always `true` because this type generator receive ComposedSchema during the upper level
        // filtering as input. Has to use this assertion statement instead of `if` condition, because to avoid
        // unreachable else statement.
        List<Schema<?>> allOfSchemas = schema.getAllOf();
        RecordMetadata recordMetadata = getRecordMetadata();
        RecordRestDescriptorNode restDescriptorNode = recordMetadata.getRestDescriptorNode();
        if (allOfSchemas.size() == 1 && allOfSchemas.get(0).get$ref() != null) {
            ReferencedTypeGenerator referencedTypeGenerator = new ReferencedTypeGenerator(allOfSchemas.get(0),
                    typeName, overrideNullable, subTypesMap, pregeneratedTypeMap);
            TypeDescriptorNode typeDescriptorNode = referencedTypeGenerator.generateTypeDescriptorNode();
            return typeDescriptorNode;
        } else {
            ImmutablePair<List<Node>, List<Schema<?>>> recordFlist = generateAllOfRecordFields(allOfSchemas);
            List<Node> recordFieldList = recordFlist.getLeft();
            List<Schema<?>> validSchemas = recordFlist.getRight();
            if (validSchemas.isEmpty()) {
                AnyDataTypeGenerator anyDataTypeGenerator = new AnyDataTypeGenerator(schema, typeName,
                        overrideNullable, subTypesMap, pregeneratedTypeMap);
                TypeDescriptorNode typeDescriptorNode = anyDataTypeGenerator.generateTypeDescriptorNode();
                return typeDescriptorNode;
            } else if (validSchemas.size() == 1) {
                TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(validSchemas.get(0), typeName,
                        null, overrideNullable, subTypesMap, pregeneratedTypeMap);
                TypeDescriptorNode typeDescriptorNode = typeGenerator.generateTypeDescriptorNode();
                return typeDescriptorNode;
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
            throws OASTypeGenException {

        List<Node> recordFieldList = new ArrayList<>();
        List<Schema<?>> validSchemas = new ArrayList<>();

        for (Schema allOfSchema : allOfSchemas) {
            if (allOfSchema.get$ref() != null) {
                String extractedSchemaName = null;
                try {
                    extractedSchemaName = GeneratorUtils.extractReferenceType(allOfSchema.get$ref());
                } catch (BallerinaOpenApiException e) {
                    throw new OASTypeGenException(e.getMessage());
                }
                String modifiedSchemaName = GeneratorUtils.getValidName(extractedSchemaName, true);
                Token typeRef = AbstractNodeFactory.createIdentifierToken(modifiedSchemaName);
                TypeReferenceNode recordField = NodeFactory.createTypeReferenceNode(createToken(ASTERISK_TOKEN),
                        typeRef, createToken(SEMICOLON_TOKEN));
                // check whether given reference schema has additional fields.
                OpenAPI openAPI = GeneratorMetaData.getInstance().getOpenAPI();
                Schema<?> refSchema = openAPI.getComponents().getSchemas().get(modifiedSchemaName);
                addAdditionalSchemas(refSchema);

                if (!pregeneratedTypeMap.containsKey(modifiedSchemaName)) {
                    pregeneratedTypeMap.put(modifiedSchemaName, createSimpleNameReferenceNode(
                            createIdentifierToken(modifiedSchemaName)));
                    TypeGenerator reffredTypeGenerator = TypeGeneratorUtils.getTypeGenerator(refSchema,
                            modifiedSchemaName, modifiedSchemaName, overrideNullable, subTypesMap, pregeneratedTypeMap);
                    TypeDescriptorNode typeDescriptorNode1 = reffredTypeGenerator.generateTypeDescriptorNode();
                    subTypesMap.put(extractedSchemaName, createTypeDefinitionNode(null,
                            createToken(PUBLIC_KEYWORD),
                            createToken(TYPE_KEYWORD),
                            createIdentifierToken(modifiedSchemaName),
                            typeDescriptorNode1,
                            createToken(SEMICOLON_TOKEN)));
                }
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
                    List<Node> recordAllFields = immutablePair.getLeft();
                    recordFieldList.addAll(recordAllFields);
                } else {
                    // TODO: Needs to improve the error message. Could not access the schema name at this level.
                    diagnostics.add(new TypeGeneratorDiagnostic(OAS_TYPE_102));
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
            throws OASTypeGenException {
        TypeDescriptorNode unionType = getUnionType(restSchemas);
        return NodeFactory.createRecordRestDescriptorNode(unionType, createToken(ELLIPSIS_TOKEN),
                createToken(SEMICOLON_TOKEN));
    }

    /**
     * Creates the UnionType done for a given schema list.
     *
     * @param schemas  List of schemas included in additional fields.
     * @return Union type
     * @throws OASTypeGenException when unsupported combination of schemas found
     */
    private TypeDescriptorNode getUnionType(List<Schema<?>> schemas) throws OASTypeGenException {

        // TODO: this has issue with generating union type with `string?|int?...
        // this will be tracked via https://github.com/ballerina-platform/openapi-tools/issues/810
        List<TypeDescriptorNode> typeDescriptorNodes = new ArrayList<>();
        for (Schema schema : schemas) {
            TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(schema, null, null,
                    overrideNullable, subTypesMap, pregeneratedTypeMap);
            TypeDescriptorNode typeDescriptorNode = typeGenerator.generateTypeDescriptorNode();
            imports.addAll(typeGenerator.getImports());
            typeDescriptorNodes.add(typeDescriptorNode);
            // error for rest field unhandled constraint support
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
