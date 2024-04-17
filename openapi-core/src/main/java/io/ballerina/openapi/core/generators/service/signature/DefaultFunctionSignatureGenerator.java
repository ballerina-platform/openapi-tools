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

package io.ballerina.openapi.core.generators.service.signature;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.exception.InvalidReferenceException;
import io.ballerina.openapi.core.generators.common.exception.UnsupportedOASDataTypeException;
import io.ballerina.openapi.core.generators.service.diagnostic.ServiceDiagnostic;
import io.ballerina.openapi.core.generators.service.exceptions.InvalidHeaderNameException;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.generators.service.parameter.HeaderParameterGenerator;
import io.ballerina.openapi.core.generators.service.parameter.QueryParameterGenerator;
import io.ballerina.openapi.core.generators.service.parameter.RequestBodyGenerator;
import io.ballerina.openapi.core.generators.service.response.ReturnTypeGenerator;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionSignatureNode;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;

public class DefaultFunctionSignatureGenerator extends FunctionSignatureGenerator {

    public DefaultFunctionSignatureGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }

    @Override
    public FunctionSignatureNode getFunctionSignature(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                      String path) throws BallerinaOpenApiException {
        ParametersGeneratorResult parametersGeneratorResult;
        parametersGeneratorResult = generateParameters(operation);
        List<Node> params = new ArrayList<>(parametersGeneratorResult.requiredParameters());

        // Handle request Body (Payload)
        if (operation.getValue().getRequestBody() != null) {
            RequestBody requestBody = operation.getValue().getRequestBody();
            requestBody = resolveRequestBodyReference(requestBody);
            RequiredParameterNode nodeForRequestBody;
            if (requestBody != null && requestBody.getContent() != null) {
                RequestBodyGenerator requestBodyGenerator = RequestBodyGenerator
                        .getRequestBodyGenerator(oasServiceMetadata, path);
                nodeForRequestBody = requestBodyGenerator.createRequestBodyNode(requestBody);
                params.add(nodeForRequestBody);
                params.add(createToken(SyntaxKind.COMMA_TOKEN));
                diagnostics.addAll(requestBodyGenerator.getDiagnostics());
            }
        }

        // For creating the order of the parameters in the function
        if (!parametersGeneratorResult.defaultableParameters().isEmpty()) {
            params.addAll(parametersGeneratorResult.defaultableParameters());
        }
        if (params.size() > 1) {
            params.remove(params.size() - 1);
        }
        SeparatedNodeList<ParameterNode> parameters = createSeparatedNodeList(params);
        ReturnTypeGenerator returnTypeGenerator = ReturnTypeGenerator.getReturnTypeGenerator(oasServiceMetadata, path);
        ReturnTypeDescriptorNode returnNode;
        returnNode = returnTypeGenerator.getReturnTypeDescriptorNode(operation, path);
        diagnostics.addAll(returnTypeGenerator.getDiagnostics());
        return createFunctionSignatureNode(createToken(SyntaxKind.OPEN_PAREN_TOKEN),
                parameters, createToken(SyntaxKind.CLOSE_PAREN_TOKEN), returnNode);

    }

    /**
     * This function for generating operation parameters.
     *
     * @param operation OAS operation
     */
    public ParametersGeneratorResult generateParameters(Map.Entry<PathItem.HttpMethod, Operation> operation) throws
            InvalidHeaderNameException {
        List<Node> requiredParams = new ArrayList<>();
        List<Node> defaultableParams = new ArrayList<>();
        Token comma = createToken(SyntaxKind.COMMA_TOKEN);
        // Handle header and query parameters
        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
            for (Parameter parameter : parameters) {
                Node param;
                if (parameter.get$ref() != null) {
                    String referenceType;
                    try {
                        referenceType = extractReferenceType(parameter.get$ref());
                    } catch (InvalidReferenceException e) {
                        Diagnostic diagnostic = e.getDiagnostic();
                        diagnostics.add(new ServiceDiagnostic(diagnostic.diagnosticInfo().code(),
                                diagnostic.message(), diagnostic.diagnosticInfo().severity()));
                        return new ParametersGeneratorResult(new ArrayList<>(), new ArrayList<>());
                    }
                    parameter = oasServiceMetadata.getOpenAPI().getComponents().getParameters().get(referenceType);
                }
                if (parameter.getIn().trim().equals(GeneratorConstants.HEADER)) {
                    HeaderParameterGenerator headerParamGenerator = new HeaderParameterGenerator(oasServiceMetadata);
                    try {
                        param = headerParamGenerator.generateParameterNode(parameter);
                    } catch (UnsupportedOASDataTypeException | InvalidReferenceException e) {
                        diagnostics.add(e.getDiagnostic());
                        continue;
                    }
                    diagnostics.addAll(headerParamGenerator.getDiagnostics());
                    if (headerParamGenerator.isNullableRequired()) {
                        isNullableRequired = true;
                    }
                    if (param.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                        defaultableParams.add(param);
                        defaultableParams.add(comma);
                    } else {
                        requiredParams.add(param);
                        requiredParams.add(comma);
                    }
                } else if (parameter.getIn().trim().equals(GeneratorConstants.QUERY)) {
                    if (parameter.getRequired() != null && parameter.getRequired() &&
                            (parameter.getSchema() != null && parameter.getSchema().getNullable() != null &&
                                    parameter.getSchema().getNullable())) {
                        isNullableRequired = true;
                    }
                    // type  BasicType boolean|int|float|decimal|string ;
                    // public type () |BasicType|BasicType []| map<json>;
                    QueryParameterGenerator queryParameterGenerator = new QueryParameterGenerator(oasServiceMetadata);
                    try {
                        param = queryParameterGenerator.generateParameterNode(parameter);
                    } catch (InvalidReferenceException | UnsupportedOASDataTypeException e) {
                        diagnostics.add(e.getDiagnostic());
                        continue;
                    }
                    diagnostics.addAll(queryParameterGenerator.getDiagnostics());
                    if (param != null) {
                        if (param.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                            defaultableParams.add(param);
                            defaultableParams.add(comma);
                        } else {
                            requiredParams.add(param);
                            requiredParams.add(comma);
                        }
                    }
                }
            }
        }
        return new ParametersGeneratorResult(requiredParams, defaultableParams);
    }

    /**
     * Resolve requestBody reference.
     */
    private RequestBody resolveRequestBodyReference(RequestBody requestBody) {
        if (requestBody.get$ref() != null) {
            try {
                String requestBodyName = extractReferenceType(requestBody.get$ref());
                requestBody = resolveRequestBodyReference(oasServiceMetadata.getOpenAPI().getComponents()
                        .getRequestBodies().get(requestBodyName.trim()));
            } catch (InvalidReferenceException e) {
                diagnostics.add(e.getDiagnostic());
                return null;
            }
        }
        return requestBody;
    }

    public record ParametersGeneratorResult(List<Node> requiredParameters, List<Node> defaultableParameters) { }
}
