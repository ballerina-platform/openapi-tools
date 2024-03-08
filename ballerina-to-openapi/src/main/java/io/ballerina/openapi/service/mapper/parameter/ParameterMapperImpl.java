/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.service.mapper.parameter;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.PathParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.openapi.service.mapper.Constants;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.IncompatibleResourceDiagnostic;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.HTTP_REQUEST;
import static io.ballerina.openapi.service.mapper.Constants.WILD_CARD_CONTENT_KEY;
import static io.ballerina.openapi.service.mapper.Constants.WILD_CARD_SUMMARY;

/**
 * The {@link ParameterMapperImpl} class is the implementation class for the {@link ParameterMapper}.
 * This class provides functionalities to map the Ballerina resource parameters to OpenAPI operation parameters.
 *
 * @since 1.9.0
 */
public class ParameterMapperImpl implements ParameterMapper {
    private final FunctionDefinitionNode functionDefinitionNode;
    private final OperationInventory operationInventory;
    private final Map<String, String> apidocs;
    private final AdditionalData additionalData;
    private final Components components;
    private final boolean treatNilableAsOptional;

    public ParameterMapperImpl(FunctionDefinitionNode functionDefinitionNode, OperationInventory operationInventory,
                               Components components, Map<String, String> apiDocs, AdditionalData additionalData,
                               Boolean treatNilableAsOptional) {
        this.functionDefinitionNode = functionDefinitionNode;
        this.operationInventory = operationInventory;
        this.apidocs = apiDocs;
        this.additionalData = additionalData;
        this.components = components;
        this.treatNilableAsOptional = treatNilableAsOptional;
    }

    public void setParameters() {
        NodeList<Node> pathParams = functionDefinitionNode.relativeResourcePath();
        if (!pathParams.isEmpty()) {
            setPathParameters(pathParams);
        }

        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();
        SeparatedNodeList<ParameterNode> parameterList = functionSignature.parameters();
        for (ParameterNode parameterNode : parameterList) {
            String parameterType = getParameterType(parameterNode);
            if (Objects.isNull(parameterType)) {
                continue;
            }
            if ((parameterType.equals("REQUEST") || parameterType.equals("PAYLOAD")) &&
                    (Constants.GET.equalsIgnoreCase(operationInventory.getHttpOperation()))) {
                DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_113;
                IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage,
                        parameterNode.location());
                additionalData.diagnostics().add(error);
                continue;
            }
            setParameter(parameterNode, parameterType);
        }
    }

    private void setParameter(ParameterNode parameterNode, String parameterType) {
        switch (parameterType) {
            case "QUERY" -> {
                QueryParameterMapper queryParameterMapper = new QueryParameterMapper(parameterNode, apidocs,
                        operationInventory, components, treatNilableAsOptional, additionalData);
                queryParameterMapper.setParameter();
            }
            case "HEADER" -> {
                HeaderParameterMapper headerParameterMapper = new HeaderParameterMapper(parameterNode, apidocs,
                        operationInventory, components, treatNilableAsOptional, additionalData);
                headerParameterMapper.setParameter();
            }
            case "PAYLOAD" -> {
                Optional<Symbol> symbol = additionalData.semanticModel().symbol(parameterNode);
                if (symbol.isEmpty() || !(symbol.get() instanceof ParameterSymbol)) {
                    return;
                }
                AnnotationNode annotation = getPayloadAnnotation(parameterNode);
                RequestBodyMapper requestBodyMapper = new RequestBodyMapper((ParameterSymbol) symbol.get(), annotation,
                        operationInventory, functionDefinitionNode, components, apidocs, additionalData);
                requestBodyMapper.setRequestBody();
            }
            case "REQUEST" -> {
                RequestBody requestBody = new RequestBody();
                MediaType mediaType = new MediaType();
                mediaType.setSchema(new Schema<>().description(WILD_CARD_SUMMARY));
                requestBody.setContent(new Content().addMediaType(WILD_CARD_CONTENT_KEY, mediaType));
                // The following method will only add the request body if it is not already set.
                operationInventory.setRequestBody(requestBody);
            }
            default -> {

            }
        }
    }

    private AnnotationNode getPayloadAnnotation(ParameterNode parameterNode) {
        if (parameterNode instanceof DefaultableParameterNode defaultableParameterNode) {
            return defaultableParameterNode.annotations().stream()
                    .filter(annotationNode -> annotationNode.annotReference().toString().trim()
                            .equals(Constants.HTTP_PAYLOAD))
                    .findFirst().orElse(null);
        } else if (parameterNode instanceof RequiredParameterNode requiredParameterNode) {
            return requiredParameterNode.annotations().stream()
                    .filter(annotationNode -> annotationNode.annotReference().toString().trim()
                            .equals(Constants.HTTP_PAYLOAD))
                    .findFirst().orElse(null);
        }
        return null;
    }

    private void setPathParameters(NodeList<Node> pathParams) {
        for (Node param: pathParams) {
            if (param instanceof ResourcePathParameterNode pathParam) {
                SemanticModel semanticModel = additionalData.semanticModel();
                PathParameterSymbol pathParameterSymbol = (PathParameterSymbol) semanticModel.symbol(pathParam).get();
                PathParameterMapper pathParameterMapper = new PathParameterMapper(pathParameterSymbol, components,
                        apidocs, operationInventory, additionalData);
                pathParameterMapper.setParameter();
            }
        }
    }

    private String getParameterType(ParameterNode parameterNode) {
        NodeList<AnnotationNode> annotationNodes = null;
        if (parameterNode instanceof DefaultableParameterNode defaultableParameterNode) {
            annotationNodes = defaultableParameterNode.annotations();
        } else if (parameterNode instanceof RequiredParameterNode requiredParameterNode) {
            String parameterTypeName = requiredParameterNode.typeName().toString().trim();
            if (parameterTypeName.equals(HTTP_REQUEST)) {
                return "REQUEST";
            } else {
                SemanticModel semanticModel = additionalData.semanticModel();
                Symbol parameterSymbol = semanticModel.symbol(requiredParameterNode).orElse(null);
                if (Objects.isNull(parameterSymbol) || !(parameterSymbol instanceof ParameterSymbol)) {
                    return null;
                }
                TypeSymbol parameterTypeSymbol = ((ParameterSymbol) parameterSymbol).typeDescriptor();
                if (!parameterTypeSymbol.subtypeOf(semanticModel.types().ANYDATA)) {
                    return null;
                }
            }
            annotationNodes = requiredParameterNode.annotations();
        }
        if (Objects.isNull(annotationNodes) || annotationNodes.isEmpty()) {
            return "QUERY";
        }
        for (AnnotationNode annotationNode: annotationNodes) {
            String annotationTypeName = annotationNode.annotReference().toString().trim();
            if (annotationTypeName.equals(Constants.HTTP_HEADER)) {
                return "HEADER";
            } else if (annotationTypeName.equals(Constants.HTTP_PAYLOAD)) {
                return "PAYLOAD";
            }
        }
        return "QUERY";
    }
}
