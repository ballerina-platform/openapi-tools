/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.service.mapper.Constants;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.IncompatibleResourceDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.OperationAdaptor;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.HTTP_REQUEST;
import static io.ballerina.openapi.service.mapper.Constants.WILD_CARD_CONTENT_KEY;
import static io.ballerina.openapi.service.mapper.Constants.WILD_CARD_SUMMARY;

/**
 * OpenAPIParameterMapper provides functionality for converting ballerina parameter to OAS parameter model.
 */
public class ParameterMapper {
    private final FunctionDefinitionNode functionDefinitionNode;
    private final OperationAdaptor operationAdaptor;
    private final Map<String, String> apidocs;
    private final List<OpenAPIMapperDiagnostic> diagnostics = new ArrayList<>();
    private final SemanticModel semanticModel;
    private final TypeMapper typeMapper;
    private final OpenAPI openAPI;
    private final boolean treatNilableAsOptional;

    public ParameterMapper(FunctionDefinitionNode functionDefinitionNode, OperationAdaptor operationAdaptor,
                           Map<String, String> apiDocs, SemanticModel semanticModel,
                           Boolean treatNilableAsOptional,
                           TypeMapper typeMapper, OpenAPI openAPI) {
        this.functionDefinitionNode = functionDefinitionNode;
        this.operationAdaptor = operationAdaptor;
        this.apidocs = apiDocs;
        this.semanticModel = semanticModel;
        this.typeMapper = typeMapper;
        this.openAPI = openAPI;
        this.treatNilableAsOptional = treatNilableAsOptional;
    }

    public List<OpenAPIMapperDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    /**
     * Create {@code Parameters} model for openAPI operation.
     */
    public void getResourceInputs(SemanticModel semanticModel) {
        //Set path parameters
        NodeList<Node> pathParams = functionDefinitionNode.relativeResourcePath();
        if (!pathParams.isEmpty()) {
            createPathParameters(pathParams);
        }
        // Set query parameters, headers and requestBody
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();
        SeparatedNodeList<ParameterNode> parameterList = functionSignature.parameters();
        for (ParameterNode parameterNode : parameterList) {
            QueryParameterMapper queryParameterMapper = new QueryParameterMapper(parameterNode, apidocs,
                    operationAdaptor, treatNilableAsOptional, typeMapper);
            if (parameterNode.kind() == SyntaxKind.REQUIRED_PARAM) {
                RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                // Handle query parameter
                if (requiredParameterNode.typeName().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                    QualifiedNameReferenceNode referenceNode =
                            (QualifiedNameReferenceNode) requiredParameterNode.typeName();
                    String typeName = (referenceNode).modulePrefix().text() + ":" + (referenceNode).identifier().text();
                    if (typeName.equals(HTTP_REQUEST) &&
                            (Constants.GET.equalsIgnoreCase(operationAdaptor.getHttpOperation()))) {
                        DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_113;
                        IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage,
                                referenceNode.location());
                        diagnostics.add(error);
                    } else if (typeName.equals(HTTP_REQUEST)) {
                        RequestBody requestBody = new RequestBody();
                        MediaType mediaType = new MediaType();
                        mediaType.setSchema(new Schema<>().description(WILD_CARD_SUMMARY));
                        requestBody.setContent(new Content().addMediaType(WILD_CARD_CONTENT_KEY, mediaType));
                        operationAdaptor.getOperation().setRequestBody(requestBody);
                    }
                }
                if (requiredParameterNode.typeName().kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE &&
                        requiredParameterNode.annotations().isEmpty()) {
                    queryParameterMapper.setParameter();
                }
                // Handle header, payload parameter
                if (requiredParameterNode.typeName() instanceof TypeDescriptorNode &&
                        !requiredParameterNode.annotations().isEmpty()) {
                    handleAnnotationParameters(semanticModel, requiredParameterNode);
                }
            } else if (parameterNode.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                DefaultableParameterNode defaultableParameterNode = (DefaultableParameterNode) parameterNode;
                // Handle header parameter
                if (defaultableParameterNode.typeName() instanceof TypeDescriptorNode &&
                        !defaultableParameterNode.annotations().isEmpty()) {
                    handleDefaultableAnnotationParameters(defaultableParameterNode);
                } else {
                    queryParameterMapper.setParameter();
                }
            }
        }
    }

    /**
     * Map path parameters to OpenAPI specification.
     */
    private void createPathParameters(NodeList<Node> pathParams) {
        for (Node param: pathParams) {
            if (param instanceof ResourcePathParameterNode pathParam) {
                PathParameterMapper pathParameterMapper = new PathParameterMapper(
                        (PathParameterSymbol) semanticModel.symbol(pathParam).get(), openAPI, apidocs, operationAdaptor,
                        semanticModel, diagnostics);
                pathParameterMapper.setParameter();
            }
        }
    }

    /**
     * This function for handle the payload and header parameters with annotation @http:Payload, @http:Header.
     */
    private void handleAnnotationParameters(SemanticModel semanticModel, RequiredParameterNode requiredParameterNode) {

        NodeList<AnnotationNode> annotations = requiredParameterNode.annotations();
        for (AnnotationNode annotation: annotations) {
            if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_HEADER)) {
                // Handle headers.
                HeaderParameterMapper headerParameterMapper = new HeaderParameterMapper(requiredParameterNode,
                        apidocs, operationAdaptor, treatNilableAsOptional, typeMapper);
                headerParameterMapper.setParameter();
            } else if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_QUERY)) {
                // Handle query parameter.
                QueryParameterMapper queryParameterMapper = new QueryParameterMapper(requiredParameterNode,
                        apidocs, operationAdaptor, treatNilableAsOptional, typeMapper);
                queryParameterMapper.setParameter();
            } else if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_PAYLOAD) &&
                    (!Constants.GET.toLowerCase(Locale.ENGLISH).equalsIgnoreCase(
                            operationAdaptor.getHttpOperation()))) {
                // Handle request payload.
                Optional<Symbol> symbol = semanticModel.symbol(requiredParameterNode);
                if (symbol.isEmpty() || !(symbol.get() instanceof ParameterSymbol)) {
                    return;
                }
                RequestBodyMapper requestBodyMapper = new RequestBodyMapper(semanticModel,
                        (ParameterSymbol) symbol.get(), annotation, operationAdaptor, typeMapper,
                        functionDefinitionNode, apidocs);
                requestBodyMapper.setRequestBody();
            } else if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_PAYLOAD) &&
                    (Constants.GET.toLowerCase(Locale.ENGLISH).equalsIgnoreCase(operationAdaptor.getHttpOperation()))) {
                DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_113;
                IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage,
                        annotation.location());
                diagnostics.add(error);
            }
        }
    }

    /**
     * This function for handle the payload and header parameters with annotation @http:Payload, @http:Header.
     */
    private void handleDefaultableAnnotationParameters(DefaultableParameterNode defaultableParameterNode) {
        NodeList<AnnotationNode> annotations = defaultableParameterNode.annotations();
        for (AnnotationNode annotation: annotations) {
            if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_HEADER)) {
                // Handle headers.
                HeaderParameterMapper headerParameterMapper = new HeaderParameterMapper(defaultableParameterNode,
                        apidocs, operationAdaptor, treatNilableAsOptional, typeMapper);
                headerParameterMapper.setParameter();
            } else if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_QUERY)) {
                // Handle query parameter.
                QueryParameterMapper queryParameterMapper = new QueryParameterMapper(defaultableParameterNode,
                        apidocs, operationAdaptor, treatNilableAsOptional, typeMapper);
                queryParameterMapper.setParameter();
            }
        }
    }
}
