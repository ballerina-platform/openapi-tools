package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.service.diagnostic.ServiceDiagnostic;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;

public abstract class ParameterGenerator {
    final OpenAPI openAPI;
    boolean isNullableRequired;
    final List<ServiceDiagnostic> diagnostics = new ArrayList<>();

    public ParameterGenerator(OASServiceMetadata oasServiceMetadata) {
        this.openAPI = oasServiceMetadata.getOpenAPI();
        this.isNullableRequired = oasServiceMetadata.isNullable();
    }

    public abstract ParameterNode generateParameterNode(Parameter parameter);

    public boolean isNullableRequired() {
        return isNullableRequired;
    }

    public List<ServiceDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
