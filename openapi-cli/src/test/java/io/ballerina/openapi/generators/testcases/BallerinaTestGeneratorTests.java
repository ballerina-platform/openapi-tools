package io.ballerina.openapi.generators.testcases;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.generators.client.BallerinaTestGenerator;
import io.ballerina.openapi.generators.common.TestUtils;
import io.ballerina.openapi.generators.schema.BallerinaSchemaGenerator;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.openapi.generators.GeneratorConstants.OAS_PATH_SEPARATOR;
import static io.ballerina.openapi.generators.GeneratorConstants.TEST_DIR;

/**
 * Test cases related to ballerina test skeleton generation.
 */
public class BallerinaTestGeneratorTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/test_cases/").toAbsolutePath();
    private static final Path PROJECT_DIR = RES_DIR.resolve("ballerina_project");
    private static final Path clientPath = RES_DIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RES_DIR.resolve("ballerina_project/types.bal");
    private static final Path testPath = RES_DIR.resolve("ballerina_project/tests/test.bal");
    private static final Path configPath = RES_DIR.resolve("ballerina_project/tests/Config.toml");

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Generate Client with test skelotins", dataProvider = "httpAuthIOProvider")
    public void generateclientWithTestSkel(String yamlFile) throws IOException, BallerinaOpenApiException,
            FormatterException, BallerinaOpenApiException {
        Files.createDirectories(Paths.get(PROJECT_DIR + OAS_PATH_SEPARATOR + TEST_DIR));
        Path definitionPath = RES_DIR.resolve("sample_yamls/" + yamlFile);
        CodeGenerator codeGenerator = new CodeGenerator();
        OpenAPI openAPI = codeGenerator.normalizeOpenAPI(definitionPath);
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(openAPI, filter);
        BallerinaSchemaGenerator schemaGenerator = new BallerinaSchemaGenerator(openAPI);
        BallerinaTestGenerator ballerinaTestGenerator = new BallerinaTestGenerator(ballerinaClientGenerator);
        SyntaxTree syntaxTreeClient = ballerinaClientGenerator.generateSyntaxTree();
        SyntaxTree syntaxTreeTest = ballerinaTestGenerator.generateSyntaxTree();
        schemaGenerator.setTypeDefinitionNodeList(ballerinaClientGenerator.getTypeDefinitionNodeList());
        schemaGenerator.setEnumDeclarationNodeList(ballerinaClientGenerator.getEnumDeclarationNodeList());
        SyntaxTree syntaxTreeSchema = schemaGenerator.generateSyntaxTree();
        String configFile = ballerinaTestGenerator.getConfigTomlFile();
        List<Diagnostic> diagnostics = getDiagnostics(syntaxTreeClient, syntaxTreeTest, syntaxTreeSchema, configFile);
        Assert.assertTrue(diagnostics.isEmpty());
    }

    public List<Diagnostic> getDiagnostics(SyntaxTree clientSyntaxTree, SyntaxTree testSyntaxTree,
                                           SyntaxTree schemaSyntaxTree, String configContent)
            throws FormatterException, IOException {
        TestUtils.writeFile(clientPath, Formatter.format(clientSyntaxTree).toString());
        TestUtils.writeFile(schemaPath, Formatter.format(schemaSyntaxTree).toString());
        TestUtils.writeFile(testPath, Formatter.format(testSyntaxTree).toString());
        TestUtils.writeFile(configPath, configContent);
        SemanticModel semanticModel = TestUtils.getSemanticModel(clientPath);
        return semanticModel.diagnostics();
    }

    @AfterMethod
    public void afterTest() {
        try {
            Files.deleteIfExists(clientPath);
            Files.deleteIfExists(schemaPath);
            Files.deleteIfExists(testPath);
            Files.deleteIfExists(configPath);
        } catch (IOException e) {
            //Ignore the exception
        }
    }

    @DataProvider(name = "httpAuthIOProvider")
    public Object[] dataProvider() {
        return new Object[]{
                "basic_auth.yaml",
                "bearer_auth.yaml",
                "oauth2_authrization_code.yaml",
                "oauth2_implicit.yaml",
                "query_api_key.yaml",
                "no_auth.yaml"
        };
    }
}
