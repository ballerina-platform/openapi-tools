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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.ballerina.openapi.generators.schema.SchemaUtils;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for schemas without type.
 *
 * @since 2.0.0
 */
public class AnyDataSchemaType extends SchemaType {

    private final boolean nullable;

    public AnyDataSchemaType(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * Generate TypeDefinitionNode when no type is given in the schema.
     * -- ex:
     * Sample OpenAPI :
     * <pre>
     *     Pet:
     *       description: Details of the pet.
     *  </pre>
     * Generated Ballerina type for the schema `Pet` :
     * <pre>
     *      public type Pet anydata;
     * </pre>
     */
    @Override
    public TypeDefinitionNode generateTypeDefinitionNode(Schema<Object> schemaValue, IdentifierToken typeName,
                                                         List<Node> schemaDoc, List<AnnotationNode> typeAnnotations)
            throws BallerinaOpenApiException {
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(schemaDoc));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createNodeList(typeAnnotations));
        return createTypeDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD),
                typeName, this.generateTypeDescriptorNode(schemaValue),
                createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate TypeDescriptorNode schemas with no type.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode(Schema schema) throws BallerinaOpenApiException {
        String anyData = SchemaUtils.getNullableType(schema, GeneratorConstants.ANY_DATA, nullable);
        return createSimpleNameReferenceNode(createIdentifierToken(anyData));
    }
}
