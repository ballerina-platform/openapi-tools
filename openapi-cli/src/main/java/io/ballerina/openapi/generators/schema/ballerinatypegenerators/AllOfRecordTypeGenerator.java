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

package io.ballerina.openapi.generators.schema.ballerinatypegenerators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.schema.TypeGeneratorUtils;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ASTERISK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.generators.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;

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
 * @since 2.0.0
 */
public class AllOfRecordTypeGenerator extends TypeGenerator {

    public AllOfRecordTypeGenerator(Schema schema) {
        super(schema);
    }

    /**
     * Generate TypeDescriptorNode for allOf schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        assert schema instanceof ComposedSchema;
        ComposedSchema composedSchema = (ComposedSchema) schema;
        List<Node> recordFieldList = generateAllOfRecordFields(composedSchema.getAllOf());
        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
        return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD), createToken(OPEN_BRACE_TOKEN),
                fieldNodes, null, createToken(CLOSE_BRACE_TOKEN));
    }

    private List<Node> generateAllOfRecordFields(List<Schema> allOfSchemas) throws BallerinaOpenApiException {
        List<Node> recordFieldList = new ArrayList<>();
        for (Schema allOfSchema : allOfSchemas) {
            if (allOfSchema.get$ref() != null) {
                Token typeRef = AbstractNodeFactory.createIdentifierToken(getValidName(
                        extractReferenceType(allOfSchema.get$ref()), true));
                TypeReferenceNode recordField = NodeFactory.createTypeReferenceNode(createToken(ASTERISK_TOKEN),
                        typeRef, createToken(SEMICOLON_TOKEN));
                recordFieldList.add(recordField);
            } else if (allOfSchema.getProperties() != null) {
                Map<String, Schema> properties = allOfSchema.getProperties();
                List<String> required = allOfSchema.getRequired();
                recordFieldList.addAll(TypeGeneratorUtils.addRecordFields(required, properties.entrySet()));
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
}
