package org.ballerinalang.generators;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.openapi.utils.GeneratorConstants.USER_DIR;

public class BallerinaServiceGeneratorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    Path resourcePath = Paths.get(System.getProperty(USER_DIR));
    Path expectedServiceFile = RES_DIR.resolve(Paths.get("generators"));
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    SyntaxTree syntaxTree;


    @Test(description = "Generate importors")
    public void generateImports() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/petstore_listeners.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate serviceDeclaration")
    public void generateService() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/petstore_service.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for multiple operations")
    public void generateMultipleOperatons() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/multiOperations.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for multiple paths")
    public void generateMultiplePath() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/multiPaths.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    //Scenario 01 - Path parameters.
    @Test(description = "Generate functionDefinitionNode for Path parameters")
    public void generatePathparameter() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/multiPathParam.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    //Scenario 02 - Query parameters.
    @Test(description = "Generate functionDefinitionNode for Query parameters")
    public void generateQueryparameter() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/multiQueryParam.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }
    //Scenario 03 - Header parameters.
    @Test(description = "Generate functionDefinitionNode for Header parameters")
    public void generateHeaderParameter() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/multiHeaderParam.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for paramter for content instead of schema")
    public void generateParameterHasContent() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/parameterTypehasContent.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    //Request Body Scenarios
    @Test(description = "Scenario 01 - Request Body has single content type(application/json)")
    public void generateJsonPayload() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario01_rb.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Scenario 01.02 - Request Body has single content type(application/octet-stream)")
    public void generateOtherPayload() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario01_02_rb.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Scenario 02 - Request Body has multiple content types with Same dataBind schema type.\n")
    public void generateRBsameDataBindingPayload() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario02_rb.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Scenario 03 - Request Body has multiple content types with Different dataBind schema types.")
    public void generateMultipleContent() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario03_rb.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }
    //Response scenarios
    @Test(description = "Scenario 01 - Response has single response without content type")
    public void generateResponseScenario01() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_01_rs.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Scenario 02 - Single response with content type")
    public void generateResponseScenario02() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_02_rs.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateResponsePayloadWithRef() throws IOException, BallerinaOpenApiException, FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/responseRefPayload.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateResponsePayloadWithRefMulti() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/responseMultipleRefPayload.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateResponsePayloadWithDifferentStatusCode() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/responseDifferentStatusCode.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "Generate functionDefinitionNode for request body with json")
    public void generateResponseDifferentStatusCode() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/responseDifferentCodes.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }

    @Test(description = "oneOf and anyOf, so you can specify alternate schemas for the response body.")
    public void generateResponserecordOnof() throws IOException, BallerinaOpenApiException,
            FormatterException {
        Path definitionPath = RES_DIR.resolve("generators/swagger/responseOneOf.yaml");
        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
    }




}
