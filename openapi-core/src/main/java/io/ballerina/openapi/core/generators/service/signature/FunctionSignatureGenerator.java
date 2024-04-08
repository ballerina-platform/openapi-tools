package io.ballerina.openapi.core.generators.service.signature;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class FunctionSignatureGenerator {

    final OASServiceMetadata oasServiceMetadata;
    boolean isNullableRequired;
    final List<Diagnostic> diagnostics = new ArrayList<>();

    public FunctionSignatureGenerator(OASServiceMetadata oasServiceMetadata) {
        this.oasServiceMetadata = oasServiceMetadata;
    }

    public boolean isNullableRequired() {
        return isNullableRequired;
    }

    public static FunctionSignatureGenerator getFunctionSignatureGenerator(OASServiceMetadata oasServiceMetadata) {
        if (oasServiceMetadata.generateWithoutDataBinding()) {
            return new LowResourceFunctionSignatureGenerator(oasServiceMetadata);
        }
        return new DefaultFunctionSignatureGenerator(oasServiceMetadata);
    }

    public abstract FunctionSignatureNode getFunctionSignature(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                               String path) throws BallerinaOpenApiException;

    public List<Diagnostic> getDiagnostics() {
        return diagnostics;
    }
}
