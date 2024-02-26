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
import io.ballerina.openapi.service.mapper.interceptor.Interceptor.InterceptorType;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InterceptorPipeline {

    private Interceptor initReqInterceptor = null;
    private Interceptor initResInterceptor = null;
    private final SemanticModel semanticModel;

    private InterceptorPipeline(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public static InterceptorPipeline build(ServiceDeclarationNode serviceDefinition, SemanticModel semanticModel,
                                            ModuleMemberVisitor moduleMemberVisitor) {
        List<Interceptor> interceptors = buildInterceptors(serviceDefinition, semanticModel, moduleMemberVisitor);

        InterceptorPipeline pipeline = new InterceptorPipeline(semanticModel);
        if (!interceptors.isEmpty()) {
            Interceptor prevInterceptor = interceptors.get(0);
            if (prevInterceptor.getType().equals(InterceptorType.REQUEST)) {
                pipeline.initReqInterceptor = prevInterceptor;
            }
            if (prevInterceptor instanceof ResponseInterceptor) {
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
        }
        return pipeline;
    }

    private static List<Interceptor> buildInterceptors(ServiceDeclarationNode serviceDefinition, SemanticModel semanticModel,
                                                       ModuleMemberVisitor moduleMemberVisitor) {
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
        if (!(optInterceptorReturn.get() instanceof TupleTypeSymbol interceptorTupleType)) {
            // Print a warning since OAS can not extract interceptors from a
            // common return type: `http:Interceptor|http:Interceptor[]`
            return new ArrayList<>();
        }

        List<Interceptor> interceptors = new ArrayList<>();
        interceptorTupleType.memberTypeDescriptors().forEach(typeDescriptor -> {
            if (typeDescriptor instanceof TypeReferenceTypeSymbol interceptorType) {
                Interceptor.InterceptorType type = getInterceptorType(interceptorType, semanticModel);
                Interceptor interceptor = switch (Objects.requireNonNull(type)) {
                    case REQUEST ->
                            new RequestInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
                    case REQUEST_ERROR ->
                            new RequestErrorInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
                    case RESPONSE ->
                            new ResponseInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
                    default -> // RESPONSE_ERROR
                            new ResponseErrorInterceptor(interceptorType, semanticModel, moduleMemberVisitor);
                };
                interceptors.add(interceptor);
            }
        });
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
        Optional<Symbol> optType = semanticModel.types().getTypeByName("ballerina", "http", "", typeName);
        if (optType.isEmpty() || !(optType.get() instanceof TypeDefinitionSymbol typeDef)) {
            return false;
        }
        return typeSymbol.subtypeOf(typeDef.typeDescriptor());
    }

    public ReturnTypes getEffectiveReturnType(ResourceMethodSymbol targetResource) {
        ReturnTypeLists returnTypeLists = getReturnTypes(targetResource);
        return ReturnTypes.create(returnTypeLists, semanticModel);
    }

    private ReturnTypeLists getReturnTypes(ResourceMethodSymbol targetResource) {
        ReturnTypeLists returnTypes = new ReturnTypeLists(new HashSet<>(), new HashSet<>());
        if (Objects.isNull(initReqInterceptor)) {
            updateReturnTypeForTarget(returnTypes, targetResource);
        } else {
            updateReturnTypeForReqInterceptor(initReqInterceptor, returnTypes, targetResource);
        }
        return returnTypes;
    }

    private void updateReturnTypeForReqInterceptor(Interceptor interceptor, ReturnTypeLists returnTypes,
                                                   ResourceMethodSymbol targetResource) {
        if (Objects.isNull(interceptor)) {
            updateReturnTypeForTarget(returnTypes, targetResource);
            return;
        }

        if (interceptor.isContinueExecution() || !interceptor.isInvokableFor(targetResource)) {
            updateReturnTypeForReqInterceptor(interceptor.getNextInReqPath(), returnTypes, targetResource);
            if (!interceptor.isInvokableFor(targetResource)) {
                return;
            }
        }

        if (interceptor.hasErrorReturn()) {
            Interceptor nextErrorInterceptor = interceptor.getNextInReqErrorPath();
            if (Objects.nonNull(nextErrorInterceptor)) {
                updateReturnTypeForReqInterceptor(nextErrorInterceptor, returnTypes, targetResource);
            } else {
                if (Objects.nonNull(initResInterceptor)) {
                    nextErrorInterceptor = initResInterceptor.getNextInResErrorPath();
                }
                if (Objects.nonNull(nextErrorInterceptor)) {
                    updateReturnTypeForResInterceptor(nextErrorInterceptor, returnTypes, null, false);
                } else {
                    returnTypes.fromInterceptors().add(interceptor.getErrorReturnType());
                }
            }
        }

        TypeSymbol nonErrorReturnType = interceptor.getNonErrorReturnType();
        if (Objects.nonNull(nonErrorReturnType)) {
            Interceptor nextResInterceptor = interceptor.getNextInResPath();
            if (Objects.nonNull(nextResInterceptor)) {
                updateReturnTypeForResInterceptor(nextResInterceptor, returnTypes, nonErrorReturnType, false);
            } else {
                returnTypes.fromInterceptors().add(nonErrorReturnType);
            }
        }
    }

    private void updateReturnTypeForResInterceptor(Interceptor interceptor, ReturnTypeLists returnTypes,
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

        if (interceptor.isContinueExecution()) {
            if (Objects.nonNull(prevReturnType)) {
                if (fromTarget) {
                    returnTypes.fromTargetResource().add(prevReturnType);
                } else {
                    returnTypes.fromInterceptors().add(prevReturnType);
                }
            }
        }

        if (interceptor.hasErrorReturn()) {
            Interceptor nextErrorInterceptor = interceptor.getNextInResErrorPath();
            if (Objects.nonNull(nextErrorInterceptor)) {
                updateReturnTypeForResInterceptor(nextErrorInterceptor, returnTypes, null, false);
            } else {
                returnTypes.fromInterceptors().add(interceptor.getErrorReturnType());
            }
        }

        TypeSymbol nonErrorReturnType = interceptor.getNonErrorReturnType();
        if (Objects.nonNull(nonErrorReturnType)) {
            Interceptor nextResInterceptor = interceptor.getNextInResPath();
            if (Objects.nonNull(nextResInterceptor)) {
                updateReturnTypeForResInterceptor(nextResInterceptor, returnTypes, nonErrorReturnType, false);
            } else {
                returnTypes.fromInterceptors().add(nonErrorReturnType);
            }
        }
    }

    private void updateReturnTypeForTarget(ReturnTypeLists returnTypes, ResourceMethodSymbol targetResource) {
        TargetResource target = new TargetResource(targetResource, semanticModel);
        TypeSymbol returnType = target.getEffectiveReturnType();
        if (Objects.isNull(initResInterceptor)) {
            returnTypes.fromTargetResource().add(returnType);
            return;
        }
        if (target.hasErrorReturn()) {
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
        TypeSymbol nonErrorReturnType = target.getNonErrorReturnType();
        if (Objects.nonNull(nonErrorReturnType)) {
            if (initResInterceptor.getType().equals(InterceptorType.RESPONSE)) {
                updateReturnTypeForResInterceptor(initResInterceptor, returnTypes, nonErrorReturnType, true);
            } else {
                Interceptor nextResponseErrorInterceptor = initResInterceptor.getNextInResPath();
                if (Objects.nonNull(nextResponseErrorInterceptor)) {
                    updateReturnTypeForResInterceptor(nextResponseErrorInterceptor, returnTypes, nonErrorReturnType, true);
                } else {
                    returnTypes.fromTargetResource().add(nonErrorReturnType);
                }
            }
        }
    }
}
