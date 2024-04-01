package io.ballerina.openapi.core.generators.client;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.Map;

public class ImplFunctionBodyGenerator extends FunctionBodyGeneratorImp {

    public ImplFunctionBodyGenerator(String path, Map.Entry<PathItem.HttpMethod, Operation> operation, OpenAPI openAPI, AuthConfigGeneratorImp ballerinaAuthConfigGeneratorImp, BallerinaUtilGenerator ballerinaUtilGenerator) {
        super(path, operation, openAPI, ballerinaAuthConfigGeneratorImp, ballerinaUtilGenerator);
    }

    @Override
    protected String getClientCallWithHeadersParam() {
        return addTargetTypeParam(super.getClientCallWithHeadersParam());
    }

    @Override
    protected String getClientCallWithRequestAndHeaders() {
        return addTargetTypeParam(super.getClientCallWithRequestAndHeaders());
    }

    @Override
    protected String getClientCallWithHeaders() {
        return addTargetTypeParam(super.getClientCallWithHeaders());
    }

    @Override
    protected String getClientCallWithRequest() {
        return addTargetTypeParam(super.getClientCallWithRequest());
    }

    @Override
    protected String getSimpleClientCall() {
        return addTargetTypeParam(super.getSimpleClientCall());
    }

    private String addTargetTypeParam(String clientCall) {
        String substring = clientCall.substring(0, clientCall.length() - 1);
        return substring + ", targetType = targetType)";
    }
}
