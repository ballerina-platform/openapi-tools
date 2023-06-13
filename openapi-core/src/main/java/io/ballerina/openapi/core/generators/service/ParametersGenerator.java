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

import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.openapi.core.GeneratorConstants;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.document.DocCommentsGenerator;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.RecordTypeGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.openapi.core.GeneratorConstants.DEFAULT_PARAM_COMMENT;
import static io.ballerina.openapi.core.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.core.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.core.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.generators.service.ServiceDiagnosticMessages.OAS_SERVICE_103;
import static io.ballerina.openapi.core.generators.service.ServiceDiagnosticMessages.OAS_SERVICE_104;
import static io.ballerina.openapi.core.generators.service.ServiceDiagnosticMessages.OAS_SERVICE_105;
import static io.ballerina.openapi.core.generators.service.ServiceDiagnosticMessages.OAS_SERVICE_106;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.getAnnotationNode;

/**
 * This class uses for generating all resource function parameters.
 *
 * @since 1.3.0
 */
public class ParametersGenerator {

    private boolean isNullableRequired;
    private final List<Node> requiredParams;
    private final List<Node> defaultableParams;
    private final OpenAPI openAPI;

    private static final List<String> paramSupportedTypes =
            new ArrayList<>(Arrays.asList(GeneratorConstants.INTEGER, GeneratorConstants.NUMBER,
                    GeneratorConstants.STRING, GeneratorConstants.BOOLEAN));

    public ParametersGenerator(boolean isNullableRequired, OpenAPI openAPI) {
        this.isNullableRequired = isNullableRequired;
        this.openAPI = openAPI;
        this.requiredParams = new ArrayList<>();
        this.defaultableParams = new ArrayList<>();
    }

    public List<Node> getRequiredParams() {
        return requiredParams;
    }

    public List<Node> getDefaultableParams() {
        return defaultableParams;
    }

    public boolean isNullableRequired() {
        return isNullableRequired;
    }

    /**
     * This function for generating operation parameters.
     *
     * @param operation OAS operation
     * @throws BallerinaOpenApiException when the parameter generation fails.
     */
    public void generateResourcesInputs(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                        List<Node> resourceFunctionDocs)
            throws BallerinaOpenApiException {

        Token comma = createToken(SyntaxKind.COMMA_TOKEN);
        // Handle header and query parameters
        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
            for (Parameter parameter : parameters) {
                Node param = null;
                if (parameter.getIn().trim().equals(GeneratorConstants.HEADER)) {
                    param = handleHeader(parameter);
                    if (param.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                        defaultableParams.add(param);
                        defaultableParams.add(comma);
                    } else {
                        requiredParams.add(param);
                        requiredParams.add(comma);
                    }
                } else if (parameter.getIn().trim().equals(GeneratorConstants.QUERY)) {
                    if (parameter.getRequired() != null && parameter.getRequired() &&
                            (parameter.getSchema() != null && parameter.getSchema().getNullable() != null &&
                                    parameter.getSchema().getNullable())) {
                        isNullableRequired = true;
                    }
                    // type  BasicType boolean|int|float|decimal|string ;
                    // public type () |BasicType|BasicType []| map<json>;
                    param = createNodeForQueryParam(parameter);
                    if (param != null) {
                        if (param.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                            defaultableParams.add(param);
                            defaultableParams.add(comma);
                        } else {
                            requiredParams.add(param);
                            requiredParams.add(comma);
                        }
                    }
                }

                if (param != null) {
                    String parameterName = param instanceof RequiredParameterNode ?
                            ((RequiredParameterNode) param).paramName().get().text() :
                            ((DefaultableParameterNode) param).paramName().get().text();
                    String paramComment = parameter.getDescription() != null && !parameter.getDescription().isBlank() ?
                            parameter.getDescription() : DEFAULT_PARAM_COMMENT;
                    MarkdownParameterDocumentationLineNode paramAPIDoc =
                            DocCommentsGenerator.createAPIParamDoc(parameterName
                                    , paramComment);
                    resourceFunctionDocs.add(paramAPIDoc);
                }
            }
        }
    }

    /**
     * This function for generating parameter ST node for header.
     * <pre> resource function get pets(@http:Header {name:"x-request-id"} string header) </pre>
     */
    private ParameterNode handleHeader(Parameter parameter) throws BallerinaOpenApiException {

        Schema<?> schema = parameter.getSchema();
        String headerType;
        TypeDescriptorNode headerTypeName;
        IdentifierToken parameterName = createIdentifierToken(GeneratorUtils.escapeIdentifier(parameter.getName()
                        .toLowerCase(Locale.ENGLISH)), AbstractNodeFactory.createEmptyMinutiaeList(),
                GeneratorUtils.SINGLE_WS_MINUTIAE);

        if (schema.getType() == null && schema.get$ref() == null) {
            // Header example:
            // 01.<pre>
            //       in: header
            //       name: X-Request-ID
            //       schema: {}
            //  </pre>
            throw new BallerinaOpenApiException(String.format(OAS_SERVICE_106.getDescription(), parameter.getName()));
        } else if (schema.get$ref() != null) {
            String type = getValidName(extractReferenceType(schema.get$ref()), true);
            Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type.trim());
            if (paramSupportedTypes.contains(refSchema.getType()) || refSchema instanceof ArraySchema) {
                headerType = type;
            } else {
                throw new BallerinaOpenApiException(String.format(OAS_SERVICE_105.getDescription(),
                        parameter.getName(), refSchema.getType()));
            }
        } else if (paramSupportedTypes.contains(schema.getType()) || schema instanceof ArraySchema) {
            headerType = convertOpenAPITypeToBallerina(schema).trim();
        } else {
            throw new BallerinaOpenApiException(String.format(OAS_SERVICE_105.getDescription(),
                    parameter.getName(), schema.getType()));
        }

        if (schema instanceof ArraySchema) {
            // TODO: Support nested arrays
            Schema<?> items = ((ArraySchema) schema).getItems();
            String arrayType;
            if (items.getType() == null && items.get$ref() == null) {
                throw new BallerinaOpenApiException(String.format(OAS_SERVICE_104.getDescription(),
                        parameter.getName()));
            } else if (items.get$ref() != null) {
                String type = getValidName(extractReferenceType(items.get$ref()), true);
                Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type.trim());
                if (paramSupportedTypes.contains(refSchema.getType())) {
                    arrayType = type;
                } else {
                    throw new BallerinaOpenApiException(String.format(OAS_SERVICE_103.getDescription(),
                            parameter.getName(), type));
                }
            } else if (!paramSupportedTypes.contains(items.getType())) {
                throw new BallerinaOpenApiException(String.format(OAS_SERVICE_103.getDescription(),
                        parameter.getName(), items.getType()));
            } else if (items.getEnum() != null && !items.getEnum().isEmpty()) {
                arrayType = OPEN_PAREN_TOKEN.stringValue() + convertOpenAPITypeToBallerina(items) +
                        CLOSE_PAREN_TOKEN.stringValue();
            } else {
                arrayType = GeneratorUtils.convertOpenAPITypeToBallerina(items);
            }
            BuiltinSimpleNameReferenceNode headerArrayItemTypeName = createBuiltinSimpleNameReferenceNode(
                    null, createIdentifierToken(arrayType));
            ArrayDimensionNode dimensionNode =
                    NodeFactory.createArrayDimensionNode(createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                            createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            NodeList<ArrayDimensionNode> nodeList = createNodeList(dimensionNode);
            headerTypeName = createArrayTypeDescriptorNode(headerArrayItemTypeName, nodeList);
        } else {
            headerTypeName = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(
                    headerType, GeneratorUtils.SINGLE_WS_MINUTIAE,
                    GeneratorUtils.SINGLE_WS_MINUTIAE));
        }
        // Create annotation for header
        // TODO: This code block is to be enabled when handle the headers handle additional parameters
        // MappingConstructorExpressionNode annotValue = NodeFactory.createMappingConstructorExpressionNode(
        //        createToken(SyntaxKind.OPEN_BRACE_TOKEN), NodeFactory.createSeparatedNodeList(),
        //        createToken(SyntaxKind.CLOSE_BRACE_TOKEN));

        AnnotationNode headerNode = getAnnotationNode(GeneratorConstants.HEADER_ANNOT, null);
        NodeList<AnnotationNode> headerAnnotations = createNodeList(headerNode);
        // Handle optional values in headers
        if (!parameter.getRequired()) {
            // If optional it behaves like default value with null ex:(string? header)
            // If schema has an enum and that has a null values then the type is already nill. Hence, the check.
            headerTypeName = headerTypeName.toString().trim().endsWith(NILLABLE) ? headerTypeName :
                    createOptionalTypeDescriptorNode(headerTypeName,
                            createToken(SyntaxKind.QUESTION_MARK_TOKEN));
        }
        // Handle default values in headers
        if (schema.getDefault() != null) {
            return getDefaultableHeaderNode(schema, headerTypeName, parameterName, headerAnnotations);
        }
        // Handle header with parameter required true and nullable ture ex: (string? header)
        if (parameter.getRequired() && schema.getNullable() != null && schema.getNullable().equals(true)) {
            isNullableRequired = true;
            headerTypeName = headerTypeName.toString().trim().endsWith(NILLABLE) ? headerTypeName :
                    createOptionalTypeDescriptorNode(headerTypeName,
                            createToken(SyntaxKind.QUESTION_MARK_TOKEN));
        }
        return createRequiredParameterNode(headerAnnotations, headerTypeName, parameterName);
    }

    /**
     * Generate ballerina default headers.
     */
    private DefaultableParameterNode getDefaultableHeaderNode(Schema<?> schema, TypeDescriptorNode headerTypeName,
                                                              IdentifierToken parameterName,
                                                              NodeList<AnnotationNode> headerAnnotations) {

        if (!schema.getType().equals(GeneratorConstants.ARRAY)) {
            return createDefaultableParameterNode(headerAnnotations, headerTypeName, parameterName,
                    createToken(SyntaxKind.EQUAL_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken("\"" +
                            schema.getDefault().toString() + "\"")));
        }
        return createDefaultableParameterNode(headerAnnotations, headerTypeName, parameterName,
                createToken(SyntaxKind.EQUAL_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(schema.getDefault().toString())));
    }

    /**
     * This for generate query parameter nodes.
     * <p>
     * Ballerina support query parameter types :
     * type BasicType boolean|int|float|decimal|string ;
     * public type  QueryParamType <map>json | () |BasicType|BasicType[];
     */
    private Node createNodeForQueryParam(Parameter parameter) throws BallerinaOpenApiException {

        Schema<?> schema = parameter.getSchema();
        NodeList<AnnotationNode> annotations = createEmptyNodeList();
        IdentifierToken parameterName = createIdentifierToken(
                GeneratorUtils.escapeIdentifier(parameter.getName().trim()),
                AbstractNodeFactory.createEmptyMinutiaeList(), GeneratorUtils.SINGLE_WS_MINUTIAE);
        boolean isSchemaNotSupported = schema == null || schema.getType() == null;
        paramSupportedTypes.add(GeneratorConstants.OBJECT);
        if (schema != null && schema.get$ref() != null) {
            String type = getValidName(extractReferenceType(schema.get$ref()), true);
            Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type);
            return handleReferencedQueryParameter(parameter, type, refSchema, annotations, parameterName);
        } else if (parameter.getContent() != null) {
            Content content = parameter.getContent();
            for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
                return handleMapJsonQueryParameter(parameter, annotations, parameterName, mediaTypeEntry);
            }
        } else if (isSchemaNotSupported) {
            ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
            throw new BallerinaOpenApiException(String.format(messages.getDescription(),
                    parameter.getSchema().getType()));
        } else if (parameter.getSchema().getDefault() != null) {
            // When query parameter has default value
            return handleDefaultQueryParameter(schema, annotations, parameterName);
        } else if (parameter.getRequired() && schema.getNullable() == null) {
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

        Schema<?> parameterSchema;
        if (mediaTypeEntry.getValue().getSchema() != null &&
                mediaTypeEntry.getValue().getSchema().get$ref() != null) {
            String type = getValidName(extractReferenceType(mediaTypeEntry.getValue().getSchema().get$ref()), true);
            parameterSchema = (Schema<?>) openAPI.getComponents().getSchemas().get(type.trim());
        } else {
            parameterSchema = mediaTypeEntry.getValue().getSchema();
        }

        if (mediaTypeEntry.getKey().equals(GeneratorConstants.APPLICATION_JSON) &&
                parameterSchema instanceof MapSchema) {
            if (parameter.getRequired()) {
                BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(GeneratorConstants.MAP_JSON));
                return createRequiredParameterNode(annotations, rTypeName, parameterName);
            } else {
                BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(GeneratorConstants.MAP_JSON));
                OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(rTypeName,
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(annotations, optionalNode, parameterName);
            }
        } else {
            String type = GeneratorUtils.getBallerinaMediaType(mediaTypeEntry.getKey(), false);
            ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
            throw new BallerinaOpenApiException(String.format(messages.getDescription(),
                    type));
        }
    }

    /**
     * This function is to handle query schema which is not have required true.
     */
    private Node handleOptionalQueryParameter(Schema<?> schema, NodeList<AnnotationNode> annotations,
                                              IdentifierToken parameterName) throws BallerinaOpenApiException {

        if (schema instanceof ArraySchema) {
            Schema<?> items = schema.getItems();
            if (items.getType() == null && items.get$ref() == null) {
                // Resource function doesn't support to query parameters with array type which doesn't have an
                // item type.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_101;
                throw new BallerinaOpenApiException(messages.getDescription());
            } else if ((!(items instanceof ObjectSchema) && !(items.getType() != null &&
                    items.getType().equals(GeneratorConstants.ARRAY)))
                    || items.get$ref() != null) {
                // create arrayTypeDescriptor
                ArrayTypeDescriptorNode arrayTypeName = getArrayTypeDescriptorNode(items);
                OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(arrayTypeName,
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(annotations, optionalNode, parameterName);
            } else if (items.getType().equals(GeneratorConstants.ARRAY)) {
                // Resource function doesn't support to the nested array type query parameters.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_100;
                throw new BallerinaOpenApiException(messages.getDescription());
            } else {
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
                throw new BallerinaOpenApiException(String.format(messages.getDescription(), "object"));
            }
        } else {
            Token name = getQueryParamTypeToken(schema);
            TypeDescriptorNode queryParamType = createBuiltinSimpleNameReferenceNode(null, name);
            // If schema has an enum with null value, the type is already nil. Hence, the check.
            if (!name.text().trim().endsWith(NILLABLE)) {
                queryParamType = createOptionalTypeDescriptorNode(queryParamType,
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
            return createRequiredParameterNode(annotations, queryParamType, parameterName);
        }
    }

    private Node handleReferencedQueryParameter(Parameter parameter, String refTypeName, Schema<?> refSchema,
                                                NodeList<AnnotationNode> annotations, IdentifierToken parameterName) {
        BuiltinSimpleNameReferenceNode refTypeNameNode = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(refTypeName));
        if (refSchema.getDefault() != null) {
            String defaultValue = refSchema.getType().equals(GeneratorConstants.STRING) ?
                    String.format("\"%s\"", refSchema.getDefault().toString()) : refSchema.getDefault().toString();
            return createDefaultableParameterNode(annotations, refTypeNameNode, parameterName,
                    createToken(SyntaxKind.EQUAL_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(defaultValue)));
        } else if (parameter.getRequired() && (refSchema.getNullable() == null || (!refSchema.getNullable()))) {
            return createRequiredParameterNode(annotations, refTypeNameNode, parameterName);
        } else {
            OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(refTypeNameNode,
                    createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            return createRequiredParameterNode(annotations, optionalNode, parameterName);
        }
    }

    private Node handleRequiredQueryParameter(Schema<?> schema, NodeList<AnnotationNode> annotations,
                                              IdentifierToken parameterName) throws BallerinaOpenApiException {

        if (schema instanceof ArraySchema) {
            Schema<?> items = ((ArraySchema) schema).getItems();
            if (!(items instanceof ArraySchema) && (items.getType() != null || (items.get$ref() != null))) {
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
            Token name = getQueryParamTypeToken(schema);
            BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null, name);
            return createRequiredParameterNode(annotations, rTypeName, parameterName);
        }
    }

    /**
     * This function generate default query parameter when OAS gives default value.
     * <p>
     * OAS code example:
     * <pre>
     *      parameters:
     *         - name: limit
     *           in: query
     *           schema:
     *             type: integer
     *             default: 10
     *             format: int32
     *  </pre>
     * generated ballerina -> int limit = 10;
     */

    private Node handleDefaultQueryParameter(Schema<?> schema, NodeList<AnnotationNode> annotations,
                                             IdentifierToken parameterName) throws BallerinaOpenApiException {

        if (schema instanceof ArraySchema) {
            Schema<?> items = ((ArraySchema) schema).getItems();
            if (!(items instanceof ArraySchema) && (items.getType() != null || (items.get$ref() != null))) {
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
            Token name = getQueryParamTypeToken(schema);
            BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null, name);
            if (schema.getType().equals(GeneratorConstants.STRING)) {
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

    private static Token getQueryParamTypeToken(Schema<?> schema) throws BallerinaOpenApiException {
        Token name;
        if (schema instanceof MapSchema) {
            // handle inline record open
            RecordTypeGenerator recordTypeGenerator = new RecordTypeGenerator(schema, null);
            TypeDescriptorNode recordNode = recordTypeGenerator.generateTypeDescriptorNode();
            name = createIdentifierToken(recordNode.toSourceCode(),
                    GeneratorUtils.SINGLE_WS_MINUTIAE,
                    GeneratorUtils.SINGLE_WS_MINUTIAE);
        } else {
            name = createIdentifierToken(GeneratorUtils.convertOpenAPITypeToBallerina(schema),
                    GeneratorUtils.SINGLE_WS_MINUTIAE,
                    GeneratorUtils.SINGLE_WS_MINUTIAE);
        }
        return name;
    }

    // Create ArrayTypeDescriptorNode using Schema
    private ArrayTypeDescriptorNode getArrayTypeDescriptorNode(Schema<?> items) throws BallerinaOpenApiException {
        String arrayName;
        if (items.get$ref() != null) {
            String referenceType = extractReferenceType(items.get$ref());
            String type = getValidName(referenceType, true);
            Schema<?> refSchema = openAPI.getComponents().getSchemas().get(referenceType);
            if (paramSupportedTypes.contains(refSchema.getType())) {
                arrayName = type;
            } else {
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
                throw new BallerinaOpenApiException(String.format(messages.getDescription(), type));
            }
        } else {
            arrayName = GeneratorUtils.convertOpenAPITypeToBallerina(items);
            if (items.getEnum() != null && !items.getEnum().isEmpty()) {
                arrayName = OPEN_PAREN_TOKEN.stringValue() + arrayName + CLOSE_PAREN_TOKEN.stringValue();
            }
        }
        Token arrayNameToken = createIdentifierToken(arrayName, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        BuiltinSimpleNameReferenceNode memberTypeDesc = createBuiltinSimpleNameReferenceNode(null, arrayNameToken);
        ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        NodeList<ArrayDimensionNode> nodeList = createNodeList(dimensionNode);

        if (items.getNullable() != null && items.getNullable() && items.getEnum() == null) {
            // generate -> int?[]
            OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(memberTypeDesc,
                    createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            return createArrayTypeDescriptorNode(optionalNode, nodeList);
        }
        // generate -> int[]
        return createArrayTypeDescriptorNode(memberTypeDesc, nodeList);
    }
}
