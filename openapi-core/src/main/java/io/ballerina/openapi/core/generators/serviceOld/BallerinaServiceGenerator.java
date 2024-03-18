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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.generators.schemaOld.BallerinaTypesGenerator;
import io.ballerina.openapi.core.generators.schemaOld.model.GeneratorMetaData;
import io.ballerina.openapi.core.generators.serviceOld.model.OASServiceMetadata;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;

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
    private final Map<String, TypeDefinitionNode> typeInclusionRecords = new HashMap<>();
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

    public List<TypeDefinitionNode> getTypeInclusionRecords() {
        List<TypeDefinitionNode> typeRecords = new ArrayList<>();
        this.typeInclusionRecords.forEach((key, value) -> {
            typeRecords.add(value);
        });
        return typeRecords;
    }

//    public SyntaxTree generateSyntaxTree() throws BallerinaOpenApiException {
//        // Create imports http and openapi
//        NodeList<ImportDeclarationNode> imports = createImportDeclarationNodes();
//        // Need to Generate Base path
//        ListenerGenerator listener = new ListenerGenerator();
//        ListenerDeclarationNode listenerDeclarationNode = listener.getListenerDeclarationNodes(openAPI.getServers());
//        NodeList<Node> absoluteResourcePath = createBasePathNodeList(listener);
//
//        SimpleNameReferenceNode listenerName = createSimpleNameReferenceNode(listenerDeclarationNode.variableName());
//        SeparatedNodeList<ExpressionNode> expressions = createSeparatedNodeList(listenerName);
//
//        // Fill the members with function
//        List<Node> functions = createResourceFunctions(openAPI, filter);
//        this.setFunctionList(functions);
//
//        NodeList<Node> members = createNodeList(functions);
//        // Create annotation if nullable property is enabled
//        // @http:ServiceConfig {
//        //     treatNilableAsOptional : false
//        //}
//        MetadataNode metadataNode = null;
//        if (isNullableRequired) {
//            metadataNode = generateServiceConfigAnnotation();
//        }
//        TypeDescriptorNode serviceType = null;
//        if (isServiceTypeRequired) {
//            serviceType = createSimpleNameReferenceNode(createIdentifierToken(SERVICE_TYPE_NAME));
//        }
//        ServiceDeclarationNode serviceDeclarationNode = createServiceDeclarationNode(
//                metadataNode, createEmptyNodeList(), createToken(SyntaxKind.SERVICE_KEYWORD,
//                        GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE),
//                serviceType, absoluteResourcePath, createToken(SyntaxKind.ON_KEYWORD,
//                        GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE), expressions,
//                createToken(SyntaxKind.OPEN_BRACE_TOKEN), members, createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
//
//        // Create module member declaration
//        NodeList<ModuleMemberDeclarationNode> moduleMembers = createNodeList(
//                listenerDeclarationNode, serviceDeclarationNode);
//
//        Token eofToken = createIdentifierToken("");
//        ModulePartNode modulePartNode = createModulePartNode(imports, moduleMembers, eofToken);
//
//        TextDocument textDocument = TextDocuments.from("");
//        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
//        return syntaxTree.modifyWith(modulePartNode);
//    }

//    private List<Node> createResourceFunctions(OpenAPI openApi, Filter filter) throws BallerinaOpenApiException {
//
//        List<Node> functions = new ArrayList<>();
//        if (!openApi.getPaths().isEmpty()) {
//            Paths paths = openApi.getPaths();
//            Set<Map.Entry<String, PathItem>> pathsItems = paths.entrySet();
//            for (Map.Entry<String, PathItem> path : pathsItems) {
//                if (!path.getValue().readOperationsMap().isEmpty()) {
//                    Map<PathItem.HttpMethod, Operation> operationMap = path.getValue().readOperationsMap();
//                    functions.addAll(applyFiltersForOperations(filter, path.getKey(), operationMap));
//                }
//            }
//        }
//        return functions;
//    }
//
//    private NodeList<Node> createBasePathNodeList(ListenerGenerator listener) {
//
//        if (GeneratorConstants.OAS_PATH_SEPARATOR.equals(listener.getBasePath())) {
//            return createNodeList(createIdentifierToken(listener.getBasePath()));
//        } else {
//            String[] basePathNode = listener.getBasePath().split(GeneratorConstants.OAS_PATH_SEPARATOR);
//            List<Node> basePath = Arrays.stream(basePathNode).filter(node -> !node.isBlank())
//                    .map(node -> createIdentifierToken(GeneratorConstants.OAS_PATH_SEPARATOR +
//                            GeneratorUtils.escapeIdentifier(node))).collect(Collectors.toList());
//            return createNodeList(basePath);
//        }
//    }

//    private List<Node> applyFiltersForOperations(Filter filter, String path,
//                                                 Map<PathItem.HttpMethod, Operation> operationMap)
//            throws BallerinaOpenApiException {
//
//        List<Node> functions = new ArrayList<>();
//        for (Map.Entry<PathItem.HttpMethod, Operation> operation : operationMap.entrySet()) {
//            List<Node> resourceFunctionDocs = new ArrayList<>();
//            addFunctionDescToAPIDocs(operation, resourceFunctionDocs);
//            //Add filter availability
//            //1.Tag filter
//            //2.Operation filter
//            //3. Both tag and operation filter
//            List<String> filterTags = filter.getTags();
//            List<String> operationTags = operation.getValue().getTags();
//            List<String> filterOperations = filter.getOperations();
//            if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
//                if (operationTags != null || ((!filterOperations.isEmpty())
//                        && (operation.getValue().getOperationId() != null))) {
//                    if ((operationTags != null && GeneratorUtils.hasTags(operationTags, filterTags)) ||
//                            ((operation.getValue().getOperationId() != null) &&
//                                    filterOperations.contains(operation.getValue().getOperationId().trim()))) {
//                        // getRelative resource path
//                        List<Node> functionRelativeResourcePath = GeneratorUtils.getRelativeResourcePath(path,
//                                operation.getValue(), resourceFunctionDocs, openAPI.getComponents(),
//                                generateWithoutDataBinding);
//                        // function call
//
//                        FunctionDefinitionNode functionDefinitionNode = generateWithoutDataBinding ?
//                                generateGenericResourceFunctions(operation,
//                                        functionRelativeResourcePath, path, resourceFunctionDocs) :
//                                getResourceFunction(operation, functionRelativeResourcePath, path,
//                                        resourceFunctionDocs);
//                        functions.add(functionDefinitionNode);
//                    }
//                }
//            } else {
//                // getRelative resource path
//                List<Node> relativeResourcePath = GeneratorUtils.getRelativeResourcePath(path, operation.getValue(),
//                        resourceFunctionDocs, openAPI.getComponents(), generateWithoutDataBinding);
//                // function call
//                FunctionDefinitionNode resourceFunction = generateWithoutDataBinding ?
//                        generateGenericResourceFunctions(operation,
//                                relativeResourcePath, path, resourceFunctionDocs) : getResourceFunction(operation,
//                        relativeResourcePath, path, resourceFunctionDocs);
//                functions.add(resourceFunction);
//            }
//        }
//        return functions;
//    }

//    private FunctionDefinitionNode generateGenericResourceFunctions(Map.Entry<PathItem.HttpMethod, Operation> operation,
//                                                                    List<Node> pathNodes, String path,
//                                                                    List<Node> resourceFunctionDocs) {
//        NodeList<Token> qualifiersList = createNodeList(createIdentifierToken(GeneratorConstants.RESOURCE,
//                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
//        Token functionKeyWord = createIdentifierToken(GeneratorConstants.FUNCTION, GeneratorUtils.SINGLE_WS_MINUTIAE,
//                GeneratorUtils.SINGLE_WS_MINUTIAE);
//        IdentifierToken functionName = createIdentifierToken(operation.getKey().name()
//                .toLowerCase(Locale.ENGLISH), GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
//        NodeList<Node> relativeResourcePath = createNodeList(pathNodes);
//        List<Node> parameters = new ArrayList<>();
//        // create parameter `http:Caller caller`
//        BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
//                createIdentifierToken(HTTP_CALLER));
//        IdentifierToken paramName = createIdentifierToken(CALLER);
//        RequiredParameterNode httpCaller = createRequiredParameterNode(createEmptyNodeList(), typeName, paramName);
//        parameters.add(httpCaller);
//        // create parameter `http:Request request`
//        parameters.add(createToken(COMMA_TOKEN));
//        BuiltinSimpleNameReferenceNode typeNameRequest = createBuiltinSimpleNameReferenceNode(null,
//                createIdentifierToken(HTTP_REQUEST));
//        IdentifierToken paramNameRequest = createIdentifierToken(REQUEST);
//        RequiredParameterNode httpRequest = createRequiredParameterNode(createEmptyNodeList(), typeNameRequest,
//                paramNameRequest);
//        parameters.add(httpRequest);
//
//        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(parameters);
//
//        ReturnTypeDescriptorNode returnTypeDescriptorNode =
//                createReturnTypeDescriptorNode(createToken(SyntaxKind.RETURNS_KEYWORD), createEmptyNodeList(),
//                        createSimpleNameReferenceNode(createIdentifierToken("error?")));
//
//        // create function signature
//        FunctionSignatureNode functionSignatureNode = createFunctionSignatureNode(createToken(
//                        SyntaxKind.OPEN_PAREN_TOKEN), parameterList, createToken(SyntaxKind.CLOSE_PAREN_TOKEN),
//                returnTypeDescriptorNode);
//        // create function body
//        FunctionBodyBlockNode functionBodyBlockNode = createFunctionBodyBlockNode(
//                createToken(SyntaxKind.OPEN_BRACE_TOKEN),
//                null, createEmptyNodeList(),
//                createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
//
//        return createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, null,
//                qualifiersList, functionKeyWord, functionName, relativeResourcePath, functionSignatureNode,
//                functionBodyBlockNode);
//
//    }
//
//    /**
//     * Generate resource function for given operation.
//     *
//     * @param operation -  OAS operation
//     * @param pathNodes -  Relative path nodes
//     * @return - {@link FunctionDefinitionNode} relevant resource
//     * @throws BallerinaOpenApiException when the process failure occur
//     */
//    private FunctionDefinitionNode getResourceFunction(Map.Entry<PathItem.HttpMethod, Operation> operation,
//                                                       List<Node> pathNodes, String path,
//                                                       List<Node> resourceFunctionDocs)
//            throws BallerinaOpenApiException {
//
//        NodeList<Token> qualifiersList = createNodeList(createIdentifierToken(GeneratorConstants.RESOURCE,
//                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
//        Token functionKeyWord = createIdentifierToken(GeneratorConstants.FUNCTION, GeneratorUtils.SINGLE_WS_MINUTIAE,
//                GeneratorUtils.SINGLE_WS_MINUTIAE);
//        IdentifierToken functionName = createIdentifierToken(operation.getKey().name()
//                .toLowerCase(Locale.ENGLISH), GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
//        NodeList<Node> relativeResourcePath = createNodeList(pathNodes);
//        ParametersGenerator parametersGenerator = new ParametersGenerator(false, openAPI);
//        parametersGenerator.generateResourcesInputs(operation, resourceFunctionDocs);
//        List<Node> params = new ArrayList<>(parametersGenerator.getRequiredParams());
//
//        // Handle request Body (Payload)
//        if (operation.getValue().getRequestBody() != null) {
//            RequestBody requestBody = operation.getValue().getRequestBody();
//            requestBody = resolveRequestBodyReference(requestBody);
//            RequiredParameterNode nodeForRequestBody = null;
//            if (requestBody.getContent() != null) {
//                RequestBodyGenerator requestBodyGen = new RequestBodyGenerator(requestBody);
//                nodeForRequestBody = requestBodyGen.createNodeForRequestBody();
//                params.add(nodeForRequestBody);
//                params.add(createToken(SyntaxKind.COMMA_TOKEN));
//            }
//
//            if (nodeForRequestBody != null && nodeForRequestBody.paramName().isPresent()) {
//                String description = requestBody.getDescription() != null && !requestBody.getDescription().isBlank()
//                        ? requestBody.getDescription() : DEFAULT_PARAM_COMMENT;
//                MarkdownParameterDocumentationLineNode paramAPIDoc =
//                        DocCommentsGenerator.createAPIParamDoc(escapeIdentifier(
//                                        nodeForRequestBody.paramName().get().text()),
//                                description.split("\n")[0]);
//                resourceFunctionDocs.add(paramAPIDoc);
//            }
//        }
//
//        // For creating the order of the parameters in the function
//        if (!parametersGenerator.getDefaultableParams().isEmpty()) {
//            params.addAll(parametersGenerator.getDefaultableParams());
//        }
//        if (params.size() > 1) {
//            params.remove(params.size() - 1);
//        }
//
//        if (!isNullableRequired) {
//            isNullableRequired = parametersGenerator.isNullableRequired();
//        }
//        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(params);
//        String pathForRecord = Objects.equals(path, SLASH) || Objects.equals(path, CATCH_ALL_PATH) ? "" :
//                GeneratorUtils.getValidName(path, true);
//        ReturnTypeGenerator returnTypeGenerator = new ReturnTypeGenerator(ballerinaSchemaGenerator, pathForRecord,
//                openAPI);
//        if (!paths.contains(path)) {
//            returnTypeGenerator.setCountForRecord(0);
//            paths.add(path);
//        }
//        ReturnTypeDescriptorNode returnNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation,
//                createEmptyNodeList(), path, resourceFunctionDocs);
//        typeInclusionRecords.putAll(returnTypeGenerator.getTypeInclusionRecords());
//
//        FunctionSignatureNode functionSignatureNode = createFunctionSignatureNode(
//                createToken(SyntaxKind.OPEN_PAREN_TOKEN),
//                parameters, createToken(SyntaxKind.CLOSE_PAREN_TOKEN), returnNode);
//
//        // Function Body Node
//        // If path parameter has some special characters, extra body statements are added to handle the complexity.
//        List<StatementNode> bodyStatements = GeneratorUtils.generateBodyStatementForComplexUrl(path);
//        FunctionBodyBlockNode functionBodyBlockNode = createFunctionBodyBlockNode(
//                createToken(SyntaxKind.OPEN_BRACE_TOKEN),
//                null,
//                bodyStatements.isEmpty() ?
//                        createEmptyNodeList() :
//                        createNodeList(bodyStatements),
//                createToken(SyntaxKind.CLOSE_BRACE_TOKEN), null);
//
//        List<AnnotationNode> annotationNodes = new ArrayList<>();
//        MetadataNode metadataNode = createMetadataNode(createMarkdownDocumentationNode(
//                createNodeList(resourceFunctionDocs)), createNodeList(annotationNodes));
//
//        return createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, metadataNode,
//                qualifiersList, functionKeyWord, functionName, relativeResourcePath, functionSignatureNode,
//                functionBodyBlockNode);
//    }
//
//    private static void addFunctionDescToAPIDocs(Map.Entry<PathItem.HttpMethod, Operation> operation,
//                                                 List<Node> resourceFunctionDocs) {
//        // Add function description
//        if (operation.getValue().getSummary() != null) {
//            resourceFunctionDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
//                    operation.getValue().getSummary(), true));
//        } else if (operation.getValue().getDescription() != null && !operation.getValue().getDescription().isBlank()) {
//            resourceFunctionDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
//                    operation.getValue().getDescription(), true));
//        } else {
//            resourceFunctionDocs.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
//                    DEFAULT_FUNC_COMMENT, true));
//        }
//    }

    /**
     * Resolve requestBody reference.
     */
//    private RequestBody resolveRequestBodyReference(RequestBody requestBody) throws BallerinaOpenApiException {
//
//        if (requestBody.get$ref() != null) {
//            String requestBodyName = GeneratorUtils.extractReferenceType(requestBody.get$ref());
//            requestBody = resolveRequestBodyReference(openAPI.getComponents()
//                    .getRequestBodies().get(requestBodyName.trim()));
//        }
//        return requestBody;
//    }
}
