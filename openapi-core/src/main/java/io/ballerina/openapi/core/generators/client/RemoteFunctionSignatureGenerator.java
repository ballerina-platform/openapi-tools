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
import io.ballerina.openapi.core.generators.client.exception.FunctionSignatureGeneratorException;
import io.ballerina.openapi.core.generators.client.parameter.HeaderParameterGenerator;
import io.ballerina.openapi.core.generators.client.parameter.PathParameterGenerator;
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

public class RemoteFunctionSignatureGenerator implements FunctionSignatureGenerator {
    OpenAPI openAPI;
    Operation operation;
    List<ClientDiagnostic> diagnostics;
    boolean treatDefaultableAsRequired = false;

    public RemoteFunctionSignatureGenerator(Operation operation, OpenAPI openAPI) {
        this.operation = operation;
        this.openAPI = openAPI;
    }

    public void setTreatDefaultableAsRequired() {
        this.treatDefaultableAsRequired = true;
    }

    @Override
    public FunctionSignatureNode generateFunctionSignature() throws FunctionSignatureGeneratorException {
        // 1. parameters - path , query, requestBody, headers
        List<Parameter> parameters = operation.getParameters();
        ParametersInfo parametersInfo = getParametersInfo(parameters);

        //filter defaultable parameters
        if (!parametersInfo.defaultable().isEmpty()) {
            parametersInfo.parameterList().addAll(parametersInfo.defaultable());
        }
        // Remove the last comma
        //check array out of bound error if parameter size is empty
        parametersInfo.parameterList().remove(parametersInfo.parameterList().size() - 1);
        SeparatedNodeList<ParameterNode> parameterNodes = createSeparatedNodeList(parametersInfo.parameterList());

        // 3. return statements
        FunctionReturnTypeGeneratorImp functionReturnType = getFunctionReturnTypeGenerator();
        Optional<ReturnTypeDescriptorNode> returnType = functionReturnType.getReturnType();
        if (returnType.isEmpty()) {
            throw new FunctionSignatureGeneratorException("Return type is not found for the operation : " +
                    operation.getOperationId());
        }
        //create function signature node
        return NodeFactory.createFunctionSignatureNode(createToken(OPEN_PAREN_TOKEN),parameterNodes, createToken(CLOSE_PAREN_TOKEN), returnType.get());
    }

    protected ParametersInfo getParametersInfo(List<Parameter> parameters) throws FunctionSignatureGeneratorException {
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
                    case "path":
                        PathParameterGenerator paramGenerator = new PathParameterGenerator(parameter, openAPI);
                        Optional<ParameterNode> param = paramGenerator.generateParameterNode(treatDefaultableAsRequired);
                        if (param.isEmpty()) {
                            throw new FunctionSignatureGeneratorException("Error while generating path parameter node");
                        }
                        // Path parameters are always required.
                        parameterList.add(param.get());
                        parameterList.add(comma);
                        break;
                    case "query":
                        QueryParameterGenerator queryParameterGenerator = new QueryParameterGenerator(parameter, openAPI);
                        Optional<ParameterNode> queryParam = queryParameterGenerator.generateParameterNode(treatDefaultableAsRequired);
                        if (queryParam.isEmpty()) {
                            throw new FunctionSignatureGeneratorException("Error while generating query parameter node");
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
                        Optional<ParameterNode> headerParam = headerParameterGenerator.generateParameterNode(treatDefaultableAsRequired);
                        if (headerParam.isEmpty()) {
                            throw new FunctionSignatureGeneratorException("Error while generating header parameter node");
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
            Optional<ParameterNode> requestBody = requestBodyGenerator.generateParameterNode(treatDefaultableAsRequired);
            if (requestBody.isEmpty()) {
                throw new FunctionSignatureGeneratorException("Error while generating request body node");
            }
            parameterList.add(requestBody.get());
            parameterList.add(comma);
        }
        return new ParametersInfo(parameterList, defaultable);
    }

    protected FunctionReturnTypeGeneratorImp getFunctionReturnTypeGenerator() {
        return new FunctionReturnTypeGeneratorImp(operation, openAPI);
    }

    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
