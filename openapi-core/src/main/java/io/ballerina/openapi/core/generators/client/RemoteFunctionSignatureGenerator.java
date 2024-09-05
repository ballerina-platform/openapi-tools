/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

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
import io.ballerina.openapi.core.generators.client.parameter.HeadersParameterGenerator;
import io.ballerina.openapi.core.generators.client.parameter.PathParameterGenerator;
import io.ballerina.openapi.core.generators.client.parameter.QueriesParameterGenerator;
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
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HEADERS;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;

public class RemoteFunctionSignatureGenerator implements FunctionSignatureGenerator {
    OpenAPI openAPI;
    Operation operation;
    List<ClientDiagnostic> diagnostics  = new ArrayList<>();
    FunctionReturnTypeGeneratorImp functionReturnTypeGenerator;
    private final String httpMethod;
    private final String path;
    private String headersParamName = HEADERS;

    private boolean hasDefaultHeader = false;
    private boolean hasHeadersParam = false;
    private boolean hasQueriesParam = false;

    public RemoteFunctionSignatureGenerator(Operation operation, OpenAPI openAPI, String httpMethod,
                                            String path) {
        this.operation = operation;
        this.openAPI = openAPI;
        this.httpMethod = httpMethod;
        this.path = path;
        this.functionReturnTypeGenerator = new FunctionReturnTypeGeneratorImp(operation, openAPI, httpMethod);
    }

    @Override
    public Optional<FunctionSignatureNode> generateFunctionSignature() {
        List<Parameter> parameters = operation.getParameters();
        ParametersInfo parametersInfo = getParametersInfo(parameters);

        if (parametersInfo == null) {
            return Optional.empty();
        }

        List<Node> paramList = getParameterNodes(parametersInfo);
        SeparatedNodeList<ParameterNode> parameterNodes = createSeparatedNodeList(paramList);

        // 3. return statements
        FunctionReturnTypeGeneratorImp returnTypeGenerator = getFunctionReturnTypeGenerator();
        Optional<ReturnTypeDescriptorNode> returnType = returnTypeGenerator.getReturnType();
        diagnostics.addAll(returnTypeGenerator.getDiagnostics());
        if (returnType.isEmpty()) {
            return Optional.empty();
        }

        return returnType.map(returnTypeDescriptorNode -> NodeFactory.createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterNodes,
                createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode));
    }

    private static List<Node> getParameterNodes(ParametersInfo parametersInfo) {
        List<Node> defaultableParams = parametersInfo.defaultableParams();
        List<Node> params = parametersInfo.requiredParams();
        List<Node> includedParam = parametersInfo.includedParam();

        // Add defaultableParams parameters
        if (!defaultableParams.isEmpty()) {
            params.addAll(defaultableParams);
        }

        // Add included record parameter
        if (!includedParam.isEmpty()) {
            params.addAll(includedParam);
        }

        // Remove the last comma
        if (!params.isEmpty()) {
            params.remove(params.size() - 1);
        }
        return params;
    }

    protected ParametersInfo getParametersInfo(List<Parameter> parameters) {
        List<Node> requiredParams = new ArrayList<>();
        List<Node> defaultableParams = new ArrayList<>();
        List<Node> includedParam = new ArrayList<>();
        Token comma = createToken(COMMA_TOKEN);

        List<Parameter> headerParameters = new ArrayList<>();
        List<Parameter> queryParameters = new ArrayList<>();
        List<Parameter> pathParameters = new ArrayList<>();

        // 1. path parameters
        if (parameters != null) {
            for (Parameter parameter : parameters) {
                if (parameter.getIn().equals("path")) {
                    PathParameterGenerator paramGenerator = new PathParameterGenerator(parameter, openAPI);
                    Optional<ParameterNode> param = paramGenerator.generateParameterNode();
                    if (param.isEmpty()) {
                        diagnostics.addAll(paramGenerator.getDiagnostics());
                        return null;
                    }
                    // Path parameters are always required.
                    requiredParams.add(param.get());
                    requiredParams.add(comma);
                }
            }
        }

        // 1. requestBody
        if (operation.getRequestBody() != null) {
            RequestBodyGenerator requestBodyGenerator = new RequestBodyGenerator(operation.getRequestBody(),
                    openAPI);
            Optional<ParameterNode> requestBody = requestBodyGenerator.generateParameterNode();
            if (requestBody.isEmpty()) {
                diagnostics.addAll(requestBodyGenerator.getDiagnostics());
                return null;
            }
            headerParameters = requestBodyGenerator.getHeaderSchemas();
            requiredParams.add(requestBody.get());
            requiredParams.add(comma);
        }

        // 2. parameters -  query, headers
        if (parameters != null) {
            populateQueryAndHeaderParameters(parameters, queryParameters, headerParameters, pathParameters);

            List<Parameter> nonHeaderParameters = new ArrayList<>(queryParameters);
            nonHeaderParameters.addAll(pathParameters);
            HeadersParameterGenerator headersParameterGenerator = new HeadersParameterGenerator(headerParameters,
                    openAPI, operation, httpMethod, path, nonHeaderParameters);
            Optional<ParameterNode> headers;
            if (headerParameters.isEmpty()) {
                hasDefaultHeader = true;
                headers = HeadersParameterGenerator.getDefaultParameterNode(nonHeaderParameters);
            } else {
                headers = headersParameterGenerator.generateParameterNode();
            }

            diagnostics.addAll(headersParameterGenerator.getDiagnostics());
            if (headersParameterGenerator.hasErrors()) {
                return null;
            }

            if (headers.isPresent()) {
                populateHeadersParamName(headers.get());
                hasHeadersParam = true;
                if (headers.get() instanceof RequiredParameterNode headerNode) {
                    requiredParams.add(headerNode);
                    requiredParams.add(comma);
                } else {
                    defaultableParams.add(headers.get());
                    defaultableParams.add(comma);
                }
            } else if (!headerParameters.isEmpty()) {
                return null;
            }

            QueriesParameterGenerator queriesParameterGenerator = new QueriesParameterGenerator(queryParameters,
                    openAPI, operation, httpMethod, path);
            Optional<ParameterNode> queries = queriesParameterGenerator.generateParameterNode();
            diagnostics.addAll(queriesParameterGenerator.getDiagnostics());
            if (queriesParameterGenerator.hasErrors()) {
                return null;
            }

            if (queries.isPresent()) {
                hasQueriesParam = true;
                includedParam.add(queries.get());
                includedParam.add(comma);
            } else if (!queryParameters.isEmpty()) {
                return null;
            }
        } else {
            ParameterNode defaultHeaderParam = HeadersParameterGenerator.getDefaultParameterNode().orElse(null);
            if (defaultHeaderParam != null) {
                populateHeadersParamName(defaultHeaderParam);
                hasDefaultHeader = true;
                hasHeadersParam = true;
                defaultableParams.add(defaultHeaderParam);
                defaultableParams.add(comma);
            }
        }
        return new ParametersInfo(requiredParams, defaultableParams, includedParam);
    }

    private void populateHeadersParamName(ParameterNode parameterNode) {
        if (parameterNode instanceof RequiredParameterNode requiredParameterNode &&
                requiredParameterNode.paramName().isPresent()) {
            headersParamName = requiredParameterNode.paramName().get().text();
        } else if (parameterNode instanceof DefaultableParameterNode parameter &&
                parameter.paramName().isPresent()) {
            headersParamName = parameter.paramName().get().text();
        }
    }

    private void populateQueryAndHeaderParameters(List<Parameter> parameters, List<Parameter> queryParameters,
                                                  List<Parameter> headerParameters, List<Parameter> pathParameters) {
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
            case "query":
                queryParameters.add(parameter);
                break;
            case "header":
                headerParameters.add(parameter);
                break;
            case "path":
                pathParameters.add(parameter);
                break;
            default:
                break;
        }
    }
}

    protected FunctionReturnTypeGeneratorImp getFunctionReturnTypeGenerator() {
        return functionReturnTypeGenerator;
    }

    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public boolean hasDefaultHeaders() {
        return hasDefaultHeader;
    }

    public boolean hasHeaders() {
        return hasHeadersParam;
    }

    public boolean hasQueries() {
        return hasQueriesParam;
    }

    public boolean hasDefaultStatusCodeBinding() {
        return functionReturnTypeGenerator.hasDefaultStatusCodeBinding();
    }

    public List<String> getNonDefaultStatusCodes() {
        return functionReturnTypeGenerator.getNonDefaultStatusCodes();
    }

    public String getHeadersParamName() {
        return headersParamName;
    }
}
