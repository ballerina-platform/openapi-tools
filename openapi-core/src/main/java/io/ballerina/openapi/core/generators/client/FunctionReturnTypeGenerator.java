package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;

import java.util.List;
import java.util.Optional;

public interface FunctionReturnTypeGenerator {

    Optional<ReturnTypeDescriptorNode> getReturnType();
    List<ClientDiagnostic> getDiagnostics();
//    getReturnType();
}
