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

package io.ballerina.openapi.corenew.service;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.corenew.typegenerator.BallerinaTypesGenerator;
import io.ballerina.openapi.corenew.typegenerator.GeneratorUtils;
import io.ballerina.openapi.corenew.typegenerator.ServiceDiagnosticMessages;
import io.ballerina.openapi.corenew.typegenerator.TypeHandler;
import io.ballerina.openapi.corenew.typegenerator.document.DocCommentsGenerator;
import io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURN_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.corenew.service.GeneratorConstants.DEFAULT_RETURN_COMMENT;
import static io.ballerina.openapi.corenew.service.GeneratorConstants.ERROR;
import static io.ballerina.openapi.corenew.service.GeneratorConstants.HTTP_RESPONSE;
import static io.ballerina.openapi.corenew.service.GeneratorConstants.RETURNS;
import static io.ballerina.openapi.corenew.typegenerator.GeneratorUtils.extractReferenceType;

/**
 * This class for generating return type definition node according to the OpenAPI specification response section.
 *
 * @since 1.3.0
 */
public class ReturnTypeGenerator {

    private final String pathRecord;
    private String httpMethod;
    private OpenAPI openAPI;

    private static int countForRecord = 0;

    public ReturnTypeGenerator(BallerinaTypesGenerator ballerinaSchemaGenerator, String pathRecord, OpenAPI openAPI) {
        this.pathRecord = pathRecord;
        this.openAPI = openAPI;
    }

    /**
     * This function used to generate return function node in the function signature.
     * Payload media type will not be added to the annotation.
     *
     * @param operation   OpenApi operation
     * @param annotations Annotation node list
     * @return return returnNode
     */
    public ReturnTypeDescriptorNode getReturnTypeDescriptorNode(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                                NodeList<AnnotationNode> annotations, String path,
                                                                List<Node> resourceFunctionDocs) throws
            BallerinaOpenApiException {

        ReturnTypeDescriptorNode returnNode;
        List<String> returnDescriptions = new ArrayList<>();
        httpMethod = operation.getKey().name().toLowerCase(Locale.ENGLISH);
        if (operation.getValue().getResponses() != null) {
            ApiResponses responses = operation.getValue().getResponses();
            if (responses.size() > 1) {
                //handle multiple response scenarios ex: status code 200, 400, 500
                TypeDescriptorNode type = handleMultipleResponse(responses, returnDescriptions, httpMethod, openAPI, pathRecord);
                returnNode = createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), annotations, type);
            } else if (responses.size() == 1) {
                //handle single response
                Iterator<Map.Entry<String, ApiResponse>> responseIterator = responses.entrySet().iterator();
                Map.Entry<String, ApiResponse> response = responseIterator.next();
                returnNode = handleSingleResponse(annotations, response, pathRecord, httpMethod);
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
     * Generate union type node when operation has multiple responses.
     */
    public static TypeDescriptorNode handleMultipleResponse(ApiResponses responses, List<String> returnDescription, String httpMethod, OpenAPI openAPI, String pathRecord)
            throws BallerinaOpenApiException {

        Set<String> qualifiedNodes = new LinkedHashSet<>();

        for (Map.Entry<String, ApiResponse> response : responses.entrySet()) {
            String responseCode = response.getKey().trim();
            String code = GeneratorConstants.HTTP_CODES_DES.get(responseCode);
            ApiResponse responseValue = response.getValue();
            Content content = responseValue != null ? responseValue.getContent() : null;
            String typeName = null;

            if (code == null && !responseCode.equals(GeneratorConstants.DEFAULT)) {
                throw new BallerinaOpenApiException(String.format(ServiceDiagnosticMessages.OAS_SERVICE_107.getDescription(), responseCode));
            }
            if (responseValue != null && responseValue.get$ref() != null) {
                String[] splits = responseValue.get$ref().split("/");
                String extractReferenceType = splits[splits.length - 1];
                responseValue = openAPI.getComponents().getResponses().get(extractReferenceType);
                content = responseValue.getContent();
            }
            if (responseCode.equals(GeneratorConstants.DEFAULT)) {
                TypeDescriptorNode record = TypeHandler.getSimpleNameReferenceNode(GeneratorConstants.HTTP_RESPONSE);
                typeName = record.toSourceCode();
            } else if (content == null && (responseValue == null || responseValue.get$ref() == null) ||
                    content != null && content.size() == 0) {
                //key and value
                QualifiedNameReferenceNode node = TypeHandler.getQualifiedNameReferenceNode(GeneratorConstants.HTTP,
                        code);
                typeName = node.toSourceCode();
            } else if (content != null) {
                TypeDescriptorNode bodyType = handleMultipleContents(content.entrySet(), pathRecord);
                //Check the default behaviour for return type according to POST method.
                boolean isWithOutStatusCode =
                        (httpMethod.equals(GeneratorConstants.POST) && responseCode.equals(GeneratorConstants.HTTP_201)) ||
                                (!httpMethod.equals(GeneratorConstants.POST) && responseCode.equals(GeneratorConstants.HTTP_200));
                if (isWithOutStatusCode) {
                    typeName = bodyType.toSourceCode();
                } else {
                    SimpleNameReferenceNode node = TypeHandler.createTypeInclusionRecord(code, bodyType);
                    typeName = node.name().text();
                }
            }
            if (typeName != null) {
                qualifiedNodes.add(typeName);
                if (responseValue.getDescription() != null && !responseValue.getDescription().isEmpty()) {
                    returnDescription.add(typeName.trim() + GeneratorConstants.PIPE + responseValue.getDescription().trim());
                }
            }
        }

        String unionType = String.join(GeneratorConstants.PIPE, qualifiedNodes);
        if (qualifiedNodes.contains(GeneratorConstants.ANYDATA)) {
            return NodeParser.parseTypeDescriptor(GeneratorConstants.ANYDATA);
        }
        return NodeParser.parseTypeDescriptor(unionType);
    }

    /**
     * Generate union type node when response has multiple content types.
     */
    public static TypeDescriptorNode handleMultipleContents(Set<Map.Entry<String, MediaType>> contentEntries, String pathRecord)
            throws BallerinaOpenApiException {
        Set<String> qualifiedNodes = new LinkedHashSet<>();
        for (Map.Entry<String, MediaType> contentType : contentEntries) {
            String recordName = getNewRecordName(pathRecord);
            ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> mediaTypeToken =
                    generateTypeDescriptorForMediaTypes(contentType, recordName);

            Optional<TypeDescriptorNode> leftNode = mediaTypeToken.left;
            Optional<TypeDefinitionNode> rightNode = mediaTypeToken.right;

            if (leftNode.isEmpty()) {
                SimpleNameReferenceNode httpResponse = TypeHandler.getSimpleNameReferenceNode(GeneratorConstants.ANYDATA);
                qualifiedNodes.add(httpResponse.name().text());
            } else if (rightNode.isPresent()) {
//                BallerinaTypesGenerator.typeDefinitionNodes.put(rightNode.get().typeName().text(), rightNode.get());
                setCountForRecord(countForRecord++);
//                qualifiedNodes.add(TypeHandler.getSimpleNameReferenceNode(recordName).toSourceCode());
                qualifiedNodes.add(rightNode.get().typeName().text());
            } else {
                TypeDescriptorNode typeDescriptorNode = leftNode.get();
                qualifiedNodes.add(typeDescriptorNode.toSourceCode());
            }
        }

        if (qualifiedNodes.size() == 1) {
            return NodeParser.parseTypeDescriptor(qualifiedNodes.iterator().next());
        }
        String unionType = String.join(GeneratorConstants.PIPE, qualifiedNodes);
        if (qualifiedNodes.contains(GeneratorConstants.ANYDATA)) {
            return NodeParser.parseTypeDescriptor(GeneratorConstants.ANYDATA);
        }
        return NodeParser.parseTypeDescriptor(unionType);
    }

    /**
     * This util is to generate return node when the operation has one response.
     */
    public ReturnTypeDescriptorNode handleSingleResponse(NodeList<AnnotationNode> annotations,
                                                                Map.Entry<String, ApiResponse> response, String pathRecord, String httpMethod)
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
                statues = TypeHandler.getSimpleNameReferenceNode(GeneratorConstants.HTTP_RESPONSE);
            } else {
                statues = TypeHandler.getQualifiedNameReferenceNode(GeneratorConstants.HTTP, code);
            }
            returnNode = createReturnTypeDescriptorNode(returnKeyWord, annotations, statues);
        } else if (responseContent != null) {
            // when the response has content values
            String responseCode = response.getKey().trim();
            boolean isWithOutStatusCode =
                    (httpMethod.equals(GeneratorConstants.POST) && responseCode.equals(GeneratorConstants.HTTP_201)) ||
                            (!httpMethod.equals(GeneratorConstants.POST) && responseCode.equals(GeneratorConstants.HTTP_200));
            if (isWithOutStatusCode) {
                // handle 200, 201 status code
                Set<Map.Entry<String, MediaType>> contentEntries = responseContent.entrySet();
                TypeDescriptorNode returnType;
                if (contentEntries.size() > 1) {
                    returnType = handleMultipleContents(contentEntries, pathRecord);
                } else {
                    returnType = getReturnNodeForSchemaType(contentEntries, getNewRecordName(pathRecord));
                }
                returnNode = createReturnTypeDescriptorNode(returnKeyWord, annotations, returnType);
            } else if (response.getKey().trim().equals(GeneratorConstants.DEFAULT)) {
                // handle status code with `default`, this maps to `http:Response`
                BuiltinSimpleNameReferenceNode type = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(GeneratorConstants.HTTP_RESPONSE));
                returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), type);
            } else {
                // handle rest of the status codes
                String code = GeneratorConstants.HTTP_CODES_DES.get(response.getKey().trim());
                TypeDescriptorNode type;

                if (responseContent.entrySet().size() > 1) {
                    // handle multiple media types
                    type = handleMultipleContents(responseContent.entrySet(), pathRecord);
                } else {
                    // handle single media type
                    Iterator<Map.Entry<String, MediaType>> contentItr = responseContent.entrySet().iterator();
                    Map.Entry<String, MediaType> mediaTypeEntry = contentItr.next();
                    String recordName = getNewRecordName(pathRecord);
                    ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> mediaTypeToken =
                            generateTypeDescriptorForMediaTypes(mediaTypeEntry, recordName);
                    Optional<TypeDefinitionNode> rightNode = mediaTypeToken.right;
                    if (rightNode.isPresent()) {
//                        BallerinaTypesGenerator.typeDefinitionNodes.put(rightNode.get().typeName().text(), rightNode.get());
                        setCountForRecord(countForRecord++);
                        type = createSimpleNameReferenceNode(createIdentifierToken(recordName));
                    } else {
                        type = mediaTypeToken.left.orElseGet(
                                () -> createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.ANYDATA)));
                    }
                }
                if (!type.toString().equals(GeneratorConstants.HTTP_RESPONSE)) {
                    SimpleNameReferenceNode recordType = TypeHandler.createTypeInclusionRecord(code, type);
                    NodeList<AnnotationNode> annotation = createEmptyNodeList();
                    returnNode = createReturnTypeDescriptorNode(returnKeyWord, annotation, recordType);
                }
            }
        }
        return returnNode;
    }

    /**
     * Generate TypeDescriptor for all the mediaTypes.
     *
     * Here return the @code{ImmutablePair<Optional<TypeDescriptorNode>, TypeDefinitionNode>} for return type.
     * Left node(key) of the return tuple represents the return type node for given mediaType details, Right Node
     * (Value) of the return tuple represents the newly generated TypeDefinitionNode for return type if it has inline
     * objects.
     */
    public static ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> generateTypeDescriptorForMediaTypes(
            Map.Entry<String, MediaType> mediaType, String recordName) throws BallerinaOpenApiException {
        String mediaTypeContent = selectMediaType(mediaType.getKey().trim());
        Schema<?> schema = mediaType.getValue().getSchema();
        return TypeHandler.generateTypeDescriptorForMediaTypes(mediaTypeContent, schema, recordName);
    }

    public TypeDescriptorNode getReturnNodeForSchemaType(Set<Map.Entry<String, MediaType>> contentEntries, String recordName)
            throws BallerinaOpenApiException {
        TypeDescriptorNode returnNode = null;
        for (Map.Entry<String, MediaType> next : contentEntries) {
            ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>>
                    mediaTypeToken = generateTypeDescriptorForMediaTypes(next, recordName);
            // right node represents the newly generated node for if there is an inline record in the returned
            // tuple.
            Optional<TypeDefinitionNode> rightNode = mediaTypeToken.right;
            if (rightNode.isPresent()) {
//                typeDefinitionNodes.put(rightNode.get().typeName().text(), rightNode.get());
                returnNode = createSimpleNameReferenceNode(createIdentifierToken(recordName));
            } else {
                returnNode = mediaTypeToken.left.orElseGet(
                        () -> createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(GeneratorConstants.ANYDATA)));
            }
        }
        return returnNode;
    }

    /**
     *
     * This util is used for selecting standard media type by looking at the user defined media type.
     */
    public static String selectMediaType(String mediaTypeContent) {
        if (mediaTypeContent.matches("application/.*\\+json") || mediaTypeContent.matches(".*/json")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_JSON;
        } else if (mediaTypeContent.matches("application/.*\\+xml") || mediaTypeContent.matches(".*/xml")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_XML;
        } else if (mediaTypeContent.matches("text/.*")) {
            mediaTypeContent = GeneratorConstants.TEXT;
        }  else if (mediaTypeContent.matches("application/.*\\+octet-stream")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_OCTET_STREAM;
        } else if (mediaTypeContent.matches("application/.*\\+x-www-form-urlencoded")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_URL_ENCODE;
        }
        return mediaTypeContent;
    }

    private static String getNewRecordName(String pathRecord) {
        return countForRecord == 0 ? pathRecord + GeneratorConstants.RESPONSE_RECORD_NAME : pathRecord + "Response_" + countForRecord;
    }

    public static void setCountForRecord(int count) {
        countForRecord = count;
    }
}
