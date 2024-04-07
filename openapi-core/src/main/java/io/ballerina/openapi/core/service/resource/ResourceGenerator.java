package io.ballerina.openapi.core.service.resource;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.service.diagnostic.ServiceDiagnostic;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ResourceGenerator {

    final OASServiceMetadata oasServiceMetadata;
    boolean isNullableRequired;
    final List<ServiceDiagnostic> diagnostics = new ArrayList<>();

    protected ResourceGenerator(OASServiceMetadata oasServiceMetadata) {
        this.oasServiceMetadata = oasServiceMetadata;
    }

    public boolean isNullableRequired() {
        return isNullableRequired;
    }

    public static ResourceGenerator createResourceGenerator(OASServiceMetadata oasServiceMetadata) {
        if (oasServiceMetadata.generateWithoutDataBinding()) {
            return new LowResourceGenerator(oasServiceMetadata);
        }
        return new DefaultResourceGenerator(oasServiceMetadata);
    }

    public abstract FunctionDefinitionNode generateResourceFunction(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                                    String path) throws BallerinaOpenApiException;

    public List<ServiceDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
