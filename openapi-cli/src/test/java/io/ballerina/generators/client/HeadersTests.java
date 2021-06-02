package io.ballerina.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static io.ballerina.generators.BallerinaClientGenerator.getFunctionBodyNode;
import static io.ballerina.generators.common.TestUtils.getOpenAPI;

public class HeadersTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();

    @Test(description = "Test for header that comes under the parameter section.")
    public void getHeaderTests() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("diagnostic_files/header_parameter.yaml");
        OpenAPI display = getOpenAPI(definitionPath);
        Set<Map.Entry<PathItem.HttpMethod, Operation>> operation =
                display.getPaths().get("/pets").readOperationsMap().entrySet();
        Iterator<Map.Entry<PathItem.HttpMethod, Operation>> iterator = operation.iterator();
        FunctionBodyNode bodyNode = getFunctionBodyNode("/pets", iterator.next());
        Assert.assertEquals(bodyNode.toString(), "{string path=string`/pets`;map<string|string[]>accHeaders=" +
                "{'X\\-Request\\-ID:'X\\-Request\\-ID,'X\\-Request\\-Client:'X\\-Request\\-Client};_=" +
                "check self.clientEp-> get(path, accHeaders, targetType=http:Response);}");
    }

}
