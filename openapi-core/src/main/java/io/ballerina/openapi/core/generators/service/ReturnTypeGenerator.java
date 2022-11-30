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
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.core.GeneratorConstants;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
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
    private final BallerinaTypesGenerator ballerinaSchemaGenerator;
    private final String pathRecord;
    private static int countForRecord = 0;

    private final Map<String, TypeDefinitionNode> typeInclusionRecords = new HashMap<>();

    public Map<String, TypeDefinitionNode> getTypeInclusionRecords() {
        return this.typeInclusionRecords;
    }

    public ReturnTypeGenerator(BallerinaTypesGenerator ballerinaSchemaGenerator, String pathRecord) {
        this.ballerinaSchemaGenerator = ballerinaSchemaGenerator;
        this.pathRecord = pathRecord;
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

        Token returnKeyWord = createIdentifierToken("returns", GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
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
                    if (response.getValue().getContent() == null &&
                            response.getValue().get$ref() == null || response.getValue().getContent().size() == 0) {
                        String code = GeneratorConstants.HTTP_CODES_DES.get(response.getKey().trim());
                        // Scenario 01: Response has single response without content type
                        TypeDescriptorNode statues;
                        if (response.getKey().trim().equals(GeneratorConstants.DEFAULT)) {
                            statues = createSimpleNameReferenceNode(createIdentifierToken(
                                    GeneratorConstants.HTTP_RESPONSE));
                        } else {
                            statues = GeneratorUtils.getQualifiedNameReferenceNode(GeneratorConstants.HTTP, code);
                        }
                        returnNode = createReturnTypeDescriptorNode(returnKeyWord, annotations, statues);
                    } else if (response.getValue().getContent() != null) {
                        if (response.getKey().trim().equals(GeneratorConstants.HTTP_200)) {
                            Iterator<Map.Entry<String, MediaType>> iterator = response.getValue().getContent()
                                    .entrySet().iterator();
                            if (response.getValue().getContent().entrySet().size() > 1) {
                                returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(),
                                        getUnionNodeForContent(iterator));
                            } else {
                                while (iterator.hasNext()) {
                                    Map.Entry<String, MediaType> next = iterator.next();
                                    ImmutablePair<Optional<TypeDescriptorNode>, TypeDefinitionNode> mediaTypeTokens =
                                            getMediaTypeToken(next);
                                    Optional<TypeDescriptorNode> mediaTypeToken = mediaTypeTokens.getLeft();
                                    if (mediaTypeToken.isEmpty()) {
                                        BuiltinSimpleNameReferenceNode type = createBuiltinSimpleNameReferenceNode(null,
                                                createIdentifierToken(GeneratorConstants.HTTP_RESPONSE));
                                        returnNode = createReturnTypeDescriptorNode(returnKeyWord,
                                                createEmptyNodeList(), type);
                                        break;
                                    } else if (mediaTypeTokens.getRight() != null) {
                                        TypeDefinitionNode rightNode = mediaTypeTokens.getRight();
                                        countForRecord = countForRecord + 1;
                                        String recordName = countForRecord == 0 ?
                                                pathRecord + "Response" : pathRecord + "Response_" + countForRecord;
                                        typeInclusionRecords.put(recordName, rightNode);
                                        returnNode = createReturnTypeDescriptorNode(returnKeyWord,
                                                createEmptyNodeList(), rightNode);
                                    } else {

                                        returnNode = createReturnTypeDescriptorNode(returnKeyWord,
                                                createEmptyNodeList(), mediaTypeToken.get());
                                    }

//                                    returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(),
//                                            mediaTypeToken.get());
                                }
                            }
                        } else if (response.getKey().trim().equals(GeneratorConstants.DEFAULT)) {
                            BuiltinSimpleNameReferenceNode type = createBuiltinSimpleNameReferenceNode(null,
                                    createIdentifierToken(GeneratorConstants.HTTP_RESPONSE));
                            returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), type);
                        } else {
                            String code = GeneratorConstants.HTTP_CODES_DES.get(response.getKey().trim());
                            Content content = response.getValue().getContent();
                            Iterator<Map.Entry<String, MediaType>> contentItr = content.entrySet().iterator();
                            TypeDescriptorNode type;
                            if (content.entrySet().size() > 1) {
                                type = getUnionNodeForContent(contentItr);
                            } else {
                                // Handle for only first content type
                                Optional<TypeDescriptorNode> nodeForContent = getNodeForContent(contentItr);
                                type = nodeForContent.isEmpty() ?
                                        createSimpleNameReferenceNode(createIdentifierToken(
                                                GeneratorConstants.HTTP_RESPONSE)) : nodeForContent.get();
                            }
                            SimpleNameReferenceNode recordType = createReturnTypeInclusionRecord(code, type);
                            NodeList<AnnotationNode> annotation = createEmptyNodeList();
                            returnNode = createReturnTypeDescriptorNode(returnKeyWord, annotation, recordType);
                        }
                    }
                }
            }
        } else {
            // --error node TypeDescriptor
            returnNode = createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                    createSimpleNameReferenceNode(createIdentifierToken("error?")));
        }
        if (GeneratorUtils.isComplexURL(path)) {
            assert returnNode != null;
            String returnStatement = returnNode.toString().trim().replace("returns", "") + "|error";
            return createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                    createSimpleNameReferenceNode(createIdentifierToken(returnStatement)));
        }
        return returnNode;
    }

    /**
     * This function uses to handle the content details in OAS to map ballerina return node.
     *
     * @param contentItr - Media type from OAS
     * @return - {@link TypeDescriptorNode} for content type in ballerina
     * @throws BallerinaOpenApiException proceed when the process break.
     */
    private Optional<TypeDescriptorNode> getNodeForContent(Iterator<Map.Entry<String, MediaType>> contentItr)
            throws BallerinaOpenApiException {

        String dataType;
        Optional<TypeDescriptorNode> type = Optional.empty();
        while (contentItr.hasNext()) {
            Map.Entry<String, MediaType> mediaTypeEntry = contentItr.next();
            if (mediaTypeEntry.getValue().getSchema() != null) {
                Schema schema = mediaTypeEntry.getValue().getSchema();
                if (schema.get$ref() != null) {
                    dataType = GeneratorUtils.getValidName(extractReferenceType(schema.get$ref().trim()), true);
                    type = Optional.ofNullable(createBuiltinSimpleNameReferenceNode(null,
                            createIdentifierToken(dataType)));
                } else if (schema instanceof ComposedSchema) {
                    Iterator<Schema> iterator = ((ComposedSchema) schema).getOneOf().iterator();
                    type = Optional.ofNullable(getUnionNodeForOneOf(iterator));
                } else {
                    ImmutablePair<Optional<TypeDescriptorNode>, TypeDefinitionNode> mediaTypeToken =
                            getMediaTypeToken(mediaTypeEntry);
                    type = mediaTypeToken.left;
                    TypeDefinitionNode rightNode = mediaTypeToken.right;
                    if (rightNode != null) {
                        countForRecord = countForRecord + 1;
                        String recordName = countForRecord == 0 ? pathRecord + "Response" :
                                pathRecord + "Response_" + countForRecord;
                        typeInclusionRecords.put(recordName, rightNode);

                        type = Optional.of(createSimpleNameReferenceNode(createIdentifierToken(recordName)));
                    }
                }
            }
        }
        return type;
    }

    /**
     * Generate union type node when operation has multiple responses.
     */
    private UnionTypeDescriptorNode getUnionNode(Iterator<Map.Entry<String, ApiResponse>> responseIter)
            throws BallerinaOpenApiException {

        List<TypeDescriptorNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = createToken(SyntaxKind.PIPE_TOKEN);
        while (responseIter.hasNext()) {
            Map.Entry<String, ApiResponse> response = responseIter.next();
            String responseCode = response.getKey().trim();
            String code = GeneratorConstants.HTTP_CODES_DES.get(responseCode);

            if (code == null && !responseCode.equals(GeneratorConstants.DEFAULT)) {
                throw new BallerinaOpenApiException(String.format(OAS_SERVICE_107.getDescription(), responseCode));
            }

            if (responseCode.equals(GeneratorConstants.DEFAULT)) {
                TypeDescriptorNode record = createSimpleNameReferenceNode(createIdentifierToken(
                        GeneratorConstants.HTTP_RESPONSE));
                qualifiedNodes.add(record);
            } else if (response.getValue().getContent() == null && response.getValue().get$ref() == null ||
                    response.getValue().getContent() != null && response.getValue().getContent().size() == 0) {
                //key and value
                QualifiedNameReferenceNode node = GeneratorUtils.getQualifiedNameReferenceNode(GeneratorConstants.HTTP,
                        code);
                qualifiedNodes.add(node);
            } else if (response.getValue().getContent() != null) {
                TypeDescriptorNode record;
                Map.Entry<String, MediaType> contentType = response.getValue().getContent().entrySet()
                        .iterator().next();

                ImmutablePair<Optional<TypeDescriptorNode>, TypeDefinitionNode> mediaTypeToken =
                        getMediaTypeToken(contentType);
                Optional<TypeDescriptorNode> returnNode = mediaTypeToken.left;
                TypeDefinitionNode rightNode = mediaTypeToken.right;

                if (returnNode.isEmpty()) {
                    record = createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.HTTP_RESPONSE));
                } else if (rightNode != null) {
                    countForRecord = countForRecord + 1;
                    String recordName = countForRecord == 0 ? pathRecord + "Response" :
                            pathRecord + "Response_" + countForRecord;
                    typeInclusionRecords.put(recordName, rightNode);

                    record = createSimpleNameReferenceNode(createIdentifierToken(recordName));
                } else  {
                    record = returnNode.get();
                }
                if (responseCode.equals(GeneratorConstants.HTTP_200)) {
                    qualifiedNodes.add(record);
                } else {
                    SimpleNameReferenceNode node = createReturnTypeInclusionRecord(code, record);
                    qualifiedNodes.add(node);
                }
            }
        }
        TypeDescriptorNode right = qualifiedNodes.get(qualifiedNodes.size() - 1);
        TypeDescriptorNode traversRight = qualifiedNodes.get(qualifiedNodes.size() - 2);
        UnionTypeDescriptorNode traversUnion = createUnionTypeDescriptorNode(traversRight, pipeToken, right);
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
    private UnionTypeDescriptorNode getUnionNodeForContent(Iterator<Map.Entry<String, MediaType>> iterator)
            throws BallerinaOpenApiException {

        List<TypeDescriptorNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = createIdentifierToken("|");
        while (iterator.hasNext()) {
            Map.Entry<String, MediaType> contentType = iterator.next();
            ImmutablePair<Optional<TypeDescriptorNode>, TypeDefinitionNode> mediaTypeToken =
                    getMediaTypeToken(contentType);

            Optional<TypeDescriptorNode> leftNode = mediaTypeToken.left;
            TypeDefinitionNode rightNode = mediaTypeToken.right;

            if (leftNode.isEmpty()) {
                continue;
            } else if (rightNode != null) {
                countForRecord = countForRecord + 1;
                String recordName = countForRecord == 0 ? pathRecord + "Response" :
                        pathRecord + "Response_" + countForRecord;
                typeInclusionRecords.put(recordName, rightNode);
                qualifiedNodes.add(createSimpleNameReferenceNode(createIdentifierToken(recordName)));
            } else {
                TypeDescriptorNode typeDescriptorNode = leftNode.get();
                qualifiedNodes.add(typeDescriptorNode);
            }
        }
        TypeDescriptorNode right = qualifiedNodes.get(qualifiedNodes.size() - 1);
        TypeDescriptorNode traversRight = qualifiedNodes.get(qualifiedNodes.size() - 2);
        UnionTypeDescriptorNode traversUnion = createUnionTypeDescriptorNode(traversRight, pipeToken, right);
        if (qualifiedNodes.size() >= 3) {
            for (int i = qualifiedNodes.size() - 3; i >= 0; i--) {
                traversUnion = createUnionTypeDescriptorNode(qualifiedNodes.get(i), pipeToken, traversUnion);
            }
        }
        return traversUnion;
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
