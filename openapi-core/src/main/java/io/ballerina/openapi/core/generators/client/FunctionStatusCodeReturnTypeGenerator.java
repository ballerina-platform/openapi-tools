package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.service.GeneratorConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.List;

import static io.ballerina.openapi.core.generators.common.GeneratorUtils.generateStatusCodeTypeInclusionRecord;

public class FunctionStatusCodeReturnTypeGenerator extends FunctionReturnTypeGeneratorImp {

    public FunctionStatusCodeReturnTypeGenerator(Operation operation, OpenAPI openAPI) {
        super(operation, openAPI);
    }

    @Override
    protected boolean populateReturnType(String statusCode, ApiResponse response, List<TypeDescriptorNode> returnTypes) {
        boolean noContentResponseFoundSuper = super.populateReturnType(statusCode, response, returnTypes);
        returnTypes.add(generateStatusCodeTypeInclusionRecord(
                GeneratorConstants.HTTP_CODES_DES.get(statusCode), response, operation.getOperationId()));
        return noContentResponseFoundSuper;
    }
}
