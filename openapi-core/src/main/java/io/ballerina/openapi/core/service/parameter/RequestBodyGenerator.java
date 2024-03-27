package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;

public interface RequestBodyGenerator {

    RequiredParameterNode createRequestBodyNode() throws OASTypeGenException;
}
