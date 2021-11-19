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

package io.ballerina.openapi.generators.schema.schematypes;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.schema.SchemaUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.generators.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for object type schema.
 *
 * @since 2.0.0
 */
public class ObjectSchemaType extends SchemaType {

    private final OpenAPI openAPI;
    private final boolean nullable;

    public ObjectSchemaType(OpenAPI openAPI, boolean nullable) {
        this.openAPI = openAPI;
        this.nullable = nullable;
    }

    /**
     * Generate TypeDefinitionNode for object types.
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
     */
    @Override
    public TypeDefinitionNode generateTypeDefinitionNode(Schema<Object> schemaValue, IdentifierToken typeName,
                                                         List<Node> schemaDoc, List<AnnotationNode> typeAnnotations)
            throws BallerinaOpenApiException {
        TypeDescriptorNode recordTypeDescriptorNode = this.generateTypeDescriptorNode(schemaValue);
        MarkdownDocumentationNode documentationNode =
                createMarkdownDocumentationNode(createNodeList(schemaDoc));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createNodeList(typeAnnotations));
        return NodeFactory.createTypeDefinitionNode(metadataNode,
                createToken(PUBLIC_KEYWORD),
                createToken(TYPE_KEYWORD), typeName,
                recordTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate TypeDescriptorNode for object type schemas.
     */
    public TypeDescriptorNode generateTypeDescriptorNode(Schema schema) throws BallerinaOpenApiException {
        if (schema.getProperties() != null) {
            Map<String, Schema> properties = schema.getProperties();
            List<Node> recordFList = new ArrayList<>();
            List<String> required = schema.getRequired();
            for (Map.Entry<String, Schema> property : properties.entrySet()) {
                SchemaUtils.addRecordFields(required, recordFList, property, this.nullable, this.openAPI);
            }
            NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFList);
            return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                    createToken(OPEN_BRACE_TOKEN), fieldNodes, null, createToken(CLOSE_BRACE_TOKEN));
        } else if (schema.get$ref() != null) {
            String type = getValidName(extractReferenceType(schema.get$ref()), true);
            Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type);
            type = SchemaUtils.getNullableType(refSchema, type, nullable);
            Token typeName = AbstractNodeFactory.createIdentifierToken(type);
            return createBuiltinSimpleNameReferenceNode(null, typeName);
        } else {
            Token typeName = AbstractNodeFactory.createIdentifierToken(
                    convertOpenAPITypeToBallerina(schema.getType().trim()));
            return createBuiltinSimpleNameReferenceNode(null, typeName);
        }
    }
}
