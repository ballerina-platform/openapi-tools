package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
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

    public RemoteFunctionSignatureGenerator(Operation operation, OpenAPI openAPI) {
        this.operation = operation;
        this.openAPI = openAPI;
    }

    @Override
    public Optional<FunctionSignatureNode> generateFunctionSignature() {
        List<String> paramName = new ArrayList<>();
        // 1. parameters - path , query, requestBody, headers
        List<Node> parameterList = new ArrayList<>();
        List<Parameter> parameters = operation.getParameters();
        List<Node> defaultable = new ArrayList<>();
        Token comma = createToken(COMMA_TOKEN);
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                if (parameter.get$ref() != null) {
                    String paramType = null;
                    try {
                        paramType = extractReferenceType(parameter.get$ref());
                    } catch (BallerinaOpenApiException e) {
                        ClientDiagnosticImp clientDiagnostic = new ClientDiagnosticImp(OAS_CLIENT_100,
                                parameter.get$ref());
                        diagnostics.add(clientDiagnostic);
                    }
                    parameter = openAPI.getComponents().getParameters().get(paramType);
                }

                String in = parameter.getIn();

                switch (in) {
                    case "path":
                        PathParameterGenerator paramGenerator = new PathParameterGenerator(parameter, openAPI);
                        Optional<ParameterNode> param = paramGenerator.generateParameterNode();
                        if (param.isEmpty()) {
                            diagnostics.addAll(paramGenerator.getDiagnostics());
                            return Optional.empty();
                        }
                        // Path parameters are always required.
                        parameterList.add(param.get());
                        parameterList.add(comma);
                        paramName.add(param.get().toString());
                        break;
                    case "query":
                        QueryParameterGenerator queryParameterGenerator = new QueryParameterGenerator(parameter,
                                openAPI);
                        Optional<ParameterNode> queryParam = queryParameterGenerator.generateParameterNode();
                        if (queryParam.isEmpty()) {
                            diagnostics.addAll(queryParameterGenerator.getDiagnostics());
                            return Optional.empty();
                        }

                        paramName.add(queryParam.get().toString());
                        if (queryParam.get() instanceof RequiredParameterNode requiredParameterNode) {
                            parameterList.add(requiredParameterNode);
                            parameterList.add(comma);
                        } else {
                            defaultable.add(queryParam.get());
                            defaultable.add(comma);
                        }
                        break;
                    case "header":
                        HeaderParameterGenerator headerParameterGenerator = new HeaderParameterGenerator(parameter,
                                openAPI);
                        Optional<ParameterNode> headerParam = headerParameterGenerator.generateParameterNode();
                        if (headerParam.isEmpty()) {
                            diagnostics.addAll(headerParameterGenerator.getDiagnostics());
                            return Optional.empty();
                        }
                        paramName.add(headerParam.get().toString());

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
            Optional<ParameterNode> requestBody = requestBodyGenerator.generateParameterNode();
            if (requestBody.isEmpty()) {
                diagnostics.addAll(requestBodyGenerator.getDiagnostics());
                return Optional.empty();
            }
            List<ParameterNode> rBheaderParameters = requestBodyGenerator.getHeaderParameters();
            parameterList.add(requestBody.get());
            parameterList.add(comma);
            if (!rBheaderParameters.isEmpty()) {
                rBheaderParameters.forEach(header -> {
                    if (!paramName.contains(header.toString())) {
                        paramName.add(header.toString());
                        if (header instanceof DefaultableParameterNode defaultableParameterNode) {
                            defaultable.add(defaultableParameterNode);
                            defaultable.add(comma);
                        } else {
                            parameterList.add(header);
                            parameterList.add(comma);
                        }
                    }
                });
            }
        }

        // filter defaultable parameters
        if (!defaultable.isEmpty()) {
            parameterList.addAll(defaultable);
        }
        // Remove the last comma
        if (!parameterList.isEmpty()) {
            parameterList.remove(parameterList.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameterNodes = createSeparatedNodeList(parameterList);

        // 3. return statements
        FunctionReturnTypeGeneratorImp functionReturnType = new FunctionReturnTypeGeneratorImp(operation, openAPI);
        Optional<ReturnTypeDescriptorNode> returnType = functionReturnType.getReturnType();
        if (returnType.isEmpty()) {
            return Optional.empty();
        }

        return returnType.map(returnTypeDescriptorNode -> NodeFactory.createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterNodes,
                createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode));

    }

    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
