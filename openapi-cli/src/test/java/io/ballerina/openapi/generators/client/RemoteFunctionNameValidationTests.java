package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;

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
            expectedExceptionsMessageRegExp = "OperationId is missing for the resource path: .*")
    public void testMissionOperationId() throws IOException, BallerinaOpenApiException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RESDIR.resolve("petstore_without_operation_id.yaml");
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
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
