package io.ballerina.openapi.core.service.response;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;

public abstract class ReturnTypeGenerator {
    final OASServiceMetadata oasServiceMetadata;

    public ReturnTypeGenerator(OASServiceMetadata oasServiceMetadata) {
        this.oasServiceMetadata = oasServiceMetadata;
    }

    public static ReturnTypeGenerator getReturnTypeGenerator(OASServiceMetadata oasServiceMetadata) {
        if (oasServiceMetadata.generateWithoutDataBinding()) {
            return new LowResourceReturnTypeGenerator(oasServiceMetadata);
        } else {
            return new DefaultReturnTypeGenerator(oasServiceMetadata);
        }
    }

    public abstract ReturnTypeDescriptorNode getReturnTypeDescriptorNode(Map.Entry<PathItem.HttpMethod, Operation>
                                                                                 operation, String path)
            throws OASTypeGenException;
}
