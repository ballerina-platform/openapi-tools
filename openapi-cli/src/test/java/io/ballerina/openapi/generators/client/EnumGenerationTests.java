package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.openapi.generators.common.TestUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;
import static io.ballerina.openapi.generators.common.TestUtils.getDiagnostics;

/**
 * Test cases for generating ballerina parameters for openapi parameters with enum schemas.
 */
public class EnumGenerationTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    @Test(description = "Tests for all the enum scenarios in remote function parameter generation:" +
            "Use case 01 : Enum in query parameter" +
            "Use case 02 : Enums in path parameter" +
            "Use case 03 : Enum in header parameter" +
            "Use case 04 : Enum in reusable parameter" +
            "Use case 05 : Enum in parameter with referenced schema")
    public void generateParametersWithEnums() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/parameters_with_enum.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/parameters_with_enum.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @Test(description = "Tests for all the nullable enum scenarios in remote function parameter generation:" +
            "Use case 01 : Nullable enum in query parameter" +
            "Use case 02 : Nullable enum in path parameter" +
            "Use case 03 : Nullable enum in header parameter" +
            "Use case 04 : Nullable enum in reusable parameter" +
            "Use case 05 : Nullable enum in parameter with referenced schema")
    public void generateParametersWithNullableEnums() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("swagger/parameters_with_nullable_enums.yaml");
        Path expectedPath = RES_DIR.resolve("ballerina/parameters_with_nullable_enums.bal");
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTree, openAPI, ballerinaClientGenerator);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }


    @AfterTest
    private void deleteGeneratedFiles() {
        try {
            TestUtils.deleteGeneratedFiles();
        } catch (IOException ignored) {
        }
    }
}
