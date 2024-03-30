package io.ballerina.openapi.core.service.signature;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;

public abstract class FunctionSignatureGenerator {

    final OASServiceMetadata oasServiceMetadata;

    public FunctionSignatureGenerator(OASServiceMetadata oasServiceMetadata) {
        this.oasServiceMetadata = oasServiceMetadata;
    }

    public static FunctionSignatureGenerator getFunctionSignatureGenerator(OASServiceMetadata oasServiceMetadata) {
        if (oasServiceMetadata.generateWithoutDataBinding()) {
            return new LowResourceFunctionSignatureGenerator(oasServiceMetadata);
        }
        return new DefaultFunctionSignatureGenerator(oasServiceMetadata);
    }

    public abstract FunctionSignatureNode getFunctionSignature(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                               String path);
}
