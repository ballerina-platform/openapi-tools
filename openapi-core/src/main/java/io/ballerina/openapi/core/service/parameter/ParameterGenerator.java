package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.generators.common.exception.InvalidReferenceException;
import io.ballerina.openapi.core.generators.common.exception.UnsupportedOASDataTypeException;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;

public abstract class ParameterGenerator {
    final OpenAPI openAPI;
    boolean isNullableRequired;
    final List<Diagnostic> diagnostics = new ArrayList<>();

    public ParameterGenerator(OASServiceMetadata oasServiceMetadata) {
        this.openAPI = oasServiceMetadata.getOpenAPI();
        this.isNullableRequired = oasServiceMetadata.isNullable();
    }

    public abstract ParameterNode generateParameterNode(Parameter parameter) throws UnsupportedOASDataTypeException,
            InvalidReferenceException;

    public boolean isNullableRequired() {
        return isNullableRequired;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
