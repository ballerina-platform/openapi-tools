package io.ballerina.generators.client;

import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.generators.BallerinaClientGenerator.getReturnType;
import static io.ballerina.generators.common.TestUtils.getOpenAPI;

public class FunctionSignatureReturnTypeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    @Test(description = "Tests for returnType")
    public void getReturnTypeTests() throws IOException, BallerinaOpenApiException {
        OpenAPI array = getOpenAPI(RES_DIR.resolve("swagger/return_type/all_return_type_operation.yaml"));
        Assert.assertEquals(getReturnType(array.getPaths().get("/jsonproducts").getGet()), "json|error");
        Assert.assertEquals(getReturnType(array.getPaths().get("/stringproducts/record").getGet()),
                "ProductArr|error");
        Assert.assertEquals(getReturnType(array.getPaths().get("/xmlproducts").getGet()), "XML|error");
        Assert.assertEquals(getReturnType(array.getPaths().get("/xmlarrayproducts").getGet()), "XMLArr|error");
    }
}
