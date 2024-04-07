package io.ballerina.openapi.core.service;

import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

public interface ListenerGenerator {

    int HTTP_PORT = 80;
    int HTTPS_PORT = 443;

    String getBasePath();

    ListenerDeclarationNode getListenerDeclarationNodes(List<Server> servers) throws BallerinaOpenApiException;
}
