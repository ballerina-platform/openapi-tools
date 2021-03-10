/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.openapi;

import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.generators.OpenApiException;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.model.GenSrcFile;
import org.ballerinalang.openapi.utils.GeneratorConstants.GenType;
import org.ballerinalang.openapi.utils.TypeExtractorUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.openapi.utils.GeneratorConstants.USER_DIR;

/**
 * Unit tests for {@link org.ballerinalang.openapi.CodeGenerator}.
 */
public class CodeGeneratorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    Path resourcePath = Paths.get(System.getProperty(USER_DIR));
    Path expectedServiceFile = RES_DIR.resolve(Paths.get("expected_gen"));
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test Ballerina skeleton generation")
    public void generateSkeleton() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore.yaml").toString();
        CodeGenerator generator = new CodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedServiceFile, "generateSkeleton.bal");
            generator.generateService(definitionPath, definitionPath, definitionPath, serviceName,
                    resourcePath.toString(), filter);
            if (Files.exists(resourcePath.resolve("openapipetstore-service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore-service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException | OpenApiException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore-service.bal");
        }
    }

    @Test(description = "Test Ballerina client generation")
    public void generateClient() {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedServiceFile, "generate_client.bal");
            generator.generateClient(definitionPath, definitionPath, clientName, resourcePath.toString(), filter);

            if (Files.exists(resourcePath.resolve("openapipetstore-client.bal"))) {
                String generatedClient = getStringFromGivenBalFile(resourcePath, "openapipetstore-client.bal");
                generatedClient = (generatedClient.trim()).replaceAll("\\s+", "");
                expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedClient.contains(expectedClientContent));
            } else {
                Assert.fail("Client was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException | OpenApiException e) {
            Assert.fail("Error while generating the client. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore-client.bal");
        }
    }

    @Test(description = "Test Ballerina client generation with request body")
    public void generateClientwithRequestBody() {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("openapi-client-rb.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedServiceFile,
                    "generate_client_requestbody.bal");
            generator.generateClient(definitionPath, definitionPath, clientName, resourcePath.toString(), filter);

            if (Files.exists(resourcePath.resolve("openapipetstore-client.bal"))) {
                String generatedClient = getStringFromGivenBalFile(resourcePath, "openapipetstore-client.bal");
                generatedClient = (generatedClient.trim()).replaceAll("\\s+", "");
                expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedClient.contains(expectedClientContent));
            } else {
                Assert.fail("Client was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException | OpenApiException e) {
            Assert.fail("Error while generating the client. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore-client.bal");
        }
    }

    @Test(description = "Test Ballerina skeleton generation")
    public void generateSkeletonForRequestbody() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("requestBody.yaml").toString();
        CodeGenerator generator = new CodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedServiceFile, "generatedRB.bal");
            generator.generateService(definitionPath, definitionPath, definitionPath, serviceName,
                    resourcePath.toString(), filter);
            if (Files.exists(resourcePath.resolve("openapipetstore-service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore-service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException | OpenApiException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore-service.bal");
        }
    }

    @Test(description = "Test Ballerina skeleton generation for multiple Path parameter")
    public void generateSkeletonForTwoPathParameter() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("multiPathParam.yaml").toString();;
        CodeGenerator generator = new CodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedServiceFile, "generated_bal.bal");
            generator.generateService(definitionPath, definitionPath, definitionPath, serviceName,
                    resourcePath.toString(), filter);
            if (Files.exists(resourcePath.resolve("openapipetstore-service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore-service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException | OpenApiException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore-service.bal");
        }
    }

    @Test(description = "Test openapi definition to ballerina source code generation",
            dataProvider = "fileProvider", enabled = false)
    public void openApiToBallerinaCodeGenTest(String yamlFile, String expectedFile) {
        String definitionPath = RES_DIR.resolve(yamlFile).toString();
        Path expectedFilePath = RES_DIR.resolve(Paths.get("expected", expectedFile));

        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedContent = new String(Files.readAllBytes(expectedFilePath));
            List<GenSrcFile> generatedFileList = generator.generateBalSource(GenType.GEN_SERVICE,
                    definitionPath, "", "", filter);
            if (generatedFileList.size() > 0) {
                GenSrcFile actualGeneratedContent = generatedFileList.get(0);
                Assert.assertEquals((actualGeneratedContent.getContent().trim()).replaceAll("\\s+", ""),
                        (expectedContent.trim()).replaceAll("\\s+", ""),
                        "expected content and actual generated content is mismatched for: " + yamlFile);
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException | OpenApiException e) {
            Assert.fail("Error while generating the ballerina content for the openapi definition: "
                    + yamlFile + " " + e.getMessage());
        }
    }

    @Test(description = "Test Ballerina skeleton generation for multiple Query parameter", enabled = true)
    public void generateSkeletonForTwoQueryParameter() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("multiQueryParam.yaml").toString();;
        CodeGenerator generator = new CodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedServiceFile, "multi_query_para.bal");
            generator.generateService(definitionPath, definitionPath, definitionPath, serviceName,
                    resourcePath.toString(), filter);
            if (Files.exists(resourcePath.resolve("openapipetstore-service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore-service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException | OpenApiException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore-service.bal");
        }
    }

    @Test(description = "Test Ballerina skeleton generation for tag filter", enabled = true)
    public void generateSkeletonForTagFilter() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstoreTag.yaml").toString();;
        CodeGenerator generator = new CodeGenerator();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list1.add("list");
        Filter filter01 = new Filter(list1, list2);

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedServiceFile, "petstoreTag.bal");
            generator.generateService(definitionPath, definitionPath, definitionPath, serviceName,
                    resourcePath.toString(), filter01);
            if (Files.exists(resourcePath.resolve("openapipetstore-service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore-service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException | OpenApiException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore-service.bal");
        }
    }

    @Test(description = "Test Ballerina skeleton generation for operation filter", enabled = true)
    public void generateSkeletonForOperationFilter() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstoreOperation.yaml").toString();;
        CodeGenerator generator = new CodeGenerator();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list1.add("list");
        list2.add("showPetById");
        Filter filter01 = new Filter(list1, list2);

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedServiceFile, "petstoreOperation.bal");
            generator.generateService(definitionPath, definitionPath, definitionPath, serviceName,
                    resourcePath.toString(), filter01);
            if (Files.exists(resourcePath.resolve("openapipetstore-service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore-service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException | OpenApiException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore-service.bal");
        }
    }


    @Test
    public void escapeIdentifierTest() {
        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier("abc"), "abc");
        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier("string"), "'string");
        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier("int"), "'int");
        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier("io.foo.bar"), "'io\\.foo\\.bar");
        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier("getV1CoreVersion"), "getV1CoreVersion");
//        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier
//        ("sample_service_\\ \\!\\:\\[\\;"), "'sample_service_\\ \\!\\:\\[\\;");
//        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier
//        ("listPets resource_!$:[;"), "'listPets\\ resource_\\!\\$\\:\\[\\;");
    }

    @Test
    public void escapeTypeTest() {
        Assert.assertEquals(TypeExtractorUtil.escapeType("abc"), "abc");
        Assert.assertEquals(TypeExtractorUtil.escapeType("string"), "string");
        Assert.assertEquals(TypeExtractorUtil.escapeType("int"), "int");
        Assert.assertEquals(TypeExtractorUtil.escapeType("io.foo.bar"), "'io\\.foo\\.bar");
        Assert.assertEquals(TypeExtractorUtil.escapeType("getV1CoreVersion"), "getV1CoreVersion");
    }

    private String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {

        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    private void deleteGeneratedFiles(String filename) {
        try {
            Files.deleteIfExists(resourcePath.resolve(filename));
            Files.deleteIfExists(resourcePath.resolve("schema.bal"));
        } catch (IOException e) {
            //Ignore the exception
        }
    }

    @DataProvider(name = "fileProvider")
    public Object[][] fileProvider() {
        return new Object[][]{
                {"emptyService.yaml", "emptyService.bal"},
                {"emptyPath.yaml", "emptyPath.bal"},
                {"noOperationId.yaml", "noOperationId.bal"},
                {"multiMethodResources.yaml", "multiMethodResources.bal"},
                {"nonEmptyPath.yaml", "nonEmptyPath.bal"},
                {"petstore.yaml", "petstore.bal"},
        };
    }
}
