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

package io.ballerina.openapi.generators.schema.schematypes.composedschematypes;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.ballerina.openapi.generators.schema.SchemaUtils;
import io.ballerina.openapi.generators.schema.schematypes.SchemaType;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for anyOf schemas.
 *
 * @since 2.0.0
 */
public class AnyOfSchemaType extends SchemaType {
    private final boolean nullable;

    public AnyOfSchemaType(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * Generate TypeDefinitionNode for anyOf schemas.
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
     */
    @Override
    public TypeDefinitionNode generateTypeDefinitionNode(Schema<Object> schemaValue, IdentifierToken typeName,
                                                         List<Node> schemaDoc, List<AnnotationNode> typeAnnotations)
            throws BallerinaOpenApiException {
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(
                createNodeList(schemaDoc));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createNodeList(typeAnnotations));
        return createTypeDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD),
                typeName, this.generateTypeDescriptorNode(schemaValue), createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate TypeDescriptorNode for anyOf schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode(Schema schema) throws BallerinaOpenApiException {
        assert schema instanceof ComposedSchema;
        ComposedSchema composedSchema = (ComposedSchema) schema;
        List<Schema> anyOf = composedSchema.getAnyOf();
        String unionTypeCont = GeneratorUtils.getOneOfUnionType(anyOf);
        unionTypeCont = SchemaUtils.getNullableType(schema, unionTypeCont, this.nullable);
        return createSimpleNameReferenceNode(createIdentifierToken(unionTypeCont));
    }
}
