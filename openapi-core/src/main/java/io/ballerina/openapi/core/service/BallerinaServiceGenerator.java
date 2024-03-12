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

package io.ballerina.openapi.core.service;

import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
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
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.typegenerator.BallerinaTypesGenerator;
import io.ballerina.openapi.core.typegenerator.GeneratorUtils;
import io.ballerina.openapi.core.typegenerator.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.typegenerator.model.Filter;
import io.ballerina.openapi.core.typegenerator.model.GeneratorMetaData;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createServiceDeclarationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.openapi.core.typegenerator.GeneratorUtils.escapeIdentifier;

/**
 * This Util class use for generating ballerina service file according to given yaml file.
 *
 * @since 1.3.0
 */
public class BallerinaServiceGenerator {

    private boolean isNullableRequired;
    private final OpenAPI openAPI;
    private final Filter filter;
    private final boolean isServiceTypeRequired;
    private final boolean generateWithoutDataBinding;
    private final BallerinaTypesGenerator ballerinaSchemaGenerator;
    private List<Node> functionList = new ArrayList<>();
    private final Set<String> paths = new LinkedHashSet<>();

    public BallerinaServiceGenerator(OASServiceMetadata oasServiceMetadata) {
        this.openAPI = oasServiceMetadata.getOpenAPI();
        this.filter = oasServiceMetadata.getFilters();
        this.isNullableRequired = false;
        this.isServiceTypeRequired = oasServiceMetadata.isServiceTypeRequired();
        this.generateWithoutDataBinding = oasServiceMetadata.generateWithoutDataBinding();
        this.ballerinaSchemaGenerator = new BallerinaTypesGenerator(openAPI, oasServiceMetadata.isNullable(),
                new LinkedList<>());
        GeneratorMetaData.createInstance(openAPI, oasServiceMetadata.isNullable(),
                oasServiceMetadata.isServiceTypeRequired());
    }

    public List<Node> getFunctionList() {
        return functionList;
    }

    public void setFunctionList(List<Node> functionList) {
        this.functionList = functionList;
    }

    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException {
        // Create imports http and openapi
        NodeList<ImportDeclarationNode> imports = ServiceGenerationUtils.createImportDeclarationNodes();
        // Need to Generate Base path
        ListenerGenerator listener = new ListenerGenerator();
        ListenerDeclarationNode listenerDeclarationNode = listener.getListenerDeclarationNodes(openAPI.getServers());
        NodeList<Node> absoluteResourcePath = createBasePathNodeList(listener);

        SimpleNameReferenceNode listenerName = createSimpleNameReferenceNode(listenerDeclarationNode.variableName());
        SeparatedNodeList<ExpressionNode> expressions = createSeparatedNodeList(listenerName);

        // Fill the members with function
        List<Node> functions = createResourceFunctions(openAPI, filter);
        this.setFunctionList(functions);

        NodeList<Node> members = createNodeList(functions);
        // Create annotation if nullable property is enabled
        // @http:ServiceConfig {
        //     treatNilableAsOptional : false
        //}
        MetadataNode metadataNode = null;
        if (isNullableRequired) {
            metadataNode = ServiceGenerationUtils.generateServiceConfigAnnotation();
        }
        TypeDescriptorNode serviceType = null;
        if (isServiceTypeRequired) {
            serviceType = createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.SERVICE_TYPE_NAME));
        }
        ServiceDeclarationNode serviceDeclarationNode = createServiceDeclarationNode(
                metadataNode, createEmptyNodeList(), createToken(SyntaxKind.SERVICE_KEYWORD,
                        GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE),
                serviceType, absoluteResourcePath, createToken(SyntaxKind.ON_KEYWORD,
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
                            escapeIdentifier(node))).collect(Collectors.toList());
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
                                operation.getValue(), openAPI.getComponents(),
                                generateWithoutDataBinding);
                        // function call

                        FunctionDefinitionNode functionDefinitionNode = generateWithoutDataBinding ?
                                generateGenericResourceFunctions(operation, functionRelativeResourcePath, path) :
                                getResourceFunction(operation, functionRelativeResourcePath, path);
                        functions.add(functionDefinitionNode);
                    }
                }
            } else {
                // getRelative resource path
                List<Node> relativeResourcePath = GeneratorUtils.getRelativeResourcePath(path, operation.getValue(),
                        openAPI.getComponents(), generateWithoutDataBinding);
                // function call
                FunctionDefinitionNode resourceFunction = generateWithoutDataBinding ?
                        generateGenericResourceFunctions(operation,
                                relativeResourcePath, path) : getResourceFunction(operation,
                        relativeResourcePath, path);
                functions.add(resourceFunction);
            }
        }
        return functions;
    }

    private FunctionDefinitionNode generateGenericResourceFunctions(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                                    List<Node> pathNodes, String path) {
        NodeList<Token> qualifiersList = createNodeList(createIdentifierToken(GeneratorConstants.RESOURCE,
                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
        Token functionKeyWord = createIdentifierToken(GeneratorConstants.FUNCTION, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        IdentifierToken functionName = createIdentifierToken(operation.getKey().name()
                .toLowerCase(Locale.ENGLISH), GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
        NodeList<Node> relativeResourcePath = createNodeList(pathNodes);
        List<Node> parameters = new ArrayList<>();
        // create parameter `http:Caller caller`
        BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.HTTP_CALLER));
        IdentifierToken paramName = createIdentifierToken(GeneratorConstants.CALLER);
        RequiredParameterNode httpCaller = createRequiredParameterNode(createEmptyNodeList(), typeName, paramName);
        parameters.add(httpCaller);
        // create parameter `http:Request request`
        parameters.add(createToken(COMMA_TOKEN));
        BuiltinSimpleNameReferenceNode typeNameRequest = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.HTTP_REQUEST));
        IdentifierToken paramNameRequest = createIdentifierToken(GeneratorConstants.REQUEST);
        RequiredParameterNode httpRequest = createRequiredParameterNode(createEmptyNodeList(), typeNameRequest,
                paramNameRequest);
        parameters.add(httpRequest);

        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(parameters);

        ReturnTypeDescriptorNode returnTypeDescriptorNode =
                createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
                        createSimpleNameReferenceNode(createIdentifierToken("error?")));

        // create function signature
        FunctionSignatureNode functionSignatureNode = createFunctionSignatureNode(createToken(
                        SyntaxKind.OPEN_PAREN_TOKEN), parameterList, createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
                returnTypeDescriptorNode);
        // create function body
        FunctionBodyBlockNode functionBodyBlockNode = createFunctionBodyBlockNode(
                createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                null, createEmptyNodeList(),
                createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);

        return createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, null,
                qualifiersList, functionKeyWord, functionName, relativeResourcePath, functionSignatureNode,
                functionBodyBlockNode);

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
        ParametersGenerator parametersGenerator = new ParametersGenerator(false, openAPI);
        parametersGenerator.generateResourcesInputs(operation);
        List<Node> params = new ArrayList<>(parametersGenerator.getRequiredParams());

        // Handle request Body (Payload)
        if (operation.getValue().getRequestBody() != null) {
            RequestBody requestBody = operation.getValue().getRequestBody();
            requestBody = resolveRequestBodyReference(requestBody);
            RequiredParameterNode nodeForRequestBody = null;
            if (requestBody.getContent() != null) {
                RequestBodyGenerator requestBodyGen = new RequestBodyGenerator(requestBody);
                nodeForRequestBody = requestBodyGen.createNodeForRequestBody();
                params.add(nodeForRequestBody);
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
        String pathForRecord = Objects.equals(path, GeneratorConstants.SLASH) || Objects.equals(path, GeneratorConstants.CATCH_ALL_PATH) ? "" :
                GeneratorUtils.getValidName(path, true);
        ReturnTypeGenerator returnTypeGenerator = new ReturnTypeGenerator(ballerinaSchemaGenerator, pathForRecord,
                openAPI);
//        if (!paths.contains(path)) {
//            returnTypeGenerator.setCountForRecord(0);
//            paths.add(path);
//        }
        ReturnTypeDescriptorNode returnNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation,
                createEmptyNodeList(), path);
//        typeInclusionRecords.putAll(BallerinaTypesGenerator.getTypeDefinitionNodes());

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
