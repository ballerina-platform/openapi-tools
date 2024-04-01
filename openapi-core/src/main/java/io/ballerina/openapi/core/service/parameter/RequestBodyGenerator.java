package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.parameters.RequestBody;

public abstract class RequestBodyGenerator {

    OASServiceMetadata oasServiceMetadata;

    RequestBodyGenerator (OASServiceMetadata oasServiceMetadata) {
        this.oasServiceMetadata = oasServiceMetadata;
    }

    public static RequestBodyGenerator getRequestBodyGenerator(OASServiceMetadata oasServiceMetadata) {
        if (oasServiceMetadata.generateWithoutDataBinding()) {
            return new LowResourceRequestBodyGenerator(oasServiceMetadata);
        }
        return new DefaultRequestBodyGenerator(oasServiceMetadata);
    }

    public abstract RequiredParameterNode createRequestBodyNode(RequestBody requestBody) throws OASTypeGenException;
}
