package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.Map;

public class PathParameterGenerator extends ParameterGenerator {

    @Override
    public ParameterNode generateParameterNode(Parameter parameter) {
        return null;
    }
}
