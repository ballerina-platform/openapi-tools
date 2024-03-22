package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.parameter.ParameterGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;
import java.util.Optional;

public class HeaderParameterGenerator implements ParameterGenerator {
    OpenAPI openAPI;
    Parameter parameter;

    List<ClientDiagnostic> diagnostics;

    public HeaderParameterGenerator(Parameter parameter, OpenAPI openAPI) {
        this.parameter = parameter;
        this.openAPI = openAPI;
    }

    @Override
    public Optional<ParameterNode> generateParameterNode() {
        return null;
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }

}
