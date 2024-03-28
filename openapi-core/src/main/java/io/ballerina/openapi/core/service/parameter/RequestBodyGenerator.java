package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.parameters.RequestBody;

public abstract class RequestBodyGenerator {

    public static RequestBodyGenerator getRequestBodyGenerator(OASServiceMetadata oasServiceMetadata,
                                                               RequestBody requestBody) {
        if (oasServiceMetadata.generateWithoutDataBinding()) {
            return new LowResourceRequestBodyGenerator();
        }
        return new DefaultRequestBodyGenerator(requestBody);
    }

    public abstract RequiredParameterNode createRequestBodyNode() throws OASTypeGenException;
}
