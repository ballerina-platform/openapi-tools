package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.common.TestConstants;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * Test client generation when server url is not given in the open-api definition.
 */
public class NoServerURLTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test for no server url with no security schema given")
    public void getClientForNoServerURL() throws IOException, BallerinaOpenApiException, FormatterException {
        CodeGenerator codeGenerator = new CodeGenerator();
        Path definitionPath = RES_DIR.resolve("swagger/missing_server_url.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/missing_server_url.bal");

        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath, true);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter, false);
        String clientSyntaxTree = ballerinaClientGenerator.getClient();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, clientSyntaxTree);
    }

    @Test(description = "Test for no server url with HTTP authentication mechanism")
    public void getClientForNoServerURLWithHTTPAuth() {
        BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(
                false, true);
        String expectedParams = TestConstants.HTTP_CLIENT_CONFIG_PARAM_NO_URL;
        StringBuilder generatedParams = new StringBuilder();
        List<Node> generatedInitParamNodes = ballerinaAuthConfigGenerator.getConfigParamForClassInit(
                "/");
        for (Node param: generatedInitParamNodes) {
            generatedParams.append(param.toString());
        }
        expectedParams = (expectedParams.trim()).replaceAll("\\s+", "");
        String generatedParamsStr = (generatedParams.toString().trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedParams, generatedParamsStr);
    }

    @Test(description = "Test for no server url with API key authentication mechanism")
    public void getClientForNoServerURLWithAPIKeyAuth() {
        BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator = new BallerinaAuthConfigGenerator(
                true, false);
        String expectedParams = TestConstants.API_KEY_CONFIG_PARAM_NO_URL;
        StringBuilder generatedParams = new StringBuilder();
        List<Node> generatedInitParamNodes = ballerinaAuthConfigGenerator.getConfigParamForClassInit(
                "/");
        for (Node param: generatedInitParamNodes) {
            generatedParams.append(param.toString());
        }
        expectedParams = (expectedParams.trim()).replaceAll("\\s+", "");
        String generatedParamsStr = (generatedParams.toString().trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedParams, generatedParamsStr);
    }
}
