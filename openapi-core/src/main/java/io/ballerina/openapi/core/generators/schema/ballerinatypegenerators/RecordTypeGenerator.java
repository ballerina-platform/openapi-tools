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
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordRestDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.schema.TypeGeneratorUtils;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ELLIPSIS_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.core.GeneratorConstants.OBJECT;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for object type schema.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *      components:
 *          schemas:
 *              Pet:
 *                  required:
 *                      - id
 *                      - name
 *                  properties:
 *                      id:
 *                          type: integer
 *                          format: int64
 *                      name:
 *                          type: string
 *                      tag:
 *                          type: string
 *                      type:
 *                          type: string
 *  </pre>
 * Generated Ballerina type for the schema `Pet` :
 * <pre>
 * public type Pet record {
 *      int id;
 *      string name;
 *      string tag?;
 *      string 'type?;
 * };
 * </pre>
 *
 * @since 1.3.0
 */
public class RecordTypeGenerator extends TypeGenerator {

    private final List<TypeDefinitionNode> typeDefinitionNodeList = new ArrayList<>();
    public RecordTypeGenerator(Schema schema, String typeName) {
        super(schema, typeName);
    }
    public List<TypeDefinitionNode> getTypeDefinitionNodeList() {
        return typeDefinitionNodeList;
    }

    /**
     * Generate TypeDescriptorNode for object type schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {

        List<Node> recordFList = new LinkedList<>();
        RecordMetadata metadataBuilder = getRecordMetadata();

        if (schema.getProperties() != null) {
            Map<String, Schema<?>> properties = schema.getProperties();
            List<String> required = schema.getRequired();
            recordFList.addAll(addRecordFields(required, properties.entrySet(), typeName));
            NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFList);
            return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                    metadataBuilder.isOpenRecord() ? createToken(OPEN_BRACE_TOKEN) : createToken(OPEN_BRACE_PIPE_TOKEN),
                    fieldNodes, metadataBuilder.getRestDescriptorNode(),
                    metadataBuilder.isOpenRecord() ? createToken(CLOSE_BRACE_TOKEN) :
                            createToken(CLOSE_BRACE_PIPE_TOKEN));
        } else {
            return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                    metadataBuilder.isOpenRecord() ? createToken(OPEN_BRACE_TOKEN) : createToken(OPEN_BRACE_PIPE_TOKEN),
                    createNodeList(recordFList), metadataBuilder.getRestDescriptorNode(),
                    metadataBuilder.isOpenRecord() ? createToken(CLOSE_BRACE_TOKEN) :
                            createToken(CLOSE_BRACE_PIPE_TOKEN));
        }
    }

    /**
     * This function is to extract the main details of the record need to be generated and return {@code
     * RecordMetadata} object with required details.
     *
     * @return {@code RecordMetadata}
     * @throws BallerinaOpenApiException throws when process has some failure.
     */
    public RecordMetadata getRecordMetadata() throws BallerinaOpenApiException {
        boolean isOpenRecord = false;
        RecordRestDescriptorNode recordRestDescNode = null;

        if (schema.getAdditionalProperties() != null) {
            Object additionalProperties = schema.getAdditionalProperties();
            if (additionalProperties.equals(true)) {
                isOpenRecord = true;
            } else if (additionalProperties instanceof Schema) {
                Schema<?> additionalPropSchema = (Schema<?>) additionalProperties;
                if (additionalPropSchema.get$ref() != null) {
                    String ballerinaType = GeneratorUtils.getValidName(GeneratorUtils.extractReferenceType(
                            additionalPropSchema.get$ref()), true);
                    SimpleNameReferenceNode recordNode =
                            NodeFactory.createSimpleNameReferenceNode(createIdentifierToken(ballerinaType));
                    recordRestDescNode =
                            NodeFactory.createRecordRestDescriptorNode(recordNode, createToken(ELLIPSIS_TOKEN),
                                    createToken(SEMICOLON_TOKEN));
                } else if (additionalPropSchema.getType() != null) {
                    recordRestDescNode = getRecordRestDescriptorNode(additionalPropSchema);
                } else {
                    isOpenRecord = true;
                }
            }
        } else if (schema.getType() != null && schema.getType().equals(OBJECT) && schema.getProperties() == null &&
                schema.getAdditionalProperties() == null) {
            // this above condition for check the free-form object [ex: type:object without any fields or
            // additional fields], that object should be mapped to the open record.
            isOpenRecord = true;
        }
        return new RecordMetadata.Builder()
                        .withIsOpenRecord(isOpenRecord)
                        .withRestDescriptorNode(recordRestDescNode).build();
    }

    /**
     * Generates {@code RecordRestDescriptorNode} for the additional properties in object schema.
     * <pre>
     *    type User record {
     *       string...;
     *     }
     * </pre>
     */
    public static RecordRestDescriptorNode getRecordRestDescriptorNode(Schema<?> additionalPropSchema)
            throws BallerinaOpenApiException {

        RecordRestDescriptorNode recordRestDescNode = null;
        String type = additionalPropSchema.getType();

        if (additionalPropSchema instanceof NumberSchema && additionalPropSchema.getFormat() != null) {
            // this is special for `NumberSchema` because it has format with its expected type.
            type = additionalPropSchema.getFormat();
        } else if (additionalPropSchema instanceof ObjectSchema || additionalPropSchema instanceof MapSchema) {
            RecordTypeGenerator record = new RecordTypeGenerator(additionalPropSchema, null);
            TypeDescriptorNode recordNode = record.generateTypeDescriptorNode();
            recordRestDescNode = NodeFactory.createRecordRestDescriptorNode(recordNode, createToken(ELLIPSIS_TOKEN),
                    createToken(SEMICOLON_TOKEN));
        } else if (additionalPropSchema instanceof ArraySchema) {
            ArrayTypeGenerator arrayTypeGenerator = new ArrayTypeGenerator(additionalPropSchema, null, null);
            TypeDescriptorNode arrayNode = arrayTypeGenerator.generateTypeDescriptorNode();
            recordRestDescNode = NodeFactory.createRecordRestDescriptorNode(arrayNode, createToken(ELLIPSIS_TOKEN),
                    createToken(SEMICOLON_TOKEN));
        } else {
            String ballerinaType = GeneratorUtils.convertOpenAPITypeToBallerina(type.trim());
            recordRestDescNode = NodeFactory.createRecordRestDescriptorNode(createIdentifierToken(ballerinaType),
                            createToken(ELLIPSIS_TOKEN), createToken(SEMICOLON_TOKEN));
        }
        return recordRestDescNode;
    }

    /**
     * This util for generating record field with given schema properties.
     */
    public List<Node> addRecordFields(List<String> required, Set<Map.Entry<String, Schema<?>>> fields,
                                      String recordName)
            throws BallerinaOpenApiException {
        // TODO: Handle allOf , oneOf, anyOf
        List<Node> recordFieldList = new ArrayList<>();
        for (Map.Entry<String, Schema<?>> field : fields) {
            String fieldNameStr = GeneratorUtils.escapeIdentifier(field.getKey().trim());
            // API doc generations
            Schema<?> fieldSchema = field.getValue();
            List<Node> schemaDoc = TypeGeneratorUtils.getFieldApiDocs(fieldSchema);
            NodeList<Node> schemaDocNodes = createNodeList(schemaDoc);

            IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(fieldNameStr);
            TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(fieldSchema, fieldNameStr, recordName);
            TypeDescriptorNode fieldTypeName = typeGenerator.generateTypeDescriptorNode();
            if (typeGenerator instanceof ArrayTypeGenerator &&
                    ((ArrayTypeGenerator) typeGenerator).getArrayItemWithConstraint() != null) {
                typeDefinitionNodeList.add(((ArrayTypeGenerator) typeGenerator).getArrayItemWithConstraint());
            } else if (typeGenerator instanceof UnionTypeGenerator &&
                    !((UnionTypeGenerator) typeGenerator).getTypeDefinitionNodeList().isEmpty()) {
                List<TypeDefinitionNode> newConstraintNode =
                        ((UnionTypeGenerator) typeGenerator).getTypeDefinitionNodeList();
                typeDefinitionNodeList.addAll(newConstraintNode);
            }
            TypeGeneratorUtils.updateRecordFieldList(required, recordFieldList, field, fieldSchema, schemaDocNodes,
                    fieldName, fieldTypeName);
        }
        return recordFieldList;
    }
}

/**
 * RecordMetadata class for containing the details to generate record node. This contains the details with whether
 * record is openapi record or not, and its restField details.
 *
 * @since 1.4.0
 */
class RecordMetadata {
    private final boolean isOpenRecord;
    private final RecordRestDescriptorNode restDescriptorNode;

    RecordMetadata(Builder builder) {

        this.isOpenRecord = builder.isOpenRecord;
        this.restDescriptorNode = builder.restDescriptorNode;
    }

    public boolean isOpenRecord() {
        return isOpenRecord;
    }

    public RecordRestDescriptorNode getRestDescriptorNode() {
        return restDescriptorNode;
    }

    /**
     * Record meta data builder class for {@code RecordMetadata}.
     *
     * @since 1.4.0
     */
    public static class Builder {
        private boolean isOpenRecord = false;
        private RecordRestDescriptorNode restDescriptorNode = null;
        public Builder withIsOpenRecord(boolean isOpenRecord) {
            this.isOpenRecord = isOpenRecord;
            return this;
        }
        public Builder withRestDescriptorNode(RecordRestDescriptorNode restDescriptorNode) {
            this.restDescriptorNode = restDescriptorNode;
            return this;
        }

        public RecordMetadata build() {
            return new RecordMetadata(this);
        }
    }
}
