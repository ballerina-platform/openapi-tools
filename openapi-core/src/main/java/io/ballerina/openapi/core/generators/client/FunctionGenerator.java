package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;

import java.util.List;
import java.util.Optional;

public interface FunctionGenerator {
    Optional<FunctionDefinitionNode> generateFunction() throws BallerinaOpenApiException;
    List<ClientDiagnostic> getDiagnostics();
}
