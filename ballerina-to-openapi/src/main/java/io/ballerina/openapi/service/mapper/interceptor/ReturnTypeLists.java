package io.ballerina.openapi.service.mapper.interceptor;

import io.ballerina.compiler.api.symbols.TypeSymbol;

import java.util.Set;

public record ReturnTypeLists(Set<TypeSymbol> fromInterceptors, Set<TypeSymbol> fromTargetResource) {
}
