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

package io.ballerina.openapi.core.generators.service.parameter;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.service.ServiceGenerationUtils;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;

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
        TypeDescriptorNode typeName;
        // Filter same data type
        HashSet<String> types = new HashSet<>();
        HashMap<String, TypeDescriptorNode> typeDescNodes = new LinkedHashMap<>();
        for (Map.Entry<String, MediaType> mime : requestBody.getContent().entrySet()) {
            typeName = GeneratorUtils.generateTypeDescForMediaType(oasServiceMetadata.getOpenAPI(), path,
                    true, mime);
            if (typeName != null) {
                types.add(typeName.toSourceCode());
                typeDescNodes.put(typeName.toSourceCode(), typeName);
            } else {
                types.add(GeneratorConstants.HTTP_REQUEST);
            }
        }
        if (types.contains(GeneratorConstants.HTTP_REQUEST) || types.isEmpty()) {
            typeName = NodeParser.parseTypeDescriptor(GeneratorConstants.HTTP_REQUEST);
        } else {
            typeName = GeneratorUtils.getUnionTypeDescriptorNodeFromTypeDescNodes(typeDescNodes);
        }
        AnnotationNode annotationNode = ServiceGenerationUtils.getAnnotationNode(GeneratorConstants.PAYLOAD_KEYWORD,
                null);
        NodeList<AnnotationNode> annotation = NodeFactory.createNodeList(annotationNode);
        String paramName = typeName.toString().equals(GeneratorConstants.HTTP_REQUEST) ?
                GeneratorConstants.REQUEST : GeneratorConstants.PAYLOAD;

        if (typeName.toString().equals(GeneratorConstants.HTTP_REQUEST)) {
            return createRequiredParameterNode(createEmptyNodeList(),
                    createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.HTTP_REQUEST)),
                    createIdentifierToken(GeneratorConstants.REQUEST));
        }
        return createRequiredParameterNode(annotation, typeName, createIdentifierToken(paramName,
                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
    }
}
