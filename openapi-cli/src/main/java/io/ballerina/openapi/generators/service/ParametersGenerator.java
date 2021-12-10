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
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.generators.GeneratorConstants.APPLICATION_JSON;
import static io.ballerina.openapi.generators.GeneratorConstants.ARRAY;
import static io.ballerina.openapi.generators.GeneratorConstants.HEADER;
import static io.ballerina.openapi.generators.GeneratorConstants.MAP_JSON;
import static io.ballerina.openapi.generators.GeneratorConstants.OBJECT;
import static io.ballerina.openapi.generators.GeneratorConstants.QUERY;
import static io.ballerina.openapi.generators.GeneratorConstants.STRING;
import static io.ballerina.openapi.generators.GeneratorUtils.SINGLE_WS_MINUTIAE;
import static io.ballerina.openapi.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.escapeIdentifier;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.getAnnotationNode;

/**
 * This class uses for generating all resource function parameters.
 *
 * @since 2.0.0
 */
public class ParametersGenerator {

    boolean isNullableRequired = false;

    public boolean isNullableRequired() {
        return isNullableRequired;
    }
    /**
     * This function for generating operation parameters.
     *
     * @param operation OAS operation
     * @return          List with parameterNodes
     * @throws BallerinaOpenApiException when the parameter generation fails.
     */
    public List<Node> generateResourcesInputs(Map.Entry<PathItem.HttpMethod, Operation> operation)
            throws BallerinaOpenApiException {
        List<Node> params = new ArrayList<>();
        List<Node> defaultable = new ArrayList<>();
        Token comma = createToken(SyntaxKind.COMMA_TOKEN);
        // Handle header and query parameters
        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
            for (Parameter parameter: parameters) {
                if (parameter.getIn().trim().equals(HEADER)) {
                    RequiredParameterNode param = handleHeader(parameter);
                    params.add(param);
                    params.add(comma);
                } else if (parameter.getIn().trim().equals(QUERY)) {
                    if (parameter.getRequired() != null && parameter.getRequired() &&
                            (parameter.getSchema().getNullable() != null &&  parameter.getSchema().getNullable())) {
                        isNullableRequired = true;
                    }
                    // type  BasicType boolean|int|float|decimal|string ;
                    // public type () |BasicType|BasicType []| map<json>;
                    Node param = createNodeForQueryParam(parameter);
                    if (param.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                        defaultable.add(param);
                        defaultable.add(comma);
                    } else {
                        params.add(param);
                        params.add(comma);
                    }
                }
            }
        }
        // Handle request Body (Payload)
        // type CustomRecord record {| anydata...; |};
        // public type PayloadType string|json|xml|byte[]|CustomRecord|CustomRecord[] ;
        if (operation.getValue().getRequestBody() != null) {
            RequestBody requestBody = operation.getValue().getRequestBody();
            if (requestBody.getContent() != null) {
                RequestBodyGenerator requestBodyGen = new RequestBodyGenerator();
                params.add(requestBodyGen.createNodeForRequestBody(requestBody));
                params.add(comma);
            }
        }
        if (!defaultable.isEmpty()) {
            params.addAll(defaultable);
        }
        if (params.size() > 1) {
            params.remove(params.size() - 1);
        }
        return params;
    }

    /**
     * This function for generating parameter ST node for header.
     */
    private RequiredParameterNode handleHeader(Parameter parameter)throws BallerinaOpenApiException {
        Schema<?> schema = parameter.getSchema();
        TypeDescriptorNode headerTypeName;
        IdentifierToken parameterName = createIdentifierToken(escapeIdentifier(parameter.getName()
                .toLowerCase(Locale.ENGLISH)), AbstractNodeFactory.createEmptyMinutiaeList(), SINGLE_WS_MINUTIAE);
        if (schema == null) {
             // Header example:
             // 01.<pre>
             //       in: header
             //       name: X-Request-ID
             //       schema: {}
             //  </pre>
            return createRequiredParameterNode(createEmptyNodeList(), createIdentifierToken(STRING, SINGLE_WS_MINUTIAE,
                            SINGLE_WS_MINUTIAE), parameterName);
        } else {
            if (!schema.getType().equals(STRING) && !(schema instanceof ArraySchema)) {
                //TO-DO: Generate diagnostic about to error type
                headerTypeName = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(STRING,
                        SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE));
            } else if (schema instanceof ArraySchema) {
                String arrayType = ((ArraySchema) schema).getItems().getType();
                BuiltinSimpleNameReferenceNode headerArrayItemTypeName = createBuiltinSimpleNameReferenceNode(
                        null, createIdentifierToken(arrayType));
                headerTypeName = createArrayTypeDescriptorNode(headerArrayItemTypeName,
                        createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                        createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            } else {
                headerTypeName = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(
                        convertOpenAPITypeToBallerina(schema.getType().trim()), SINGLE_WS_MINUTIAE,
                        SINGLE_WS_MINUTIAE));
            }
            // Create annotation for header
            MappingConstructorExpressionNode annotValue = NodeFactory.createMappingConstructorExpressionNode(
                    createToken(SyntaxKind.OPEN_BRACE_TOKEN), NodeFactory.createSeparatedNodeList(),
                    createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
            AnnotationNode headerNode = getAnnotationNode("Header", annotValue);
            NodeList<AnnotationNode> headerAnnotations = NodeFactory.createNodeList(headerNode);

            return createRequiredParameterNode(headerAnnotations, headerTypeName, parameterName);
        }
    }

    /**
     * This for generate query parameter nodes.
     *
     * Ballerina support query parameter types :
     * type BasicType boolean|int|float|decimal|string ;
     * public type  QueryParamType <map>json | () |BasicType|BasicType[];
     */
    private Node createNodeForQueryParam(Parameter parameter) throws BallerinaOpenApiException {
        Schema<?> schema = parameter.getSchema();
        NodeList<AnnotationNode> annotations = createEmptyNodeList();
        IdentifierToken parameterName = createIdentifierToken(escapeIdentifier(parameter.getName().trim()),
                AbstractNodeFactory.createEmptyMinutiaeList(), SINGLE_WS_MINUTIAE);
        boolean isSchemaSupported = schema == null || schema.get$ref() != null || schema.getType() == null
                || schema.getType().equals(OBJECT) || schema instanceof ObjectSchema || schema.getProperties() != null;
        if (parameter.getContent() != null) {
            Content content = parameter.getContent();
            for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
                return handleMapJsonQueryParameter(parameter, annotations, parameterName, mediaTypeEntry);
            }
        } else if (isSchemaSupported) {
            ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
            throw new BallerinaOpenApiException(String.format(messages.getDescription(),
                    parameter.getSchema().getType()));
        } else if (parameter.getSchema().getDefault() != null) {
            // When query parameter has default value
            return handleDefaultQueryParameter(schema, annotations, parameterName);
        } else if (parameter.getRequired() != null && parameter.getRequired() && schema.getNullable() == null) {
            // Required typeDescriptor
            return handleRequiredQueryParameter(schema, annotations, parameterName);
        } else {
            // Optional typeDescriptor
            return handleOptionalQueryParameter(schema, annotations, parameterName);
        }
        return null;
    }

    /**
     * Handle query parameter for content type which has application/json.
     * example:
     * <pre>
     *     parameters:
     *         - name: petType
     *           in: query
     *           content:
     *             application/json:
     *               schema:
     *                 type: object
     *                 additionalProperties: true
     * </pre>
     */
    private RequiredParameterNode handleMapJsonQueryParameter(Parameter parameter, NodeList<AnnotationNode> annotations,
                                                           IdentifierToken parameterName,
                                                           Map.Entry<String, MediaType> mediaTypeEntry)
            throws BallerinaOpenApiException {

        if (mediaTypeEntry.getKey().equals(APPLICATION_JSON) &&
                mediaTypeEntry.getValue().getSchema() instanceof MapSchema) {
            if (parameter.getRequired()) {
                BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(MAP_JSON));
                return createRequiredParameterNode(annotations, rTypeName, parameterName);
            } else {
                BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(MAP_JSON));
                OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(rTypeName,
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(annotations, optionalNode, parameterName);
            }
        } else {
            ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
            throw new BallerinaOpenApiException(String.format(messages.getDescription(),
                    "content"));
        }
    }

    /**
     * This function is to handle query schema which is not have required true.
     */
    private Node handleOptionalQueryParameter(Schema<?> schema, NodeList<AnnotationNode> annotations,
                                              IdentifierToken parameterName) throws BallerinaOpenApiException {
        if (schema instanceof ArraySchema) {
            Schema<?> items = ((ArraySchema) schema).getItems();
            if (items.getType() == null) {
                // Resource function doesn't support to query parameters with array type which doesn't have an
                // item type.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_101;
                throw new BallerinaOpenApiException(messages.getDescription());
            } else if (!(items instanceof ObjectSchema) && !(items.getType().equals(ARRAY))) {
                // create arrayTypeDescriptor
                ArrayTypeDescriptorNode arrayTypeName = getArrayTypeDescriptorNode(items);
                OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(arrayTypeName,
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(annotations, optionalNode, parameterName);
            } else if (items.getType().equals(ARRAY)) {
                // Resource function doesn't support to the nested array type query parameters.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_100;
                throw new BallerinaOpenApiException(messages.getDescription());
            } else {
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
                throw new BallerinaOpenApiException(String.format(messages.getDescription(), "object"));
            }
        } else {
            Token name = createIdentifierToken(convertOpenAPITypeToBallerina(
                    schema.getType().toLowerCase(Locale.ENGLISH).trim()), SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE);
            BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null, name);
            OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(rTypeName,
                    createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            return createRequiredParameterNode(annotations, optionalNode, parameterName);
        }
    }

    private Node handleRequiredQueryParameter(Schema<?> schema, NodeList<AnnotationNode> annotations,
                                              IdentifierToken parameterName) throws BallerinaOpenApiException {
        if (schema instanceof ArraySchema) {
            Schema<?> items = ((ArraySchema) schema).getItems();
            if (!(items instanceof ArraySchema) && items.getType() != null) {
                ArrayTypeDescriptorNode arrayTypeName = getArrayTypeDescriptorNode(items);
                return createRequiredParameterNode(annotations, arrayTypeName, parameterName);
            } else if (items.getType() == null) {
                // Resource function doesn't support query parameters for array types that doesn't have an item type.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_101;
                throw new BallerinaOpenApiException(messages.getDescription());
            } else if (items instanceof ObjectSchema) {
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
                throw new BallerinaOpenApiException(String.format(messages.getDescription(), "object"));
            } else {
                // Resource function doesn't support to the nested array type query parameters.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_100;
                throw new BallerinaOpenApiException(messages.getDescription());
            }
        } else {
            Token name = createIdentifierToken(convertOpenAPITypeToBallerina(
                    schema.getType().toLowerCase(Locale.ENGLISH).trim()), SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE);
            BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null, name);
            return createRequiredParameterNode(annotations, rTypeName, parameterName);
        }
    }

    /**
     *  This function generate default query parameter when OAS gives default value.
     *
     *  OAS code example:
     *  <pre>
     *      parameters:
     *         - name: limit
     *           in: query
     *           schema:
     *             type: integer
     *             default: 10
     *             format: int32
     *  </pre>
     *  generated ballerina -> int limit = 10;
     */

    private Node handleDefaultQueryParameter(Schema<?> schema, NodeList<AnnotationNode> annotations,
                                              IdentifierToken parameterName) throws BallerinaOpenApiException {
        if (schema instanceof ArraySchema) {
            Schema<?> items = ((ArraySchema) schema).getItems();
            if (!(items instanceof ArraySchema) && items.getType() != null) {
                ArrayTypeDescriptorNode arrayTypeName = getArrayTypeDescriptorNode(items);
                return createDefaultableParameterNode(annotations, arrayTypeName, parameterName,
                        createToken(SyntaxKind.EQUAL_TOKEN),
                        createSimpleNameReferenceNode(createIdentifierToken(schema.getDefault().toString())));
            } else if (items.getType() == null) {
                // Resource function doesn't support to query parameters with array type which hasn't item type.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_101;
                throw new BallerinaOpenApiException(messages.getDescription());
            } else {
                // Resource function doesn't support to the nested array type query parameters.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_100;
                throw new BallerinaOpenApiException(messages.getDescription());
            }
        } else {
            Token name = createIdentifierToken(convertOpenAPITypeToBallerina(
                    schema.getType().toLowerCase(Locale.ENGLISH).trim()), SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE);
            BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null, name);
            if (schema.getType().equals(STRING)) {
                return createDefaultableParameterNode(annotations, rTypeName, parameterName,
                        createToken(SyntaxKind.EQUAL_TOKEN),
                        createSimpleNameReferenceNode(createIdentifierToken('"' +
                                schema.getDefault().toString() + '"')));
            }
            return createDefaultableParameterNode(annotations, rTypeName, parameterName,
                    createToken(SyntaxKind.EQUAL_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(schema.getDefault().toString())));
        }
    }


    // Create ArrayTypeDescriptorNode using Schema
    private  ArrayTypeDescriptorNode getArrayTypeDescriptorNode(Schema<?> items) throws BallerinaOpenApiException {

        Token arrayName = createIdentifierToken(convertOpenAPITypeToBallerina(items.getType().toLowerCase(
                        Locale.ENGLISH).trim()), SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE);
        BuiltinSimpleNameReferenceNode memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, arrayName);
        if (items.getNullable() != null && items.getNullable()) {
            // generate -> int?[]
            OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(memberTypeDesc,
                    createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            return createArrayTypeDescriptorNode(optionalNode, createToken(SyntaxKind.OPEN_BRACKET_TOKEN),
                    null, createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        }
        // generate -> int[]
        return createArrayTypeDescriptorNode(memberTypeDesc, createToken(SyntaxKind.OPEN_BRACKET_TOKEN),
                null, createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
    }
}
