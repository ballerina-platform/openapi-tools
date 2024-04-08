package io.ballerina.openapi.core.generators.service.parameter;

import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;

public abstract class RequestBodyGenerator {

    final OASServiceMetadata oasServiceMetadata;
    final String path;
    final List<Diagnostic> diagnostics = new ArrayList<>();

    RequestBodyGenerator (OASServiceMetadata oasServiceMetadata, String path) {
        this.oasServiceMetadata = oasServiceMetadata;
        this.path = path;
    }

    public static RequestBodyGenerator getRequestBodyGenerator(OASServiceMetadata oasServiceMetadata, String path) {
        if (oasServiceMetadata.generateWithoutDataBinding()) {
            return new LowResourceRequestBodyGenerator(oasServiceMetadata, path);
        }
        return new DefaultRequestBodyGenerator(oasServiceMetadata, path);
    }

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }

    public abstract RequiredParameterNode createRequestBodyNode(RequestBody requestBody);
}
