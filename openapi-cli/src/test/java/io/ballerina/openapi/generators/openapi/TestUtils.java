/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.generators.openapi;

import io.ballerina.openapi.cmd.OASContractGenerator;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This Util class for storing the common utils related to test in ballerina to openAPI command implementation.
 */
public class TestUtils {

    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi/").toAbsolutePath();

    private static String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    public static void deleteGeneratedFiles(String filename, Path tempDir) {
        try {
            Files.deleteIfExists(tempDir.resolve(filename));
            Files.deleteIfExists(tempDir.resolve("schema.bal"));
        } catch (IOException ignored) {
        }
    }

    public static void compareWithGeneratedFile(Path ballerinaFilePath, String yamlFile) throws IOException {
        compareWithGeneratedFile(new OASContractGenerator(), ballerinaFilePath, yamlFile);
    }

    public static List<OpenAPIConverterDiagnostic> compareWithGeneratedFile(OASContractGenerator openApiConverter,
                                                                            Path ballerinaFilePath, String yamlFile)
                                                                            throws IOException {
        Path tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
        try {
            String expectedYamlContent = getStringFromGivenBalFile(RES_DIR.resolve("expected_gen"), yamlFile);
            openApiConverter.generateOAS3DefinitionsAllService(
                    ballerinaFilePath, tempDir, null, false);
            if (Files.exists(tempDir.resolve("payloadV_openapi.yaml"))) {
                String generatedYaml = getStringFromGivenBalFile(tempDir, "payloadV_openapi.yaml");
                generatedYaml = (generatedYaml.trim()).replaceAll("\\s+", "");
                expectedYamlContent = (expectedYamlContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedYaml.contains(expectedYamlContent));
            } else {
                Assert.fail("Yaml was not generated");
            }
            return openApiConverter.getErrors();
        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
            return List.of();
        } finally {
            deleteGeneratedFiles("payloadV_openapi.yaml", tempDir);
            deleteDirectory(tempDir);
            System.gc();
        }
    }

    public static void deleteDirectory(Path path) {
        if (!Files.exists(path)) {
            return;
        }

        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException ignored) {
            //ignore
        }
    }
}
