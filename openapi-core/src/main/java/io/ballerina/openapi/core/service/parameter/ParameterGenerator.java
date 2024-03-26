package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;

public abstract class ParameterGenerator {

    OpenAPI openAPI;
    boolean isNullableRequired;

    abstract ParameterNode generateParameterNode(Map.Entry<PathItem.HttpMethod, Operation> operation);
}
