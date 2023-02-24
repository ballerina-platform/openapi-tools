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
import io.ballerina.compiler.syntax.tree.NodeParser;
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
import io.swagger.v3.oas.models.media.MediaType;
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
import static io.ballerina.openapi.core.GeneratorConstants.MAP_STRING;
import static io.ballerina.openapi.core.GeneratorConstants.PAYLOAD;
import static io.ballerina.openapi.core.GeneratorConstants.PIPE;
import static io.ballerina.openapi.core.GeneratorConstants.REQUEST;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.getAnnotationNode;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.handleMediaType;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.selectMediaType;

/**
 * This class for generating request body payload for OAS requestBody section.
 *
 * @since 1.3.0
 */
public class RequestBodyGenerator {
    private final RequestBody requestBody;

    public RequestBodyGenerator(RequestBody requestBody) {
        this.requestBody = requestBody;
    }

    /**
     * This for creating request Body for given request object.
     */
    public RequiredParameterNode createNodeForRequestBody() throws BallerinaOpenApiException {
        // type CustomRecord record {| anydata...; |};
        // public type PayloadType string|json|xml|byte[]|CustomRecord|CustomRecord[] ;
        Optional<TypeDescriptorNode> typeName;
        // Filter same data type
        List<String> types = new ArrayList<>();
        for (Map.Entry<String, MediaType> mime : requestBody.getContent().entrySet()) {
            typeName = getNodeForPayloadType(mime);
            if (typeName.isPresent()) {
                types.add(typeName.get().toString());
            } else {
                types.add(HTTP_REQUEST);
            }
        }
        if (types.size() > 1 && !types.contains(HTTP_REQUEST)) {
            String result = String.join(PIPE, types);
            typeName = Optional.of(NodeParser.parseTypeDescriptor(result));
        } else if (types.size() > 1 && types.contains(HTTP_REQUEST)) {
            typeName = Optional.of(NodeParser.parseTypeDescriptor(HTTP_REQUEST));
        } else {
            typeName = Optional.of(NodeParser.parseTypeDescriptor(types.get(0)));
        }
        AnnotationNode annotationNode = getAnnotationNode(GeneratorConstants.PAYLOAD_KEYWORD, null);
        NodeList<AnnotationNode> annotation = NodeFactory.createNodeList(annotationNode);
        String paramName = typeName.get().toString().equals(HTTP_REQUEST) ? REQUEST : PAYLOAD;

        if (typeName.get().toString().equals(HTTP_REQUEST)) {
            return createRequiredParameterNode(createEmptyNodeList(),
                    createSimpleNameReferenceNode(createIdentifierToken(HTTP_REQUEST)), createIdentifierToken(REQUEST));
        }
        return createRequiredParameterNode(annotation, typeName.get(), createIdentifierToken(paramName,
                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
    }

    /**
     * This util function is for generating type node for request payload in resource function.
     */
    private Optional<TypeDescriptorNode> getNodeForPayloadType(Map.Entry<String, MediaType> mediaType)
            throws BallerinaOpenApiException {

        Optional<TypeDescriptorNode> typeName;
        if (mediaType.getValue() != null && mediaType.getValue().getSchema() != null &&
                mediaType.getValue().getSchema().get$ref() != null) {
            String reference = mediaType.getValue().getSchema().get$ref();
            String schemaName = GeneratorUtils.getValidName(extractReferenceType(reference), true);
            String mediaTypeContent = selectMediaType(mediaType.getKey().trim());
            IdentifierToken identifierToken;
            switch (mediaTypeContent) {
                case GeneratorConstants.APPLICATION_XML:
                    identifierToken = createIdentifierToken(GeneratorConstants.XML);
                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
                    break;
                case GeneratorConstants.TEXT:
                    identifierToken = createIdentifierToken(schemaName);
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
                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(createIdentifierToken(schemaName)));
                    break;
                case GeneratorConstants.APPLICATION_URL_ENCODE:
                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(createIdentifierToken(MAP_STRING)));
                    //Commented due to the data binding issue in the ballerina http module
                    //TODO: Related issue:https://github.com/ballerina-platform/ballerina-standard-library/issues/4090
//                    typeName = Optional.ofNullable(createSimpleNameReferenceNode(createIdentifierToken(
//                            GeneratorUtils.getValidName(schemaName, true))));
                    break;
                default:
                    ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> mediaTypeTokens =
                            handleMediaType(mediaType, null);
                    if (mediaTypeTokens.getLeft().isPresent()) {
                        typeName = mediaTypeTokens.getLeft();
                    } else {
                        identifierToken = createIdentifierToken(HTTP_REQUEST);
                        typeName = Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
                    }
            }
        } else {
            ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> mediaTypeTokens =
                    handleMediaType(mediaType, null);
            typeName = mediaTypeTokens.left;
        }
        return typeName;
    }

}
