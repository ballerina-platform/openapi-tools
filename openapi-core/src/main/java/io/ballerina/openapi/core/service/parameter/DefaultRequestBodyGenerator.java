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

package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.ServiceGenerationUtils;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;

/**
 * This class for generating request body payload for OAS requestBody section.
 *
 * @since 1.3.0
 */
public class DefaultRequestBodyGenerator extends RequestBodyGenerator {

    public DefaultRequestBodyGenerator(OASServiceMetadata oasServiceMetadata, String path) {
        super(oasServiceMetadata, path);
    }

    /**
     * This for creating request Body for given request object.
     */
    public RequiredParameterNode createRequestBodyNode(RequestBody requestBody) {
        // type CustomRecord record {| anydata...; |};
        // public type PayloadType string|json|xml|byte[]|CustomRecord|CustomRecord[];
        Optional<TypeDescriptorNode> typeName;
        // Filter same data type
        HashSet<String> types = new HashSet<>();
        ArrayList<TypeDescriptorNode> typeDescNodes = new ArrayList<>();
        for (Map.Entry<String, MediaType> mime : requestBody.getContent().entrySet()) {
            typeName = getNodeForPayloadType(mime);
            if (typeName.isPresent()) {
                types.add(typeName.get().toSourceCode());
                typeDescNodes.add(typeName.get());
            } else {
                types.add(GeneratorConstants.HTTP_REQUEST);
            }
        }
        if (types.size() > 1 && types.contains(GeneratorConstants.HTTP_REQUEST)) {
            typeName = Optional.of(NodeParser.parseTypeDescriptor(GeneratorConstants.HTTP_REQUEST));
        } else if (types.size() > 1) {
            TypeDescriptorNode unionTypeDescriptorNode = null;
            TypeDescriptorNode leftTypeDesc = typeDescNodes.get(0);
            for (int i = 1; i < typeDescNodes.size(); i++) {
                TypeDescriptorNode rightTypeDesc = typeDescNodes.get(i);
                unionTypeDescriptorNode = createUnionTypeDescriptorNode(leftTypeDesc, createToken(PIPE_TOKEN),
                        rightTypeDesc);
                leftTypeDesc = unionTypeDescriptorNode;
            }
            typeName = Optional.of(unionTypeDescriptorNode);
        } else {
            typeName = Optional.of(typeDescNodes.get(0));
        }
        AnnotationNode annotationNode = ServiceGenerationUtils.getAnnotationNode(GeneratorConstants.PAYLOAD_KEYWORD,
                null);
        NodeList<AnnotationNode> annotation = NodeFactory.createNodeList(annotationNode);
        String paramName = typeName.get().toString().equals(GeneratorConstants.HTTP_REQUEST) ?
                GeneratorConstants.REQUEST : GeneratorConstants.PAYLOAD;

        if (typeName.get().toString().equals(GeneratorConstants.HTTP_REQUEST)) {
            return createRequiredParameterNode(createEmptyNodeList(),
                    createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.HTTP_REQUEST)),
                    createIdentifierToken(GeneratorConstants.REQUEST));
        }
        return createRequiredParameterNode(annotation, typeName.get(), createIdentifierToken(paramName,
                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
    }

    private Optional<TypeDescriptorNode> getNodeForPayloadType(Map.Entry<String, MediaType> mediaType) {
        TypeDescriptorNode typeName;
//        String mediaTypeContent = selectMediaType(mediaType.getKey().trim());
//        typeName = switch (mediaTypeContent) {
//            case GeneratorConstants.APPLICATION_XML -> generateTypeDescriptorForXMLContent();
//            case GeneratorConstants.TEXT -> generateTypeDescriptorForTextContent(oasServiceMetadata.getOpenAPI(),
//                    mediaType.getValue().getSchema());
//            case GeneratorConstants.APPLICATION_OCTET_STREAM -> generateTypeDescriptorForOctetStreamContent();
//            case GeneratorConstants.APPLICATION_JSON -> {
//                if (mediaType.getValue() != null && mediaType.getValue().getSchema() != null) {
//                    yield generateTypeDescriptorForJsonContent(mediaType);
//                } else {
//                    yield createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.ANYDATA));
//                }
//            }
//            case GeneratorConstants.APPLICATION_URL_ENCODE -> generateTypeDescriptorForMapStringContent();
//            default -> createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.HTTP_REQUEST));
//        };
        return Optional.of(GeneratorUtils.generateTypeDescForToMediaType(oasServiceMetadata.getOpenAPI(), path,
                true, mediaType));
    }
}
