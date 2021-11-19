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

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.schema.SchemaUtils;
import io.ballerina.openapi.generators.schema.schematypes.SchemaType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ASTERISK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.generators.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for allOf schemas.
 *
 * @since 2.0.0
 */
public class AllOfSchemaType extends SchemaType {
    private final OpenAPI openAPI;
    private final boolean nullable;

    public AllOfSchemaType(OpenAPI openAPI, boolean nullable) {
        this.openAPI = openAPI;
        this.nullable = nullable;
    }

    /**
     * Generate TypeDefinitionNode for allOf schemas.
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
     */
    @Override
    public TypeDefinitionNode generateTypeDefinitionNode(Schema<Object> schemaValue, IdentifierToken typeName,
                                                         List<Node> schemaDoc, List<AnnotationNode> typeAnnotations)
            throws BallerinaOpenApiException {
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                (RecordTypeDescriptorNode) this.generateTypeDescriptorNode(schemaValue);
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(schemaDoc));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        return NodeFactory.createTypeDefinitionNode(metadataNode,
                createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD), typeName, recordTypeDescriptorNode,
                AbstractNodeFactory.createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate TypeDescriptorNode for allOf schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode(Schema schema) throws BallerinaOpenApiException {
        assert schema instanceof ComposedSchema;
        ComposedSchema composedSchema = (ComposedSchema) schema;
        List<Node> recordFieldList = new ArrayList<>();
        for (Schema allOfSchema : composedSchema.getAllOf()) {
            if (allOfSchema.getType() == null && allOfSchema.get$ref() != null) {
                Token typeRef = AbstractNodeFactory.createIdentifierToken(getValidName(
                        extractReferenceType(allOfSchema.get$ref()), true));
                // Type Reference Nodes in record fields does not have documentation
                TypeReferenceNode recordField = NodeFactory.createTypeReferenceNode(createToken(ASTERISK_TOKEN),
                        typeRef, createToken(SEMICOLON_TOKEN));
                recordFieldList.add(recordField);
            } else if (allOfSchema.getProperties() != null) {
                Map<String, Schema> properties = allOfSchema.getProperties();
                for (Map.Entry<String, Schema> field : properties.entrySet()) {
                    SchemaUtils.addRecordFields
                            (allOfSchema.getRequired(), recordFieldList, field, this.nullable, this.openAPI);
                }
            } else if (allOfSchema instanceof ComposedSchema) {
                ComposedSchema allOfNested = (ComposedSchema) allOfSchema;
                if (allOfNested.getAllOf() != null) {
                    for (Schema nestedSchema : allOfNested.getAllOf()) {
                        if (nestedSchema instanceof ObjectSchema) {
                            ObjectSchema objectSchema = (ObjectSchema) nestedSchema;
                            List<String> requiredField = objectSchema.getRequired();
                            Map<String, Schema> properties = objectSchema.getProperties();
                            // TODO: add api documentation
                            for (Map.Entry<String, Schema> field : properties.entrySet()) {
                                SchemaUtils.addRecordFields
                                        (requiredField, recordFieldList, field, this.nullable, this.openAPI);
                            }
                        }
                    }
                }
                // TODO handle OneOf, AnyOf
            }
        }
        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
        return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD), createToken(OPEN_BRACE_TOKEN),
                fieldNodes, null, createToken(CLOSE_BRACE_TOKEN));
    }
}
