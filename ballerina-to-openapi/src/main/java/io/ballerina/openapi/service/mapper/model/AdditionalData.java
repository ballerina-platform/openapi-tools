package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;

import java.util.List;

public record AdditionalData(SemanticModel semanticModel,
                             ModuleMemberVisitor moduleMemberVisitor,
                             List<OpenAPIMapperDiagnostic> diagnostics) {
}
