package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.exception.FunctionSignatureGeneratorException;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.List;

public interface FunctionSignatureGenerator {
    FunctionSignatureNode generateFunctionSignature() throws FunctionSignatureGeneratorException;
    List<ClientDiagnostic> getDiagnostics();
}
