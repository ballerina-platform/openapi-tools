package io.ballerina.openapi.core.generators.client;

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.model.tree.FunctionNode;

import java.util.List;

public interface FunctionGenerator {
    FunctionNode generateFunction();
    List<Diagnostic> getDiagnostics();
}
