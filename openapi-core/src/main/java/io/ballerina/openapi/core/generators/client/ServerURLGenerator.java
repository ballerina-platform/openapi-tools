package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;

import java.util.List;

public interface ServerURLGenerator {
    ParameterNode generateServerURL();
    List<ClientDiagnostic> getDiagnostics();
}
