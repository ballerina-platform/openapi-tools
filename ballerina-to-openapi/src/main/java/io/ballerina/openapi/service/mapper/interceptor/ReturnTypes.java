package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;

import java.util.Objects;
import java.util.Set;

public record ReturnTypes(TypeSymbol fromInterceptors, TypeSymbol fromTargetResource) {

    public static ReturnTypes create(ReturnTypeLists returnTypeLists, SemanticModel semanticModel) {
        TypeSymbol fromInterceptors = buildTypeSymbolFromList(returnTypeLists.fromInterceptors(), semanticModel);
        TypeSymbol fromTargetResource = buildTypeSymbolFromList(returnTypeLists.fromTargetResource(), semanticModel);
        return new ReturnTypes(fromInterceptors, fromTargetResource);
    }

    private static TypeSymbol buildTypeSymbolFromList(Set<TypeSymbol> typeSymbols, SemanticModel semanticModel) {
        if (Objects.isNull(typeSymbols) || typeSymbols.isEmpty()) {
            return null;
        }
        return semanticModel.types().builder().UNION_TYPE.withMemberTypes(
                typeSymbols.toArray(TypeSymbol[]::new)).build();
    }
}
