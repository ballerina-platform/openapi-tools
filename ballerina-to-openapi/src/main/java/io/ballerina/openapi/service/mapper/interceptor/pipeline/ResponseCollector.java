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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.interceptor.model.ResponseInfo;
import io.ballerina.openapi.service.mapper.interceptor.types.Interceptor;
import io.ballerina.openapi.service.mapper.interceptor.types.TargetResource;

import java.util.Objects;

/**
 * This {@link ResponseCollector} class provides functionality to collect the response information from the
 * interceptors.
 *
 * @since 1.9.0
 */
public final class ResponseCollector {

    private ResponseCollector() {}

    public static ResponseInfo getResponseInfo(InterceptorPipeline pipeline, ResourceMethodSymbol targetResource) {
        SemanticModel semanticModel = pipeline.getSemanticModel();
        Interceptor initReqInterceptor = pipeline.getInitReqInterceptor();
        Interceptor initResInterceptor = pipeline.getInitResInterceptor();

        TargetResource target = new TargetResource(targetResource, semanticModel);
        ResponseInfo responseInfoFromInterceptors = new ResponseInfo(target.hasDataBinding());
        if (Objects.isNull(initReqInterceptor)) {
            processTargetResource(responseInfoFromInterceptors, target, initResInterceptor);
        } else {
            processReqInterceptor(initReqInterceptor, responseInfoFromInterceptors, target, initResInterceptor);
        }
        return responseInfoFromInterceptors;
    }

    private static void processReqInterceptor(Interceptor interceptor, ResponseInfo responseInfo,
                                              TargetResource targetResource, Interceptor initResInterceptor) {
        if (Objects.isNull(interceptor)) {
            processTargetResource(responseInfo, targetResource, initResInterceptor);
            return;
        }

        if (interceptor.isContinueExecution() || !interceptor.isInvokable(targetResource)) {
            processReqInterceptor(interceptor.getNextInReqPath(), responseInfo, targetResource,
                    initResInterceptor);
            if (!interceptor.isInvokable(targetResource)) {
                return;
            }
        }

        if (interceptor.hasErrorReturn() || interceptor.hasDataBinding()) {
            if (interceptor.hasDataBinding()) {
                responseInfo.markDataBindingError();
            }
            updateErrorReturnTypeInReqPath(interceptor, responseInfo, targetResource, initResInterceptor);
        }

        TypeSymbol nonErrorReturnType = interceptor.getNonErrorReturnType();
        updateNonErrorReturnTypeForInterceptor(interceptor, nonErrorReturnType, responseInfo, false);
    }

    private static void updateErrorReturnTypeInReqPath(Interceptor interceptor, ResponseInfo responseInfo,
                                                       TargetResource targetResource, Interceptor initResInterceptor) {
        Interceptor nextErrorInterceptor = interceptor.getNextInReqErrorPath();
        if (Objects.nonNull(nextErrorInterceptor) &&
                nextErrorInterceptor.isInvokable(targetResource)) {
            processReqInterceptor(nextErrorInterceptor, responseInfo, targetResource, initResInterceptor);
        } else {
            nextErrorInterceptor = getNextErrorInterceptor(initResInterceptor);
            updateErrorReturnTypeForInterceptor(interceptor, responseInfo, nextErrorInterceptor);
        }
    }

    private static Interceptor getNextErrorInterceptor(Interceptor initResInterceptor) {
        Interceptor nextErrorInterceptor = null;
        if (Objects.nonNull(initResInterceptor)) {
            if (initResInterceptor.getType().equals(Interceptor.InterceptorType.RESPONSE_ERROR)) {
                nextErrorInterceptor = initResInterceptor;
            } else {
                nextErrorInterceptor = initResInterceptor.getNextInResErrorPath();
            }
        }
        return nextErrorInterceptor;
    }

    private static void updateErrorReturnTypeForInterceptor(Interceptor interceptor, ResponseInfo responseInfo,
                                                            Interceptor nextErrorInterceptor) {
        if (Objects.nonNull(nextErrorInterceptor)) {
            processResInterceptor(nextErrorInterceptor, responseInfo, null, false);
        } else {
            responseInfo.addReturnTypeFromInterceptors(interceptor.getErrorReturnType());
        }
    }

    private static void updateNonErrorReturnTypeForInterceptor(Interceptor interceptor, TypeSymbol nonErrorReturnType,
                                                               ResponseInfo responseInfo, boolean fromTarget) {
        if (Objects.nonNull(nonErrorReturnType)) {
            Interceptor nextResInterceptor = interceptor.getNextInResPath();
            if (Objects.nonNull(nextResInterceptor)) {
                processResInterceptor(nextResInterceptor, responseInfo, nonErrorReturnType, fromTarget);
            } else {
                updateReturnType(responseInfo, nonErrorReturnType, fromTarget);
            }
        }
    }

    private static void processResInterceptor(Interceptor interceptor, ResponseInfo responseInfo,
                                              TypeSymbol prevReturnType, boolean fromTarget) {
        if (interceptor.isContinueExecution() && Objects.nonNull(prevReturnType)) {
            updateNonErrorReturnTypeForInterceptor(interceptor, prevReturnType, responseInfo, fromTarget);
        }

        if (interceptor.hasErrorReturn()) {
            Interceptor nextErrorInterceptor = interceptor.getNextInResErrorPath();
            updateErrorReturnTypeForInterceptor(interceptor, responseInfo, nextErrorInterceptor);
        }

        TypeSymbol nonErrorReturnType = interceptor.getNonErrorReturnType();
        updateNonErrorReturnTypeForInterceptor(interceptor, nonErrorReturnType, responseInfo, false);
    }

    private static void updateReturnType(ResponseInfo responseInfo, TypeSymbol prevReturnType,
                                         boolean fromTarget) {
        if (fromTarget) {
            responseInfo.addReturnTypeFromTargetResource(prevReturnType);
        } else {
            responseInfo.addReturnTypeFromInterceptors(prevReturnType);
        }
    }

    private static void processTargetResource(ResponseInfo responseInfo, TargetResource targetResource,
                                              Interceptor initResInterceptor) {
        TypeSymbol returnType = targetResource.getEffectiveReturnType();
        if (Objects.isNull(initResInterceptor)) {
            responseInfo.addReturnTypeFromTargetResource(returnType);
            return;
        }
        if (targetResource.hasErrorReturn() || targetResource.hasDataBinding()) {
            if (targetResource.hasDataBinding()) {
                responseInfo.markDataBindingError();
            }
            updateErrorReturnTypeForTarget(responseInfo, targetResource, initResInterceptor);
        }
        TypeSymbol nonErrorReturnType = targetResource.getNonErrorReturnType();
        if (Objects.nonNull(nonErrorReturnType)) {
            updateNonErrorReturnTypeForTarget(responseInfo, nonErrorReturnType, initResInterceptor);
        }
    }

    private static void updateNonErrorReturnTypeForTarget(ResponseInfo responseInfo, TypeSymbol nonErrorReturnType,
                                                          Interceptor initResInterceptor) {
        if (initResInterceptor.getType().equals(Interceptor.InterceptorType.RESPONSE)) {
            processResInterceptor(initResInterceptor, responseInfo, nonErrorReturnType, true);
        } else {
            Interceptor nextResponseErrorInterceptor = initResInterceptor.getNextInResPath();
            if (Objects.nonNull(nextResponseErrorInterceptor)) {
                processResInterceptor(nextResponseErrorInterceptor, responseInfo,
                        nonErrorReturnType, true);
            } else {
                responseInfo.addReturnTypeFromTargetResource(nonErrorReturnType);
            }
        }
    }

    private static void updateErrorReturnTypeForTarget(ResponseInfo responseInfo,
                                                       TargetResource target, Interceptor initResInterceptor) {
        if (initResInterceptor.getType().equals(Interceptor.InterceptorType.RESPONSE_ERROR)) {
            responseInfo.markDataBindingErrorHandled();
            processResInterceptor(initResInterceptor, responseInfo, null, true);
        } else {
            Interceptor nextResponseErrorInterceptor = initResInterceptor.getNextInResErrorPath();
            if (Objects.nonNull(nextResponseErrorInterceptor)) {
                responseInfo.markDataBindingErrorHandled();
                processResInterceptor(nextResponseErrorInterceptor, responseInfo, null, true);
            } else if (Objects.nonNull(target.getErrorReturnType())) {
                responseInfo.addReturnTypeFromTargetResource(target.getErrorReturnType());
            }
        }
    }
}
