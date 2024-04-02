package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.exception.FunctionSignatureGeneratorException;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.List;
import java.util.Optional;

public interface FunctionSignatureGenerator {
    Optional<FunctionSignatureNode> generateFunctionSignature() throws FunctionSignatureGeneratorException;
    List<ClientDiagnostic> getDiagnostics();
}
