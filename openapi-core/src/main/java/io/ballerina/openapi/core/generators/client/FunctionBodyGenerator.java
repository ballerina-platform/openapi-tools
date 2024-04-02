package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;

import java.util.List;
import java.util.Optional;

public interface FunctionBodyGenerator {

    Optional<FunctionBodyNode> getFunctionBodyNode();
    List<ClientDiagnostic> getDiagnostics();
}
