package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;

import java.util.Map;
import java.util.Optional;

public class RequestInterceptor extends Interceptor {

    public RequestInterceptor(TypeReferenceTypeSymbol typeSymbol, SemanticModel semanticModel,
                              ModuleMemberVisitor moduleMemberVisitor) {
        super(typeSymbol, semanticModel, moduleMemberVisitor);
    }

    @Override
    protected void extractInterceptorDetails(SemanticModel semanticModel) {
        Map<String, MethodSymbol> serviceMethods = serviceClass.methods();
        Optional<ResourceMethodSymbol> resourceMethodOpt = serviceMethods.values().stream()
                .filter(methodSymbol -> methodSymbol instanceof ResourceMethodSymbol)
                .map(methodSymbol -> (ResourceMethodSymbol) methodSymbol)
                .findFirst();
        if (resourceMethodOpt.isEmpty()) {
            return;
        }
        ResourceMethodSymbol resourceMethod = resourceMethodOpt.get();
        Optional<TypeSymbol> optReturnType = resourceMethod.typeDescriptor().returnTypeDescriptor();
        this.resourceMethod = resourceMethod;
        optReturnType.ifPresent(this::setReturnType);
    }

    @Override
    public InterceptorType getType() {
        return InterceptorType.REQUEST;
    }

    @Override
    public boolean isNotInvokable(ResourceMethodSymbol targetResource) {
        return !ResourceMatcher.match(resourceMethod, targetResource, semanticModel);
    }
}
