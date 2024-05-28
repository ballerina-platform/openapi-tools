package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.openapi.core.generators.client.mock.MockFunctionBodyGenerator;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.openapi.generators.common.GeneratorTestUtils.getOpenAPI;

public class MockClientGenerationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client/mock").toAbsolutePath();
    @Test
    public void mockClientTest() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("basic_response_example.yaml");
        OpenAPI openapi = getOpenAPI(definitionPath);
        TypeHandler.createInstance(openapi, false);
        String path = "/api/v1/payment_run_schedules";
        Set<Map.Entry<PathItem.HttpMethod, Operation>> pathItem = openapi.getPaths().get(path).
                readOperationsMap().entrySet();
        Iterator<Map.Entry<PathItem.HttpMethod, Operation>> iterator = pathItem.iterator();
        Map.Entry<PathItem.HttpMethod, Operation> operation = iterator.next();

        MockFunctionBodyGenerator mockFunctionBodyGenerator = new MockFunctionBodyGenerator(path, operation, openapi,
                false);
        Optional<FunctionBodyNode> functionBodyNode = mockFunctionBodyGenerator.getFunctionBodyNode();
        FunctionBodyNode body = functionBodyNode.get();
        String string = body.toString();
    }

    @Test
    public void mockClientTestWithReferenceExample() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("basic_response_example.yaml");
        OpenAPI openapi = getOpenAPI(definitionPath);
        TypeHandler.createInstance(openapi, false);
        String path = "/api/v1/payment_run_schedules";
        Set<Map.Entry<PathItem.HttpMethod, Operation>> pathItem = openapi.getPaths().get(path).
                readOperationsMap().entrySet();
        Iterator<Map.Entry<PathItem.HttpMethod, Operation>> iterator = pathItem.iterator();
        Map.Entry<PathItem.HttpMethod, Operation> operation = iterator.next();

        MockFunctionBodyGenerator mockFunctionBodyGenerator = new MockFunctionBodyGenerator(path, operation, openapi,
                false);
        Optional<FunctionBodyNode> functionBodyNode = mockFunctionBodyGenerator.getFunctionBodyNode();
        FunctionBodyNode body = functionBodyNode.get();
        String string = body.toString();

    }
}
