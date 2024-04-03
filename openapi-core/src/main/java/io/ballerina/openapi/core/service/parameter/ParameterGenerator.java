package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.parameters.Parameter;

public abstract class ParameterGenerator {
    final OpenAPI openAPI;
    boolean isNullableRequired;
    // todo : check on how to handle nullable in servicegenerator annotation generation

    public ParameterGenerator(OASServiceMetadata oasServiceMetadata) {
        this.openAPI = oasServiceMetadata.getOpenAPI();
        this.isNullableRequired = oasServiceMetadata.isNullable();
    }

    public abstract ParameterNode generateParameterNode(Parameter parameter);
}
