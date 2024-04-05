package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.parameters.RequestBody;

public abstract class RequestBodyGenerator {

    final OASServiceMetadata oasServiceMetadata;
    final String path;

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

    public abstract RequiredParameterNode createRequestBodyNode(RequestBody requestBody) throws OASTypeGenException;
}
