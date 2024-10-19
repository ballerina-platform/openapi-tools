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
package io.ballerina.openapi.service.mapper.interceptor.pipeline;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.interceptor.model.RequestParameterInfo;
import io.ballerina.openapi.service.mapper.interceptor.types.Interceptor;
import io.ballerina.openapi.service.mapper.interceptor.types.TargetResource;

import java.util.List;
import java.util.Objects;

/**
 * This {@link RequestParameterCollector} class provides functionality to collect the request parameter information
 * from the interceptors.
 *
 * @since 1.9.0
 */
public final class RequestParameterCollector {

    private RequestParameterCollector() {}

    public static RequestParameterInfo getRequestParameterInfo(InterceptorPipeline pipeline,
                                                               ResourceMethodSymbol targetResource) {
        SemanticModel semanticModel = pipeline.getSemanticModel();
        Interceptor initReqInterceptor = pipeline.getInitReqInterceptor();
        List<OpenAPIMapperDiagnostic> diagnostics = pipeline.getDiagnostics();

        RequestParameterInfo requestParameterInfoFromInterceptors = new RequestParameterInfo(semanticModel);
        TargetResource target = new TargetResource(targetResource, semanticModel);
        if (Objects.isNull(initReqInterceptor)) {
            return requestParameterInfoFromInterceptors;
        } else {
            processReqInterceptor(initReqInterceptor, requestParameterInfoFromInterceptors, target, diagnostics);
        }
        return requestParameterInfoFromInterceptors;
    }

    private static void processReqInterceptor(Interceptor interceptor, RequestParameterInfo requestParameterInfo,
                                              TargetResource targetResource,
                                              List<OpenAPIMapperDiagnostic> diagnostics) {
        if (Objects.isNull(interceptor)) {
            return;
        }

        if (interceptor.isContinueExecution() || !interceptor.isInvokable(targetResource)) {
            processReqInterceptor(interceptor.getNextInReqPath(), requestParameterInfo, targetResource, diagnostics);
            if (!interceptor.isInvokable(targetResource)) {
                return;
            }
        }

        requestParameterInfo.addReqParameters(interceptor.getParameterNodes(), diagnostics, false);

        if (interceptor.hasErrorReturn()) {
            updateErrorReturnTypeInReqPath(interceptor, requestParameterInfo, targetResource, diagnostics);
        }
    }

    private static void updateErrorReturnTypeInReqPath(Interceptor interceptor, RequestParameterInfo responseInfo,
                                                       TargetResource targetResource,
                                                       List<OpenAPIMapperDiagnostic> diagnostics) {
        Interceptor nextErrorInterceptor = interceptor.getNextInReqErrorPath();
        if (Objects.nonNull(nextErrorInterceptor) &&
                nextErrorInterceptor.isInvokable(targetResource)) {
            processReqInterceptor(nextErrorInterceptor, responseInfo, targetResource, diagnostics);
        }
    }
}
