package io.ballerina.openapi.core.generators.service.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.parameters.Parameter;

public class PathParameterGenerator extends ParameterGenerator {

    public PathParameterGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }

    @Override
    public ParameterNode generateParameterNode(Parameter parameter) {
        return null;
    }
}
