package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.BallerinaCodeGenerator;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.core.GeneratorUtils.getValidName;

/**
 * check whether the remote function names generated are matching with ballerina standard.
 */
public class RemoteFunctionNameValidationTests {
    private static final Path RESDIR =
            Paths.get("src/test/resources/generators/client/swagger").toAbsolutePath();
    SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "When path parameter has given unmatch data type in ballerina",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "OpenAPI definition has errors: " +
                    "\\R\\ROperationId is missing in the resource path: .*")
    public void testMissionOperationId() throws IOException, BallerinaOpenApiException {
        BallerinaCodeGenerator codeGenerator = new BallerinaCodeGenerator();
        Path definitionPath = RESDIR.resolve("petstore_without_operation_id.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false,
                false);
        ballerinaClientGenerator.generateSyntaxTree();
    }

    @Test(description = "Check whether the formatted function namee are meeting ballerina coding conventions",
            dataProvider = "sampleProvider")
    public void testFunctionNameGeneration(String operationId, String expectedFunctionName) {
        String generatedFunctionName = getValidName(operationId, false);
        Assert.assertEquals(generatedFunctionName, expectedFunctionName);
    }

    @DataProvider(name = "sampleProvider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"get-pet-name", "getPetName"},
                {"GET_Add_permission", "getAddPermission"},
                {"ListBankAccount", "listBankAccount"},
                {"chat.media.download", "chatMediaDownload"}
        };
    }
}
