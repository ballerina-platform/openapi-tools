package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.List;
import java.util.Optional;

public interface ServerURLGenerator {
    ParameterNode generateServerURL();
    List<Diagnostic> getDiagnostics();
}
