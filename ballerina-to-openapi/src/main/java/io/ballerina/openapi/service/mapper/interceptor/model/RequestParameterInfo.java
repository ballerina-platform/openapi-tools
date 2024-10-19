/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
package io.ballerina.openapi.service.mapper.interceptor.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.interceptor.InterceptorMapperException;
import io.ballerina.openapi.service.mapper.utils.MapperCommonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This {@link RequestParameterInfo} class represents the request parameter information collected from the interceptors.
 *
 * @since 1.9.0
 */
public class RequestParameterInfo {
    private final Map<String, RequestParameter> reqParameters;
    private final SemanticModel semanticModel;

    public RequestParameterInfo(SemanticModel semanticModel) {
        reqParameters = new HashMap<>();
        this.semanticModel = semanticModel;
    }

    public void addReqParameters(Iterable<ParameterNode> parameterNodes, List<OpenAPIMapperDiagnostic> diagnostics,
                                 boolean fromTargetResource) {
        for (ParameterNode parameterNode : parameterNodes) {
            try {
                addReqParameter(parameterNode, fromTargetResource);
            } catch (InterceptorMapperException e) {
                addWarningDiagnostic(diagnostics, e.getMessage());
            }
        }
    }

    private static void addWarningDiagnostic(List<OpenAPIMapperDiagnostic> diagnostics, String cause) {
        ExceptionDiagnostic error = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_127, cause);
        diagnostics.add(error);
    }

    private void addReqParameter(ParameterNode parameterNode, boolean fromTargetResource)
            throws InterceptorMapperException {
        ParameterSymbol parameterSymbol = getParameterSymbol(parameterNode);
        if (Objects.isNull(parameterSymbol)) {
            return;
        }
        String parameterName = getParameterName(parameterSymbol);
        if (Objects.isNull(parameterName)) {
            return;
        }
        addReqParameter(parameterName, parameterNode, parameterSymbol.typeDescriptor(), fromTargetResource);
    }

    private void addReqParameter(String parameterName, ParameterNode parameterNode, TypeSymbol parameterType,
                                 boolean fromTargetResource) throws InterceptorMapperException {
        if (!parameterType.subtypeOf(semanticModel.types().ANYDATA)) {
            return;
        }
        if (reqParameters.containsKey(parameterName)) {
            updateReqParameter(parameterName, parameterNode, parameterType, fromTargetResource);
            return;
        }
        reqParameters.put(parameterName, new RequestParameter(parameterNode, parameterType));
    }

    private void updateReqParameter(String parameterName, ParameterNode parameterNode, TypeSymbol parameterType,
                                    boolean fromTargetResource) throws InterceptorMapperException {
        TypeSymbol existingType = reqParameters.get(parameterName).parameterType();
        if (parameterType.subtypeOf(existingType)) {
            reqParameters.put(parameterName, new RequestParameter(parameterNode, parameterType));
        } else if (!existingType.subtypeOf(parameterType)) {
            String expMessage = "incompatible types found for the parameter: " + parameterName;
            if (fromTargetResource) {
                reqParameters.put(parameterName, new RequestParameter(parameterNode, parameterType));
                expMessage += ". Defaulting to the type from the target resource";
            }
            throw new InterceptorMapperException(expMessage);
        }
    }

    private ParameterSymbol getParameterSymbol(ParameterNode parameterNode) {
        Symbol symbol = semanticModel.symbol(parameterNode).orElse(null);
        if (symbol instanceof ParameterSymbol parameterSymbol) {
            return parameterSymbol;
        }
        return null;
    }

    private String getParameterName(ParameterSymbol parameterNode) {
        return parameterNode.getName().map(MapperCommonUtils::unescapeIdentifier).orElse(null);
    }

    public Iterable<ParameterNode> getReqParameterNodes() {
        return reqParameters.values().stream()
                .map(RequestParameter::parameterNode)
                .collect(Collectors.toList());
    }
}
