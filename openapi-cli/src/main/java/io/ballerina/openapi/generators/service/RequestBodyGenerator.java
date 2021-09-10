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

package io.ballerina.openapi.generators.service;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.extractReferenceType;

public class RequestBodyGenerator {
    /**
     * This for creating request Body for given request object.
     */
    public List<Node> createNodeForRequestBody(RequestBody requestBody) throws BallerinaOpenApiException {
        List<Node> params = new ArrayList<>();
        Token comma = createToken(SyntaxKind.COMMA_TOKEN);

        List<Node> literals = new ArrayList<>();
        MappingConstructorExpressionNode annotValue;
        TypeDescriptorNode typeName;

        if (requestBody.getContent().entrySet().size() > 1) {
            IdentifierToken mediaType = createIdentifierToken("mediaType");
            // --create value expression
            // ---create expression
            // Filter same data type
            HashSet<Map.Entry<String, MediaType>> equalDataType = new HashSet();
            Set<Map.Entry<String, MediaType>> entries = requestBody.getContent().entrySet();
            Iterator<Map.Entry<String, MediaType>> iterator = entries.iterator();
            List<Map.Entry<String, MediaType>> updatedEntries = new ArrayList<>(entries);
            while (iterator.hasNext()) {
//             remove element from updateEntries
                Map.Entry<String, MediaType> mediaTypeEntry = iterator.next();
                updatedEntries.remove(mediaTypeEntry);
                if (!updatedEntries.isEmpty()) {
                    Iterator<Map.Entry<String, MediaType>> updateIter = updatedEntries.iterator();
                    while (updateIter.hasNext()) {
                        Map.Entry<String, MediaType> updateNext = updateIter.next();
                        MediaType parentValue = mediaTypeEntry.getValue();
                        MediaType childValue = updateNext.getValue();
                        if (parentValue.getSchema().get$ref() != null && childValue.getSchema().get$ref() != null) {
                            String parentRef = parentValue.getSchema().get$ref().trim();
                            String childRef = childValue.getSchema().get$ref().trim();
                            if (extractReferenceType(parentRef).equals(extractReferenceType(childRef))) {
                                equalDataType.add(updateNext);
                            }
                        }
                    }
                    if (!equalDataType.isEmpty()) {
                        equalDataType.add(mediaTypeEntry);
                        break;
                    }
                }
            }
            if (!equalDataType.isEmpty()) {

                typeName = getIdentifierTokenForJsonSchema(equalDataType.iterator().next().getValue().getSchema());
                Iterator<Map.Entry<String, MediaType>> iter = equalDataType.iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, MediaType> next = iter.next();
                    literals.add(createIdentifierToken('"' + next.getKey().trim() + '"'));
                    literals.add(comma);
                }
                literals.remove(literals.size() - 1);
                SeparatedNodeList<Node> expression = NodeFactory.createSeparatedNodeList(literals);
                ListConstructorExpressionNode valueExpr = NodeFactory.createListConstructorExpressionNode(
                        createToken(
                                SyntaxKind.OPEN_BRACKET_TOKEN), expression, createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
                SpecificFieldNode specificFieldNode = NodeFactory.createSpecificFieldNode(
                        null, mediaType, createToken(SyntaxKind.COLON_TOKEN), valueExpr);
                SeparatedNodeList<MappingFieldNode> fields = NodeFactory.createSeparatedNodeList(specificFieldNode);
                annotValue = NodeFactory.createMappingConstructorExpressionNode(createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                        fields, createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
            } else {
                typeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken("json"));
                annotValue = NodeFactory.createMappingConstructorExpressionNode(createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                        NodeFactory.createSeparatedNodeList(), createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
            }
        } else {
            Iterator<Map.Entry<String, MediaType>> content = requestBody.getContent().entrySet().iterator();
            Map.Entry<String, MediaType> next = createBasicLiteralNodeList(comma, leading, trailing, literals, content);
            typeName = getIdentifierToken(next);
            annotValue = NodeFactory.createMappingConstructorExpressionNode(createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                    NodeFactory.createSeparatedNodeList(), createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
        }

        AnnotationNode annotationNode = getAnnotationNode("Payload ", annotValue);
        NodeList<AnnotationNode> annotation =  NodeFactory.createNodeList(annotationNode);
        Token paramName = createIdentifierToken(" payload");

        RequiredParameterNode payload =
                createRequiredParameterNode(annotation, typeName, paramName);

        params.add(payload);
        params.add(comma);
        return params;
    }

    /**
     * Generate typeDescriptor for application/json type.
     */
    private  TypeDescriptorNode getIdentifierTokenForJsonSchema(Schema schema) throws BallerinaOpenApiException {
        IdentifierToken identifierToken;
        if (schema != null) {
            if (schema.get$ref() != null) {
                identifierToken = createIdentifierToken(extractReferenceType(schema.get$ref()));
            } else if (schema.getType() != null) {
                if (schema instanceof ObjectSchema) {
                    ReturnTypeGenerator returnTypeGenerator = new ReturnTypeGenerator();
                    return returnTypeGenerator.getRecordTypeDescriptorNode(schema);
                } else if (schema instanceof ArraySchema) {
                    TypeDescriptorNode member;
                    if (((ArraySchema) schema).getItems().get$ref() != null) {
                        member = createBuiltinSimpleNameReferenceNode(null,
                                createIdentifierToken(extractReferenceType(((ArraySchema) schema).
                                        getItems().get$ref())));
                    } else if (!(((ArraySchema) schema).getItems() instanceof ArraySchema)) {
                        member = createBuiltinSimpleNameReferenceNode(null,
                                createIdentifierToken("string"));
                    } else {
                        member = createBuiltinSimpleNameReferenceNode(null,
                                createIdentifierToken(convertOpenAPITypeToBallerina(
                                        ((ArraySchema) schema).getItems().getType())));
                    }
                    return  createArrayTypeDescriptorNode(member, createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                            createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
                } else {
                    identifierToken =  createIdentifierToken(schema.getType() + " ");
                }
            } else if (schema instanceof ComposedSchema) {
                if (((ComposedSchema) schema).getOneOf() != null) {
                    Iterator<Schema> iterator = ((ComposedSchema) schema).getOneOf().iterator();
                    return getUnionNodeForOneOf(iterator);
                } else {
                    identifierToken =  createIdentifierToken("json ");
                }
            } else {
                identifierToken =  createIdentifierToken("json ");
            }
        } else {
            identifierToken =  createIdentifierToken("json ");
        }
        return createSimpleNameReferenceNode(identifierToken);
    }

    private Map.Entry<String, MediaType> createBasicLiteralNodeList(Token comma, MinutiaeList leading,
                                                                    MinutiaeList trailing, List<Node> literals,
                                                                    Iterator<Map.Entry<String, MediaType>> con) {
        Map.Entry<String, MediaType> next = con.next();
        String text = next.getKey();
        Token literalToken = AbstractNodeFactory.createLiteralValueToken(null, text, leading, trailing);
        BasicLiteralNode basicLiteralNode = NodeFactory.createBasicLiteralNode(null, literalToken);
        literals.add(basicLiteralNode);
        literals.add(comma);
        return next;
    }

}
