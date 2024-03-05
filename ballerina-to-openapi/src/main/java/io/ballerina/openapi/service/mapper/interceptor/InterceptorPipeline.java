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
package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.interceptor.Interceptor.InterceptorType;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.service.mapper.Constants.BALLERINA;
import static io.ballerina.openapi.service.mapper.Constants.EMPTY;
import static io.ballerina.openapi.service.mapper.Constants.HTTP;
import static io.ballerina.openapi.service.mapper.Constants.INTERCEPTOR;

/**
 * This {@link InterceptorPipeline} class represents the interceptor pipeline. This class is responsible for building
 * the interceptor pipeline and extracting the effective return types from the pipeline.
 *
 * @since 1.9.0
 */
public class InterceptorPipeline {

    private Interceptor initReqInterceptor = null;
    private Interceptor initResInterceptor = null;
    private final SemanticModel semanticModel;

    private InterceptorPipeline(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public static InterceptorPipeline build(ServiceDeclarationNode serviceDefinition, AdditionalData additionalData) {
        List<Interceptor> interceptors = buildInterceptors(serviceDefinition, additionalData);

        if (!interceptors.isEmpty()) {
            InterceptorPipeline pipeline = new InterceptorPipeline(additionalData.semanticModel());
            Interceptor prevInterceptor = interceptors.get(0);
            if (prevInterceptor.getType().equals(InterceptorType.REQUEST)) {
                pipeline.initReqInterceptor = prevInterceptor;
            } else if (prevInterceptor instanceof ResponseInterceptor) {
                pipeline.initResInterceptor = prevInterceptor;
            }
            for (int i = 1; i < interceptors.size(); i++) {
                prevInterceptor.setNextInReqPath(interceptors.get(i));
                interceptors.get(i).setNextInResPath(prevInterceptor);
                prevInterceptor = interceptors.get(i);
                if (prevInterceptor.getType().equals(InterceptorType.REQUEST) &&
                        Objects.isNull(pipeline.initReqInterceptor)) {
                    pipeline.initReqInterceptor = prevInterceptor;
                } else if (prevInterceptor instanceof ResponseInterceptor) {
                    pipeline.initResInterceptor = prevInterceptor;
                }
            }
            return pipeline;
        }
        return null;
    }

    private static List<Interceptor> buildInterceptors(ServiceDeclarationNode serviceDefinition,
                                                       AdditionalData additionalData) {
        SemanticModel semanticModel = additionalData.semanticModel();
        ModuleMemberVisitor moduleMemberVisitor = additionalData.moduleMemberVisitor();
        Optional<Symbol> optServiceSymbol = semanticModel.symbol(serviceDefinition);
        if (optServiceSymbol.isEmpty() ||
                !(optServiceSymbol.get() instanceof ServiceDeclarationSymbol serviceSymbol)) {
            return new ArrayList<>();
        }

        Optional<TypeSymbol> optInterceptorReturn = serviceSymbol.methods().get("createInterceptors").
                typeDescriptor().returnTypeDescriptor();
        if (optInterceptorReturn.isEmpty()) {
            return new ArrayList<>();
        }

        TypeSymbol interceptorReturn = optInterceptorReturn.get();
        try {
            if (interceptorReturn instanceof TypeReferenceTypeSymbol interceptorType &&
                    isSubTypeOf(interceptorReturn, INTERCEPTOR, semanticModel)) {
                Interceptor.InterceptorType type = getInterceptorType(interceptorType, semanticModel);
                Interceptor interceptor = getInterceptor(interceptorType, type, semanticModel, moduleMemberVisitor);
                return List.of(interceptor);
            } else if (interceptorReturn instanceof TupleTypeSymbol interceptorTupleType) {
                return getInterceptorListFromReturnType(interceptorTupleType, semanticModel, moduleMemberVisitor);
            }
        } catch (InterceptorMapperException e) {
            addWarningDiagnostic(additionalData, e.getMessage());
            return new ArrayList<>();
        }

        String cause = "the return type of `createInterceptors` function should be defined as a tuple with specific" +
                " interceptor types as members. For example: `[ResponseInterceptor_, RequestInterceptor_, " +
                "RequestErrorInterceptor_]`";
        addWarningDiagnostic(additionalData, cause);
        return new ArrayList<>();
    }

    private static void addWarningDiagnostic(AdditionalData additionalData, String cause) {
        ExceptionDiagnostic error = new ExceptionDiagnostic(DiagnosticMessages.OAS_CONVERTOR_126, cause);
        additionalData.diagnostics().add(error);
    }

    private static Interceptor getInterceptor(TypeReferenceTypeSymbol interceptorType, InterceptorType type,
                                              SemanticModel semanticModel, ModuleMemberVisitor moduleMemberVisitor)
            throws InterceptorMapperException {
        return switch (Objects.requireNonNull(type)) {
            case REQUEST -> new RequestInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
            case REQUEST_ERROR -> new RequestErrorInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
            case RESPONSE -> new ResponseInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
            default -> // RESPONSE_ERROR
                    new ResponseErrorInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
        };
    }

    private static List<Interceptor> getInterceptorListFromReturnType(TupleTypeSymbol interceptorTupleType,
                                                                      SemanticModel semanticModel,
                                                                      ModuleMemberVisitor moduleMemberVisitor)
            throws InterceptorMapperException {
        List<Interceptor> interceptors = new ArrayList<>();
        for (TypeSymbol typeDescriptor : interceptorTupleType.memberTypeDescriptors()) {
            if (typeDescriptor instanceof TypeReferenceTypeSymbol interceptorType) {
                InterceptorType type = getInterceptorType(interceptorType, semanticModel);
                Interceptor interceptor = getInterceptor(interceptorType, type, semanticModel,
                        moduleMemberVisitor);
                interceptors.add(interceptor);
            }
        }
        return interceptors;
    }

    private static InterceptorType getInterceptorType(TypeSymbol interceptorType,
                                                                  SemanticModel semanticModel) {
        if (isSubTypeOf(interceptorType, "RequestInterceptor", semanticModel)) {
            return InterceptorType.REQUEST;
        } else if (isSubTypeOf(interceptorType, "RequestErrorInterceptor", semanticModel)) {
            return InterceptorType.REQUEST_ERROR;
        } else if (isSubTypeOf(interceptorType, "ResponseInterceptor", semanticModel)) {
            return InterceptorType.RESPONSE;
        } else { // ResponseErrorInterceptor
            return InterceptorType.RESPONSE_ERROR;
        }
    }

    private static boolean isSubTypeOf(TypeSymbol typeSymbol, String typeName, SemanticModel semanticModel) {
        Optional<Symbol> optType = semanticModel.types().getTypeByName(BALLERINA, HTTP, EMPTY, typeName);
        if (optType.isEmpty() || !(optType.get() instanceof TypeDefinitionSymbol typeDef)) {
            return false;
        }
        return typeSymbol.subtypeOf(typeDef.typeDescriptor());
    }

    public ReturnTypes getEffectiveReturnType(ResourceMethodSymbol targetResource) {
        return getReturnTypes(targetResource);
    }

    private ReturnTypes getReturnTypes(ResourceMethodSymbol targetResource) {
        ReturnTypes returnTypes = new ReturnTypes(new HashSet<>(), new HashSet<>());
        if (Objects.isNull(initReqInterceptor)) {
            updateReturnTypeForTarget(returnTypes, targetResource);
        } else {
            updateReturnTypeForReqInterceptor(initReqInterceptor, returnTypes, targetResource);
        }
        return returnTypes;
    }

    private void updateReturnTypeForReqInterceptor(Interceptor interceptor, ReturnTypes returnTypes,
                                                   ResourceMethodSymbol targetResource) {
        if (Objects.isNull(interceptor)) {
            updateReturnTypeForTarget(returnTypes, targetResource);
            return;
        }

        if (interceptor.isContinueExecution() || !interceptor.isInvokable(targetResource)) {
            updateReturnTypeForReqInterceptor(interceptor.getNextInReqPath(), returnTypes, targetResource);
            if (!interceptor.isInvokable(targetResource)) {
                return;
            }
        }

        if (interceptor.hasErrorReturn()) {
            Interceptor nextErrorInterceptor = interceptor.getNextInReqErrorPath();
            if (Objects.nonNull(nextErrorInterceptor) && nextErrorInterceptor.isInvokable(targetResource)) {
                updateReturnTypeForReqInterceptor(nextErrorInterceptor, returnTypes, targetResource);
            } else {
                nextErrorInterceptor = null;
                if (Objects.nonNull(initResInterceptor)) {
                    if (initResInterceptor.getType().equals(InterceptorType.RESPONSE_ERROR)) {
                        nextErrorInterceptor = initResInterceptor;
                    } else {
                        nextErrorInterceptor = initResInterceptor.getNextInResErrorPath();
                    }
                }
                updateErrorReturnTypeForInterceptor(interceptor, returnTypes, nextErrorInterceptor);
            }
        }

        TypeSymbol nonErrorReturnType = interceptor.getNonErrorReturnType();
        updateNonErrorReturnTypeForInterceptor(interceptor, nonErrorReturnType, returnTypes, false);
    }

    private void updateErrorReturnTypeForInterceptor(Interceptor interceptor, ReturnTypes returnTypes,
                                                     Interceptor nextErrorInterceptor) {
        if (Objects.nonNull(nextErrorInterceptor)) {
            updateReturnTypeForResInterceptor(nextErrorInterceptor, returnTypes, null, false);
        } else {
            returnTypes.fromInterceptors().add(interceptor.getErrorReturnType());
        }
    }

    private void updateNonErrorReturnTypeForInterceptor(Interceptor interceptor, TypeSymbol nonErrorReturnType,
                                                        ReturnTypes returnTypes, boolean fromTarget) {
        if (Objects.nonNull(nonErrorReturnType)) {
            Interceptor nextResInterceptor = interceptor.getNextInResPath();
            if (Objects.nonNull(nextResInterceptor)) {
                updateReturnTypeForResInterceptor(nextResInterceptor, returnTypes, nonErrorReturnType, fromTarget);
            } else {
                returnTypes.fromInterceptors().add(nonErrorReturnType);
            }
        }
    }

    private void updateReturnTypeForResInterceptor(Interceptor interceptor, ReturnTypes returnTypes,
                                                   TypeSymbol prevReturnType, boolean fromTarget) {
        if (Objects.isNull(interceptor)) {
            if (Objects.nonNull(prevReturnType)) {
                if (fromTarget) {
                    returnTypes.fromTargetResource().add(prevReturnType);
                } else {
                    returnTypes.fromInterceptors().add(prevReturnType);
                }
            }
            return;
        }

        if (interceptor.isContinueExecution() && Objects.nonNull(prevReturnType)) {
            updateNonErrorReturnTypeForInterceptor(interceptor, prevReturnType, returnTypes, fromTarget);
        }

        if (interceptor.hasErrorReturn()) {
            Interceptor nextErrorInterceptor = interceptor.getNextInResErrorPath();
            updateErrorReturnTypeForInterceptor(interceptor, returnTypes, nextErrorInterceptor);
        }

        TypeSymbol nonErrorReturnType = interceptor.getNonErrorReturnType();
        updateNonErrorReturnTypeForInterceptor(interceptor, nonErrorReturnType, returnTypes, false);
    }

    private void updateReturnTypeForTarget(ReturnTypes returnTypes, ResourceMethodSymbol targetResource) {
        TargetResource target = new TargetResource(targetResource, semanticModel);
        TypeSymbol returnType = target.getEffectiveReturnType();
        if (Objects.isNull(initResInterceptor)) {
            returnTypes.fromTargetResource().add(returnType);
            return;
        }
        if (target.hasErrorReturn()) {
            updateErrorReturnTypeForTarget(returnTypes, target);
        }
        TypeSymbol nonErrorReturnType = target.getNonErrorReturnType();
        if (Objects.nonNull(nonErrorReturnType)) {
            updateNonErrorReturnTypeForTarget(returnTypes, nonErrorReturnType);
        }
    }

    private void updateNonErrorReturnTypeForTarget(ReturnTypes returnTypes, TypeSymbol nonErrorReturnType) {
        if (initResInterceptor.getType().equals(InterceptorType.RESPONSE)) {
            updateReturnTypeForResInterceptor(initResInterceptor, returnTypes, nonErrorReturnType, true);
        } else {
            Interceptor nextResponseErrorInterceptor = initResInterceptor.getNextInResPath();
            if (Objects.nonNull(nextResponseErrorInterceptor)) {
                updateReturnTypeForResInterceptor(nextResponseErrorInterceptor, returnTypes,
                        nonErrorReturnType, true);
            } else {
                returnTypes.fromTargetResource().add(nonErrorReturnType);
            }
        }
    }

    private void updateErrorReturnTypeForTarget(ReturnTypes returnTypes, TargetResource target) {
        if (initResInterceptor.getType().equals(InterceptorType.RESPONSE_ERROR)) {
            updateReturnTypeForResInterceptor(initResInterceptor, returnTypes, null, true);
        } else {
            Interceptor nextResponseErrorInterceptor = initResInterceptor.getNextInResErrorPath();
            if (Objects.nonNull(nextResponseErrorInterceptor)) {
                updateReturnTypeForResInterceptor(nextResponseErrorInterceptor, returnTypes, null, true);
            } else {
                returnTypes.fromTargetResource().add(target.getErrorReturnType());
            }
        }
    }
}
