/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.cmd;

import io.ballerina.openapi.OpenAPITest;
import io.ballerina.openapi.TestUtil;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.openapi.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.TestUtil.OUT;
import static io.ballerina.openapi.TestUtil.RESOURCES_PATH;
import static io.ballerina.openapi.TestUtil.TEST_DISTRIBUTION_PATH;

/**
 * This {@code BallerinaToOpenAPITests} contains all the ballerina to openapi command with compiler annotation.
 */
public class BallerinaToOpenAPITests extends OpenAPITest {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH + "/ballerina_sources");

    @BeforeClass
    public void setupDistributions() throws IOException {
        TestUtil.cleanDistribution();
    }

    @Test(description = "Check ballerina to openapi generator command with annotation")
    public void annotationWithTitleAndVersion() throws IOException, InterruptedException {
        executeCommand("project_1/service.bal", "service_openapi.yaml",
                "project_1/result.yaml");
    }

    @Test(description = "Annotation with contract and version field")
    public void annotationWithVersionAndContract() throws IOException, InterruptedException {
        executeCommand("project_2/service.bal", "greeting_openapi.yaml",
                "project_2/result.yaml");
    }

    @Test(description = "Annotation with contract and title field")
    public void annotationWithTitleAndContract() throws IOException, InterruptedException {
        executeCommand("project_3/service.bal", "title_openapi.yaml",
                "project_3/result.yaml");
    }

    @Test(description = "Annotation with blank title")
    public void annotationWithBlankTitle() throws IOException, InterruptedException {
        executeCommand("project_4/service.bal", "blankTitle_openapi.yaml",
                "project_4/result.yaml");
    }

    @Test(description = "Annotation with blank version")
    public void annotationWithBlankVersion() throws IOException, InterruptedException {
        executeCommand("project_5/service.bal", "blankVersion_openapi.yaml",
                "project_5/result.yaml");
    }

    @Test(description = "Annotation with blank version and title")
    public void annotationWithBlankTitleAndVersion() throws IOException, InterruptedException {
        executeCommand("project_6/service.bal", "bothBlank_openapi.yaml",
                "project_6/result.yaml");
    }

    @Test(description = "Check ballerina to openapi generator command")
    public void multipleService() throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add("project_7/service.bal");
        Assert.assertTrue(TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs));
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve("mTitle_openapi.yaml")));
    }

    @Test(description = "Check ballerina to openapi generator command with multiple service with annotation")
    public void multipleServiceWithAnnotation() throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add("project_8/service.bal");
        Assert.assertTrue(TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs));
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve("mTitle01_openapi.yaml")));
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve("mVersion_openapi.yaml")));
    }

    @Test(description = "Annotation with version")
    public void annotationWithVersion() throws IOException, InterruptedException {
        executeCommand("project_9/service.bal", "versionBase_openapi.yaml",
                "project_9/result.yaml");
    }

    @Test(description = "Annotation with title")
    public void annotationWithTitle() throws IOException, InterruptedException {
        executeCommand("project_10/service.bal", "titleBase_openapi.yaml",
                "project_10/result.yaml");
    }

    @Test(description = "Test for service declaration with non http service")
    public void nonHttpService() throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add("project_11/service.bal");
        TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertFalse(Files.exists(TEST_RESOURCE.resolve("query_openapi.yaml")));
    }

    @Test(description = "Service is with non openapi annotation")
    public void nonOpenAPIAnnotation() throws IOException, InterruptedException {
        executeCommand("project_non_openapi_annotation/service.bal", "service_openapi.yaml",
                "project_non_openapi_annotation/result.yaml");
    }

    @Test(description = "Service is with non openapi annotation and slash as base path")
    public void nonOpenAPIAnnotationWithSlash() throws IOException, InterruptedException {
        executeCommand("project_non_openapi_annotation_with_base_path/service.bal", "payload_openapi.yaml",
                "project_non_openapi_annotation_with_base_path/result.yaml");
    }

    @Test(description = "Service is with non openapi annotation and without a base path")
    public void nonOpenAPIAnnotationWithWithoutBasePath() throws IOException, InterruptedException {
        executeCommand("project_non_openapi_annotation_without_base_path/service_file.bal",
                "service_file_openapi.yaml",
                "project_non_openapi_annotation_without_base_path/result.yaml");
    }

    @Test(description = "Service is with non openapi annotation and without a base path")
    public void exampleMapping() throws IOException, InterruptedException {
        executeCommand("examples/response_example/service.bal",
                "convert_openapi.yaml",
                "examples/response_example/result.yaml");
    }

    //TODO enable after resolving dependency issue
    @Test(description = "Service is with openapi annotation include all oas info section details", enabled = false)
    public void openAPInForSectionTest() throws IOException, InterruptedException {
        executeCommand("project_openapi_info/service_file.bal", "info_openapi.yaml",
                "project_openapi_info/result.yaml");
    }

    @Test(description = "Service with OpenAPI example and examples annotations")
    public void openAPIExampleAnnotations() throws IOException, InterruptedException {
        executeCommand("project_openapi_examples/service_file.bal", "api_openapi.yaml",
                "project_openapi_examples/result.yaml");
    }

    @Test(description = "Generate without ballerina extension option")
    public void openAPIGenWithoutBalExt() throws IOException, InterruptedException {
        executeCommand("project_openapi_bal_ext/main.bal", "api_v1_openapi.yaml",
                "project_openapi_bal_ext/result_0.yaml");
    }

    @Test(description = "Generate with ballerina extension option")
    public void openAPIGenWithBalExt() throws IOException, InterruptedException {
        executeCommand("project_openapi_bal_ext/main.bal", "api_v1_openapi.yaml",
                "project_openapi_bal_ext/result_1.yaml", true);
    }

    @Test(description = "Map included record type which has same fields")
    public void includedRecordTest() throws IOException, InterruptedException {
        // 1. generate OAS
        executeCommand("project_openapi_with_included_test/service_file.bal",
                "payloadV_openapi.yaml",
                "project_openapi_with_included_test/result.yaml", false);
        // 2. generate type using the OAS which generated point 1
        generateTypes();
    }

    private void generateTypes() throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(String.valueOf(TEST_RESOURCE.resolve("payloadV_openapi.yaml")));
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertTrue(Files.exists(Paths.get(tmpDir.toString()).resolve("types.bal")));
        String generatedOpenAPI = getStringFromGivenBalFile(Paths.get(tmpDir.toString()).resolve("types.bal"));
        String expectedYaml = getStringFromGivenBalFile(TEST_RESOURCE.resolve(
                "resources/expected_types.bal"));
        Assert.assertEquals(expectedYaml, generatedOpenAPI);
    }

    @Test(description = "Generate with openapi serviceConfig annotation to service contract type")
    public void openapiServiceConfigForServiceContractType() throws IOException, InterruptedException {
        executeCommand("project_openapi_info_with_service_contract_type/service_file.bal",
                "v1_openapi.yaml",
                "project_openapi_info_with_service_contract_type/result.yaml", true);
    }

    @Test(description = "Generate openapi for service contract type with resourceInfo annotation")
    public void openapiResourceConfigForServiceContractType() throws IOException, InterruptedException {
        executeCommand("project_resource_info_with_service_contract_type/service_file.bal",
                "v1_openapi.yaml",
                "project_resource_info_with_service_contract_type/result.yaml", true);
    }

    @AfterClass
    public void cleanUp() throws IOException {
        TestUtil.cleanDistribution();
    }

    private String getStringFromGivenBalFile(Path expectedServiceFile) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile);
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining(System.lineSeparator()));
        expectedServiceLines.close();
        return expectedServiceContent.trim().replaceAll("\\s+", "").replaceAll(System.lineSeparator(), "");
    }

    private void executeCommand(String resourcePath, String generatedFile, String expectedPath) throws IOException,
            InterruptedException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(resourcePath);
        Assert.assertTrue(TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs));
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve(generatedFile)));
        String generatedOpenAPI = getStringFromGivenBalFile(TEST_RESOURCE.resolve(generatedFile));
        String expectedYaml = getStringFromGivenBalFile(TEST_RESOURCE.resolve(expectedPath));
        Assert.assertEquals(expectedYaml, generatedOpenAPI);
    }

    private void executeCommand(String resourcePath, String generatedFile, String expectedPath, boolean balExt)
            throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add(resourcePath);
        buildArgs.add("--with-bal-ext");
        buildArgs.add(String.valueOf(balExt));
        Assert.assertTrue(TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs));
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve(generatedFile)));
        String generatedOpenAPI = getStringFromGivenBalFile(TEST_RESOURCE.resolve(generatedFile));
        String expectedYaml = getStringFromGivenBalFile(TEST_RESOURCE.resolve(expectedPath));
        Assert.assertEquals(expectedYaml, generatedOpenAPI);
    }

    public Process getProcessForOpenAPISpecGeneration(String ballerinaFilePath) throws IOException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add(0, "openapi");
        buildArgs.add("-i");
        buildArgs.add(ballerinaFilePath);
        buildArgs.add("-o");
        buildArgs.add(tmpDir.toString());

        String balFile = "bal";

        if (System.getProperty("os.name").startsWith("Windows")) {
            balFile = "bal.bat";
        }
        buildArgs.add(0, TEST_DISTRIBUTION_PATH.resolve(DISTRIBUTION_FILE_NAME).resolve("bin")
                .resolve(balFile).toString());
        OUT.println("Executing: " + StringUtils.join(buildArgs, ' '));
        ProcessBuilder pb = new ProcessBuilder(buildArgs);
        pb.directory(TEST_RESOURCE.toFile());
        return pb.start();
    }
}
