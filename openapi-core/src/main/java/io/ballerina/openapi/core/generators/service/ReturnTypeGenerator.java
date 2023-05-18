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
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
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
import io.ballerina.openapi.core.generators.document.DocCommentsGenerator;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.core.GeneratorConstants.ANYDATA;
import static io.ballerina.openapi.core.GeneratorConstants.DEFAULT_RETURN_COMMENT;
import static io.ballerina.openapi.core.GeneratorConstants.ERROR;
import static io.ballerina.openapi.core.GeneratorConstants.HTTP_RESPONSE;
import static io.ballerina.openapi.core.GeneratorConstants.PIPE;
import static io.ballerina.openapi.core.GeneratorConstants.POST;
import static io.ballerina.openapi.core.GeneratorConstants.RESPONSE_RECORD_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.RETURNS;
import static io.ballerina.openapi.core.generators.service.ServiceDiagnosticMessages.OAS_SERVICE_107;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.handleMediaType;

/**
 * This class for generating return type definition node according to the OpenAPI specification response section.
 *
 * @since 1.3.0
 */
public class ReturnTypeGenerator {

    private final BallerinaTypesGenerator ballerinaSchemaGenerator;
    private final String pathRecord;
    private static int countForRecord = 0;
    private String httpMethod;

    private final Map<String, TypeDefinitionNode> typeInclusionRecords = new HashMap<>();

    public Map<String, TypeDefinitionNode> getTypeInclusionRecords() {
        return this.typeInclusionRecords;
    }

    public static void setCount(int count) {
        ReturnTypeGenerator.countForRecord = count;
    }

    public void setCountForRecord(int count) {
        setCount(count);
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
                                                                NodeList<AnnotationNode> annotations, String path,
                                                                List<Node> resourceFunctionDocs)
            throws BallerinaOpenApiException {

        ReturnTypeDescriptorNode returnNode;
        List<String> returnDescriptions = new ArrayList<>();
        httpMethod = operation.getKey().name().toLowerCase(Locale.ENGLISH);
        if (operation.getValue().getResponses() != null) {
            ApiResponses responses = operation.getValue().getResponses();
            if (responses.size() > 1) {
                //handle multiple response scenarios ex: status code 200, 400, 500
                TypeDescriptorNode type = handleMultipleResponse(responses, returnDescriptions);
                returnNode = createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), annotations, type);
            } else if (responses.size() == 1) {
                //handle single response
                Iterator<Map.Entry<String, ApiResponse>> responseIterator = responses.entrySet().iterator();
                Map.Entry<String, ApiResponse> response = responseIterator.next();
                returnNode = handleSingleResponse(annotations, response);
                // checks `ifEmpty` not `ifBlank` because user can intentionally set the description to empty string
                if (response.getValue().getDescription() != null && !response.getValue().getDescription().isEmpty()) {
                    returnDescriptions.add(response.getValue().getDescription());
                } else {
                    // need to discuss
                    returnDescriptions.add(DEFAULT_RETURN_COMMENT);
                }
            } else {
                TypeDescriptorNode defaultType = createSimpleNameReferenceNode(createIdentifierToken(HTTP_RESPONSE));
                returnNode = createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), annotations, defaultType);
                returnDescriptions.add(HTTP_RESPONSE);
            }
        } else {
            // --error node TypeDescriptor
            returnNode = createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                    createSimpleNameReferenceNode(createIdentifierToken("error?")));
            returnDescriptions.add(ERROR);
        }

        // Add return description to the resource function
        if (returnDescriptions.size() > 1) {
            StringBuilder returnDescriptionForUnions = new StringBuilder(
                    "# + return - returns can be any of following types\n");
            String typeDescriptionTemplate = "#            %s (%s)%n";
            for (String description : returnDescriptions) {
                String[] values = description.split("\\|");
                // Replace new lines and tabs in the description by space.
                String responseDescription = values[1].replaceAll("[\\r\\n\\t]", " ");
                returnDescriptionForUnions.append(String.format(typeDescriptionTemplate,
                        values[0], responseDescription));
            }
            String dummyTypeWithDescription = returnDescriptionForUnions.toString() + "type a A;";
            ModuleMemberDeclarationNode moduleMemberDeclarationNode = NodeParser.parseModuleMemberDeclaration(
                    dummyTypeWithDescription);
            TypeDefinitionNode typeDefinitionNode = (TypeDefinitionNode) moduleMemberDeclarationNode;
            MetadataNode metadataNode = typeDefinitionNode.metadata().get();
            MarkdownDocumentationNode returnDoc = (MarkdownDocumentationNode) metadataNode.children().get(0);
            resourceFunctionDocs.add(returnDoc);
        } else {
            MarkdownParameterDocumentationLineNode returnDoc =
                    DocCommentsGenerator.createAPIParamDoc(RETURN_KEYWORD.stringValue(), returnDescriptions.get(0));
            resourceFunctionDocs.add(returnDoc);
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
            String responseCode = response.getKey().trim();
            boolean isWithOutStatusCode =
                    (httpMethod.equals(POST) && responseCode.equals(GeneratorConstants.HTTP_201)) ||
                            (!httpMethod.equals(POST) && responseCode.equals(GeneratorConstants.HTTP_200));
            if (isWithOutStatusCode) {
                // handle 200, 201 status code
                Set<Map.Entry<String, MediaType>> contentEntries = responseContent.entrySet();
                returnNode = getReturnNodeForSchemaType(contentEntries);
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
                    type = handleMultipleContents(responseContent.entrySet());
                } else {
                    // handle single media type
                    Iterator<Map.Entry<String, MediaType>> contentItr = responseContent.entrySet().iterator();
                    Map.Entry<String, MediaType> mediaTypeEntry = contentItr.next();
                    String recordName = getNewRecordName();
                    ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> mediaTypeToken =
                            handleMediaType(mediaTypeEntry, recordName);
                    Optional<TypeDefinitionNode> rightNode = mediaTypeToken.right;
                    if (rightNode.isPresent()) {
                        typeInclusionRecords.put(recordName, rightNode.get());
                        setCountForRecord(countForRecord++);
                        type = createSimpleNameReferenceNode(createIdentifierToken(recordName));
                    } else {
                        type = mediaTypeToken.left.orElseGet(
                                () -> createSimpleNameReferenceNode(createIdentifierToken(ANYDATA)));
                    }
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
    private ReturnTypeDescriptorNode getReturnNodeForSchemaType(Set<Map.Entry<String, MediaType>> contentEntries)
            throws BallerinaOpenApiException {

        Token returnKeyWord = createToken(RETURNS_KEYWORD);
        ReturnTypeDescriptorNode returnNode = null;

        int contentTypeNumber = contentEntries.size();
        if (contentTypeNumber > 1) {
            TypeDescriptorNode type = handleMultipleContents(contentEntries);
            returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), type);
        } else {
            for (Map.Entry<String, MediaType> next : contentEntries) {
                String recordName = getNewRecordName();
                ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>>
                        mediaTypeToken = handleMediaType(next, recordName);
                // right node represents the newly generated node for if there is an inline record in the returned
                // tuple.
                Optional<TypeDefinitionNode> rightNode = mediaTypeToken.right;

                if (rightNode.isPresent()) {
                    typeInclusionRecords.put(recordName, rightNode.get());
                    setCountForRecord(countForRecord++);
                    SimpleNameReferenceNode type = createSimpleNameReferenceNode(createIdentifierToken(recordName));
                    returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), type);
                } else {
                    TypeDescriptorNode returnType;
                    returnType = mediaTypeToken.left.orElseGet(
                            () -> createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(ANYDATA)));
                    returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), returnType);
                }
            }
        }
        return returnNode;
    }

    /**
     * Generate union type node when operation has multiple responses.
     */
    private TypeDescriptorNode handleMultipleResponse(ApiResponses responses, List<String> returnDescription)
            throws BallerinaOpenApiException {

        Set<String> qualifiedNodes = new LinkedHashSet<>();

        for (Map.Entry<String, ApiResponse> response : responses.entrySet()) {
            String responseCode = response.getKey().trim();
            String code = GeneratorConstants.HTTP_CODES_DES.get(responseCode);
            Content content = response.getValue().getContent();
            String typeName = null;

            if (code == null && !responseCode.equals(GeneratorConstants.DEFAULT)) {
                throw new BallerinaOpenApiException(String.format(OAS_SERVICE_107.getDescription(), responseCode));
            }

            if (responseCode.equals(GeneratorConstants.DEFAULT)) {
                TypeDescriptorNode record = createSimpleNameReferenceNode(createIdentifierToken(HTTP_RESPONSE));
                typeName = record.toSourceCode();
            } else if (content == null && response.getValue().get$ref() == null ||
                    content != null && content.size() == 0) {
                //key and value
                QualifiedNameReferenceNode node = GeneratorUtils.getQualifiedNameReferenceNode(GeneratorConstants.HTTP,
                        code);
                typeName = node.toSourceCode();
            } else if (content != null) {
                TypeDescriptorNode bodyType = handleMultipleContents(content.entrySet());
                //Check the default behaviour for return type according to POST method.
                boolean isWithOutStatusCode =
                        (httpMethod.equals(POST) && responseCode.equals(GeneratorConstants.HTTP_201)) ||
                                (!httpMethod.equals(POST) && responseCode.equals(GeneratorConstants.HTTP_200));
                if (isWithOutStatusCode) {
                    typeName = bodyType.toSourceCode();
                } else {
                    SimpleNameReferenceNode node = createReturnTypeInclusionRecord(code, bodyType);
                    typeName = node.name().text();
                }
            }
            if (typeName != null) {
                qualifiedNodes.add(typeName);
                if (response.getValue().getDescription() != null && !response.getValue().getDescription().isEmpty()) {
                    returnDescription.add(typeName.trim() + PIPE + response.getValue().getDescription().trim());
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
    private TypeDescriptorNode handleMultipleContents(Set<Map.Entry<String, MediaType>> contentEntries)
            throws BallerinaOpenApiException {
        Set<String> qualifiedNodes = new LinkedHashSet<>();
        for (Map.Entry<String, MediaType> contentType : contentEntries) {
            String recordName = getNewRecordName();
            ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> mediaTypeToken =
                    handleMediaType(contentType, recordName);

            Optional<TypeDescriptorNode> leftNode = mediaTypeToken.left;
            Optional<TypeDefinitionNode> rightNode = mediaTypeToken.right;

            if (leftNode.isEmpty()) {
                SimpleNameReferenceNode httpResponse = createSimpleNameReferenceNode(createIdentifierToken(ANYDATA));
                qualifiedNodes.add(httpResponse.name().text());
            } else if (rightNode.isPresent()) {
                typeInclusionRecords.put(recordName, rightNode.get());
                setCountForRecord(countForRecord++);
                qualifiedNodes.add(createSimpleNameReferenceNode(createIdentifierToken(recordName)).toSourceCode());
            } else {
                TypeDescriptorNode typeDescriptorNode = leftNode.get();
                qualifiedNodes.add(typeDescriptorNode.toSourceCode());
            }
        }

        if (qualifiedNodes.size() == 1) {
            return NodeParser.parseTypeDescriptor(qualifiedNodes.iterator().next());
        }
        String unionType = String.join(PIPE, qualifiedNodes);
        if (qualifiedNodes.contains(ANYDATA)) {
            return NodeParser.parseTypeDescriptor(ANYDATA);
        }
        return NodeParser.parseTypeDescriptor(unionType);
    }

    private String getNewRecordName() {
        return countForRecord == 0 ? pathRecord + RESPONSE_RECORD_NAME : pathRecord + "Response_" + countForRecord;
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
