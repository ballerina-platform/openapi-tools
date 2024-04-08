package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.client.AuthConfigGeneratorImp;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.generators.common.TestConstants;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.GeneratorTestUtils
        .compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * Test client generation when server url is not given in the open-api definition.
 */
public class NoServerURLTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test for no server url with no security schema given")
    public void getClientForNoServerURL() throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RES_DIR.resolve("swagger/missing_server_url.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/missing_server_url.bal");

        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Test for no server url with HTTP authentication mechanism")
    public void getClientForNoServerURLWithHTTPAuth() {
        AuthConfigGeneratorImp ballerinaAuthConfigGenerator = new AuthConfigGeneratorImp(
                false, true, new ArrayList<>());
        String expectedParams = TestConstants.HTTP_CLIENT_CONFIG_PARAM_NO_URL;
        StringBuilder generatedParams = new StringBuilder();
        List<ParameterNode> generatedInitParamNodes = ballerinaAuthConfigGenerator.getConfigParamForClassInit();
        for (Node param: generatedInitParamNodes) {
            generatedParams.append(param.toString());
        }
        expectedParams = (expectedParams.trim()).replaceAll("\\s+", "");
        String generatedParamsStr = (generatedParams.toString().trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedParams, generatedParamsStr);
    }

//    @Test(description = "Test for no server url with API key authentication mechanism")
//    public void getClientForNoServerURLWithAPIKeyAuth() {
//        AuthConfigGeneratorImp ballerinaAuthConfigGenerator = new AuthConfigGeneratorImp(
//                true, false, new ArrayList<>());
//        String expectedParams = TestConstants.API_KEY_CONFIG_PARAM_NO_URL;
//        StringBuilder generatedParams = new StringBuilder();
//        List<ParameterNode> generatedInitParamNodes = ballerinaAuthConfigGenerator.getConfigParamForClassInit();
//        for (Node param: generatedInitParamNodes) {
//            generatedParams.append(param.toString());
//        }
//        expectedParams = (expectedParams.trim()).replaceAll("\\s+", "");
//        String generatedParamsStr = (generatedParams.toString().trim()).replaceAll("\\s+", "");
//        Assert.assertEquals(expectedParams, generatedParamsStr);
//    }
}
