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

package io.ballerina.openapi.core.generators.service;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.openapi.core.GeneratorConstants;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.core.GeneratorConstants.ANYDATA;
import static io.ballerina.openapi.core.GeneratorConstants.HTTP_RESPONSE;
import static io.ballerina.openapi.core.GeneratorConstants.PIPE;
import static io.ballerina.openapi.core.GeneratorConstants.RETURNS;
import static io.ballerina.openapi.core.generators.service.ServiceDiagnosticMessages.OAS_SERVICE_107;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.getMediaTypeToken;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.getUnionNodeForOneOf;

/**
 * This class for generating return type definition node according to the OpenAPI specification response section.
 *
 * @since 1.3.0
 */
public class ReturnTypeGenerator {

    private final Map<String, TypeDefinitionNode> typeInclusionRecords = new HashMap<>();

    public Map<String, TypeDefinitionNode> getTypeInclusionRecords() {

        return this.typeInclusionRecords;
    }

    /**
     * This function used to generate return function node in the function signature.
     * Payload media type will not be added to the annotation.
     *
     * @param operation   OpenApi operation
     * @param annotations Annotation node list
     * @return return returnNode
     * @throws BallerinaOpenApiException
     */
    public ReturnTypeDescriptorNode getReturnTypeDescriptorNode(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                                NodeList<AnnotationNode> annotations, String path)
            throws BallerinaOpenApiException {

        ReturnTypeDescriptorNode returnNode;
        if (operation.getValue().getResponses() != null) {
            ApiResponses responses = operation.getValue().getResponses();
            if (responses.size() > 1) {
                //handle multiple response scenarios ex: status code 200, 400, 500
                TypeDescriptorNode type = handleMultipleResponse(responses);
                returnNode = createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), annotations, type);
            } else {
                //handle single response
                Iterator<Map.Entry<String, ApiResponse>> responseIterator = responses.entrySet().iterator();
                Map.Entry<String, ApiResponse> response = responseIterator.next();
                returnNode = handleSingleResponse(annotations, response);
            }
        } else {
            // --error node TypeDescriptor
            returnNode = createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                    createSimpleNameReferenceNode(createIdentifierToken("error?")));
        }
        if (GeneratorUtils.isComplexURL(path)) {
            assert returnNode != null;
            String returnStatement = returnNode.toString().trim().replace(RETURNS, "") + "|error";
            return createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                    createSimpleNameReferenceNode(createIdentifierToken(returnStatement)));
        }
        return returnNode;
    }

    /**
     * This util is to generate return node when the operation has one response.
     */
    private ReturnTypeDescriptorNode handleSingleResponse(NodeList<AnnotationNode> annotations,
                                                          Map.Entry<String, ApiResponse> response)
            throws BallerinaOpenApiException {

        ReturnTypeDescriptorNode returnNode = null;
        Token returnKeyWord = createToken(RETURNS_KEYWORD);
        ApiResponse responseValue = response.getValue();
        Content responseContent = responseValue.getContent();

        if (responseContent == null && responseValue.get$ref() == null ||
                (responseContent != null && responseContent.size() == 0)) {
            // response has single response without content type or not having reference response.
            String code = GeneratorConstants.HTTP_CODES_DES.get(response.getKey().trim());
            TypeDescriptorNode statues;
            if (response.getKey().trim().equals(GeneratorConstants.DEFAULT)) {
                statues = createSimpleNameReferenceNode(createIdentifierToken(HTTP_RESPONSE));
            } else {
                statues = GeneratorUtils.getQualifiedNameReferenceNode(GeneratorConstants.HTTP, code);
            }
            returnNode = createReturnTypeDescriptorNode(returnKeyWord, annotations, statues);
        } else if (responseContent != null) {
            // when the response has content values
            if (response.getKey().trim().equals(GeneratorConstants.HTTP_200)) {
                // handle 200 status code
                Set<Map.Entry<String, MediaType>> contentEntries = responseContent.entrySet();
                returnNode = getReturnNodeForStatusCode200WithContent(contentEntries);
            } else if (response.getKey().trim().equals(GeneratorConstants.DEFAULT)) {
                // handle status code with `default`, this maps to `http:Response`
                BuiltinSimpleNameReferenceNode type = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(HTTP_RESPONSE));
                returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), type);
            } else {
                // handle rest of the status codes
                String code = GeneratorConstants.HTTP_CODES_DES.get(response.getKey().trim());
                TypeDescriptorNode type;

                if (responseContent.entrySet().size() > 1) {
                    // handle multiple media types
                    Optional<TypeDescriptorNode> unionNodeForContent =
                            handleMultipleContents(responseContent.entrySet());
                    type = unionNodeForContent.orElseGet(
                            () -> createSimpleNameReferenceNode(createIdentifierToken(HTTP_RESPONSE)));
                } else {
                    // handle single media type
                    Iterator<Map.Entry<String, MediaType>> contentItr = responseContent.entrySet().iterator();
                    Map.Entry<String, MediaType> mediaTypeEntry = contentItr.next();
                    Optional<TypeDescriptorNode> nodeForContent = handleSingleContent(mediaTypeEntry);
                    type = nodeForContent.orElseGet(
                            () -> createSimpleNameReferenceNode(createIdentifierToken(ANYDATA)));
                }
                if (!type.toString().equals(HTTP_RESPONSE)) {
                    SimpleNameReferenceNode recordType = createReturnTypeInclusionRecord(code, type);
                    NodeList<AnnotationNode> annotation = createEmptyNodeList();
                    returnNode = createReturnTypeDescriptorNode(returnKeyWord, annotation, recordType);
                }
            }
        }
        return returnNode;
    }

    /**
     * This util function is for handling the response which has 200 status code with content types.
     *
     * @param contentEntries collection of content entries
     */
    private ReturnTypeDescriptorNode getReturnNodeForStatusCode200WithContent(
            Set<Map.Entry<String, MediaType>> contentEntries) throws BallerinaOpenApiException {

        Token returnKeyWord = createToken(RETURNS_KEYWORD);
        ReturnTypeDescriptorNode returnNode = null;

        int contentTypeNumber = contentEntries.size();
        if (contentTypeNumber > 1) {
            Optional<TypeDescriptorNode> unionNode = handleMultipleContents(contentEntries);
            TypeDescriptorNode type =
                    unionNode.orElseGet(() -> createSimpleNameReferenceNode(createIdentifierToken(HTTP_RESPONSE)));
            returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), type);
        } else {
            for (Map.Entry<String, MediaType> next : contentEntries) {
                Optional<TypeDescriptorNode> mediaTypeToken = getMediaTypeToken(next);
                if (mediaTypeToken.isEmpty()) {
                    BuiltinSimpleNameReferenceNode type = createBuiltinSimpleNameReferenceNode(null,
                            createIdentifierToken(ANYDATA));
                    returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), type);
                    break;
                }
                returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), mediaTypeToken.get());
            }
        }
        return returnNode;
    }

    /**
     * This function uses to handle the content details in OAS to map ballerina return node.
     *
     * @param mediaTypeEntry - Media type from OAS
     * @return - {@link TypeDescriptorNode} for content type in ballerina
     * @throws BallerinaOpenApiException proceed when the process break.
     */
    private Optional<TypeDescriptorNode> handleSingleContent(Map.Entry<String, MediaType> mediaTypeEntry)
            throws BallerinaOpenApiException {

        String dataType;
        Optional<TypeDescriptorNode> type = Optional.empty();
        if (mediaTypeEntry.getValue().getSchema() != null) {
            Schema<?> schema = mediaTypeEntry.getValue().getSchema();
            if (schema.get$ref() != null) {
                dataType = GeneratorUtils.getValidName(extractReferenceType(schema.get$ref().trim()), true);
                type = Optional.ofNullable(createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(dataType)));
            } else if (schema instanceof ComposedSchema) {
                Iterator<Schema> iterator = ((ComposedSchema) schema).getOneOf().iterator();
                type = Optional.ofNullable(getUnionNodeForOneOf(iterator));
            } else {
                type = getMediaTypeToken(mediaTypeEntry);
            }
        }
        return type;
    }

    /**
     * Generate union type node when operation has multiple responses.
     */
    private TypeDescriptorNode handleMultipleResponse(ApiResponses responses)
            throws BallerinaOpenApiException {

        Set<String> qualifiedNodes = new HashSet<>();

        for (Map.Entry<String, ApiResponse> response : responses.entrySet()) {
            String responseCode = response.getKey().trim();
            String code = GeneratorConstants.HTTP_CODES_DES.get(responseCode);

            if (code == null && !responseCode.equals(GeneratorConstants.DEFAULT)) {
                throw new BallerinaOpenApiException(String.format(OAS_SERVICE_107.getDescription(), responseCode));
            }

            if (responseCode.equals(GeneratorConstants.DEFAULT)) {
                TypeDescriptorNode record = createSimpleNameReferenceNode(createIdentifierToken(
                        HTTP_RESPONSE));
                qualifiedNodes.add(record.toString());
            } else if (response.getValue().getContent() == null && response.getValue().get$ref() == null ||
                    response.getValue().getContent() != null && response.getValue().getContent().size() == 0) {
                //key and value
                QualifiedNameReferenceNode node = GeneratorUtils.getQualifiedNameReferenceNode(GeneratorConstants.HTTP,
                        code);
                qualifiedNodes.add(node.toString());
            } else if (response.getValue().getContent() != null) {
                Set<Map.Entry<String, MediaType>> entries = response.getValue().getContent().entrySet();
                Optional<TypeDescriptorNode> returnNode = handleMultipleContents(entries);
                TypeDescriptorNode record;
                record = returnNode.orElseGet(
                        () -> createSimpleNameReferenceNode(createIdentifierToken(ANYDATA)));
                if (responseCode.equals(GeneratorConstants.HTTP_200)) {
                    qualifiedNodes.add(record.toString());
                } else {
                    SimpleNameReferenceNode node = createReturnTypeInclusionRecord(code, record);
                    qualifiedNodes.add(node.name().text());
                }
            }
        }

        String unionType = String.join(PIPE, qualifiedNodes);
        if (qualifiedNodes.contains(ANYDATA)) {
            return NodeParser.parseTypeDescriptor(ANYDATA);
        }
        return NodeParser.parseTypeDescriptor(unionType);
    }

    /**
     * Generate union type node when response has multiple content types.
     */
    private Optional<TypeDescriptorNode> handleMultipleContents(Set<Map.Entry<String, MediaType>> contentEntries)
            throws BallerinaOpenApiException {

        Set<String> qualifiedNodes = new HashSet<>();
        for (Map.Entry<String, MediaType> contentType : contentEntries) {
            Optional<TypeDescriptorNode> node = getMediaTypeToken(contentType);
            if (node.isEmpty()) {
                SimpleNameReferenceNode httpResponse = createSimpleNameReferenceNode(createIdentifierToken(ANYDATA));
                qualifiedNodes.add(httpResponse.name().text());
                continue;
            }
            qualifiedNodes.add(node.get().toString());
        }
        if (qualifiedNodes.size() == 1) {
            return Optional.of(NodeParser.parseTypeDescriptor(qualifiedNodes.iterator().next()));
        }
        String unionType = String.join(PIPE, qualifiedNodes);
        if (qualifiedNodes.contains(ANYDATA)) {
            return Optional.of(NodeParser.parseTypeDescriptor(ANYDATA));
        }
        return Optional.of(NodeParser.parseTypeDescriptor(unionType));
    }

    /**
     * Create recordType TypeDescriptor.
     */
    private SimpleNameReferenceNode createReturnTypeInclusionRecord(String statusCode, TypeDescriptorNode type) {

        String recordName = statusCode + GeneratorUtils.getValidName(type.toString(), true);
        Token recordKeyWord = createToken(RECORD_KEYWORD);
        Token bodyStartDelimiter = createIdentifierToken("{|");
        // Create record fields
        List<Node> recordFields = new ArrayList<>();
        // Type reference node
        Token asteriskToken = createIdentifierToken("*");
        QualifiedNameReferenceNode typeNameField = GeneratorUtils.getQualifiedNameReferenceNode(GeneratorConstants.HTTP,
                statusCode);
        TypeReferenceNode typeReferenceNode = createTypeReferenceNode(
                asteriskToken,
                typeNameField,
                createToken(SyntaxKind.SEMICOLON_TOKEN));
        recordFields.add(typeReferenceNode);

        IdentifierToken fieldName = createIdentifierToken(GeneratorConstants.BODY, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        RecordFieldNode recordFieldNode = createRecordFieldNode(
                null, null,
                type,
                fieldName, null,
                createToken(SyntaxKind.SEMICOLON_TOKEN));
        recordFields.add(recordFieldNode);

        NodeList<Node> fieldsList = createSeparatedNodeList(recordFields);
        Token bodyEndDelimiter = createIdentifierToken("|}");

        RecordTypeDescriptorNode recordTypeDescriptorNode = createRecordTypeDescriptorNode(
                recordKeyWord,
                bodyStartDelimiter,
                fieldsList, null,
                bodyEndDelimiter);

        TypeDefinitionNode typeDefinitionNode = createTypeDefinitionNode(null,
                createToken(PUBLIC_KEYWORD),
                createToken(TYPE_KEYWORD),
                createIdentifierToken(recordName),
                recordTypeDescriptorNode,
                createToken(SEMICOLON_TOKEN));

        typeInclusionRecords.put(recordName, typeDefinitionNode);

        return createSimpleNameReferenceNode(createIdentifierToken(recordName));
    }
}
