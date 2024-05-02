/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParameterizedTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RETURNS_KEYWORD;

/**
 * This class is used to generate the function signature of the client external method's implementation function.
 *
 * @since 1.9.0
 */
public class ImplFunctionSignatureGenerator {

    List<Node> parameterNodes = new ArrayList<>();
    ReturnTypeDescriptorNode returnTypeDescriptorNode = null;

    ResourceFunctionSignatureGenerator resourceFunctionSignatureGenerator;


    public ImplFunctionSignatureGenerator(Operation operation, OpenAPI openAPI, String httpMethod, String path,
                                          FunctionDefinitionNode clientExternFunction) {
        FunctionSignatureNode functionSignatureNode = clientExternFunction.functionSignature();
        if (Objects.isNull(functionSignatureNode)) {
            return;
        }

        populateParameterNodes(clientExternFunction);
        populateReturnTypeDesc(clientExternFunction);

        // TODO: Find a better way to get the information about parameters
        resourceFunctionSignatureGenerator = new ResourceFunctionSignatureGenerator(operation, openAPI, httpMethod,
                path);
        resourceFunctionSignatureGenerator.generateFunctionSignature();
    }

    private void populateReturnTypeDesc(FunctionDefinitionNode clientExternFunction) {
        FunctionSignatureNode functionSignatureNode = clientExternFunction.functionSignature();
        Optional<ParameterNode> targetType = findTargetType(functionSignatureNode);
        targetType.ifPresent(this::setReturnTypeDescriptor);
    }

    private Optional<ParameterNode> findTargetType(FunctionSignatureNode functionSignatureNode) {
        return functionSignatureNode.parameters().stream().filter(
                parameterNode -> parameterNode instanceof DefaultableParameterNode defaultableParameterNode &&
                        defaultableParameterNode.paramName().isPresent() &&
                        defaultableParameterNode.paramName().get().text().equals("targetType")
        ).findFirst();
    }

    private void setReturnTypeDescriptor(ParameterNode targetType) {
        Node node = ((DefaultableParameterNode) targetType).typeName();
        if (node instanceof ParameterizedTypeDescriptorNode targetTypeNode) {
            Optional<TypeParameterNode> typeParameterNode = targetTypeNode.typeParamNode();
            typeParameterNode.ifPresent(parameterNode ->
                    returnTypeDescriptorNode = createReturnTypeDescriptorNode(createToken(RETURNS_KEYWORD),
                            createEmptyNodeList(), createUnionTypeDescriptorNode(typeParameterNode.get().typeNode(),
                                    createToken(PIPE_TOKEN),
                                    createSimpleNameReferenceNode(createIdentifierToken("error")))));
        }
    }

    private void populateParameterNodes(FunctionDefinitionNode clientExternFunction) {
        addParametersFromPath(clientExternFunction);
        addParametersFromSignature(clientExternFunction);
    }

    private void addParametersFromSignature(FunctionDefinitionNode clientExternFunction) {
        FunctionSignatureNode functionSignatureNode = clientExternFunction.functionSignature();
        for (ParameterNode paramNode : functionSignatureNode.parameters()) {
            parameterNodes.add(removeDefaultValue(paramNode));
            parameterNodes.add(createToken(COMMA_TOKEN));
        }
    }

    private void addParametersFromPath(FunctionDefinitionNode clientExternFunction) {
        NodeList<Node> pathParams = clientExternFunction.relativeResourcePath();
        if (Objects.isNull(pathParams) || pathParams.isEmpty()) {
            return;
        }

        for (Node node : pathParams) {
            if (node instanceof ResourcePathParameterNode pathParameterNode) {
                Node pathParam = createRequiredParameterNode(createEmptyNodeList(), pathParameterNode.typeDescriptor(),
                        pathParameterNode.paramName().orElse(null));
                parameterNodes.add(pathParam);
                parameterNodes.add(createToken(COMMA_TOKEN));
            }
        }
    }

    private Node removeDefaultValue(ParameterNode parameterNode) {
        if (parameterNode instanceof DefaultableParameterNode defaultableParameterNode) {
            return createRequiredParameterNode(defaultableParameterNode.annotations(),
                    defaultableParameterNode.typeName(), defaultableParameterNode.paramName().orElse(null));
        }
        return parameterNode;
    }

    public Optional<FunctionSignatureNode> generateFunctionSignature() {
        if (!parameterNodes.isEmpty()) {
            parameterNodes.remove(parameterNodes.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameterNodeList = createSeparatedNodeList(parameterNodes);
        return Optional.of(createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN), parameterNodeList,
                createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode));
    }

    public boolean hasDefaultHeaders() {
        return resourceFunctionSignatureGenerator.hasDefaultHeaders();
    }

    public boolean hasHeaders() {
        return resourceFunctionSignatureGenerator.hasHeaders();
    }

    public boolean hasQueries() {
        return resourceFunctionSignatureGenerator.hasQueries();
    }
}
