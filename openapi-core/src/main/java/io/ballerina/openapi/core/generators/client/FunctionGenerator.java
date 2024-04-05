package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.util.List;
import java.util.Optional;

public interface FunctionGenerator {
    Optional<FunctionDefinitionNode> generateFunction() throws BallerinaOpenApiException;
    List<Diagnostic> getDiagnostics();
}
