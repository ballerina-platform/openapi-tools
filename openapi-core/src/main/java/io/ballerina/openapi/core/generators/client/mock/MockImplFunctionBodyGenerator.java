package io.ballerina.openapi.core.generators.client.mock;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.openapi.core.generators.client.AuthConfigGeneratorImp;
import io.ballerina.openapi.core.generators.client.BallerinaUtilGenerator;
import io.ballerina.openapi.core.generators.client.ImplFunctionBodyGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.List;
import java.util.Map;

public class MockImplFunctionBodyGenerator extends ImplFunctionBodyGenerator {
    public MockImplFunctionBodyGenerator(String path, Map.Entry<PathItem.HttpMethod, Operation> operation,
                                         OpenAPI openAPI, AuthConfigGeneratorImp ballerinaAuthConfigGeneratorImp,
                                         BallerinaUtilGenerator ballerinaUtilGenerator,
                                         List<ImportDeclarationNode> imports, boolean hasHeaders,
                                         boolean hasDefaultHeaders, boolean hasQueries) {
        super(path, operation, openAPI, ballerinaAuthConfigGeneratorImp, ballerinaUtilGenerator,
                imports, hasHeaders, hasDefaultHeaders, hasQueries);
    }

}
