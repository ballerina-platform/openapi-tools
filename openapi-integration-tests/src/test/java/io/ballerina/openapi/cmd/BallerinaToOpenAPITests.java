/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.openapi.cmd.TestUtil.DISTRIBUTIONS_DIR;
import static io.ballerina.openapi.cmd.TestUtil.RESOURCES_PATH;

/**
 * This {@code BallerinaToOpenAPITests} contains all the ballerina to openapi command with compiler annotation.
 */
public class BallerinaToOpenAPITests {
    public static final String DISTRIBUTION_FILE_NAME = DISTRIBUTIONS_DIR.toString();
    public static final Path TEST_RESOURCE = Paths.get(RESOURCES_PATH.toString() + "/ballerina_sources");

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
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve("mTitle_openapi.yaml")));
    }

    @Test(description = "Check ballerina to openapi generator command with multiple service with annotation")
    public void multipleServiceWithAnnotation() throws IOException, InterruptedException {
        List<String> buildArgs = new LinkedList<>();
        buildArgs.add("-i");
        buildArgs.add("project_8/service.bal");
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
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
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertFalse(Files.exists(TEST_RESOURCE.resolve("query_openapi.yaml")));
    }

    @AfterClass
    public void cleanUp() throws IOException {
        TestUtil.cleanDistribution();
    }

    //Replace contract file path in generated service file with common URL.
    public String replaceContractPath(Stream<String> expectedServiceLines, String expectedService,
                                      String generatedService) {

        Pattern pattern = Pattern.compile("\\bcontract\\b: \"(.*?)\"");
        Matcher matcher = pattern.matcher(generatedService);
        matcher.find();
        String contractPath = "contract: " + "\"" + matcher.group(1) + "\"";
        expectedService = expectedService.replaceAll("\\bcontract\\b: \"(.*?)\"",
                Matcher.quoteReplacement(contractPath));
        expectedServiceLines.close();
        return expectedService;
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
        boolean successful = TestUtil.executeOpenAPI(DISTRIBUTION_FILE_NAME, TEST_RESOURCE, buildArgs);
        Assert.assertTrue(Files.exists(TEST_RESOURCE.resolve(generatedFile)));
        String generatedOpenAPI = getStringFromGivenBalFile(TEST_RESOURCE.resolve(generatedFile));
        String expectedYaml = getStringFromGivenBalFile(TEST_RESOURCE.resolve(expectedPath));
        Assert.assertEquals(expectedYaml, generatedOpenAPI);
    }
}
