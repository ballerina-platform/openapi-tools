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
package io.ballerina.openapi.generators.openapi;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.openapi.generators.openapi.TestUtils.deleteDirectory;

/**
 * This test class contains the service nodes related special scenarios.
 */
public class ServiceDeclarationNodesTests {
    private static final Path RES_DIR =
            Paths.get("src/test/resources/ballerina-to-openapi/advance").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Multiple services with same absolute path")
    public void multipleServiceWithSameAbsolute() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("multiple_services.bal");
        Path tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        try {
            String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("openapi"),
                    "multiple_service_01.yaml");
            OpenApiConverter openApiConverter = new OpenApiConverter();
            openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null, false);

            if (Files.exists(tempDir.resolve("hello_openapi.yaml")) && findFile(tempDir, "hello-") != null) {
                String generatedYaml = getStringFromGivenBalFile(tempDir, "hello_openapi.yaml");
                generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
                expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedYaml.contains(expectedYamlContent));
            } else {
                Assert.fail("Yaml was not generated");
            }
        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteDirectory(tempDir);
            System.gc();
        }
    }

    @Test(description = "Multiple services with absolute path as '/'. ")
    public void multipleServiceWithOutAbsolute() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("multiple_services_without_base_path.bal");
        Path tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        try {
            String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("openapi"),
                    "multiple_service_02.yaml");
            OpenApiConverter openApiConverter = new OpenApiConverter();
            openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null,
                    false);

            if (Files.exists(tempDir.resolve("multiple_services_without_base_path_openapi.yaml")) &&
                    findFile(tempDir, "multiple_services_without_base_path-") != null) {
                String generatedYaml = getStringFromGivenBalFile(tempDir,
                        "multiple_services_without_base_path_openapi.yaml");
                generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
                expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedYaml.contains(expectedYamlContent));
            } else {
                Assert.fail("Yaml was not generated");
            }
        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteDirectory(tempDir);
            System.gc();
        }
    }

    @Test(description = "Multiple services with no absolute path")
    public void multipleServiceNoBasePath() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("multiple_services_no_base_path.bal");
        Path tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        try {
            String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("openapi"),
                    "multiple_service_03.yaml");
            OpenApiConverter openApiConverter = new OpenApiConverter();
            openApiConverter.generateOAS3DefinitionsAllService(ballerinaFilePath, tempDir, null,
                    false);

            if (Files.exists(tempDir.resolve("multiple_services_no_base_path_openapi.yaml")) &&
                    findFile(tempDir, "multiple_services_no_base_path-") != null) {
                String generatedYaml = getStringFromGivenBalFile(tempDir,
                        "multiple_services_no_base_path_openapi.yaml");
                generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
                expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedYaml.contains(expectedYamlContent));
            } else {
                Assert.fail("Yaml was not generated");
            }
        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteDirectory(tempDir);
            System.gc();
        }
    }

    private static String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    public static String findFile(Path dir, String dirName) {
        FilenameFilter fileNameFilter = (dir1, name) -> name.startsWith(dirName);
        String[] fileNames = Objects.requireNonNull(dir.toFile().list(fileNameFilter));
        return fileNames.length > 0 ? fileNames[0] : null;
    }
}
