package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.model.tree.FunctionNode;

import java.util.List;
import java.util.Optional;

public interface FunctionGenerator {
    Optional<FunctionDefinitionNode> generateFunction();
    List<Diagnostic> getDiagnostics();
}
