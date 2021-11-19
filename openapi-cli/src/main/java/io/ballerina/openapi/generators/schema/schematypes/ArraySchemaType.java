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
import io.ballerina.openapi.generators.schema.SchemaUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
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
import static io.ballerina.openapi.generators.GeneratorConstants.MAX_ARRAY_LENGTH;
import static io.ballerina.openapi.generators.GeneratorConstants.NILLABLE;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for array schemas.
 *
 * @since 2.0.0
 */
public class ArraySchemaType extends SchemaType {
    private final OpenAPI openAPI;
    private final boolean nullable;

    public ArraySchemaType(OpenAPI openAPI, boolean nullable) {
        this.openAPI = openAPI;
        this.nullable = nullable;
    }

    /**
     * Generate TypeDefinitionNode for object types.
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
     * Generate TypeDescriptorNode for array type schemas. If array type is not given, type will be `AnyData`
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode(Schema schema) throws BallerinaOpenApiException {
        assert schema instanceof ArraySchema;
        ArraySchema arraySchema = (ArraySchema) schema;
        String fieldTypeNameStr;
        fieldTypeNameStr = SchemaUtils.getSchemaType
                (arraySchema.getItems(), this.nullable, this.openAPI).toString().trim();
        if (fieldTypeNameStr.endsWith(NILLABLE)) {
            fieldTypeNameStr = fieldTypeNameStr.substring(0, fieldTypeNameStr.length() - 1);
        }
        String arrayBrackets = "[]";
        if (arraySchema.getMaxItems() != null) {
            if (arraySchema.getMaxItems() <= MAX_ARRAY_LENGTH) {
                arrayBrackets = "[" + arraySchema.getMaxItems() + "]";
            } else {
                throw new BallerinaOpenApiException("Maximum item count defined in the definition exceeds the " +
                        "maximum ballerina array length.");
            }
        }
        fieldTypeNameStr = SchemaUtils.getNullableType(arraySchema, fieldTypeNameStr + arrayBrackets, this.nullable);
        return createSimpleNameReferenceNode(createIdentifierToken(fieldTypeNameStr));
    }
}
