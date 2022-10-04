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
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.GeneratorConstants;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.RequestBody;

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
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createServiceDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.createImportDeclarationNodes;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.generateServiceConfigAnnotation;

/**
 * This Util class use for generating ballerina service file according to given yaml file.
 *
 * @since 1.3.0
 */
public class BallerinaServiceGenerator {
    private boolean isNullableRequired;
    private final OpenAPI openAPI;
    private final Filter filter;
    private final Map<String, TypeDefinitionNode> typeInclusionRecords = new HashMap<>();

    public BallerinaServiceGenerator(OpenAPI openAPI, Filter filter) {
        this.openAPI = openAPI;
        this.filter = filter;
        this.isNullableRequired = false;
    }

    public List<TypeDefinitionNode> getTypeInclusionRecords() {
        List<TypeDefinitionNode> typeRecords = new ArrayList<>();
        this.typeInclusionRecords.forEach((key, value) -> {
            typeRecords.add(value);
        });
        return typeRecords;
    }

    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException {
        // Create imports http and openapi
        NodeList<ImportDeclarationNode> imports = createImportDeclarationNodes();
        // Need to Generate Base path
        ListenerGenerator listener = new ListenerGenerator();
        ListenerDeclarationNode listenerDeclarationNode = listener.getListenerDeclarationNodes(openAPI.getServers());
        NodeList<Node> absoluteResourcePath = createBasePathNodeList(listener);

        SimpleNameReferenceNode listenerName = createSimpleNameReferenceNode(listenerDeclarationNode.variableName());
        SeparatedNodeList<ExpressionNode> expressions = createSeparatedNodeList(listenerName);

        // Fill the members with function
        List<Node> functions = createResourceFunctions(openAPI, filter);

        NodeList<Node> members = createNodeList(functions);
        // Create annotation if nullable property is enabled
        // @http:ServiceConfig {
        //     treatNilableAsOptional : false
        //}
        MetadataNode metadataNode = null;
        if (isNullableRequired) {
            metadataNode = generateServiceConfigAnnotation();
        }
        ServiceDeclarationNode serviceDeclarationNode = createServiceDeclarationNode(
                metadataNode, createEmptyNodeList(), createToken(SyntaxKind.SERVICE_KEYWORD,
                        GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE),
                null, absoluteResourcePath, createToken(SyntaxKind.ON_KEYWORD,
                        GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE), expressions,
                createToken(SyntaxKind.OPEN_BRACE_TOKEN), members, createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);

        // Create module member declaration
        NodeList<ModuleMemberDeclarationNode> moduleMembers = createNodeList(
                listenerDeclarationNode, serviceDeclarationNode);

        Token eofToken = createIdentifierToken("");
        ModulePartNode modulePartNode = createModulePartNode(imports, moduleMembers, eofToken);

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private List<Node> createResourceFunctions(OpenAPI openApi, Filter filter) throws BallerinaOpenApiException {

        List<Node> functions = new ArrayList<>();
        if (!openApi.getPaths().isEmpty()) {
            Paths paths = openApi.getPaths();
            Set<Map.Entry<String, PathItem>> pathsItems = paths.entrySet();
            for (Map.Entry<String, PathItem> path : pathsItems) {
                if (!path.getValue().readOperationsMap().isEmpty()) {
                    Map<PathItem.HttpMethod, Operation> operationMap = path.getValue().readOperationsMap();
                    functions.addAll(applyFiltersForOperations(filter, path.getKey(), operationMap));
                }
            }
        }
        return functions;
    }

    private NodeList<Node> createBasePathNodeList(ListenerGenerator listener) {

        if (GeneratorConstants.OAS_PATH_SEPARATOR.equals(listener.getBasePath())) {
            return createNodeList(createIdentifierToken(listener.getBasePath()));
        } else {
            String[] basePathNode = listener.getBasePath().split(GeneratorConstants.OAS_PATH_SEPARATOR);
            List<Node> basePath = Arrays.stream(basePathNode).filter(node -> !node.isBlank())
                    .map(node -> createIdentifierToken(GeneratorConstants.OAS_PATH_SEPARATOR +
                            GeneratorUtils.escapeIdentifier(node))).collect(Collectors.toList());
            return createNodeList(basePath);
        }
    }

    private List<Node> applyFiltersForOperations(Filter filter, String path,
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
            List<String> filterOperations = filter.getOperations();
            if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
                if (operationTags != null || ((!filterOperations.isEmpty())
                        && (operation.getValue().getOperationId() != null))) {
                    if ((operationTags != null && GeneratorUtils.hasTags(operationTags, filterTags)) ||
                            ((operation.getValue().getOperationId() != null) &&
                                    filterOperations.contains(operation.getValue().getOperationId().trim()))) {
                        // getRelative resource path
                        List<Node> functionRelativeResourcePath = GeneratorUtils.getRelativeResourcePath(path,
                                operation.getValue());
                        // function call
                        FunctionDefinitionNode functionDefinitionNode = getResourceFunction(operation,
                                functionRelativeResourcePath, path);
                        functions.add(functionDefinitionNode);
                    }
                }
            } else {
                // getRelative resource path
                List<Node> relativeResourcePath = GeneratorUtils.getRelativeResourcePath(path, operation.getValue());
                // function call
                FunctionDefinitionNode resourceFunction = getResourceFunction(operation, relativeResourcePath, path);
                functions.add(resourceFunction);
            }
        }
        return functions;
    }

    /**
     * Generate resource function for given operation.
     *
     * @param operation -  OAS operation
     * @param pathNodes -  Relative path nodes
     * @return - {@link FunctionDefinitionNode} relevant resource
     * @throws BallerinaOpenApiException when the process failure occur
     */
    private FunctionDefinitionNode getResourceFunction(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                       List<Node> pathNodes, String path)
            throws BallerinaOpenApiException {

        NodeList<Token> qualifiersList = createNodeList(createIdentifierToken(GeneratorConstants.RESOURCE,
                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
        Token functionKeyWord = createIdentifierToken(GeneratorConstants.FUNCTION, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        IdentifierToken functionName = createIdentifierToken(operation.getKey().name()
                .toLowerCase(Locale.ENGLISH), GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
        NodeList<Node> relativeResourcePath = createNodeList(pathNodes);
        ParametersGenerator parametersGenerator = new ParametersGenerator(false);
        parametersGenerator.generateResourcesInputs(operation);
        List<Node> params = new ArrayList<>(parametersGenerator.getRequiredParams());

        // Handle request Body (Payload)
        if (operation.getValue().getRequestBody() != null) {
            RequestBody requestBody = operation.getValue().getRequestBody();
            requestBody = resolveRequestBodyReference(requestBody);
            if (requestBody.getContent() != null) {
                RequestBodyGenerator requestBodyGen = new RequestBodyGenerator(this.openAPI.getComponents(),
                        requestBody);
                params.add(requestBodyGen.createNodeForRequestBody());
                params.add(createToken(SyntaxKind.COMMA_TOKEN));
            }
        }

        // For creating the order of the parameters in the function
        if (!parametersGenerator.getDefaultableParams().isEmpty()) {
            params.addAll(parametersGenerator.getDefaultableParams());
        }
        if (params.size() > 1) {
            params.remove(params.size() - 1);
        }

        if (!isNullableRequired) {
            isNullableRequired = parametersGenerator.isNullableRequired();
        }
        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(params);
        ReturnTypeGenerator returnTypeGenerator = new ReturnTypeGenerator();
        ReturnTypeDescriptorNode returnNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation,
                createEmptyNodeList(), path);
        typeInclusionRecords.putAll(returnTypeGenerator.getTypeInclusionRecords());

        FunctionSignatureNode functionSignatureNode = createFunctionSignatureNode(
                createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                parameters, createToken(SyntaxKind.CLOSE_PAREN_TOKEN), returnNode);

        // Function Body Node
        // If path parameter has some special characters, extra body statements are added to handle the complexity.
        List<StatementNode> bodyStatements = GeneratorUtils.generateBodyStatementForComplexUrl(path);
        FunctionBodyBlockNode functionBodyBlockNode = createFunctionBodyBlockNode(
                createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                null,
                bodyStatements.isEmpty() ?
                        createEmptyNodeList() :
                        createNodeList(bodyStatements),
                createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);

        return createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, null,
                qualifiersList, functionKeyWord, functionName, relativeResourcePath, functionSignatureNode,
                functionBodyBlockNode);
    }

    /**
     * Resolve requestBody reference.
     */
    private RequestBody resolveRequestBodyReference(RequestBody requestBody) throws BallerinaOpenApiException {

        if (requestBody.get$ref() != null) {
            String requestBodyName = GeneratorUtils.extractReferenceType(requestBody.get$ref());
            requestBody = resolveRequestBodyReference(openAPI.getComponents()
                    .getRequestBodies().get(requestBodyName.trim()));
        }
        return requestBody;
    }
}
