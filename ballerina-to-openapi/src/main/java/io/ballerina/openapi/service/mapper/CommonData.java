package io.ballerina.openapi.service.mapper;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.openapi.service.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.model.ModuleMemberVisitor;

import java.util.List;

public record CommonData(SemanticModel semanticModel,
                         ModuleMemberVisitor moduleMemberVisitor,
                         List<OpenAPIMapperDiagnostic> diagnostics) {
}
