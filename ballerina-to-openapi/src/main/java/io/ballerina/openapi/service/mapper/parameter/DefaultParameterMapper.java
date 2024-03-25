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
import io.ballerina.openapi.service.mapper.Constants;
import io.ballerina.openapi.service.mapper.ServiceMapperFactory;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.OperationInventory;
import io.ballerina.openapi.service.mapper.type.TypeMapper;
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
 * The {@link DefaultParameterMapper} class is the implementation class for the {@link ParameterMapper}.
 * This class provides functionalities to map the Ballerina resource parameters to OpenAPI operation parameters.
 *
 * @since 1.9.0
 */
public class DefaultParameterMapper implements ParameterMapper {
    public enum ParameterType {
        PAYLOAD, REQUEST, QUERY, HEADER, OTHER
    }
    private final FunctionDefinitionNode functionDefinitionNode;
    private final OperationInventory operationInventory;
    private final Map<String, String> apiDocs;
    private final AdditionalData additionalData;
    private final boolean treatNilableAsOptional;
    private final TypeMapper typeMapper;

    public DefaultParameterMapper(FunctionDefinitionNode functionDefinitionNode, OperationInventory operationInventory,
                                  Map<String, String> apiDocs, AdditionalData additionalData,
                                  Boolean treatNilableAsOptional, ServiceMapperFactory serviceMapperFactory) {
        this.functionDefinitionNode = functionDefinitionNode;
        this.operationInventory = operationInventory;
        this.apiDocs = apiDocs;
        this.additionalData = additionalData;
        this.treatNilableAsOptional = treatNilableAsOptional;
        this.typeMapper = serviceMapperFactory.getTypeMapper();
    }

    public void setParameters() throws ParameterMapperException {
        NodeList<Node> pathParams = functionDefinitionNode.relativeResourcePath();
        if (!pathParams.isEmpty()) {
            setPathParameters(pathParams);
        }

        Iterable<ParameterNode> parameterList = getParameterNodes();
        for (ParameterNode parameterNode : parameterList) {
            setParameter(parameterNode);
        }
    }

    private void setParameter(ParameterNode parameterNode) throws ParameterMapperException {
        ParameterType parameterType = getParameterType(parameterNode);
        if (parameterType.equals(ParameterType.OTHER)) {
            return;
        }
        if ((parameterType.equals(ParameterType.REQUEST) || parameterType.equals(ParameterType.PAYLOAD)) &&
                (Constants.GET.equalsIgnoreCase(operationInventory.getHttpOperation()))) {
            ExceptionDiagnostic error = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_113,
                    parameterNode.location());
            additionalData.diagnostics().add(error);
            return;
        }
        setParameter(parameterNode, parameterType);
    }

    protected Iterable<ParameterNode> getParameterNodes() {
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();
        return functionSignature.parameters();
    }

    private void setParameter(ParameterNode parameterNode, ParameterType parameterType)
            throws ParameterMapperException {
        switch (parameterType) {
            case QUERY -> {
                QueryParameterMapper queryParameterMapper = new QueryParameterMapper(parameterNode, apiDocs,
                        operationInventory, treatNilableAsOptional, additionalData, typeMapper);
                queryParameterMapper.setParameter();
            }
            case HEADER -> {
                HeaderParameterMapper headerParameterMapper = new HeaderParameterMapper(parameterNode, apiDocs,
                        operationInventory, treatNilableAsOptional, additionalData, typeMapper);
                headerParameterMapper.setParameter();
            }
            case PAYLOAD -> {
                Optional<Symbol> symbol = additionalData.semanticModel().symbol(parameterNode);
                if (symbol.isEmpty() || !(symbol.get() instanceof ParameterSymbol)) {
                    return;
                }
                AnnotationNode annotation = getPayloadAnnotation(parameterNode);
                RequestBodyMapper requestBodyMapper = new RequestBodyMapper((ParameterSymbol) symbol.get(), annotation,
                        operationInventory, functionDefinitionNode, apiDocs, additionalData, typeMapper);
                requestBodyMapper.setRequestBody();
            }
            case REQUEST -> {
                RequestBody requestBody = new RequestBody();
                MediaType mediaType = new MediaType();
                mediaType.setSchema(new Schema<>().description(WILD_CARD_SUMMARY));
                requestBody.setContent(new Content().addMediaType(WILD_CARD_CONTENT_KEY, mediaType));
                // The following method will only add the request body if it is not already set.
                operationInventory.setRequestBody(requestBody);
            }
            case OTHER -> {
                // Do nothing
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

    private void setPathParameters(NodeList<Node> pathParams) throws ParameterMapperException {
        for (Node param: pathParams) {
            if (param instanceof ResourcePathParameterNode pathParam) {
                SemanticModel semanticModel = additionalData.semanticModel();
                PathParameterSymbol pathParameterSymbol = (PathParameterSymbol) semanticModel.symbol(pathParam).get();
                PathParameterMapper pathParameterMapper = new PathParameterMapper(pathParameterSymbol, apiDocs,
                        operationInventory, typeMapper);
                pathParameterMapper.setParameter();
            }
        }
    }

    private ParameterType getParameterType(ParameterNode parameterNode) {
        ParameterType parameterType = getObjectParameterType(parameterNode);
        if (Objects.nonNull(parameterType)) {
            return parameterType;
        }

        NodeList<AnnotationNode> annotationNodes = getAnnotationNodes(parameterNode);
        if (Objects.isNull(annotationNodes) || annotationNodes.isEmpty()) {
            return ParameterType.QUERY;
        }

        return getParameterTypeFromAnnotation(annotationNodes);
    }

    private ParameterType getObjectParameterType(ParameterNode parameterNode) {
        ParameterType parameterType = null;
        if (parameterNode instanceof RequiredParameterNode requiredParameterNode) {
            String parameterTypeName = requiredParameterNode.typeName().toString().trim();
            parameterType = parameterTypeName.equals(HTTP_REQUEST) ? ParameterType.REQUEST :
                    getOtherParameterType(requiredParameterNode);
        }
        return parameterType;
    }

    private ParameterType getOtherParameterType(RequiredParameterNode requiredParameterNode) {
        SemanticModel semanticModel = additionalData.semanticModel();
        Symbol parameterSymbol = semanticModel.symbol(requiredParameterNode).orElse(null);
        if (Objects.isNull(parameterSymbol) || !(parameterSymbol instanceof ParameterSymbol)) {
            return ParameterType.OTHER;
        }
        TypeSymbol parameterTypeSymbol = ((ParameterSymbol) parameterSymbol).typeDescriptor();
        if (!parameterTypeSymbol.subtypeOf(semanticModel.types().ANYDATA)) {
            return ParameterType.OTHER;
        }
        return null;
    }

    private static ParameterType getParameterTypeFromAnnotation(NodeList<AnnotationNode> annotationNodes) {
        for (AnnotationNode annotationNode: annotationNodes) {
            String annotationTypeName = annotationNode.annotReference().toString().trim();
            if (annotationTypeName.equals(Constants.HTTP_HEADER)) {
                return ParameterType.HEADER;
            } else if (annotationTypeName.equals(Constants.HTTP_PAYLOAD)) {
                return ParameterType.PAYLOAD;
            }
        }
        return ParameterType.QUERY;
    }

    private static NodeList<AnnotationNode> getAnnotationNodes(ParameterNode parameterNode) {
        NodeList<AnnotationNode> annotationNodes = null;
        if (parameterNode instanceof DefaultableParameterNode defaultableParameterNode) {
            annotationNodes = defaultableParameterNode.annotations();
        } else if (parameterNode instanceof RequiredParameterNode requiredParameterNode) {
            annotationNodes = requiredParameterNode.annotations();
        }
        return annotationNodes;
    }
}
