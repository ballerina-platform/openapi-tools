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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.openapi.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.generators.GeneratorUtils.getQualifiedNameReferenceNode;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.extractReferenceType;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.getMediaTypeToken;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.getUnionNodeForOneOf;

public class ReturnTypeGenerator {
    /**
     * This function used to generate return function node in the function signature.
     * Payload media type will not be added to the annotation.
     * @param operation     OpenApi operation
     * @param annotations   Annotation node list
     * @return return returnNode
     * @throws BallerinaOpenApiException
     */
    public ReturnTypeDescriptorNode getReturnTypeDescriptorNode(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                                NodeList<AnnotationNode> annotations) throws BallerinaOpenApiException {
        Token returnKeyWord = createIdentifierToken("returns ");
        ReturnTypeDescriptorNode returnNode = null;
        if (operation.getValue().getResponses() != null) {
            ApiResponses responses = operation.getValue().getResponses();
            Iterator<Map.Entry<String, ApiResponse>> responseIter = responses.entrySet().iterator();
            if (responses.size() > 1) {
                UnionTypeDescriptorNode type = getUnionNode(responseIter);
                returnNode = createReturnTypeDescriptorNode(returnKeyWord, annotations, type);
            } else {
                while (responseIter.hasNext()) {
                    Map.Entry<String, ApiResponse> response = responseIter.next();
                    if (response.getValue().getContent() == null && response.getValue().get$ref() == null) {
                        String code = GeneratorConstants.HTTP_CODES_DES.get(response.getKey().trim());
                        // Scenario 01
                        QualifiedNameReferenceNode statues = getQualifiedNameReferenceNode("http", code);
                        returnNode = createReturnTypeDescriptorNode(returnKeyWord, annotations, statues);
                    } else if (response.getValue().getContent() != null) {

                        if (response.getKey().trim().equals("200")) {
                            Iterator<Map.Entry<String, MediaType>> iterator =
                                    response.getValue().getContent().entrySet().iterator();

                            if (response.getValue().getContent().entrySet().size() > 1) {
                                returnNode = createReturnTypeDescriptorNode(returnKeyWord,
                                        createEmptyNodeList(), getUnionNodeForContent(iterator));
                            } else {
                                while (iterator.hasNext()) {
                                    Map.Entry<String, MediaType> next = iterator.next();
                                    returnNode = createReturnTypeDescriptorNode(returnKeyWord,
                                            createEmptyNodeList(), getMediaTypeToken(next));
                                }
                            }

                        } else if (response.getKey().trim().equals("default")) {
                            BuiltinSimpleNameReferenceNode type  = createBuiltinSimpleNameReferenceNode(null,
                                    createIdentifierToken("http:Response"));
                            returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), type);

                        } else {
                            String code = GeneratorConstants.HTTP_CODES_DES.get(response.getKey().trim());
                            Content content = response.getValue().getContent();
                            Iterator<Map.Entry<String, MediaType>> contentItr = content.entrySet().iterator();

                            TypeDescriptorNode type = null;
                            if (content.entrySet().size() > 1) {
                                type = getUnionNodeForContent(contentItr);
                            } else {
                                // Handle for only first content type
                                String dataType;
                                while (contentItr.hasNext()) {
                                    Map.Entry<String, MediaType> mediaTypeEntry = contentItr.next();
                                    if (mediaTypeEntry.getValue().getSchema() != null) {
                                        Schema schema = mediaTypeEntry.getValue().getSchema();
                                        if (schema.get$ref() != null) {
                                            dataType = extractReferenceType(schema.get$ref().trim());
                                            type = createBuiltinSimpleNameReferenceNode(null,
                                                    createIdentifierToken(dataType));
                                        } else if (schema instanceof ObjectSchema) {
                                            type = getRecordTypeDescriptorNode(schema);
                                        } else if (schema instanceof ComposedSchema) {
                                            Iterator<Schema> iterator = ((ComposedSchema) schema).getOneOf().iterator();
                                            type = getUnionNodeForOneOf(iterator);
                                        } else {
                                            type =  getMediaTypeToken(mediaTypeEntry);
                                        }
                                    }
                                }
                            }

                            RecordTypeDescriptorNode recordType = createRecordTypeDescriptorNode(code, type);
                            NodeList<AnnotationNode> ann  = createEmptyNodeList();
                            returnNode = createReturnTypeDescriptorNode(returnKeyWord, ann, recordType);
                        }
                    }
                }
            }
        } else {
            // --error node TypeDescriptor
            returnNode = createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                    createSimpleNameReferenceNode(createIdentifierToken("error?")));
        }
        return returnNode;
    }

    /**
     * This for generate record node for object schema.
     */
    public TypeDescriptorNode getRecordTypeDescriptorNode(Schema schema) throws BallerinaOpenApiException {

        TypeDescriptorNode type;
        Token recordKeyWord = createIdentifierToken("record ");
        Token bodyStartDelimiter = createIdentifierToken("{|");
        // Create record fields
        List<Node> recordfields = new ArrayList<>();
        if (schema.getProperties() != null) {
            Map<String, Schema> properties =
                    (Map<String, Schema>) schema.getProperties();
            for (Map.Entry<String, Schema> field: properties.entrySet()) {
                Token fieldName = createIdentifierToken(field.getKey().trim());
                String typeF;
                if (field.getValue().get$ref() != null) {
                    typeF = extractReferenceType(field.getValue().get$ref());
                } else {
                    typeF = convertOpenAPITypeToBallerina(field.getValue().getType());
                }
                Token typeR = createIdentifierToken(typeF + " ");
                RecordFieldNode recordFieldNode =
                        NodeFactory.createRecordFieldNode(null, null, typeR, fieldName,
                                null, createToken(SyntaxKind.SEMICOLON_TOKEN));
                recordfields.add(recordFieldNode);
            }
        }

        NodeList<Node> fieldsList = NodeFactory.createSeparatedNodeList(recordfields);
        Token bodyEndDelimiter = createIdentifierToken("|}");
        type = NodeFactory.createRecordTypeDescriptorNode(recordKeyWord,
                bodyStartDelimiter, fieldsList, null,
                bodyEndDelimiter);
        return type;
    }

    /**
     * Generate union type node when operation has multiple responses.
     */
    private UnionTypeDescriptorNode getUnionNode(Iterator<Map.Entry<String, ApiResponse>> responseIter)
            throws BallerinaOpenApiException {
        List<TypeDescriptorNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = createIdentifierToken("|");

        while (responseIter.hasNext()) {
            Map.Entry<String, ApiResponse> response = responseIter.next();
            String code = GeneratorConstants.HTTP_CODES_DES.get(response.getKey().trim());
            if (response.getValue().getContent() == null && response.getValue().get$ref() == null) {
                //key and value
                QualifiedNameReferenceNode node = getQualifiedNameReferenceNode("http", code);
                qualifiedNodes.add(node);
            } else if (response.getValue().getContent() != null) {
                TypeDescriptorNode record =
                        getMediaTypeToken(response.getValue().getContent().entrySet().iterator().next());
                if (response.getKey().trim().equals("200"))  {
                    qualifiedNodes.add(record);
                } else if (response.getKey().trim().equals("default")) {
                    record = createSimpleNameReferenceNode(
                            createIdentifierToken("http:Response"));
                    qualifiedNodes.add(record);
                } else {
                    RecordTypeDescriptorNode node = createRecordTypeDescriptorNode(code, record);
                    qualifiedNodes.add(node);
                }
            }
        }
        TypeDescriptorNode right = qualifiedNodes.get(qualifiedNodes.size() - 1);
        TypeDescriptorNode traversRight = qualifiedNodes.get(qualifiedNodes.size() - 2);
        UnionTypeDescriptorNode traversUnion = createUnionTypeDescriptorNode(traversRight, pipeToken,
                right);
        if (qualifiedNodes.size() >= 3) {
            for (int i = qualifiedNodes.size() - 3; i >= 0; i--) {
                traversUnion = createUnionTypeDescriptorNode(qualifiedNodes.get(i), pipeToken, traversUnion);
            }
        }
        return traversUnion;
    }

    /**
     * Generate union type node when response has multiple content types.
     */
    private  UnionTypeDescriptorNode getUnionNodeForContent (Iterator<Map.Entry<String, MediaType>> iterator)
            throws BallerinaOpenApiException {
        List<SimpleNameReferenceNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = createIdentifierToken("|");

        while (iterator.hasNext()) {
            Map.Entry<String, MediaType> contentType = iterator.next();
            TypeDescriptorNode node = getMediaTypeToken(contentType);
            qualifiedNodes.add((SimpleNameReferenceNode) node);

        }
        SimpleNameReferenceNode right = qualifiedNodes.get(qualifiedNodes.size() - 1);
        SimpleNameReferenceNode traversRight = qualifiedNodes.get(qualifiedNodes.size() - 2);
        UnionTypeDescriptorNode traversUnion = createUnionTypeDescriptorNode(traversRight, pipeToken,
                right);
        if (qualifiedNodes.size() >= 3) {
            for (int i = qualifiedNodes.size() - 3; i >= 0; i--) {
                traversUnion = createUnionTypeDescriptorNode(qualifiedNodes.get(i), pipeToken,
                        traversUnion);
            }
        }
        return traversUnion;
    }

    /**
     * Create recordType TypeDescriptor.
     */
    private static RecordTypeDescriptorNode createRecordTypeDescriptorNode(String code, TypeDescriptorNode type) {

        // Create Type
        Token recordKeyWord = createIdentifierToken("record ");
        Token bodyStartDelimiter = createIdentifierToken("{|");
        // Create record fields
        List<Node> recordFields = new ArrayList<>();
        // Type reference node
        Token asteriskToken = createIdentifierToken("*");
        QualifiedNameReferenceNode typeNameField = getQualifiedNameReferenceNode("http", code);
        TypeReferenceNode typeReferenceNode =
                NodeFactory.createTypeReferenceNode(asteriskToken, typeNameField, createToken(SyntaxKind.SEMICOLON_TOKEN));
        recordFields.add(typeReferenceNode);
        // Record field name
        IdentifierToken fieldName = createIdentifierToken(" body");
        RecordFieldNode recordFieldNode =
                NodeFactory.createRecordFieldNode(null, null, type, fieldName,
                        null, createToken(SyntaxKind.SEMICOLON_TOKEN));
        recordFields.add(recordFieldNode);

        NodeList<Node> fieldsList = NodeFactory.createSeparatedNodeList(recordFields);
        Token bodyEndDelimiter = createIdentifierToken("|}");

        return NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                fieldsList, null, bodyEndDelimiter);
    }

}
