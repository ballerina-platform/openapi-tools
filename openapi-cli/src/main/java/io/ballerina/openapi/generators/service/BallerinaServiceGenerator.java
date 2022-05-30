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
import io.ballerina.compiler.syntax.tree.MetadataNode;
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
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import static io.ballerina.openapi.generators.GeneratorConstants.OAS_PATH_SEPARATOR;
import static io.ballerina.openapi.generators.GeneratorConstants.RESOURCE;
import static io.ballerina.openapi.generators.GeneratorUtils.SINGLE_WS_MINUTIAE;
import static io.ballerina.openapi.generators.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.generators.GeneratorUtils.getRelativeResourcePath;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.createImportDeclarationNodes;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.generateServiceConfigAnnotation;

/**
 * This Util class use for generating ballerina service file according to given yaml file.
 */
public class BallerinaServiceGenerator {
    private boolean isNullableRequired;
    private OpenAPI openAPI;
    private Filter filter;

    // initialize the instance as null.
    private static BallerinaServiceGenerator instance = null;
    private final Map<String, TypeDefinitionNode> typeInclusionRecords = new HashMap<>();
    public List<TypeDefinitionNode> getTypeInclusionRecords() {
        List<TypeDefinitionNode> typeRecords = new ArrayList<>();
        if (this.typeInclusionRecords.size() > 0) {
            this.typeInclusionRecords.forEach((key, value) -> {
                typeRecords.add(value);
            });
        }
        return typeRecords;
    }

    BallerinaServiceGenerator() {}

    // Check if the instance is null, within a synchronized block. If so, create the object
    public static BallerinaServiceGenerator getInstanceDoubleLocking() {
        synchronized (BallerinaServiceGenerator.class) {
            if (instance == null) {
                instance = new BallerinaServiceGenerator();
            }
        }
        return instance;
    }
    public void initialize(OpenAPI openApi, Filter filter) {
            this.openAPI = openApi;
            this.filter = filter;
            this.isNullableRequired = false;
    }

    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException {
        // Create imports http and openapi
        NodeList<ImportDeclarationNode> imports = createImportDeclarationNodes();
        // Need to Generate Base path
        ListenerGenerator listener = new ListenerGenerator();
        ListenerDeclarationNode listenerDeclarationNode = listener.getListenerDeclarationNodes(openAPI.getServers());
        NodeList<Node> absoluteResourcePath = createBasePathNodeList(listener);

        SimpleNameReferenceNode listenerName = createSimpleNameReferenceNode(listenerDeclarationNode.variableName());
        SeparatedNodeList<ExpressionNode> expressions = NodeFactory.createSeparatedNodeList(listenerName);

        // Fill the members with function
        List<Node> functions = createResourceFunctions(openAPI, filter);

        NodeList<Node> members = NodeFactory.createNodeList(functions);
        // Create annotation if nullable property is enabled
        // @http:ServiceConfig {
        //     treatNilableAsOptional : false
        //}
        MetadataNode metadataNode = null;
        if (isNullableRequired) {
            metadataNode = generateServiceConfigAnnotation();
        }
        ServiceDeclarationNode serviceDeclarationNode = NodeFactory.createServiceDeclarationNode(
                metadataNode, createEmptyNodeList(), createToken(SyntaxKind.SERVICE_KEYWORD,
                        SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE),
                null, absoluteResourcePath, createToken(SyntaxKind.ON_KEYWORD,
                        SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE), expressions,
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
        if (OAS_PATH_SEPARATOR.equals(listener.getBasePath())) {
            return AbstractNodeFactory.createNodeList(createIdentifierToken(listener.getBasePath()));
        } else {
            String[] basePathNode = listener.getBasePath().split(OAS_PATH_SEPARATOR);
            List<Node> basePath = Arrays.stream(basePathNode).filter(node -> !node.isBlank())
                    .map(node -> createIdentifierToken(OAS_PATH_SEPARATOR +
                            escapeIdentifier(node))).collect(Collectors.toList());
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
                    if (GeneratorUtils.hasTags(operationTags, filterTags) ||
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
        NodeList<Token> qualifiersList = NodeFactory.createNodeList(createIdentifierToken(RESOURCE, SINGLE_WS_MINUTIAE,
                SINGLE_WS_MINUTIAE));
        Token functionKeyWord = createIdentifierToken(FUNCTION, SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE);
        IdentifierToken functionName = createIdentifierToken(operation.getKey().name()
                .toLowerCase(Locale.ENGLISH), SINGLE_WS_MINUTIAE, SINGLE_WS_MINUTIAE);
        NodeList<Node> relativeResourcePath = NodeFactory.createNodeList(pathNodes);
        ParametersGenerator parametersGenerator = new ParametersGenerator(false, openAPI.getComponents());
        List<Node> params = parametersGenerator.generateResourcesInputs(operation);
        if (!isNullableRequired) {
            isNullableRequired = parametersGenerator.isNullableRequired();
        }
        SeparatedNodeList<ParameterNode> parameters = AbstractNodeFactory.createSeparatedNodeList(params);
        ReturnTypeGenerator returnTypeGenerator = new ReturnTypeGenerator();
        ReturnTypeDescriptorNode returnNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation,
                createEmptyNodeList());
        typeInclusionRecords.putAll(returnTypeGenerator.getTypeInclusionRecords());

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
