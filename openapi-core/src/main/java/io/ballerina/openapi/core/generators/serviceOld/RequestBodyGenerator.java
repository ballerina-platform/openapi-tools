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

package io.ballerina.openapi.core.generators.serviceOld;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;

/**
 * This class for generating request body payload for OAS requestBody section.
 *
 * @since 1.3.0
 */
public class RequestBodyGenerator {
//    private final RequestBody requestBody;
//
//    public RequestBodyGenerator(RequestBody requestBody) {
//        this.requestBody = requestBody;
//    }
//
//    /**
//     * This for creating request Body for given request object.
//     */
//    public RequiredParameterNode createNodeForRequestBody() throws BallerinaOpenApiException {
//        // type CustomRecord record {| anydata...; |};
//        // public type PayloadType string|json|xml|byte[]|CustomRecord|CustomRecord[] ;
//        Optional<TypeDescriptorNode> typeName;
//        // Filter same data type
//        HashSet<String> types = new HashSet<>();
//        for (Map.Entry<String, MediaType> mime : requestBody.getContent().entrySet()) {
//            typeName = getNodeForPayloadType(mime);
//            if (typeName.isPresent()) {
//                types.add(typeName.get().toString());
//            } else {
//                types.add(HTTP_REQUEST);
//            }
//        }
//        if (types.size() > 1 && types.contains(HTTP_REQUEST)) {
//            typeName = Optional.of(NodeParser.parseTypeDescriptor(HTTP_REQUEST));
//        } else if (types.size() > 1) {
//            String result = String.join(PIPE, types);
//            typeName = Optional.of(NodeParser.parseTypeDescriptor(result));
//        } else {
//            typeName = Optional.of(NodeParser.parseTypeDescriptor(types.iterator().next()));
//        }
//        AnnotationNode annotationNode = getAnnotationNode(GeneratorConstants.PAYLOAD_KEYWORD, null);
//        NodeList<AnnotationNode> annotation = NodeFactory.createNodeList(annotationNode);
//        String paramName = typeName.get().toString().equals(HTTP_REQUEST) ? REQUEST : PAYLOAD;
//
//        if (typeName.get().toString().equals(HTTP_REQUEST)) {
//            return createRequiredParameterNode(createEmptyNodeList(),
//                    createSimpleNameReferenceNode(createIdentifierToken(HTTP_REQUEST)), createIdentifierToken(REQUEST));
//        }
//        return createRequiredParameterNode(annotation, typeName.get(), createIdentifierToken(paramName,
//                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
//    }

//    /**
}
