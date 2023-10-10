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

package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.document.DocCommentsGenerator;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNilLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.AT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.openapi.core.ErrorMessages.invalidPathParamType;
import static io.ballerina.openapi.core.GeneratorConstants.APPLICATION_OCTET_STREAM;
import static io.ballerina.openapi.core.GeneratorConstants.ARRAY;
import static io.ballerina.openapi.core.GeneratorConstants.BOOLEAN;
import static io.ballerina.openapi.core.GeneratorConstants.EMPTY_RECORD;
import static io.ballerina.openapi.core.GeneratorConstants.HTTP_REQUEST;
import static io.ballerina.openapi.core.GeneratorConstants.INTEGER;
import static io.ballerina.openapi.core.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.core.GeneratorConstants.NUMBER;
import static io.ballerina.openapi.core.GeneratorConstants.OBJECT;
import static io.ballerina.openapi.core.GeneratorConstants.PAYLOAD;
import static io.ballerina.openapi.core.GeneratorConstants.REQUEST;
import static io.ballerina.openapi.core.GeneratorConstants.SQUARE_BRACKETS;
import static io.ballerina.openapi.core.GeneratorConstants.STRING;
import static io.ballerina.openapi.core.GeneratorConstants.X_BALLERINA_DEPRECATED_REASON;
import static io.ballerina.openapi.core.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.core.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.core.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.GeneratorUtils.getBallerinaMediaType;
import static io.ballerina.openapi.core.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.GeneratorUtils.isArraySchema;
import static io.ballerina.openapi.core.GeneratorUtils.isComposedSchema;
import static io.ballerina.openapi.core.GeneratorUtils.isObjectSchema;

/**
 * This util class uses for generating {@link FunctionSignatureNode} for given OAS
 * operation.
 *
 * @since 1.3.0
 */
public class FunctionSignatureGenerator {
    private final OpenAPI openAPI;
    private final BallerinaTypesGenerator ballerinaSchemaGenerator;
    private final List<TypeDefinitionNode> typeDefinitionNodeList;
    private FunctionReturnTypeGenerator functionReturnType;
    private boolean deprecatedParamFound = false;
    private boolean isResource;

    public List<TypeDefinitionNode> getTypeDefinitionNodeList() {
        return typeDefinitionNodeList;
    }

    public FunctionSignatureGenerator(OpenAPI openAPI,
                                      BallerinaTypesGenerator ballerinaSchemaGenerator,
                                      List<TypeDefinitionNode> typeDefinitionNodeList, boolean isResource) {

        this.openAPI = openAPI;
        this.ballerinaSchemaGenerator = ballerinaSchemaGenerator;
        this.typeDefinitionNodeList = typeDefinitionNodeList;
        this.functionReturnType = new FunctionReturnTypeGenerator
                (openAPI, ballerinaSchemaGenerator, typeDefinitionNodeList);
        this.isResource = isResource;

    }

    /**
     * This function for generate function signatures.
     *
     * @param operation - openapi operation
     * @return {@link FunctionSignatureNode}
     * @throws BallerinaOpenApiException - throws exception when node creation fails.
     */
    public FunctionSignatureNode getFunctionSignatureNode(Operation operation, List<Node> remoteFunctionDoc)
            throws BallerinaOpenApiException {
        // Store the parameters for method.
        List<Node> parameterList = new ArrayList<>();

        setFunctionParameters(operation, parameterList, createToken(COMMA_TOKEN), remoteFunctionDoc);
        functionReturnType = new FunctionReturnTypeGenerator
                (openAPI, ballerinaSchemaGenerator, typeDefinitionNodeList);

        if (parameterList.size() >= 2) {
            parameterList.remove(parameterList.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(parameterList);
        //Create Return type - function with response
        String returnType = functionReturnType.getReturnType(operation, true);
        ApiResponses responses = operation.getResponses();
        Collection<ApiResponse> values = responses.values();
        Iterator<ApiResponse> iteratorRes = values.iterator();
        if (iteratorRes.hasNext()) {
            ApiResponse response = iteratorRes.next();
            if (response.getDescription() != null && !response.getDescription().isBlank()) {
                MarkdownParameterDocumentationLineNode returnDoc =
                        DocCommentsGenerator.createAPIParamDoc("return",
                        response.getDescription());
                remoteFunctionDoc.add(returnDoc);
            }
        }

        // Return Type
        ReturnTypeDescriptorNode returnTypeDescriptorNode = createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD),
                createEmptyNodeList(), createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(returnType)));
        return createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN), parameters, createToken(CLOSE_PAREN_TOKEN),
                returnTypeDescriptorNode);
    }

    /**
     * Generate function parameters.
     */
    private void setFunctionParameters(Operation operation, List<Node> parameterList, Token comma,
                                       List<Node> remoteFunctionDoc) throws BallerinaOpenApiException {

        List<Parameter> parameters = operation.getParameters();
        List<Node> defaultable = new ArrayList<>();
        List<Node> deprecatedParamDocComments = new ArrayList<>();
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                if (parameter.getDescription() != null && !parameter.getDescription().isBlank()) {
                    MarkdownDocumentationNode paramAPIDoc =
                            DocCommentsGenerator.createAPIParamDocFromSring(getValidName(
                                    parameter.getName(), false), parameter.getDescription());
                    remoteFunctionDoc.add(paramAPIDoc);
                }
                List<AnnotationNode> parameterAnnotationNodeList =
                        getParameterAnnotationNodeList(parameter, deprecatedParamDocComments);
                String in;
                if (parameter.get$ref() != null) {
                    String paramType = extractReferenceType(parameter.get$ref());
                    parameter = openAPI.getComponents().getParameters().get(paramType);
                    in = parameter.getIn();
                } else {
                    in = parameter.getIn();
                }

                switch (in) {
                    case "path":
                        Node param = getPathParameters(parameter, createNodeList(parameterAnnotationNodeList));
                        // Path parameters are always required.
                        if (!isResource) {
                            parameterList.add(param);
                            parameterList.add(comma);
                        }
                        break;
                    case "query":
                        Node paramq = getQueryParameters(parameter, createNodeList(parameterAnnotationNodeList));
                        if (paramq instanceof RequiredParameterNode) {
                            parameterList.add(paramq);
                            parameterList.add(comma);
                        } else {
                            defaultable.add(paramq);
                            defaultable.add(comma);
                        }
                        break;
                    case "header":
                        Node paramh = getHeaderParameter(parameter, createNodeList(parameterAnnotationNodeList));
                        if (paramh instanceof RequiredParameterNode) {
                            parameterList.add(paramh);
                            parameterList.add(comma);
                        } else {
                            defaultable.add(paramh);
                            defaultable.add(comma);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        // Handle RequestBody
        if (operation.getRequestBody() != null) {
            setRequestBodyParameters(operation.getOperationId(), operation.getRequestBody(), remoteFunctionDoc,
                    parameterList, defaultable);
        }
        remoteFunctionDoc.addAll(deprecatedParamDocComments);
        //Filter defaultable parameters
        if (!defaultable.isEmpty()) {
            parameterList.addAll(defaultable);
        }
    }

    private List<AnnotationNode> getParameterAnnotationNodeList(Parameter parameter,
                                                                List<Node> deprecatedParamDocComments) {
        List<AnnotationNode> parameterAnnotationNodeList = new ArrayList<>();
        DocCommentsGenerator.extractDisplayAnnotation(parameter.getExtensions(), parameterAnnotationNodeList);
        if (parameter.getDeprecated() != null && parameter.getDeprecated()) {
            if (!this.deprecatedParamFound) {
                deprecatedParamDocComments.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                        "# Deprecated parameters", false));
                this.deprecatedParamFound = true;
            }
            String deprecatedDescription = "";
            if (parameter.getExtensions() != null) {
                for (Map.Entry<String, Object> extension : parameter.getExtensions().entrySet()) {
                    if (extension.getKey().trim().equals(X_BALLERINA_DEPRECATED_REASON)) {
                        deprecatedDescription = extension.getValue().toString();
                        break;
                    }
                }
            }
            MarkdownParameterDocumentationLineNode paramAPIDoc =
                    DocCommentsGenerator.createAPIParamDoc(getValidName(
                            parameter.getName(), false), deprecatedDescription);
            deprecatedParamDocComments.add(paramAPIDoc);
            parameterAnnotationNodeList.add(createAnnotationNode(createToken(AT_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken("deprecated")), null));
        }
        return parameterAnnotationNodeList;
    }

    /**
     * Create query parameters.
     * <p>
     * Note: currently ballerina supports for this data type.
     * type BasicType boolean|int|float|decimal|string;
     * public type QueryParamType ()|BasicType|BasicType[];
     */
    public Node getQueryParameters(Parameter parameter, NodeList<AnnotationNode> parameterAnnotationNodeList)
            throws BallerinaOpenApiException {

        TypeDescriptorNode typeName;

        Schema parameterSchema = parameter.getSchema();
        String paramType = "";
        if (parameterSchema.get$ref() != null) {
            paramType = getValidName(extractReferenceType(parameterSchema.get$ref()), true);
            parameterSchema = openAPI.getComponents().getSchemas().get(paramType.trim());
        } else {
            paramType = convertOpenAPITypeToBallerina(parameterSchema);
            if (getOpenAPIType(parameterSchema).equals(ARRAY)) {
                if (getOpenAPIType(parameterSchema.getItems()) != null) {
                    String itemType = getOpenAPIType(parameterSchema.getItems());
                    if (itemType.equals(STRING) || itemType.equals(INTEGER) || itemType.equals(BOOLEAN) ||
                            itemType.equals(NUMBER)) {
                        if (parameterSchema.getItems().getEnum() != null &&
                                !parameterSchema.getItems().getEnum().isEmpty()) {
                            paramType = OPEN_PAREN_TOKEN.stringValue() +
                                    convertOpenAPITypeToBallerina(parameterSchema.getItems()) +
                                    CLOSE_PAREN_TOKEN.stringValue() + SQUARE_BRACKETS;
                        } else {
                            paramType = convertOpenAPITypeToBallerina(parameterSchema.getItems()) + SQUARE_BRACKETS;
                        }
                    } else {
                        throw new BallerinaOpenApiException("Unsupported parameter type is found in the parameter : " +
                                parameter.getName());
                    }
                } else if (parameterSchema.getItems().get$ref() != null) {
                    paramType = getValidName(extractReferenceType(
                            parameterSchema.getItems().get$ref().trim()), true) + SQUARE_BRACKETS;
                } else {
                    throw new BallerinaOpenApiException("Please define the array item type of the parameter : " +
                            parameter.getName());
                }
            }
        }
        if (parameter.getRequired()) {
            typeName = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(paramType));
            IdentifierToken paramName =
                    createIdentifierToken(getValidName(parameter.getName().trim(), false));
            return createRequiredParameterNode(parameterAnnotationNodeList, typeName, paramName);
        } else {
            IdentifierToken paramName =
                    createIdentifierToken(getValidName(parameter.getName().trim(), false));
            // Handle given default values in query parameter.
            if (parameterSchema.getDefault() != null) {
                typeName = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(paramType));
                LiteralValueToken literalValueToken;
                if (getOpenAPIType(parameterSchema).equals(STRING)) {
                    literalValueToken = createLiteralValueToken(null,
                            '"' + parameterSchema.getDefault().toString() + '"', createEmptyMinutiaeList(),
                            createEmptyMinutiaeList());
                } else {
                    literalValueToken =
                            createLiteralValueToken(null, parameterSchema.getDefault().toString(),
                                    createEmptyMinutiaeList(),
                                    createEmptyMinutiaeList());

                }
                return createDefaultableParameterNode(parameterAnnotationNodeList, typeName, paramName,
                        createToken(EQUAL_TOKEN), literalValueToken);
            } else {
                paramType = paramType.endsWith(NILLABLE) ? paramType : paramType + NILLABLE;
                typeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(paramType));
                NilLiteralNode nilLiteralNode =
                        createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
                return createDefaultableParameterNode(parameterAnnotationNodeList, typeName, paramName,
                        createToken(EQUAL_TOKEN), nilLiteralNode);
            }
        }
    }

    /**
     * Create path parameters.
     */
    public Node getPathParameters(Parameter parameter, NodeList<AnnotationNode> parameterAnnotationNodeList)
            throws BallerinaOpenApiException {

        IdentifierToken paramName = createIdentifierToken(getValidName(parameter.getName(), false));
        String type = "";
        Schema parameterSchema = parameter.getSchema();
        if (parameterSchema.get$ref() != null) {
            type = getValidName(extractReferenceType(parameterSchema.get$ref()), true);
            Schema schema = openAPI.getComponents().getSchemas().get(type.trim());
            TypeDefinitionNode typeDefinitionNode = ballerinaSchemaGenerator.getTypeDefinitionNode
                    (schema, type, new ArrayList<>());
            if (typeDefinitionNode.typeDescriptor().kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
                throw new BallerinaOpenApiException(String.format(
                        "Path parameter: '%s' is invalid. Ballerina does not support object type path parameters.",
                        parameter.getName()));
            }
        } else {
            type = convertOpenAPITypeToBallerina(parameter.getSchema());
            if (type.equals("anydata") || type.equals(SQUARE_BRACKETS) || type.equals("record {}")) {
                throw new BallerinaOpenApiException(invalidPathParamType(parameter.getName().trim()));
            }
        }
        BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(type));
        return createRequiredParameterNode(parameterAnnotationNodeList, typeName, paramName);
    }

    /**
     * Create header when it comes under the parameter section in swagger.
     */
    private Node getHeaderParameter(Parameter parameter, NodeList<AnnotationNode> parameterAnnotationNodeList)
            throws BallerinaOpenApiException {

        Schema schema = parameter.getSchema();
        IdentifierToken paramName = createIdentifierToken(getValidName(parameter.getName().trim(), false));
        return getHeader(parameter.getRequired(), schema, paramName, parameterAnnotationNodeList);
    }

    /**
     * Create header when it comes under the multipart/form-data encoding section in swagger.
     */
    private Node getHeaderEncoding(Map.Entry<String, Header> parameter, List<Node> remoteFunctionDoc)
            throws BallerinaOpenApiException {

        boolean isRequired = false;
        Schema schema = parameter.getValue().getSchema();
        IdentifierToken paramName = createIdentifierToken(getValidName(parameter.getKey(), false));
        List<AnnotationNode> parameterAnnotationNodeList = new ArrayList<>();

        if (parameter.getValue().getDescription() != null && !parameter.getValue().getDescription().isBlank()) {
            MarkdownParameterDocumentationLineNode paramAPIDoc = DocCommentsGenerator.createAPIParamDoc(
                    getValidName(parameter.getKey(), false), parameter.getValue().getDescription());
            remoteFunctionDoc.add(paramAPIDoc);
        }
        DocCommentsGenerator.extractDisplayAnnotation(parameter.getValue().getExtensions(),
                parameterAnnotationNodeList);

        if (parameter.getValue().getRequired() != null) {
            isRequired = parameter.getValue().getRequired();
        }
        return getHeader(isRequired, schema, paramName, createNodeList(parameterAnnotationNodeList));
    }

    /**
     * Create header for header parameter and encoding.
     */
    private Node getHeader(boolean isRequiredHeader, Schema schema, IdentifierToken paramName,
                           NodeList<AnnotationNode> parameterAnnotationNodeList) throws BallerinaOpenApiException {

        if (isRequiredHeader) {
            String type = convertOpenAPITypeToBallerina(schema);
            if (isArraySchema(schema)) {
                if (schema.getItems().get$ref() != null) {
                    type = extractReferenceType(schema.getItems().get$ref()) + SQUARE_BRACKETS;
                } else if (schema.getItems().getEnum() != null && !schema.getItems().getEnum().isEmpty()) {
                    type = OPEN_PAREN_TOKEN.stringValue() + convertOpenAPITypeToBallerina(schema.getItems())
                            + CLOSE_PAREN_TOKEN.stringValue() + SQUARE_BRACKETS;
                } else {
                    type = convertOpenAPITypeToBallerina(schema.getItems()) + SQUARE_BRACKETS;
                }
            }
            BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(type));
            return createRequiredParameterNode(parameterAnnotationNodeList, typeName, paramName);
        } else {
            if (schema.getDefault() != null) {
                BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(convertOpenAPITypeToBallerina(schema)));
                LiteralValueToken literalValueToken;
                if (getOpenAPIType(schema).equals(STRING)) {
                    literalValueToken = createLiteralValueToken(null,
                            '"' + schema.getDefault().toString() + '"', createEmptyMinutiaeList(),
                            createEmptyMinutiaeList());
                } else {
                    literalValueToken = createLiteralValueToken(null, schema.getDefault().toString(),
                            createEmptyMinutiaeList(), createEmptyMinutiaeList());
                }
                return createDefaultableParameterNode(parameterAnnotationNodeList, typeName, paramName,
                        createToken(EQUAL_TOKEN), literalValueToken);
            } else {
                String type = convertOpenAPITypeToBallerina(schema);
                String nillableType = type.endsWith(NILLABLE) ? type : type + NILLABLE;
                if (isArraySchema(schema)) {
                    if (schema.getItems().get$ref() != null) {
                        nillableType = extractReferenceType(schema.getItems().get$ref()) +
                                SQUARE_BRACKETS + NILLABLE;
                    } else if (schema.getItems().getEnum() != null &&
                            !schema.getItems().getEnum().isEmpty()) {
                        nillableType = OPEN_PAREN_TOKEN.stringValue() +
                                convertOpenAPITypeToBallerina(schema.getItems()) +
                                CLOSE_PAREN_TOKEN.stringValue() + SQUARE_BRACKETS
                                + NILLABLE;
                    } else {
                        nillableType = convertOpenAPITypeToBallerina(schema.getItems()) + SQUARE_BRACKETS
                                + NILLABLE;
                    }
                }
                BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(nillableType));
                NilLiteralNode nilLiteralNode =
                        createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
                return createDefaultableParameterNode(parameterAnnotationNodeList, typeName, paramName,
                        createToken(EQUAL_TOKEN), nilLiteralNode);
            }
        }
    }

    /**
     * Create request body parameter.
     */
    private void setRequestBodyParameters(String operationId, RequestBody requestBody, List<Node> requestBodyDoc,
                                          List<Node> parameterList, List<Node> defaultable)
            throws BallerinaOpenApiException {

        Content requestBodyContent;
        String referencedRequestBodyName = "";
        if (requestBody.get$ref() != null) {
            referencedRequestBodyName = extractReferenceType(requestBody.get$ref()).trim();
            RequestBody referencedRequestBody = openAPI.getComponents()
                    .getRequestBodies().get(referencedRequestBodyName);
            requestBodyContent = referencedRequestBody.getContent();
            // note : when there is referenced request body, the description at the reference is ignored.
            // Need to consider the description at the component level
            requestBody.setDescription(referencedRequestBody.getDescription());
        } else {
            requestBodyContent = requestBody.getContent();
        }

        Iterator<Map.Entry<String, MediaType>> iterator = requestBodyContent.entrySet().iterator();
        while (iterator.hasNext()) {
            // This implementation currently for first content type
            Map.Entry<String, MediaType> mediaTypeEntry = iterator.next();
            Schema schema = mediaTypeEntry.getValue().getSchema();
            String paramType = "";
            //Take payload type
            if (schema != null && GeneratorUtils.isSupportedMediaType(mediaTypeEntry)) {
                String mediaTypeEntryKey = mediaTypeEntry.getKey();
                if (mediaTypeEntryKey.equals(APPLICATION_OCTET_STREAM) ||
                        mediaTypeEntryKey.matches("application/.*\\+octet-stream")) {
                     paramType = getBallerinaMediaType(mediaTypeEntryKey, true);
                } else {
                    if (schema.get$ref() != null) {
                        paramType = getValidName(extractReferenceType(schema.get$ref().trim()), true);
                    } else if (getOpenAPIType(schema) != null && !getOpenAPIType(schema).equals(ARRAY) &&
                            !getOpenAPIType(schema).equals(
                            OBJECT)) {
                        paramType = convertOpenAPITypeToBallerina(schema);
                    } else if (isArraySchema(schema)) {
                        //TODO: handle nested array - this is impossible to handle
                        paramType = getRequestBodyParameterForArraySchema(operationId, mediaTypeEntry, schema);
                    } else if (isObjectSchema(schema) || schema.getProperties() != null) {
                        paramType = getRequestBodyParameterForObjectSchema(referencedRequestBodyName, schema);
                    } else { // composed and object schemas are handled by the flatten
                        paramType = getBallerinaMediaType(mediaTypeEntryKey, true);
                    }
                }
            } else {
                paramType = getBallerinaMediaType(mediaTypeEntry.getKey(), true);
            }

            String paramName = paramType.equals(HTTP_REQUEST) ? REQUEST : PAYLOAD;
            if (!paramType.isBlank()) {
                List<AnnotationNode> annotationNodes = new ArrayList<>();
                DocCommentsGenerator.extractDisplayAnnotation(requestBody.getExtensions(), annotationNodes);
                SimpleNameReferenceNode typeName = createSimpleNameReferenceNode(createIdentifierToken(paramType));
                IdentifierToken paramNameToken = createIdentifierToken(paramName);
                RequiredParameterNode payload = createRequiredParameterNode(
                        createNodeList(annotationNodes), typeName, paramNameToken);
                if (requestBody.getDescription() != null && !requestBody.getDescription().isBlank()) {
                    MarkdownDocumentationNode paramAPIDoc =
                            DocCommentsGenerator.createAPIParamDocFromSring(escapeIdentifier(paramName),
                                    requestBody.getDescription().split("\n")[0]);
                    requestBodyDoc.add(paramAPIDoc);
                }
                parameterList.add(payload);
                parameterList.add(createToken((COMMA_TOKEN)));
            }

            if (mediaTypeEntry.getKey().equals(javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA)
                    && mediaTypeEntry.getValue().getEncoding() != null) {
                List<String> headerList = new ArrayList<>();
                for (Map.Entry<String, Encoding> entry : mediaTypeEntry.getValue().getEncoding().entrySet()) {
                    if (entry.getValue().getHeaders() != null) {
                        for (Map.Entry<String, Header> header : entry.getValue().getHeaders().entrySet()) {
                            if (!headerList.contains(header.getKey())) {
                                Node headerParameter = getHeaderEncoding(header, requestBodyDoc);
                                if (headerParameter instanceof RequiredParameterNode) {
                                    parameterList.add(headerParameter);
                                    parameterList.add(createToken((COMMA_TOKEN)));
                                } else {
                                    defaultable.add(headerParameter);
                                    defaultable.add(createToken((COMMA_TOKEN)));
                                }
                                headerList.add(header.getKey());
                            }
                        }
                    }
                }
            }
            break;
        }
    }

    private String getRequestBodyParameterForObjectSchema (String recordName, Schema objectSchema)
            throws BallerinaOpenApiException {
        recordName = getValidName(recordName + "_RequestBody", true);
        if (objectSchema.getProperties() == null || objectSchema.getProperties().isEmpty()) {
            return EMPTY_RECORD;
        }
        TypeDefinitionNode record =
                ballerinaSchemaGenerator.getTypeDefinitionNode(objectSchema, recordName, new ArrayList<>());
        GeneratorUtils.updateTypeDefNodeList(recordName, record, typeDefinitionNodeList);
        return recordName;
    }

    /**
     * Generate RequestBody for array type schema.
     */
    private String getRequestBodyParameterForArraySchema(String operationId, Map.Entry<String, MediaType> next,
                                                         Schema<?> arraySchema) throws BallerinaOpenApiException {

        String paramType;
        Schema<?> arrayItems = arraySchema.getItems();
        if (getOpenAPIType(arrayItems) != null) {
            paramType = convertOpenAPITypeToBallerina(arrayItems) + SQUARE_BRACKETS;
        } else if (arrayItems.get$ref() != null) {
            paramType = getValidName(extractReferenceType(arrayItems.get$ref()), true) + SQUARE_BRACKETS;
        } else if (isComposedSchema(arrayItems)) {
            paramType = "CompoundArrayItem" + getValidName(operationId, true) + "Request";
            // TODO - Add API doc by checking requestBody
            TypeDefinitionNode arrayTypeNode =
                    ballerinaSchemaGenerator.getTypeDefinitionNode(arraySchema, paramType, new ArrayList<>());
            GeneratorUtils.updateTypeDefNodeList(paramType, arrayTypeNode, typeDefinitionNodeList);
        } else {
            paramType = getBallerinaMediaType(next.getKey().trim(), true) + SQUARE_BRACKETS;
        }
        return paramType;
    }

}
