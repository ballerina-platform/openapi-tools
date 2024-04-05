package io.ballerina.openapi.core.generators.document;

import io.swagger.v3.oas.models.Operation;

public record OperationDetails(String operationId, Operation operation, String path, String method) {
}
