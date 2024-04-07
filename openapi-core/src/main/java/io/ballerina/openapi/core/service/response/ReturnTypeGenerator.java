package io.ballerina.openapi.core.service.response;

import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ReturnTypeGenerator {
    final OASServiceMetadata oasServiceMetadata;
    final String path;
    final List<Diagnostic> diagnostics = new ArrayList<>();

    public ReturnTypeGenerator(OASServiceMetadata oasServiceMetadata, String path) {
        this.oasServiceMetadata = oasServiceMetadata;
        this.path = path;
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public static ReturnTypeGenerator getReturnTypeGenerator(OASServiceMetadata oasServiceMetadata, String path) {
        if (oasServiceMetadata.generateWithoutDataBinding()) {
            return new LowResourceReturnTypeGenerator(oasServiceMetadata, path);
        } else {
            return new DefaultReturnTypeGenerator(oasServiceMetadata, path);
        }
    }

    public abstract ReturnTypeDescriptorNode getReturnTypeDescriptorNode(Map.Entry<PathItem.HttpMethod,
            Operation> operation, String path) throws BallerinaOpenApiException;
}
