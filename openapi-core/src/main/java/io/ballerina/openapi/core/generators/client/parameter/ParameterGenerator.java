package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;

import java.util.List;

public interface ParameterGenerator {
    ParameterNode generateParameter();
    List<ClientDiagnostic> getDiagnostics();

}
