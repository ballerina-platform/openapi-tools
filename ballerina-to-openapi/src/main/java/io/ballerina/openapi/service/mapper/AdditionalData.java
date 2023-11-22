package io.ballerina.openapi.service.mapper;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;

import java.util.List;

public record AdditionalData(SemanticModel semanticModel,
                             ModuleMemberVisitor moduleMemberVisitor,
                             List<OpenAPIMapperDiagnostic> diagnostics) {
}
