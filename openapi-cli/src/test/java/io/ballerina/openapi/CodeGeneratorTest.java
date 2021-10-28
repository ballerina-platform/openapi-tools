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
package io.ballerina.openapi;

import io.ballerina.openapi.cmd.CodeGenerator;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.cmd.model.GenSrcFile;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.ballerina.openapi.generators.GeneratorUtils;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.openapi.cmd.model.GenSrcFile.GenFileType.GEN_SRC;
import static io.ballerina.openapi.generators.GeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.generators.GeneratorConstants.CONFIG_FILE_NAME;
import static io.ballerina.openapi.generators.GeneratorConstants.GenType.GEN_CLIENT;
import static io.ballerina.openapi.generators.GeneratorConstants.TEST_FILE_NAME;
import static io.ballerina.openapi.generators.GeneratorConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.generators.GeneratorConstants.USER_DIR;
import static io.ballerina.openapi.generators.GeneratorConstants.UTIL_FILE_NAME;

/**
 * Unit tests for {@link CodeGenerator}.
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
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter, false);
            if (Files.exists(resourcePath.resolve("openapipetstore_service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore_service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore_service.bal");
        }
    }

    @Test(description = "Test Ballerina client generation")
    public void generateClient() {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedServiceFile, "generate_client.bal");
            generator.generateClient(definitionPath, clientName, resourcePath.toString(), filter, false);
            if (Files.exists(resourcePath.resolve("client.bal"))) {
                String generatedClient = getStringFromGivenBalFile(resourcePath, "client.bal");
                generatedClient = (generatedClient.trim()).replaceAll("\\s+", "");
                expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedClient.contains(expectedClientContent));
            } else {
                Assert.fail("Client was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the client. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("client.bal");
        }
    }

    @Test(description = "Test duplicated files generation")
    public void generateDuplicatedFiles() {
        List<File> duplicatedFileList = new ArrayList<>();
        duplicatedFileList.add(new File(TEST_FILE_NAME));
        duplicatedFileList.add(new File(CONFIG_FILE_NAME));
        duplicatedFileList.add(new File(CLIENT_FILE_NAME));
        duplicatedFileList.add(new File(UTIL_FILE_NAME));
        duplicatedFileList.add(new File(TYPE_FILE_NAME));

        String testPackageName = "testDuplicatedFiles";
        GenSrcFile duplicatedTestFile = new GenSrcFile(GEN_SRC, testPackageName, TEST_FILE_NAME, "");
        GenSrcFile duplicatedConfigFile = new GenSrcFile(GEN_SRC, testPackageName, CONFIG_FILE_NAME, "");
        GenSrcFile duplicatedClientFile = new GenSrcFile(GEN_SRC, testPackageName, CLIENT_FILE_NAME, "");
        GenSrcFile duplicatedUtilsFile = new GenSrcFile(GEN_SRC, testPackageName, UTIL_FILE_NAME, "");
        GenSrcFile duplicatedTypesFile = new GenSrcFile(GEN_SRC, testPackageName, TYPE_FILE_NAME, "");

        GeneratorUtils.setGeneratedFileName(duplicatedFileList, duplicatedTestFile, 0);
        GeneratorUtils.setGeneratedFileName(duplicatedFileList, duplicatedConfigFile, 0);
        GeneratorUtils.setGeneratedFileName(duplicatedFileList, duplicatedClientFile, 0);
        GeneratorUtils.setGeneratedFileName(duplicatedFileList, duplicatedUtilsFile, 0);
        GeneratorUtils.setGeneratedFileName(duplicatedFileList, duplicatedTypesFile, 0);

        Assert.assertEquals(duplicatedTestFile.getFileName(), "test.1.bal");
        Assert.assertEquals(duplicatedConfigFile.getFileName(), "Config.1.toml");
        Assert.assertEquals(duplicatedClientFile.getFileName(), "client.1.bal");
        Assert.assertEquals(duplicatedUtilsFile.getFileName(), "utils.1.bal");
        Assert.assertEquals(duplicatedTypesFile.getFileName(), "types.1.bal");
    }

    @Test(description = "Test Ballerina client generation with doc comments in class init function")
    public void generateClientWithInitDocComments() {
        final String clientName = "client";
        String definitionPath = RES_DIR.resolve("x_init_description.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedServiceFile, "x_init_description.bal");
            generator.generateClient(definitionPath, clientName, resourcePath.toString(), filter, false);

            if (Files.exists(resourcePath.resolve("client.bal"))) {
                String generatedClient = getStringFromGivenBalFile(resourcePath, "client.bal");
                generatedClient = (generatedClient.trim()).replaceAll("\\s+", "");
                expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedClient.contains(expectedClientContent));
            } else {
                Assert.fail("Client was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the client. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("client.bal");
        }
    }

    @Test(description = "Test Ballerina types generation when nullable option is given")
    public void generateTypesWithNullableFields() {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedServiceFile, "nullable_types.bal");
            generator.generateClient(definitionPath, clientName, resourcePath.toString(), filter, true);

            if (Files.exists(resourcePath.resolve("types.bal"))) {
                String generatedClient = getStringFromGivenBalFile(resourcePath, "types.bal");
                generatedClient = (generatedClient.trim()).replaceAll("\\s+", "");
                expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedClient.contains(expectedClientContent));
            } else {
                Assert.fail("Types were not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the client. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("client.bal");
        }
    }

    @Test(description = "Test Ballerina types generation when nullable option is given in the cmd and definition both")
    public void generateTypesWithNullableFieldsAndGlobalNullableTrue() {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore_nullable_false.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedServiceFile, "nullable_false_types.bal");
            generator.generateClient(definitionPath, clientName, resourcePath.toString(), filter, true);

            if (Files.exists(resourcePath.resolve("types.bal"))) {
                String generatedClient = getStringFromGivenBalFile(resourcePath, "types.bal");
                generatedClient = (generatedClient.trim()).replaceAll("\\s+", "");
                expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedClient.contains(expectedClientContent));
            } else {
                Assert.fail("Types were not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the client. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("client.bal");
        }
    }

    @Test(description = "Test Ballerina utils file generation")
    public void generateUtilsFile() {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedServiceFile, "utils.bal");
            generator.generateClient(definitionPath, clientName, resourcePath.toString(), filter, true);

            if (Files.exists(resourcePath.resolve("utils.bal"))) {
                String generatedClient = getStringFromGivenBalFile(resourcePath, "utils.bal");
                generatedClient = (generatedClient.trim()).replaceAll("\\s+", "");
                expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedClient.contains(expectedClientContent));
            } else {
                Assert.fail("Utils were not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the connector. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("client.bal");
        }
    }

    @Test(description = "Test Ballerina Config.toml file generation for API Key authentication")
    public void generateConfigFile() {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore_with_apikey_auth.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedConfigContent = getStringFromGivenBalFile(expectedServiceFile, "api_key_config.toml");
            generator.generateClient(definitionPath, clientName, resourcePath.toString(), filter, true);

            if (Files.exists(resourcePath.resolve("tests/Config.toml"))) {
                String generateConfigContent = getStringFromGivenBalFile(resourcePath, "tests/Config.toml");
                generateConfigContent = (generateConfigContent.trim()).replaceAll("\\s+", "");
                expectedConfigContent = (expectedConfigContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generateConfigContent.contains(expectedConfigContent));
            } else {
                Assert.fail("Config.toml was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the connector. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("client.bal");
        }
    }

    @Test(description = "Test Ballerina client generation")
    public void generateFilteredClient() {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore_tags.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        List<String> listTags = new ArrayList<>();
        listTags.add("pets");
        listTags.add("dogs");
        Filter filterCustom = new Filter(listTags, list2);
        try {
            String expectedSchemaContent = getStringFromGivenBalFile(expectedServiceFile, "type_filtered_by_tags.bal");
            generator.generateClient(definitionPath, clientName, resourcePath.toString(),
                    filterCustom, false);
            if (Files.exists(resourcePath.resolve("types.bal")) && Files.exists(resourcePath.resolve("types.bal"))) {
                String generatedSchema = getStringFromGivenBalFile(resourcePath, "types.bal");
                generatedSchema = (generatedSchema.trim()).replaceAll("\\s+", "");
                expectedSchemaContent = (expectedSchemaContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedSchema.contains(expectedSchemaContent));
            } else {
                Assert.fail("Client was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the client. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("client.bal");
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
            generator.generateClient(definitionPath, clientName, resourcePath.toString(), filter, false);

            if (Files.exists(resourcePath.resolve("client.bal"))) {
                String generatedClient = getStringFromGivenBalFile(resourcePath, "client.bal");
                generatedClient = (generatedClient.trim()).replaceAll("\\s+", "");
                expectedClientContent = (expectedClientContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedClient.contains(expectedClientContent));
            } else {
                Assert.fail("Client was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the client. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore_client.bal");
        }
    }

    @Test(description = "Test Ballerina skeleton generation")
    public void generateSkeletonForRequestbody() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("requestBody.yaml").toString();
        CodeGenerator generator = new CodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedServiceFile, "generatedRB.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter, false);
            if (Files.exists(resourcePath.resolve("openapipetstore_service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore_service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore_service.bal");
        }
    }

    @Test(description = "Test Ballerina skeleton generation for multiple Path parameter")
    public void generateSkeletonForTwoPathParameter() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("multiPathParam.yaml").toString();
        CodeGenerator generator = new CodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedServiceFile, "generated_bal.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter, false);
            if (Files.exists(resourcePath.resolve("openapipetstore_service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore_service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore_service.bal");
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
            List<GenSrcFile> generatedFileList = generator.generateBalSource(GeneratorConstants.GenType.GEN_SERVICE,
                    definitionPath, "", filter, false);
            if (generatedFileList.size() > 0) {
                GenSrcFile actualGeneratedContent = generatedFileList.get(0);
                Assert.assertEquals((actualGeneratedContent.getContent().trim()).replaceAll("\\s+", ""),
                        (expectedContent.trim()).replaceAll("\\s+", ""),
                        "expected content and actual generated content is mismatched for: " + yamlFile);
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the ballerina content for the openapi definition: "
                    + yamlFile + " " + e.getMessage());
        }
    }

    @Test(description = "Test Ballerina skeleton generation for multiple Query parameter")
    public void generateSkeletonForTwoQueryParameter() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("multiQueryParam.yaml").toString();
        CodeGenerator generator = new CodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedServiceFile, "multi_query_para.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter, false);
            if (Files.exists(resourcePath.resolve("openapipetstore_service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore_service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore_service.bal");
        }
    }

    @Test(description = "Test Ballerina skeleton generation for tag filter")
    public void generateSkeletonForTagFilter() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstoreTag.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list1.add("list");
        Filter filter01 = new Filter(list1, list2);

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedServiceFile, "petstoreTag.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter01, false);
            if (Files.exists(resourcePath.resolve("openapipetstore_service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore_service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore_service.bal");
        }
    }

    @Test(description = "Test Ballerina skeleton generation for operation filter")
    public void generateSkeletonForOperationFilter() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstoreOperation.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list1.add("list");
        list2.add("showPetById");
        Filter filter01 = new Filter(list1, list2);

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedServiceFile, "petstoreOperation.bal");
            generator.generateService(definitionPath,  serviceName, resourcePath.toString(), filter01, false);
            if (Files.exists(resourcePath.resolve("openapipetstore_service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore_service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");

                Assert.assertTrue(generatedService.contains(expectedServiceContent));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore_service.bal");
        }
    }

    @Test(description = "Test code generation when no schemas given")
    public void testCodeGenerationWithoutSchemas() {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("no_schema.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        try {
            generator.generateClient(definitionPath, clientName, resourcePath.toString(), filter, false);
            if (Files.exists(resourcePath.resolve("client.bal")) &&
                    Files.notExists(resourcePath.resolve("types.bal"))) {
                Assert.assertTrue(true);
            } else {
                Assert.fail("Empty types.bal file has been generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the client. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("client.bal");
        }
    }

    @Test(description = "Functionality tests when invalid OpenAPI definition is given",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "OpenAPI definition has errors: .*")
    public void testForInvalidDefinition() throws IOException, BallerinaOpenApiException,
            FormatterException {
        final String clientName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("invalid_petstore.yaml").toString();
        CodeGenerator generator = new CodeGenerator();
        generator.generateBalSource(GEN_CLIENT, definitionPath, clientName, filter, false);
    }

    @Test
    public void escapeIdentifierTest() {
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("abc"), "abc");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("string"), "'string");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("int"), "'int");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("io.foo.bar"), "'io\\.foo\\.bar");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("getV1CoreVersion"), "getV1CoreVersion");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("org-invitation"), "'org\\-invitation");
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
            Files.deleteIfExists(resourcePath.resolve("client.bal"));
            Files.deleteIfExists(resourcePath.resolve("types.bal"));
            Files.deleteIfExists(resourcePath.resolve("utils.bal"));
            Files.deleteIfExists(resourcePath.resolve("test.bal"));
            Files.deleteIfExists(resourcePath.resolve("Config.toml"));
            FileUtils.deleteDirectory(new File(resourcePath + "/tests"));
        } catch (IOException ignored) {
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

    @AfterTest
    public void clean() {
        System.setErr(null);
        System.setOut(null);
    }
}
