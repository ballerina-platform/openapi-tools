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

package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
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
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.DocCommentsGenerator;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.ballerina.openapi.generators.schema.BallerinaTypesGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.AT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.openapi.ErrorMessages.invalidPathParamType;
import static io.ballerina.openapi.generators.GeneratorConstants.ANY_TYPE;
import static io.ballerina.openapi.generators.GeneratorConstants.BINARY;
import static io.ballerina.openapi.generators.GeneratorConstants.BYTE;
import static io.ballerina.openapi.generators.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.generators.GeneratorConstants.SQUARE_BRACKETS;
import static io.ballerina.openapi.generators.GeneratorConstants.STRING;
import static io.ballerina.openapi.generators.GeneratorConstants.VENDOR_SPECIFIC_TYPE;
import static io.ballerina.openapi.generators.GeneratorConstants.X_BALLERINA_DEPRECATED_REASON;
import static io.ballerina.openapi.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.generators.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.generators.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;

/**
 * This util class uses for generating {@link io.ballerina.compiler.syntax.tree.FunctionSignatureNode} for given OAS
 * operation.
 */
public class FunctionSignatureGenerator {
    private final OpenAPI openAPI;
    private final BallerinaTypesGenerator ballerinaSchemaGenerator;
    private final List<TypeDefinitionNode> typeDefinitionNodeList;
    private FunctionReturnTypeGenerator functionReturnType;
    private boolean deprecatedParamFound = false;

    public List<TypeDefinitionNode> getTypeDefinitionNodeList() {
        return typeDefinitionNodeList;
    }

    public FunctionSignatureGenerator(OpenAPI openAPI,
                                      BallerinaTypesGenerator ballerinaSchemaGenerator,
                                      List<TypeDefinitionNode> typeDefinitionNodeList) {

        this.openAPI = openAPI;
        this.ballerinaSchemaGenerator = ballerinaSchemaGenerator;
        this.typeDefinitionNodeList = typeDefinitionNodeList;
        this.functionReturnType =  new FunctionReturnTypeGenerator
                (openAPI, ballerinaSchemaGenerator, typeDefinitionNodeList);

    }

    /**
     * This function for generate function signatures.
     *
     * @param operation     - openapi operation
     * @return {@link io.ballerina.compiler.syntax.tree.FunctionSignatureNode}
     * @throws BallerinaOpenApiException - throws exception when node creation fails.
     */
    public FunctionSignatureNode getFunctionSignatureNode(Operation operation, List<Node> remoteFunctionDoc)
            throws BallerinaOpenApiException {
        // Store the parameters for method.
        List<Node> parameterList =  new ArrayList<>();
        functionReturnType =  new FunctionReturnTypeGenerator
                (openAPI, ballerinaSchemaGenerator, typeDefinitionNodeList);
        setFunctionParameters(operation, parameterList, createToken(COMMA_TOKEN), remoteFunctionDoc);

        if (parameterList.size() >= 2) {
            parameterList.remove(parameterList.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(parameterList);
        //Create Return type - function with response
        String returnType = functionReturnType.getReturnType(operation, true);
        ApiResponses responses = operation.getResponses();
        Collection<ApiResponse> values = responses.values();
        Iterator<ApiResponse> iteratorRes = values.iterator();
        ApiResponse next = iteratorRes.next();
        if (next.getDescription() != null) {
            MarkdownParameterDocumentationLineNode returnDoc = DocCommentsGenerator.createAPIParamDoc("return",
                    next.getDescription());
            remoteFunctionDoc.add(returnDoc);
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
            for (Parameter parameter: parameters) {
                if (parameter.getDescription() != null) {
                    MarkdownParameterDocumentationLineNode paramAPIDoc =
                            DocCommentsGenerator.createAPIParamDoc(getValidName(
                                            parameter.getName(), false), parameter.getDescription());
                    remoteFunctionDoc.add(paramAPIDoc);
                }
                List<AnnotationNode> parameterAnnotationNodeList =
                        getParameterAnnotationNodeList(parameter, deprecatedParamDocComments);
                String in = parameter.getIn();
                switch (in) {
                    case "path":
                        Node param = getPathParameters(parameter, createNodeList(parameterAnnotationNodeList));
                        // Path parameters are always required.
                        parameterList.add(param);
                        parameterList.add(comma);

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
            RequestBody requestBody = operation.getRequestBody();
            if (requestBody.getContent() != null) {
                List<Node> requestBodyparam = setRequestBodyParameters(operation.getOperationId(), requestBody,
                        remoteFunctionDoc);
                parameterList.addAll(requestBodyparam);
                parameterList.add(comma);
            } else if (requestBody.get$ref() != null) {
                String requestBodyName = extractReferenceType(requestBody.get$ref());
                RequestBody requestBodySchema = openAPI.getComponents().getRequestBodies().get(requestBodyName.trim());
                List<Node> requestBodyparam = setRequestBodyParameters(operation.getOperationId(), requestBodySchema,
                        remoteFunctionDoc);
                parameterList.addAll(requestBodyparam);
                parameterList.add(comma);
            }
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
                for (Map.Entry<String, Object> extension: parameter.getExtensions().entrySet()) {
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
     *
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
        } else {
            paramType = convertOpenAPITypeToBallerina(parameterSchema.getType().trim());
            if (parameterSchema.getType().equals("number")) {
                if (parameterSchema.getFormat() != null) {
                    paramType = convertOpenAPITypeToBallerina(parameterSchema.getFormat().trim());
                }
            }
            if (parameterSchema instanceof ArraySchema) {
                ArraySchema arraySchema = (ArraySchema) parameterSchema;
                if (arraySchema.getItems().getType() != null) {
                    String itemType = arraySchema.getItems().getType();
                    if (itemType.equals("string") || itemType.equals("integer") || itemType.equals("boolean")
                            || itemType.equals("number")) {
                        paramType = convertOpenAPITypeToBallerina(itemType) + SQUARE_BRACKETS;
                    }
                } else if (arraySchema.getItems().get$ref() != null) {
                    paramType = getValidName(extractReferenceType(
                            arraySchema.getItems().get$ref().trim()), true) + SQUARE_BRACKETS;
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
                if (parameterSchema.getType().equals("string")) {
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
                typeName = createOptionalTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(paramType)), createToken(QUESTION_MARK_TOKEN));
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
            if (schema instanceof ObjectSchema) {
                throw new BallerinaOpenApiException("Ballerina does not support object type path parameters.");
            }
        } else {
            type = convertOpenAPITypeToBallerina(parameter.getSchema().getType().trim());
            if (type.equals("anydata") || type.equals("[]") || type.equals("record {}")) {
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
        String type = convertOpenAPITypeToBallerina(parameter.getSchema().getType().trim());
        if (schema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema) schema;
            if (arraySchema.getItems().get$ref() != null) {
                type = extractReferenceType(arraySchema.getItems().get$ref()) + SQUARE_BRACKETS;
            } else {
                type = convertOpenAPITypeToBallerina(arraySchema.getItems().getType().trim()) + SQUARE_BRACKETS;
            }
        }
        if (parameter.getRequired()) {
            BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(type));
            IdentifierToken paramName = createIdentifierToken(getValidName(parameter.getName().trim(),
                    false));
            return createRequiredParameterNode(parameterAnnotationNodeList, typeName, paramName);
        } else {
            IdentifierToken paramName = createIdentifierToken(getValidName(parameter.getName().trim(),
                    false));
            if (schema.getDefault() != null) {
                BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(convertOpenAPITypeToBallerina(parameter.getSchema().getType().trim())));
                LiteralValueToken literalValueToken;
                if (schema.getType().equals("string")) {
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
                type = convertOpenAPITypeToBallerina(parameter.getSchema().getType().trim()) + NILLABLE;
                if (schema instanceof ArraySchema) {
                    ArraySchema arraySchema = (ArraySchema) schema;
                    if (arraySchema.getItems().get$ref() != null) {
                        type = extractReferenceType(arraySchema.getItems().get$ref()) + "[]?";
                    } else {
                        type = convertOpenAPITypeToBallerina(arraySchema.getItems().getType().trim()) + "[]?";
                    }
                }
                BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(type));
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
    private  List<Node> setRequestBodyParameters(String operationId, RequestBody requestBody,
                                                 List<Node> requestBodyDoc)
            throws BallerinaOpenApiException {
        List<Node> parameterList = new ArrayList<>();
        Content content = requestBody.getContent();
        Iterator<Map.Entry<String, MediaType>> iterator = content.entrySet().iterator();
        while (iterator.hasNext()) {
            // This implementation currently for first content type
            Map.Entry<String, MediaType> next = iterator.next();
            Schema schema = next.getValue().getSchema();
            String paramType = "";
            //Take payload type
            if (next.getKey().equals(ANY_TYPE) || next.getKey().contains(VENDOR_SPECIFIC_TYPE)) {
                paramType = SyntaxKind.BYTE_KEYWORD.stringValue() + "[]";
            } else if (schema.get$ref() != null) {
                paramType = getValidName(extractReferenceType(schema.get$ref().trim()), true);
            } else if (schema.getType() != null && !schema.getType().equals("array") && !schema.getType().equals(
                    "object")) {
                String typeOfPayload = schema.getType().trim();
                if (typeOfPayload.equals(STRING) && schema.getFormat() != null
                        && (schema.getFormat().equals(BINARY) || schema.getFormat().equals(BYTE))) {
                    paramType = convertOpenAPITypeToBallerina(schema.getFormat());
                } else {
                    paramType = convertOpenAPITypeToBallerina(typeOfPayload);
                }
            } else if (schema instanceof ArraySchema) {
                //TODO: handle nested array - this is impossible to handle
                ArraySchema arraySchema = (ArraySchema) schema;
                paramType = getRequestBodyParameterForArraySchema(operationId, next, arraySchema);
            } else { // composed and object schemas are handled by the flatten
                paramType = GeneratorUtils.getBallerinaMediaType(next.getKey());
            }
            if (!paramType.isBlank()) {
                List<AnnotationNode> annotationNodes  = new ArrayList<>();
                DocCommentsGenerator.extractDisplayAnnotation(requestBody.getExtensions(), annotationNodes);
                SimpleNameReferenceNode typeName = createSimpleNameReferenceNode(createIdentifierToken(paramType));
                IdentifierToken paramName = createIdentifierToken("payload");
                RequiredParameterNode payload = createRequiredParameterNode(
                        createNodeList(annotationNodes), typeName, paramName);
                if (requestBody.getDescription() != null) {
                    MarkdownParameterDocumentationLineNode paramAPIDoc =
                            DocCommentsGenerator.createAPIParamDoc(escapeIdentifier("payload"),
                                    requestBody.getDescription().split("\n")[0]);
                    requestBodyDoc.add(paramAPIDoc);
                }
                parameterList.add(payload);
            }
            break;
        }
        return parameterList;
    }

    /**
     * Generate RequestBody for array type schema.
     */
    private String getRequestBodyParameterForArraySchema(String operationId, Map.Entry<String, MediaType> next,
                                                         ArraySchema arraySchema) throws BallerinaOpenApiException {

        String paramType;
        Schema<?> arrayItems = arraySchema.getItems();
        if (arrayItems.getType() != null) {
            paramType = convertOpenAPITypeToBallerina(arrayItems.getType()) + SQUARE_BRACKETS;
        } else if (arrayItems.get$ref() != null) {
            paramType = getValidName(extractReferenceType(arrayItems.get$ref()), true) + SQUARE_BRACKETS;
        } else if (arrayItems instanceof ComposedSchema) {
            paramType = "CompoundArrayItem" +  getValidName(operationId, true) + "Request";
            // TODO - Add API doc by checking requestBody
            TypeDefinitionNode arrayTypeNode =
                    ballerinaSchemaGenerator.getTypeDefinitionNode(arraySchema, paramType, new ArrayList<>());
            functionReturnType.updateTypeDefinitionNodeList(paramType, arrayTypeNode);
        } else {
            paramType = GeneratorUtils.getBallerinaMediaType(next.getKey().trim()) + SQUARE_BRACKETS;
        }
        return paramType;
    }
}
