package io.ballerina.openapi.core.service;

import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.model.GenSrcFile;
import io.ballerina.openapi.core.generators.type.GeneratorUtils;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.generators.type.model.Filter;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.service.parameter.ParametersGeneratorImpl;
import io.ballerina.openapi.core.service.parameter.RequestBodyGeneratorImpl;
import io.ballerina.openapi.core.service.response.ReturnTypeGenerator;
import io.ballerina.openapi.core.service.response.ReturnTypeGeneratorFactory;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createResourcePathParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SLASH_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DEFAULT_FILE_HEADER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DO_NOT_MODIFY_FILE_HEADER;
import static io.ballerina.openapi.core.generators.type.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.core.generators.type.GeneratorUtils.getPathParameterType;
import static io.ballerina.openapi.core.generators.type.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.generators.type.GeneratorUtils.resolveReferenceType;
import static io.ballerina.openapi.core.service.ServiceGenerationUtils.extractReferenceType;

public abstract class ServiceGenerator {

    final OASServiceMetadata oasServiceMetadata;

    public ServiceGenerator(OASServiceMetadata oasServiceMetadata) {
        this.oasServiceMetadata = oasServiceMetadata;
    }

    public static List<GenSrcFile> generateServiceFiles(OASServiceMetadata oasServiceMetadata) throws
            FormatterException {
        List<GenSrcFile> sourceFiles = new ArrayList<>();
        String mainContent;
        if (oasServiceMetadata.isGenerateOnlyServiceType()) {
            ServiceObjectGenerator serviceObjectGenerator = new ServiceObjectGenerator(oasServiceMetadata);
            serviceObjectGenerator.generateSyntaxTree();
            String serviceType = Formatter.format(serviceObjectGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    "service_type.bal",
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DO_NOT_MODIFY_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + serviceType));
        } else {
            ServiceObjectGenerator serviceObjectGenerator = new ServiceObjectGenerator(oasServiceMetadata);
            String serviceType = Formatter.format(serviceObjectGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    "service_type.bal",
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DO_NOT_MODIFY_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + serviceType));
            ServiceGeneratorImpl serviceGenerator = new ServiceGeneratorImpl(oasServiceMetadata);
            mainContent = Formatter.format(serviceGenerator.generateSyntaxTree()).toSourceCode();
            sourceFiles.add(new GenSrcFile(GenSrcFile.GenFileType.GEN_SRC, oasServiceMetadata.getSrcPackage(),
                    oasServiceMetadata.getSrcFile(),
                    (oasServiceMetadata.getLicenseHeader().isBlank() ? DEFAULT_FILE_HEADER :
                            oasServiceMetadata.getLicenseHeader()) + mainContent));
        }
        return sourceFiles;
    }

    public abstract SyntaxTree generateSyntaxTree();

    List<Node> createResourceFunctions(OpenAPI openApi, Filter filter) {
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

    private List<Node> applyFiltersForOperations(Filter filter, String path,
                                                 Map<PathItem.HttpMethod, Operation> operationMap) {
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
                        List<Node> functionRelativeResourcePath = getRelativeResourcePath(path,
                                operation.getValue(), oasServiceMetadata.getOpenAPI().getComponents(),
                                oasServiceMetadata.generateWithoutDataBinding());
                        // function call

                        FunctionDefinitionNode functionDefinitionNode = oasServiceMetadata.generateWithoutDataBinding() ?
                                generateGenericResourceFunctions(operation, functionRelativeResourcePath, path) :
                                getResourceFunction(operation, functionRelativeResourcePath, path);
                        functions.add(functionDefinitionNode);
                    }
                }
            } else {
                // getRelative resource path
                List<Node> relativeResourcePath = getRelativeResourcePath(path, operation.getValue(),
                        oasServiceMetadata.getOpenAPI().getComponents(), oasServiceMetadata.generateWithoutDataBinding());
                // function call
                FunctionDefinitionNode resourceFunction = oasServiceMetadata.generateWithoutDataBinding() ?
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
     * @throws OASTypeGenException when the process failure occur
     */
    private FunctionDefinitionNode getResourceFunction(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                       List<Node> pathNodes, String path) {

        NodeList<Token> qualifiersList = createNodeList(createIdentifierToken(GeneratorConstants.RESOURCE,
                GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE));
        Token functionKeyWord = createIdentifierToken(GeneratorConstants.FUNCTION, GeneratorUtils.SINGLE_WS_MINUTIAE,
                GeneratorUtils.SINGLE_WS_MINUTIAE);
        IdentifierToken functionName = createIdentifierToken(operation.getKey().name()
                .toLowerCase(Locale.ENGLISH), GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
        NodeList<Node> relativeResourcePath = createNodeList(pathNodes);
        ParametersGeneratorImpl parametersGenerator = new ParametersGeneratorImpl(false,
                oasServiceMetadata.getOpenAPI());
        try {
            parametersGenerator.generateResourcesInputs(operation);
        } catch (OASTypeGenException e) {
            throw new RuntimeException();
        }
        List<Node> params = new ArrayList<>(parametersGenerator.getRequiredParams());

        // Handle request Body (Payload)
        if (operation.getValue().getRequestBody() != null) {
            RequestBody requestBody = operation.getValue().getRequestBody();
            requestBody = resolveRequestBodyReference(requestBody);
            RequiredParameterNode nodeForRequestBody = null;
            if (requestBody.getContent() != null) {
                RequestBodyGeneratorImpl requestBodyGen = new RequestBodyGeneratorImpl(requestBody);
                try {
                    nodeForRequestBody = requestBodyGen.createRequestBodyNode();
                } catch (OASTypeGenException e) {
                    throw new RuntimeException(e);
                }
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

//        if (!oasServiceMetadata.isNullable()) {
//            isNullableRequired = parametersGenerator.isNullableRequired();
//        }
        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(params);
        String pathForRecord = Objects.equals(path, GeneratorConstants.SLASH) || Objects.equals(path, GeneratorConstants.CATCH_ALL_PATH) ? "" :
                getValidName(path, true);
        ReturnTypeGenerator returnTypeGenerator = ReturnTypeGeneratorFactory.getReturnTypeGenerator(false,
                pathForRecord, oasServiceMetadata.getOpenAPI());
        ReturnTypeDescriptorNode returnNode = null;
        try {
            returnNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation,
                    createEmptyNodeList(), path);
        } catch (OASTypeGenException e) {
            throw new RuntimeException(e);
        }

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
    private RequestBody resolveRequestBodyReference(RequestBody requestBody) {
        if (requestBody.get$ref() != null) {
            try {
                String requestBodyName = GeneratorUtils.extractReferenceType(requestBody.get$ref());
                requestBody = resolveRequestBodyReference(oasServiceMetadata.getOpenAPI().getComponents()
                        .getRequestBodies().get(requestBodyName.trim()));
            } catch (OASTypeGenException e) {
                throw new RuntimeException();
            }
        }
        return requestBody;
    }

    /**
     * Generated resource function relative path node list.
     *
     * @param path      - resource path
     * @param operation - resource operation
     * @return - node lists
     * @throws OASTypeGenException
     */
    private List<Node> getRelativeResourcePath(String path, Operation operation,
                                                     Components components, boolean isWithoutDataBinding) {

        List<Node> functionRelativeResourcePath = new ArrayList<>();
        String[] pathNodes = path.split(GeneratorConstants.SLASH);
        if (pathNodes.length >= 2) {
            for (String pathNode : pathNodes) {
                if (pathNode.contains(GeneratorConstants.OPEN_CURLY_BRACE)) {
                    String pathParam = pathNode;
                    pathParam = pathParam.substring(pathParam.indexOf(GeneratorConstants.OPEN_CURLY_BRACE) + 1);
                    pathParam = pathParam.substring(0, pathParam.indexOf(GeneratorConstants.CLOSE_CURLY_BRACE));
                    pathParam = getValidName(pathParam, false);

                    /**
                     * TODO -> `onCall/[string id]\.json` type of url won't support from syntax
                     * issue https://github.com/ballerina-platform/ballerina-spec/issues/1138
                     * <pre>resource function get onCall/[string id]\.json() returns string {}</>
                     */
                    if (operation.getParameters() != null) {
                        extractPathParameterDetails(operation, functionRelativeResourcePath, pathNode,
                                pathParam, components, isWithoutDataBinding);
                    }
                } else if (!pathNode.isBlank()) {
                    IdentifierToken idToken = createIdentifierToken(escapeIdentifier(pathNode.trim()));
                    functionRelativeResourcePath.add(idToken);
                    functionRelativeResourcePath.add(createToken(SLASH_TOKEN));
                }
            }
            functionRelativeResourcePath.remove(functionRelativeResourcePath.size() - 1);
        } else if (pathNodes.length == 0) {
            IdentifierToken idToken = createIdentifierToken(".");
            functionRelativeResourcePath.add(idToken);
        } else {
            IdentifierToken idToken = createIdentifierToken(pathNodes[1].trim());
            functionRelativeResourcePath.add(idToken);
        }
        return functionRelativeResourcePath;
    }


    private void extractPathParameterDetails(Operation operation, List<Node> functionRelativeResourcePath,
                                                    String pathNode, String pathParam,
                                                    Components components, boolean isWithoutDataBinding) {
        // check whether path parameter segment has special character
        String[] split = pathNode.split(io.ballerina.openapi.core.generators.type.GeneratorConstants.CLOSE_CURLY_BRACE, 2);
        Pattern pattern = Pattern.compile(io.ballerina.openapi.core.generators.type.GeneratorConstants.SPECIAL_CHARACTERS_REGEX);
        Matcher matcher = pattern.matcher(split[1]);
        boolean hasSpecialCharacter = matcher.find();

        for (Parameter parameter : operation.getParameters()) {
            if (parameter.get$ref() != null) {
                try {
                    parameter = GeneratorMetaData.getInstance().getOpenAPI().getComponents()
                            .getParameters().get(extractReferenceType(parameter.get$ref()));
                } catch (OASTypeGenException e) {
                    throw new RuntimeException(e);
                }
            }
            if (parameter.getIn() == null) {
                continue;
            }
            if (pathParam.trim().equals(getValidName(parameter.getName().trim(), false))
                    && parameter.getIn().equals("path")) {
                String paramType;
                if (parameter.getSchema().get$ref() != null) {
                    try {
                        paramType = resolveReferenceType(parameter.getSchema(), components, isWithoutDataBinding,
                                pathParam);
                    } catch (OASTypeGenException e) {
                        throw new RuntimeException(e);
                    }
                    Schema<?> schema = GeneratorMetaData.getInstance().getOpenAPI().getComponents().getSchemas().get(paramType);
                    try {
                        TypeHandler.getInstance().generateTypeDescriptorForOASSchema(schema, paramType);
                    } catch (OASTypeGenException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    paramType = getPathParameterType(parameter.getSchema(), pathParam);
                    if (paramType.endsWith(GeneratorConstants.NILLABLE)) {
                        throw new RuntimeException("Path parameter value cannot be null.");
                    }
                }

                // TypeDescriptor
                BuiltinSimpleNameReferenceNode builtSNRNode = createBuiltinSimpleNameReferenceNode(
                        null,
                        parameter.getSchema() == null || hasSpecialCharacter ?
                                createIdentifierToken(io.ballerina.openapi.core.generators.type.GeneratorConstants.STRING) :
                                createIdentifierToken(paramType));
                IdentifierToken paramName = createIdentifierToken(
                        hasSpecialCharacter ?
                                getValidName(pathNode, false) :
                                pathParam);
                ResourcePathParameterNode resourcePathParameterNode =
                        createResourcePathParameterNode(
                                SyntaxKind.RESOURCE_PATH_SEGMENT_PARAM,
                                createToken(OPEN_BRACKET_TOKEN),
                                NodeFactory.createEmptyNodeList(),
                                builtSNRNode,
                                null,
                                paramName,
                                createToken(CLOSE_BRACKET_TOKEN));
                functionRelativeResourcePath.add(resourcePathParameterNode);
                functionRelativeResourcePath.add(createToken(SLASH_TOKEN));
                break;
            }
        }
    }
}
