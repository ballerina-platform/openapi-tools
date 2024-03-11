package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.List;

public interface FunctionSignatureGenerator {
    FunctionSignatureNode generateFunctionSignature();
    List<Diagnostic> getDiagnostics();
}
