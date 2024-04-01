package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;

import java.util.List;
import java.util.Optional;

public interface ParameterGenerator {
    //type handler attribute
    Optional<ParameterNode> generateParameterNode(boolean treatDefaultableAsRequired);
    List<ClientDiagnostic> getDiagnostics();

}
