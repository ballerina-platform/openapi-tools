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
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.corenew.typegenerator.GeneratorUtils;
import io.ballerina.openapi.corenew.typegenerator.TypeHandler;
import io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;

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
        // public type PayloadType string|json|xml|byte[]|CustomRecord|CustomRecord[];
        Optional<TypeDescriptorNode> typeName;
        // Filter same data type
        HashSet<String> types = new HashSet<>();
        for (Map.Entry<String, MediaType> mime : requestBody.getContent().entrySet()) {
            typeName = TypeHandler.getNodeForPayloadType(mime);
            if (typeName.isPresent()) {
                types.add(typeName.get().toString());
            } else {
                types.add(GeneratorConstants.HTTP_REQUEST);
            }
        }
        if (types.size() > 1 && types.contains(GeneratorConstants.HTTP_REQUEST)) {
            typeName = Optional.of(NodeParser.parseTypeDescriptor(GeneratorConstants.HTTP_REQUEST));
        } else if (types.size() > 1) {
            String result = String.join(GeneratorConstants.PIPE, types);
            typeName = Optional.of(NodeParser.parseTypeDescriptor(result));
        } else {
            typeName = Optional.of(NodeParser.parseTypeDescriptor(types.iterator().next()));
        }
        AnnotationNode annotationNode = ServiceGenerationUtils.getAnnotationNode(GeneratorConstants.PAYLOAD_KEYWORD, null);
        NodeList<AnnotationNode> annotation = NodeFactory.createNodeList(annotationNode);
        String paramName = typeName.get().toString().equals(GeneratorConstants.HTTP_REQUEST) ? GeneratorConstants.REQUEST : GeneratorConstants.PAYLOAD;

        if (typeName.get().toString().equals(GeneratorConstants.HTTP_REQUEST)) {
            return createRequiredParameterNode(createEmptyNodeList(),
                    createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.HTTP_REQUEST)), createIdentifierToken(GeneratorConstants.REQUEST));
        }
        return createRequiredParameterNode(annotation, typeName.get(), createIdentifierToken(paramName,
                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
    }
}
