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
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.GeneratorConstants;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.core.GeneratorConstants.HTTP_REQUEST;
import static io.ballerina.openapi.core.GeneratorConstants.PAYLOAD;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.getAnnotationNode;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.getMediaTypeToken;

/**
 * This class for generating request body payload for OAS requestBody section.
 *
 * @since 1.3.0
 */
public class RequestBodyGenerator {
    private final Components components;
    private final RequestBody requestBody;

    public RequestBodyGenerator(Components components, RequestBody requestBody) {
        this.components = components;
        this.requestBody = requestBody;
    }

    /**
     * This for creating request Body for given request object.
     */
    public RequiredParameterNode createNodeForRequestBody() throws BallerinaOpenApiException {
        // type CustomRecord record {| anydata...; |};
        // public type PayloadType string|json|xml|byte[]|CustomRecord|CustomRecord[] ;
        List<Node> literals = new ArrayList<>();
        MappingConstructorExpressionNode annotValue = null;
        Optional<TypeDescriptorNode> typeName;
        // Filter same data type
        HashSet<Map.Entry<String, MediaType>> equalDataType = filterMediaTypes(requestBody);
        if (!equalDataType.isEmpty()) {
            typeName = getNodeForPayloadType(components, equalDataType.iterator().next());
            SeparatedNodeList<MappingFieldNode> fields = fillRequestAnnotationValues(literals, equalDataType);
            annotValue = NodeFactory.createMappingConstructorExpressionNode(
                    createToken(SyntaxKind.OPEN_BRACE_TOKEN), fields, createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
        } else {
            Iterator<Map.Entry<String, MediaType>> content = requestBody.getContent().entrySet().iterator();
            Map.Entry<String, MediaType> mime = content.next();
            equalDataType.add(mime);
            typeName = getNodeForPayloadType(components, mime);
            if (mime.getKey().equals(GeneratorConstants.APPLICATION_URL_ENCODE)) {
                SeparatedNodeList<MappingFieldNode> fields = fillRequestAnnotationValues(literals, equalDataType);
                annotValue = NodeFactory.createMappingConstructorExpressionNode(
                        createToken(SyntaxKind.OPEN_BRACE_TOKEN), fields, createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
            }
        }
        AnnotationNode annotationNode = getAnnotationNode(GeneratorConstants.PAYLOAD_KEYWORD, annotValue);
        NodeList<AnnotationNode> annotation = NodeFactory.createNodeList(annotationNode);
        Token paramName = createIdentifierToken(PAYLOAD, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);

        if (typeName.isEmpty()) {
            return createRequiredParameterNode(createEmptyNodeList(),
                    createSimpleNameReferenceNode(createIdentifierToken(HTTP_REQUEST)), createIdentifierToken(PAYLOAD));
        }
        return createRequiredParameterNode(annotation, typeName.get(), paramName);
    }

    /**
     * This util function is for generating type node for request payload in resource function.
     */
    private Optional<TypeDescriptorNode> getNodeForPayloadType(Components components,
                                                              Map.Entry<String, MediaType> mediaType)
            throws BallerinaOpenApiException {

        Optional<TypeDescriptorNode> typeName;
        if (mediaType.getValue().getSchema().get$ref() != null) {
            String schemaName = extractReferenceType(mediaType.getValue().getSchema().get$ref());
            Map<String, Schema> schemas = components.getSchemas();
            String mediaTypeContent = mediaType.getKey().trim();
            if (mediaTypeContent.matches(GeneratorConstants.TEXT_WILDCARD_REGEX)) {
                mediaTypeContent = GeneratorConstants.TEXT;
            }
            IdentifierToken identifierToken;
            switch (mediaTypeContent) {
                case GeneratorConstants.APPLICATION_XML:
                    identifierToken = createIdentifierToken(GeneratorConstants.XML);
                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
                    break;
                case GeneratorConstants.TEXT:
                    identifierToken = createIdentifierToken(GeneratorConstants.STRING);
                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
                    break;
                case GeneratorConstants.APPLICATION_OCTET_STREAM:
                    ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                            createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                            createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
                    typeName = Optional.ofNullable(createArrayTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(
                                    null, createIdentifierToken(GeneratorConstants.BYTE)),
                            NodeFactory.createNodeList(dimensionNode)));
                    break;
                case GeneratorConstants.APPLICATION_JSON:
                    Schema<?> schema = schemas.get(GeneratorUtils.getValidName(schemaName, true));
                    // This condition is to avoid wrong code generation for union type request body. If given schema
                    // has oneOf type , then it will not be able to support via ballerina. We need to pick it mime
                    // type instead of schema type.
                    if ((schema instanceof ComposedSchema) && (((ComposedSchema) schema).getOneOf() != null)) {
                        identifierToken = createIdentifierToken(GeneratorConstants.JSON);
                        typeName = Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
                    } else {
                        typeName = Optional.ofNullable(createSimpleNameReferenceNode(createIdentifierToken(
                                GeneratorUtils.getValidName(schemaName, true))));
                    }
                    break;
                case GeneratorConstants.APPLICATION_URL_ENCODE:
                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(createIdentifierToken(
                            GeneratorUtils.getValidName(schemaName, true))));
                    break;
                default:
                    identifierToken = createIdentifierToken(GeneratorConstants.JSON);
                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
            }
        } else {
            ImmutablePair<Optional<TypeDescriptorNode>, TypeDefinitionNode> mediaTypeTokens =
                    getMediaTypeToken(mediaType, null);
            typeName = mediaTypeTokens.left;
        }
        return typeName;
    }

    /**
     * This function fill the annotation values.
     * <p>
     * 01. when field has some override media type
     * <pre> @http:Payload{mediaType: "application/x-www-form-urlencoded"} User payload</pre>
     * 02. when field has list media value
     * <pre> @http:Payload{mediaType: ["application/json", "application/snowflake+json"]} User payload</pre>
     */
    private SeparatedNodeList<MappingFieldNode> fillRequestAnnotationValues(List<Node> literals,
                                                                            HashSet<Map.Entry<String,
                                                                                    MediaType>> equalDataTypes) {

        Token comma = createToken(SyntaxKind.COMMA_TOKEN);
        IdentifierToken mediaType = createIdentifierToken(GeneratorConstants.MEDIA_TYPE_KEYWORD);

        Iterator<Map.Entry<String, MediaType>> iter = equalDataTypes.iterator();
        SpecificFieldNode specificFieldNode;
        while (iter.hasNext()) {
            Map.Entry<String, MediaType> next = iter.next();
            literals.add(createIdentifierToken('"' + next.getKey().trim() + '"', GeneratorUtils.SINGLE_WS_MINUTIAE,
                    GeneratorUtils.SINGLE_WS_MINUTIAE));
            literals.add(comma);
        }
        literals.remove(literals.size() - 1);
        SeparatedNodeList<Node> expression = NodeFactory.createSeparatedNodeList(literals);
        if (equalDataTypes.size() == 1) {
            BasicLiteralNode valueExpr = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                    createIdentifierToken('"' + equalDataTypes.iterator().next().getKey() + '"'));
            specificFieldNode = NodeFactory.createSpecificFieldNode(null, mediaType,
                    createToken(SyntaxKind.COLON_TOKEN), valueExpr);
        } else {
            ListConstructorExpressionNode valueExpr = NodeFactory.createListConstructorExpressionNode(
                    createToken(SyntaxKind.OPEN_BRACKET_TOKEN), expression,
                    createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            specificFieldNode = NodeFactory.createSpecificFieldNode(null, mediaType,
                    createToken(SyntaxKind.COLON_TOKEN), valueExpr);
        }
        return NodeFactory.createSeparatedNodeList(specificFieldNode);
    }

    //Extract same datatype
    private HashSet<Map.Entry<String, MediaType>> filterMediaTypes(RequestBody requestBody)
            throws BallerinaOpenApiException {

        HashSet<Map.Entry<String, MediaType>> equalDataType = new HashSet<>();
        Set<Map.Entry<String, MediaType>> entries = requestBody.getContent().entrySet();
        Iterator<Map.Entry<String, MediaType>> iterator = entries.iterator();
        List<Map.Entry<String, MediaType>> updatedEntries = new ArrayList<>(entries);
        while (iterator.hasNext()) {
            // Remove element from updateEntries
            Map.Entry<String, MediaType> mediaTypeEntry = iterator.next();
            updatedEntries.remove(mediaTypeEntry);
            if (!updatedEntries.isEmpty()) {
                getSameDataTypeMedia(equalDataType, updatedEntries, mediaTypeEntry);
                if (!equalDataType.isEmpty()) {
                    equalDataType.add(mediaTypeEntry);
                    break;
                }
            }
        }
        return equalDataType;
    }

    private void getSameDataTypeMedia(HashSet<Map.Entry<String, MediaType>> equalDataType,
                                      List<Map.Entry<String, MediaType>> updatedEntries,
                                      Map.Entry<String, MediaType> mediaTypeEntry) throws BallerinaOpenApiException {

        for (Map.Entry<String, MediaType> updateNext : updatedEntries) {
            MediaType parentValue = mediaTypeEntry.getValue();
            MediaType childValue = updateNext.getValue();
            if (parentValue.getSchema().get$ref() != null && childValue.getSchema().get$ref() != null) {
                String parentRef = parentValue.getSchema().get$ref().trim();
                String childRef = childValue.getSchema().get$ref().trim();
                if (extractReferenceType(parentRef).equals(extractReferenceType(childRef))) {
                    equalDataType.add(updateNext);
                }
            }
        }
    }
}
