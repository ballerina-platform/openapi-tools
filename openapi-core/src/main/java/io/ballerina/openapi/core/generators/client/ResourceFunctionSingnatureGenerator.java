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

public class ResourceFunctionSingnatureGenerator implements FunctionSignatureGenerator {
    private final Operation operation;
    private final OpenAPI openAPI;
    private final List<ClientDiagnostic> diagnostics = new ArrayList<>();
    public ResourceFunctionSingnatureGenerator(Operation operation, OpenAPI openAPI) {
        this.operation = operation;
        this.openAPI = openAPI;
    }

    @Override
    public Optional<FunctionSignatureNode> generateFunctionSignature() {
        // 1. parameters - path , query, requestBody, headers
        List<String> paramName = new ArrayList<>();
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
                        DiagnosticMessages diagnostic = OAS_CLIENT_100;
                        ClientDiagnosticImp clientDiagnostic = new ClientDiagnosticImp(diagnostic,
                                parameter.get$ref());
                        diagnostics.add(clientDiagnostic);
                    }
                    parameter = openAPI.getComponents().getParameters().get(paramType);
                }

                String in = parameter.getIn();

                switch (in) {
                    case "query":
                        QueryParameterGenerator queryParameterGenerator = new QueryParameterGenerator(parameter,
                                openAPI);
                        Optional<ParameterNode> queryParam = queryParameterGenerator.generateParameterNode();
                        if (queryParam.isEmpty()) {
                            diagnostics.addAll(queryParameterGenerator.getDiagnostics());
                            return Optional.empty();
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
                        HeaderParameterGenerator headerParameterGenerator = new HeaderParameterGenerator(parameter,
                                openAPI);
                        Optional<ParameterNode> headerParam = headerParameterGenerator.generateParameterNode();
                        if (headerParam.isEmpty()) {
                            diagnostics.addAll(headerParameterGenerator.getDiagnostics());
                            return Optional.empty();
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
            Optional<ParameterNode> requestBody = requestBodyGenerator.generateParameterNode();
            if (requestBody.isEmpty()) {
                diagnostics.addAll(requestBodyGenerator.getDiagnostics());
                return Optional.empty();
            }
            parameterList.add(requestBody.get());
            parameterList.add(comma);

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

        //filter defaultable parameters
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
            diagnostics.addAll(functionReturnType.getDiagnostics());
            return Optional.empty();
        }
        return returnType.map(returnTypeDescriptorNode -> NodeFactory.createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterNodes,
                createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode));
        //create function signature node
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return null;
    }
}
