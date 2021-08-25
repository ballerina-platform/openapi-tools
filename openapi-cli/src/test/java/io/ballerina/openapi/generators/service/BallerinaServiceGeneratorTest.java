///*
// * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * WSO2 Inc. licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
//package io.ballerina.openapi.generators.service;
//
//import io.ballerina.compiler.syntax.tree.SyntaxTree;
//import io.ballerina.openapi.cmd.Filter;
//import io.ballerina.openapi.exception.BallerinaOpenApiException;
//import io.ballerina.openapi.generators.GeneratorConstants;
//import org.ballerinalang.formatter.core.FormatterException;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * All the tests related to the BallerinaServiceGenerator util.
// */
//public class BallerinaServiceGeneratorTest {
//    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
//    Path resourcePath = Paths.get(System.getProperty(GeneratorConstants.USER_DIR));
//    Path expectedServiceFile = RES_DIR.resolve(Paths.get("generators"));
//    List<String> list1 = new ArrayList<>();
//    List<String> list2 = new ArrayList<>();
//    Filter filter = new Filter(list1, list2);
//    SyntaxTree syntaxTree;
//
//
//    @Test(description = "Generate importors")
//    public void generateImports() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/petstore_listeners.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("importors.bal");
//    }
//
//    @Test(description = "Generate listeners")
//    public void generatelisteners() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/petstore_listeners02.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("listeners.bal");
//    }
//
//    @Test(description = "Generate listeners")
//    public void generatelisteners02() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/petstore_listeners03.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("listeners03.bal");
//    }
//
//    @Test(description = "Generate serviceDeclaration")
//    public void generateService() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/petstore_service.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("service_gen.bal");
//    }
//
//    @Test(description = "Generate functionDefinitionNode for multiple operations")
//    public void generateMultipleOperations() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/multiOperations.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("multi_operations.bal");
//    }
//
//    @Test(description = "Generate functionDefinitionNode for multiple paths")
//    public void generateMultiplePath() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/multiPaths.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("multi_paths.bal");
//    }
//
//    //Scenario 01 - Path parameters.
//    @Test(description = "Generate functionDefinitionNode for Path parameters")
//    public void generatePathparameter() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/multiPathParam.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("path_parameters.bal");
//    }
//    //Scenario 02 - Path parameters.
//    @Test(description = "Generate functionDefinitionNode for only Path parameters")
//    public void generatePathparameter02() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/multiPathParam02.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("path_parameters02.bal");
//    }
//    //Scenario 02 - Query parameters.
//    @Test(description = "Generate functionDefinitionNode for Query parameters")
//    public void generateQueryparameter() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/multiQueryParam.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("query_parameters.bal");
//
//    }
//    //Scenario 03 - Header parameters.
//    @Test(description = "Generate functionDefinitionNode for Header parameters")
//    public void generateHeaderParameter() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/multiHeaderParam.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("header_parameters.bal");
//    }
//
//    @Test(description = "Generate functionDefinitionNode for paramter for content instead of schema")
//    public void generateParameterHasContent() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/parameterTypehasContent.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("param_type_with_content.bal");
//    }
//
//    //Request Body Scenarios
//    @Test(description = "Scenario 01 - Request Body has single content type(application/json)")
//    public void generateJsonPayload() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario01_rb.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_01_rb.bal");
//    }
//
//    @Test(description = "Scenario 01.02 - Request Body has single content type(application/octet-stream)")
//    public void generateOtherPayload() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario01_02_rb.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_0102_rb.bal");
//    }
//
//    @Test(description = "Scenario 02 - Request Body has multiple content types with Same dataBind schema type.\n")
//    public void generateRBsameDataBindingPayload() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario02_rb.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_02_rb.bal");
//    }
//
//    @Test(description = "Scenario 03 - Request Body has multiple content types with Different dataBind schema types.")
//    public void generateMultipleContent() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario03_rb.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_03_rb.bal");
//    }
//    //Response scenarios
//    @Test(description = "Scenario 01 - Response has single response without content type")
//    public void generateResponseScenario01() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_01_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_01_rs.bal");
//    }
//
//    @Test(description = "Scenario 02 - Single response with content type")
//    public void generateResponseScenario02() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_02_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_02_rs.bal");
//    }
//
//    @Test(description = "Scenario 03 - Single response with content type application/json")
//    public void generateResponseScenario03() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_03_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_03_rs.bal");
//    }
//
//    @Test(description = "Scenario 04 - Response has multiple responses without content type")
//    public void generateResponseScenario04() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_04_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_04_rs.bal");
//    }
//
//    @Test(description = "Scenario 05 - Error response with a schema")
//    public void generateResponseScenario05() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_05_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_05_rs.bal");
//    }
//
//    @Test(description = "Scenario 06 - Error response with a schema with application/json")
//    public void generateResponseScenario06() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_06_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_06_rs.bal");
//    }
//
//    @Test(description = "Scenario 07 - Single response has multiple content types")
//    public void generateResponseScenario07() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_07_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_07_rs.bal");
//    }
//
//    @Test(description = "Scenario 08 - Single response has inline record for dataType")
//    public void generateResponseScenario08() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_08_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_08_rs.bal");
//    }
//
//    @Test(description = "Scenario 09 - Single response has inline record for dataType with different status code")
//    public void generateResponseScenario09() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_09_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_09_rs.bal");
//    }
//
//    @Test(description = "Scenario 10 - Response with a custom media type")
//    public void generateResponseScenario10() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_10_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_10_rs.bal");
//    }
//
//    @Test(description = "Scenario 11 - Response has OneOf and AnyOf type 200 ok")
//    public void generateResponseScenario11() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_11_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_11_rs.bal");
//    }
//
//    @Test(description = "Scenario 12 - Response has OneOf and AnyOf type for error status code")
//    public void generateResponseScenario12() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_12_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_12_rs.bal");
//    }
//
//    @Test(description = "Scenario 13 - Single response has multiple content types with different error code")
//    public void generateResponseScenario13() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_13_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_13_rs.bal");
//    }
//
//    @Test(description = "Scenario 14 - Multiple response with same mediaType")
//    public void generateResponseScenario14() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_14_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_14_rs.bal");
//    }
//
//    @Test(description = "Scenario 15 - Response has array type data Binding")
//    public void generateResponseScenario15() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_15_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_15_rs.bal");
//    }
//
//    @Test(description = "Scenario 16 - Response has array type data Binding with error code")
//    public void generateResponseScenario16() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_16_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_16_rs.bal");
//    }
//    // Scenario 17, 18 is invalid
//    @Test(description = "Scenario 19 - Multiple response with different mediaType")
//    public void generateResponseScenario19() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/scenario_19_rs.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("scenario_19_rs.bal");
//    }
//
//    @Test(description = "Generate functionDefinitionNode for request body with json")
//    public void generateResponsePayloadWithRef() throws IOException, BallerinaOpenApiException, FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/responseRefPayload.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//    }
//
//    @Test(description = "Generate functionDefinitionNode for request body with json")
//    public void generateResponsePayloadWithRefMulti() throws IOException, BallerinaOpenApiException,
//            FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/responseMultipleRefPayload.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//    }
//
//    @Test(description = "Generate functionDefinitionNode for request body with json")
//    public void generateResponsePayloadWithDifferentStatusCode() throws IOException, BallerinaOpenApiException,
//            FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/responseDifferentStatusCode.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//    }
//
//    @Test(description = "Generate functionDefinitionNode for request body with json")
//    public void generateResponseDifferentStatusCode() throws IOException, BallerinaOpenApiException,
//            FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/responseDifferentCodes.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//    }
//
//    @Test(description = "oneOf and anyOf, so you can specify alternate schemas for the response body.")
//    public void generateResponserecordOneOf() throws IOException, BallerinaOpenApiException,
//            FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/responseOneOf.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//    }
//
//    @Test(description = "Default response handling")
//    public void generateResponseDefault() throws IOException, BallerinaOpenApiException,
//            FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/petstore_default.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("petstore_default.bal");
//    }
//
//    @Test(description = "Path with special characters ")
//    public void testWithSpecialCharacters() throws IOException, BallerinaOpenApiException,
//            FormatterException {
//        Path definitionPath = RES_DIR.resolve("generators/swagger/path_with_special_characters.yaml");
//        syntaxTree = BallerinaServiceGenerator.generateSyntaxTree(definitionPath, "listeners", filter);
//        compareGeneratedSyntaxTreewithExpectedSyntaxTree("path_with_special_characters.bal");
//    }
//    //Get string as a content of ballerina file
//    private String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
//        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
//        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
//        expectedServiceLines.close();
//        return expectedServiceContent;
//    }
//
//    private void compareGeneratedSyntaxTreewithExpectedSyntaxTree(String s) throws IOException {
//
//        String expectedBallerinaContent = getStringFromGivenBalFile(RES_DIR.resolve("generators/ballerina"), s);
//        String generatedSyntaxTree = syntaxTree.toString();
//
//        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
//        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
//        Assert.assertTrue(generatedSyntaxTree.contains(expectedBallerinaContent));
//    }
//}
