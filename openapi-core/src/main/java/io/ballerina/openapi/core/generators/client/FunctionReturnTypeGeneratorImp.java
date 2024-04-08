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

package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DEFAULT_RETURN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.ERROR;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.OPTIONAL_ERROR;

/**
 * This util class for maintain the operation response with ballerina return type.
 *
 * @since 1.9.0
 */
public class FunctionReturnTypeGeneratorImp implements FunctionReturnTypeGenerator {
    private OpenAPI openAPI;
    private Operation operation;
    List<ClientDiagnostic> diagnostics = new ArrayList<>();

    public FunctionReturnTypeGeneratorImp(Operation operation, OpenAPI openAPI) {
        this.openAPI = openAPI;
        this.operation = operation;
    }

    /**
     * Get return type of the remote function.
     *
     * @return string with return type.
     * @throws BallerinaOpenApiException - throws exception if creating return type fails.
     */
    @Override
    public Optional<ReturnTypeDescriptorNode> getReturnType() {
        //Todo handle reference reusable response schema
        List<TypeDescriptorNode> returnTypes = new ArrayList<>();
        HashSet<String> returnTypesSet = new HashSet<>();
        boolean noContentResponseFound = false;
        if (operation.getResponses() != null) {
            ApiResponses responses = operation.getResponses();
            for (Map.Entry<String, ApiResponse> entry : responses.entrySet()) {
                noContentResponseFound = false;
                String statusCode = entry.getKey();
                ApiResponse response = entry.getValue();
                if (statusCode.startsWith("2")) {
                    Content content = response.getContent();
                    if (content != null && content.size() > 0) {
                        Set<Map.Entry<String, MediaType>> mediaTypes = content.entrySet();
                        for (Map.Entry<String, MediaType> media : mediaTypes) {
                            TypeDescriptorNode type;
                            if (media.getValue().getSchema() != null) {
                                Schema<?> schema = media.getValue().getSchema();
                                Optional<TypeDescriptorNode> dataType = getDataType(media, schema);
                                if (dataType.isPresent()) {
                                    type = dataType.get();
                                } else {
                                    String mediaType = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(),
                                            false);
                                    type = createSimpleNameReferenceNode(createIdentifierToken(mediaType));
                                }
                            } else {
                                String mediaType = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(),
                                        false);
                                type = createSimpleNameReferenceNode(createIdentifierToken(mediaType));
                            }
                            if (!returnTypesSet.contains(type.toString())) {
                                returnTypesSet.add(type.toString());
                                returnTypes.add(type);
                            }
                            // Currently support for first media type
                            break;
                        }
                    } else {
                        noContentResponseFound = true;
                    }
                }
                if ((statusCode.startsWith("1") || statusCode.startsWith("2") || statusCode.startsWith("3")) &&
                        response.getContent() == null) {
                    noContentResponseFound = true;
                }
            }
        }
        if (!returnTypes.isEmpty()) {
            if (noContentResponseFound) {
                return Optional.of(createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(),
                        createUnionTypeDescriptorNode(returnTypes.get(0), createToken(PIPE_TOKEN),
                                createSimpleNameReferenceNode(createIdentifierToken(OPTIONAL_ERROR)))));
//                returnTypes.add(createSimpleNameReferenceNode(createIdentifierToken(ERROR + NILLABLE)));
            } else {
                return Optional.of(createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(),
                        createUnionTypeDescriptorNode(returnTypes.get(0), createToken(PIPE_TOKEN),
                                createSimpleNameReferenceNode(createIdentifierToken(ERROR)))));
//                returnTypes.add(createSimpleNameReferenceNode(createIdentifierToken(ERROR)));
            }

        } else if (noContentResponseFound) {
            return Optional.of(createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(),
                    createIdentifierToken(OPTIONAL_ERROR)));
        } else {
            return Optional.of(createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD), createEmptyNodeList(),
                    createIdentifierToken(DEFAULT_RETURN)));
        }
    }

    /**
     * Get return data type by traversing OAS schemas.
     */
    private Optional<TypeDescriptorNode> getDataType(Map.Entry<String, MediaType> media, Schema<?> schema) {
        //add regex to check all JSON types
        if (media.getKey().trim().matches(".*/json") && schema != null) {
            return TypeHandler.getInstance().getTypeNodeFromOASSchema(schema);
        } else {
            String type = GeneratorUtils.getBallerinaMediaType(media.getKey().trim(), false);
            return Optional.of(createSimpleNameReferenceNode(createIdentifierToken(type)));
        }
    }
        @Override
        public List<ClientDiagnostic> getDiagnostics() {
            return diagnostics;
        }
}
