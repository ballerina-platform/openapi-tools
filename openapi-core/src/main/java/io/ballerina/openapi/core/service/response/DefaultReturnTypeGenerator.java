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

package io.ballerina.openapi.core.service.response;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.type.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.GeneratorConstants;
import io.ballerina.openapi.core.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.openapi.core.service.ServiceGenerationUtils;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.openapi.core.service.GeneratorConstants.DEFAULT_RETURN_COMMENT;
import static io.ballerina.openapi.core.service.GeneratorConstants.ERROR;
import static io.ballerina.openapi.core.service.GeneratorConstants.HTTP_RESPONSE;
import static io.ballerina.openapi.core.service.GeneratorConstants.RETURNS;

/**
 * This class for generating return type definition node according to the OpenAPI specification response section.
 *
 * @since 1.3.0
 */
public class DefaultReturnTypeGenerator extends ReturnTypeGenerator {

    private static int countForRecord = 0;

    public DefaultReturnTypeGenerator(OASServiceMetadata oasServiceMetadata, String pathRecord) {
        super(oasServiceMetadata, pathRecord);
    }

    /**
     * This function used to generate return function node in the function signature.
     * Payload media type will not be added to the annotation.
     *
     * @param operation   OpenApi operation
     * @return return returnNode
     */
    public ReturnTypeDescriptorNode getReturnTypeDescriptorNode(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                                String path)
            throws OASTypeGenException {
        ReturnTypeDescriptorNode returnNode;
        String httpMethod = operation.getKey().name().toLowerCase(Locale.ENGLISH);
        if (operation.getValue().getResponses() != null) {
            ApiResponses responses = operation.getValue().getResponses();
            if (responses.size() > 1) {
                //handle multiple response scenarios ex: status code 200, 400, 500
                TypeDescriptorNode type = handleMultipleResponse(responses, httpMethod,
                        oasServiceMetadata.getOpenAPI(), pathRecord);
                returnNode = createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(), type);
            } else if (responses.size() == 1) {
                //handle single response
                Iterator<Map.Entry<String, ApiResponse>> responseIterator = responses.entrySet().iterator();
                Map.Entry<String, ApiResponse> response = responseIterator.next();
                returnNode = handleSingleResponse(response, pathRecord, httpMethod);
            } else {
                TypeDescriptorNode defaultType = createSimpleNameReferenceNode(createIdentifierToken(HTTP_RESPONSE));
                returnNode = createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(), defaultType);
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
     * Generate union type node when operation has multiple responses.
     */
    private static TypeDescriptorNode handleMultipleResponse(ApiResponses responses, String httpMethod,
                                                             OpenAPI openAPI, String pathRecord)
            throws OASTypeGenException {

        Set<String> qualifiedNodes = new LinkedHashSet<>();

        for (Map.Entry<String, ApiResponse> response : responses.entrySet()) {
            String responseCode = response.getKey().trim();
            String code = GeneratorConstants.HTTP_CODES_DES.get(responseCode);
            ApiResponse responseValue = response.getValue();
            Content content = responseValue != null ? responseValue.getContent() : null;
            String typeName = null;

            if (code == null && !responseCode.equals(GeneratorConstants.DEFAULT)) {
                throw new OASTypeGenException(String.format(ServiceDiagnosticMessages.OAS_SERVICE_107.getDescription(),
                        responseCode));
            }
            if (responseValue != null && responseValue.get$ref() != null) {
                String[] splits = responseValue.get$ref().split("/");
                String extractReferenceType = splits[splits.length - 1];
                responseValue = openAPI.getComponents().getResponses().get(extractReferenceType);
                content = responseValue.getContent();
            }
            if (responseCode.equals(GeneratorConstants.DEFAULT)) {
                TypeDescriptorNode record = createSimpleNameReferenceNode(createIdentifierToken(HTTP_RESPONSE));
                typeName = record.toSourceCode();
            } else if (content == null && (responseValue == null || responseValue.get$ref() == null) ||
                    content != null && content.isEmpty()) {
                //key and value
                QualifiedNameReferenceNode node = ServiceGenerationUtils.getQualifiedNameReferenceNode(
                        GeneratorConstants.HTTP, code);
                typeName = node.toSourceCode();
            } else if (content != null) {
                //Check the default behaviour for return type according to POST method.
                boolean isWithOutStatusCode =
                        (httpMethod.equals(GeneratorConstants.POST) &&
                                responseCode.equals(GeneratorConstants.HTTP_201)) ||
                                (!httpMethod.equals(GeneratorConstants.POST) &&
                                        responseCode.equals(GeneratorConstants.HTTP_200));
                if (isWithOutStatusCode) {
                    typeName = handleMultipleContents(content.entrySet(), pathRecord).toSourceCode();
                } else {
                    TypeDescriptorNode statusCodeTypeInclusionRecord = TypeHandler.getInstance()
                            .createStatusCodeTypeInclusionRecord(code, responseValue);
                    typeName = statusCodeTypeInclusionRecord.toSourceCode();
                }
            }
            if (typeName != null) {
                qualifiedNodes.add(typeName);
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
    private static TypeDescriptorNode handleMultipleContents(Set<Map.Entry<String, MediaType>> contentEntries,
                                                            String pathRecord)
            throws OASTypeGenException {
        Set<String> qualifiedNodes = new LinkedHashSet<>();
        for (Map.Entry<String, MediaType> contentType : contentEntries) {
//            String recordName = getNewRecordName(pathRecord);

            TypeDescriptorNode mediaTypeToken = ServiceGenerationUtils.generateTypeDescriptorForMediaTypes(contentType);
            if (mediaTypeToken == null) {
                SimpleNameReferenceNode httpResponse = createSimpleNameReferenceNode(createIdentifierToken(
                        GeneratorConstants.ANYDATA));
                qualifiedNodes.add(httpResponse.name().text());
            } else if (mediaTypeToken instanceof NameReferenceNode) {
                setCountForRecord(countForRecord++);
                qualifiedNodes.add(mediaTypeToken.toSourceCode());
            } else {
                qualifiedNodes.add(mediaTypeToken.toSourceCode());
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
    private ReturnTypeDescriptorNode handleSingleResponse(Map.Entry<String, ApiResponse> response, String pathRecord,
                                                          String httpMethod)
            throws OASTypeGenException {

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
                statues = ServiceGenerationUtils.getQualifiedNameReferenceNode(GeneratorConstants.HTTP, code);
            }
            returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), statues);
        } else if (responseContent != null) {
            // when the response has content values
            String responseCode = response.getKey().trim();
            boolean isWithOutStatusCode =
                    (httpMethod.equals(GeneratorConstants.POST) && responseCode.equals(GeneratorConstants.HTTP_201)) ||
                            (!httpMethod.equals(GeneratorConstants.POST) &&
                                    responseCode.equals(GeneratorConstants.HTTP_200));
            if (isWithOutStatusCode) {
                // handle 200, 201 status code
                Set<Map.Entry<String, MediaType>> contentEntries = responseContent.entrySet();
                TypeDescriptorNode returnType;
                if (contentEntries.size() > 1) {
                    returnType = handleMultipleContents(contentEntries, pathRecord);
                } else {
                    returnType = getReturnNodeForSchemaType(contentEntries.iterator().next(),
                            getNewRecordName(pathRecord));
                }
                returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), returnType);
            } else if (response.getKey().trim().equals(GeneratorConstants.DEFAULT)) {
                // handle status code with `default`, this maps to `http:Response`
                BuiltinSimpleNameReferenceNode type = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(HTTP_RESPONSE));
                returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(), type);
            } else {
                // handle rest of the status codes
                String code = GeneratorConstants.HTTP_CODES_DES.get(response.getKey().trim());
                TypeDescriptorNode statusCodeTypeInclusionRecord = TypeHandler.getInstance()
                        .createStatusCodeTypeInclusionRecord(code, responseValue);
                returnNode = createReturnTypeDescriptorNode(returnKeyWord, createEmptyNodeList(),
                        statusCodeTypeInclusionRecord);
            }
        }
        return returnNode;
    }

    private TypeDescriptorNode getReturnNodeForSchemaType(Map.Entry<String, MediaType> contentEntry, String recordName)
            throws OASTypeGenException {
        TypeDescriptorNode returnNode;
        TypeDescriptorNode mediaTypeToken = ServiceGenerationUtils
                .generateTypeDescriptorForMediaTypes(contentEntry);
        if (mediaTypeToken == null) {
            returnNode = createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.ANYDATA));
        } else {
            returnNode = mediaTypeToken;
        }
        return returnNode;
    }

    private static String getNewRecordName(String pathRecord) {
        return countForRecord == 0 ? pathRecord + GeneratorConstants.RESPONSE_RECORD_NAME : pathRecord + "Response_" + countForRecord;
    }

    public static void setCountForRecord(int count) {
        countForRecord = count;
    }
}
