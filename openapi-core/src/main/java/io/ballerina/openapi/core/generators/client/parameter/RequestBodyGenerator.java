package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;

import java.util.List;
import java.util.Optional;

public class RequestBodyGenerator implements ParameterGenerator {
    @Override
    public Optional<ParameterNode> generateParameterNode() {
        return null;
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return null;
    }
}
