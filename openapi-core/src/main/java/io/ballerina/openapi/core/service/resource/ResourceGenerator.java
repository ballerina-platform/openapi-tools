package io.ballerina.openapi.core.service.resource;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;

public abstract class ResourceGenerator {

    final OASServiceMetadata oasServiceMetadata;

    protected ResourceGenerator(OASServiceMetadata oasServiceMetadata) {
        this.oasServiceMetadata = oasServiceMetadata;
    }

    public static ResourceGenerator createResourceGenerator(OASServiceMetadata oasServiceMetadata) {
        if (oasServiceMetadata.generateWithoutDataBinding()) {
            return new LowResourceGenerator(oasServiceMetadata);
        }
        return new DefaultResourceGenerator(oasServiceMetadata);
    }

    public abstract FunctionDefinitionNode generateResourceFunction(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                                    String path);
}
