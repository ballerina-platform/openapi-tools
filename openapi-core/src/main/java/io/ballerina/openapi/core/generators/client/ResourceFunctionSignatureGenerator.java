package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.core.generators.client.parameter.HeaderParameterGenerator;
import io.ballerina.openapi.core.generators.client.parameter.QueryParameterGenerator;
import io.ballerina.openapi.core.generators.client.parameter.RequestBodyGenerator;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages.OAS_CLIENT_100;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;

public class ResourceFunctionSignatureGenerator implements FunctionSignatureGenerator {
    protected final Operation operation;
    protected final OpenAPI openAPI;
    private final List<ClientDiagnostic> diagnostics = new ArrayList<>();
    public ResourceFunctionSignatureGenerator(Operation operation, OpenAPI openAPI) {
        this.operation = operation;
        this.openAPI = openAPI;
    }

    @Override
    public Optional<FunctionSignatureNode> generateFunctionSignature() {
        // 1. parameters - path , query, requestBody, headers
        List<Parameter> parameters = operation.getParameters();
        ParametersInfo parametersInfo = getParametersInfo(parameters);

        if (parametersInfo == null) {
            return Optional.empty();
        }

        List<Node> defaultableParameters = parametersInfo.defaultable();
        List<Node> parameterList = parametersInfo.parameterList();

        //filter defaultable parameters
        if (!defaultableParameters.isEmpty()) {
            parameterList.addAll(defaultableParameters);
        }
        // Remove the last comma
        //check array out of bound error if parameter size is empty
        if (!parameterList.isEmpty()) {
            parameterList.remove(parameterList.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameterNodes = createSeparatedNodeList(parameterList);

        // 3. return statements
        FunctionReturnTypeGeneratorImp functionReturnType = getFunctionReturnTypeGenerator();
        Optional<ReturnTypeDescriptorNode> returnType = functionReturnType.getReturnType();
        //todo: need to handle this
        //            throw new FunctionSignatureGeneratorException("Return type is not found for the operation : " +
        //                    operation.getOperationId());
        return returnType.map(returnTypeDescriptorNode -> NodeFactory.createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN), parameterNodes,
                createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode));
        //create function signature node
    }

    protected ParametersInfo getParametersInfo(List<Parameter> parameters) {
        List<Node> parameterList = new ArrayList<>();
        List<Node> defaultable = new ArrayList<>();
        Token comma = createToken(COMMA_TOKEN);
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                if (parameter.get$ref() != null) {
                    String paramType = null;
                    try {
                        paramType = extractReferenceType(parameter.get$ref());
                    } catch (BallerinaOpenApiException e) {
                        DiagnosticMessages diagnostic = OAS_CLIENT_100;
                        ClientDiagnosticImp clientDiagnostic = new ClientDiagnosticImp(diagnostic.getCode(),
                                diagnostic.getDescription(), parameter.get$ref());
                        diagnostics.add(clientDiagnostic);
                    }
                    parameter = openAPI.getComponents().getParameters().get(paramType);
                }

                String in = parameter.getIn();

                switch (in) {
                    case "query":
                        QueryParameterGenerator queryParameterGenerator = new QueryParameterGenerator(parameter, openAPI);
                        Optional<ParameterNode> queryParam = queryParameterGenerator.generateParameterNode(false);
                        if (queryParam.isEmpty()) {
                            //TODO: need to handle this
                            return null;
//                            throw new FunctionSignatureGeneratorException("Error while generating query parameter node");
                        }
                        if (queryParam.get() instanceof RequiredParameterNode requiredParameterNode) {
                            parameterList.add(requiredParameterNode);
                            parameterList.add(comma);
                        } else {
                            defaultable.add(queryParam.get());
                            defaultable.add(comma);
                        }
                        break;
                    case "header":
                        HeaderParameterGenerator headerParameterGenerator = new HeaderParameterGenerator(parameter, openAPI);
                        Optional<ParameterNode> headerParam = headerParameterGenerator.generateParameterNode(false);
                        if (headerParam.isEmpty()) {
                            //todo: need to handle this
                            return null;
//                            throw new FunctionSignatureGeneratorException("Error while generating header parameter node");
                        }
                        if (headerParam.get() instanceof RequiredParameterNode headerNode) {
                            parameterList.add(headerNode);
                            parameterList.add(comma);
                        } else {
                            defaultable.add(headerParam.get());
                            defaultable.add(comma);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        // 2. requestBody
        if (operation.getRequestBody() != null) {
            RequestBodyGenerator requestBodyGenerator = new RequestBodyGenerator(operation.getRequestBody(), openAPI);
            Optional<ParameterNode> requestBody = requestBodyGenerator.generateParameterNode(false);
            if (requestBody.isEmpty()) {
                //todo: need to handle this
                return null;
//                throw new FunctionSignatureGeneratorException("Error while generating request body node");
            }
            parameterList.add(requestBody.get());
            parameterList.add(comma);
        }
        return new ParametersInfo(parameterList, defaultable);
    }

    protected FunctionReturnTypeGeneratorImp getFunctionReturnTypeGenerator() {
        return new FunctionReturnTypeGeneratorImp(operation, openAPI);
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return null;
    }
}
