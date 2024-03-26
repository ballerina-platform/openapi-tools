package io.ballerina.openapi.core.service.response;

import io.swagger.v3.oas.models.OpenAPI;

public class ReturnTypeGeneratorFactory {

    public static ReturnTypeGenerator getReturnTypeGenerator(boolean generateWithoutDataBinding, String pathRecord,
                                                             OpenAPI openAPI) {
        if (generateWithoutDataBinding) {
            return new LowResourceReturnTypeGenerator();
        } else {
            return new ReturnTypeGeneratorImpl(pathRecord, openAPI);
        }
    }
}
