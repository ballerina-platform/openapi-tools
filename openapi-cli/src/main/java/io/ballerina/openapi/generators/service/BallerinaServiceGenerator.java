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

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.generators.GeneratorConstants.FUNCTION;
import static io.ballerina.openapi.generators.GeneratorConstants.RESOURCE;
import static io.ballerina.openapi.generators.GeneratorUtils.getRelativeResourcePath;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.escapeIdentifier;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.getMinutiaes;

/**
 * This Util class use for generating ballerina service file according to given yaml file.
 */
public class BallerinaServiceGenerator {
    // Add basicLiteralNode
    private  GeneratorUtils generatorUtils = new GeneratorUtils();

    public SyntaxTree generateSyntaxTree(OpenAPI openApi, Filter filter) throws BallerinaOpenApiException {
        // Create imports http and openapi
        NodeList<ImportDeclarationNode> imports = createImportDeclarationNodes();
        // Need to Generate Base path
        ListenerGenerator listener = new ListenerGenerator();
        ListenerDeclarationNode listenerDeclarationNode = listener.getListenerDeclarationNodes(openApi.getServers());
        NodeList<Node> absoluteResourcePath = createBasePathNodeList(listener);

        SimpleNameReferenceNode listenerName = createSimpleNameReferenceNode(listenerDeclarationNode.variableName());
        SeparatedNodeList<ExpressionNode> expressions = NodeFactory.createSeparatedNodeList(listenerName);

        // Fill the members with function
        List<Node> functions = createResourceFunctions(openApi, filter);

        NodeList<Node> members = NodeFactory.createNodeList(functions);

        ServiceDeclarationNode serviceDeclarationNode = NodeFactory.createServiceDeclarationNode(
                null, createEmptyNodeList(), createIdentifierToken("service", getMinutiaes(), getMinutiaes()),
                null, absoluteResourcePath, createIdentifierToken("on",
                        getMinutiaes(), getMinutiaes()), expressions,
                createToken(SyntaxKind.OPEN_BRACE_TOKEN), members, createToken(SyntaxKind.CLOSE_BRACE_TOKEN));

        // Create module member declaration
        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(
                listenerDeclarationNode, serviceDeclarationNode);

        Token eofToken = createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private NodeList<ImportDeclarationNode> createImportDeclarationNodes() {
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA
                , GeneratorConstants.HTTP);
        return AbstractNodeFactory.createNodeList(importForHttp);
    }

    private List<Node> createResourceFunctions(OpenAPI openApi, Filter filter) throws BallerinaOpenApiException {
        List<Node> functions =  new ArrayList<>();
        if (!openApi.getPaths().isEmpty()) {
            Paths paths = openApi.getPaths();
            Set<Map.Entry<String, PathItem>> pathsItems = paths.entrySet();
            for (Map.Entry<String, PathItem> path : pathsItems) {
                if (!path.getValue().readOperationsMap().isEmpty()) {
                    Map<PathItem.HttpMethod, Operation> operationMap = path.getValue().readOperationsMap();
                    functions.addAll(applyFiltersForOperations(filter, path, operationMap));
                }
            }
        }
        return functions;
    }

    private NodeList<Node> createBasePathNodeList(ListenerGenerator listener) {
        if ("/".equals(listener.getBasePath())) {
            return AbstractNodeFactory.createNodeList(createIdentifierToken(listener.getBasePath()));
        } else {
            String[] basePathNode = listener.getBasePath().split("/");
            List<Node> basePath = Arrays.stream(basePathNode).filter(node -> !node.isBlank())
                    .map(node -> createIdentifierToken("/" + escapeIdentifier(node))).collect(Collectors.toList());
            return AbstractNodeFactory.createNodeList(basePath);
        }
    }

    private List<Node> applyFiltersForOperations(Filter filter, Map.Entry<String, PathItem> path,
                                                 Map<PathItem.HttpMethod, Operation> operationMap)
            throws BallerinaOpenApiException {
        List<Node> functions = new ArrayList<>();
        for (Map.Entry<PathItem.HttpMethod, Operation> operation : operationMap.entrySet()) {
            //Add filter availability
            //1.Tag filter
            //2.Operation filter
            //3. Both tag and operation filter
            List<String> filterTags = filter.getTags();
            List<String> operationTags = operation.getValue().getTags();
            List<String> filterOperations  = filter.getOperations();
            if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
                if (operationTags != null || ((!filterOperations.isEmpty())
                        && (operation.getValue().getOperationId() != null))) {
                    if (generatorUtils.hasTags(operationTags, filterTags) ||
                            ((operation.getValue().getOperationId() != null) &&
                            filterOperations.contains(operation.getValue().getOperationId().trim()))) {
                        // getRelative resource path
                        List<Node> functionRelativeResourcePath = getRelativeResourcePath(path, operation);
                        // function call
                        FunctionDefinitionNode functionDefinitionNode = getResourceFunction(operation,
                                functionRelativeResourcePath);
                        functions.add(functionDefinitionNode);
                    }
                }
            } else {
                // getRelative resource path
                List<Node> relativeResourcePath = getRelativeResourcePath(path, operation);
                // function call
                FunctionDefinitionNode resourceFunction = getResourceFunction(operation, relativeResourcePath);
                functions.add(resourceFunction);
            }
        }
        return functions;
    }

    /**
     * Generate resource function for given operation.
     *
     * @param operation     -  OAS operation
     * @param pathNodes     -  Relative path nodes
     * @return              - {@link FunctionDefinitionNode} relevant resource
     * @throws BallerinaOpenApiException when the process failure occur
     */
    private FunctionDefinitionNode getResourceFunction(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                       List<Node> pathNodes) throws BallerinaOpenApiException {
        NodeList<Token> qualifiersList = NodeFactory.createNodeList(createIdentifierToken(RESOURCE, getMinutiaes(),
                getMinutiaes()));
        Token functionKeyWord = createIdentifierToken(FUNCTION, getMinutiaes(), getMinutiaes());
        IdentifierToken functionName = createIdentifierToken(operation.getKey().name()
                .toLowerCase(Locale.ENGLISH), getMinutiaes(), getMinutiaes());
        NodeList<Node> relativeResourcePath = NodeFactory.createNodeList(pathNodes);
        ParametersGenerator parametersGenerator = new ParametersGenerator();
        List<Node> params = parametersGenerator.generateResourcesInputs(operation);

        SeparatedNodeList<ParameterNode> parameters = AbstractNodeFactory.createSeparatedNodeList(params);
        ReturnTypeGenerator returnTypeGenerator = new ReturnTypeGenerator();
        ReturnTypeDescriptorNode returnNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation,
                createEmptyNodeList());

        FunctionSignatureNode functionSignatureNode = NodeFactory.createFunctionSignatureNode(
                createToken(SyntaxKind.OPEN_PAREN_TOKEN), parameters, createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                        returnNode);

        // Function Body Node
        FunctionBodyBlockNode functionBodyBlockNode = NodeFactory.createFunctionBodyBlockNode(
                createToken(SyntaxKind.OPEN_BRACE_TOKEN), null, createEmptyNodeList(),
                createToken(SyntaxKind.CLOSE_BRACE_TOKEN));

        return NodeFactory.createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, null,
                qualifiersList, functionKeyWord, functionName, relativeResourcePath, functionSignatureNode,
                functionBodyBlockNode);
    }
}
