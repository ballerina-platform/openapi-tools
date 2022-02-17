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
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
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
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.generators.GeneratorConstants.MEDIA_TYPE_KEYWORD;
import static io.ballerina.openapi.generators.GeneratorConstants.PAYLOAD;
import static io.ballerina.openapi.generators.GeneratorConstants.PAYLOAD_KEYWORD;
import static io.ballerina.openapi.generators.GeneratorUtils.SINGLE_WS_MINUTIAE;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.extractReferenceType;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.getAnnotationNode;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.getMediaTypeToken;

/**
 * This class for generating request body payload for OAS requestBody section.
 *
 * @since 2.0.0
 */
public class RequestBodyGenerator {
    /**
     * This for creating request Body for given request object.
     */
    public RequiredParameterNode createNodeForRequestBody(Components components, RequestBody requestBody)
            throws BallerinaOpenApiException {
        Token comma = createToken(SyntaxKind.COMMA_TOKEN);
        List<Node> literals = new ArrayList<>();
        MappingConstructorExpressionNode annotValue;
        TypeDescriptorNode typeName;
        if (requestBody.getContent().entrySet().size() > 1) {
            IdentifierToken mediaType = createIdentifierToken(MEDIA_TYPE_KEYWORD);
            // Filter same data type
            HashSet<Map.Entry<String, MediaType>> equalDataType = filterMediaTypes(requestBody);
            if (!equalDataType.isEmpty()) {
                typeName = getNodeForPayloadType(components, equalDataType.iterator().next());
                SeparatedNodeList<MappingFieldNode> fields = fillRequestAnnotationValues(comma, literals, mediaType,
                        equalDataType);
                annotValue = NodeFactory.createMappingConstructorExpressionNode(
                        createToken(SyntaxKind.OPEN_BRACE_TOKEN), fields, createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
            } else {
                typeName = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(
                        GeneratorConstants.JSON, SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE));
                annotValue = null;
            }
        } else {
            Iterator<Map.Entry<String, MediaType>> content = requestBody.getContent().entrySet().iterator();
            Map.Entry<String, MediaType> mediaType = content.next();
            typeName = getNodeForPayloadType(components, mediaType);
            annotValue = null;
        }
        AnnotationNode annotationNode = getAnnotationNode(PAYLOAD_KEYWORD, annotValue);
        NodeList<AnnotationNode> annotation =  NodeFactory.createNodeList(annotationNode);
        Token paramName = createIdentifierToken(PAYLOAD, SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE);
        return createRequiredParameterNode(annotation, typeName, paramName);
    }

    private TypeDescriptorNode getNodeForPayloadType(Components components, Map.Entry<String, MediaType> mediaType)
            throws BallerinaOpenApiException {
        TypeDescriptorNode typeName;
        if (mediaType.getValue().getSchema().get$ref() != null) {
            String schemaName = extractReferenceType(mediaType.getValue().getSchema().get$ref());
            Schema<?> schema = components.getSchemas().get(schemaName);
            if (schema instanceof ComposedSchema && (((ComposedSchema) schema).getOneOf() != null)) {
                String mediaTypeContent = mediaType.getKey().trim();
                if (mediaTypeContent.matches("text/.*")) {
                    mediaTypeContent = "text";
                }
                IdentifierToken identifierToken;
                switch (mediaTypeContent) {
                    case "application/xml":
                        identifierToken = createIdentifierToken(GeneratorConstants.XML);
                        typeName = createSimpleNameReferenceNode(identifierToken);
                        break;
                    case "text":
                        identifierToken = createIdentifierToken(GeneratorConstants.STRING);
                        typeName = createSimpleNameReferenceNode(identifierToken);
                        break;
                    case "application/octet-stream":
                        ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
                        typeName = createArrayTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(
                                        null, createIdentifierToken(GeneratorConstants.BYTE)),
                                NodeFactory.createNodeList(dimensionNode));
                        break;
                    case "application/json":
                    default:
                        identifierToken = createIdentifierToken(GeneratorConstants.JSON);
                        typeName = createSimpleNameReferenceNode(identifierToken);
                }
            } else {
                typeName = getMediaTypeToken(mediaType);
            }
        } else {
            typeName = getMediaTypeToken(mediaType);
        }
        return typeName;
    }

    private SeparatedNodeList<MappingFieldNode> fillRequestAnnotationValues(Token comma, List<Node> literals,
                                                                            IdentifierToken mediaType,
                                                                            HashSet<Map.Entry<String,
                                                                                    MediaType>> equalDataType) {

        Iterator<Map.Entry<String, MediaType>> iter = equalDataType.iterator();
        while (iter.hasNext()) {
            Map.Entry<String, MediaType> next = iter.next();
            literals.add(createIdentifierToken('"' + next.getKey().trim() + '"', SINGLE_WS_MINUTIAE,
                    SINGLE_WS_MINUTIAE));
            literals.add(comma);
        }
        literals.remove(literals.size() - 1);
        SeparatedNodeList<Node> expression = NodeFactory.createSeparatedNodeList(literals);
        ListConstructorExpressionNode valueExpr = NodeFactory.createListConstructorExpressionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), expression, createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        SpecificFieldNode specificFieldNode = NodeFactory.createSpecificFieldNode(null, mediaType,
                createToken(SyntaxKind.COLON_TOKEN), valueExpr);
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
