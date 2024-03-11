package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.parameter.ParameterGenerator;

import java.util.List;

public class HeaderParameterGenerator implements ParameterGenerator {
    @Override
    public ParameterNode generateParameter() {
        return null;
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return null;
    }
}
