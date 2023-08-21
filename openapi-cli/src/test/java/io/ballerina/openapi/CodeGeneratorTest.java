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

import io.ballerina.openapi.cmd.BallerinaCodeGenerator;
import io.ballerina.openapi.core.GeneratorConstants;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.service.ServiceGenerationUtils;
import io.ballerina.openapi.core.model.Filter;
import io.ballerina.openapi.core.model.GenSrcFile;
import io.ballerina.openapi.generators.common.TestUtils;
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

import static io.ballerina.openapi.core.GeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.CONFIG_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.TEST_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.UTIL_FILE_NAME;
import static io.ballerina.openapi.core.model.GenSrcFile.GenFileType.GEN_SRC;

/**
 * Unit tests for {@link BallerinaCodeGenerator}.
 */
public class CodeGeneratorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    public static final String USER_DIR = "user.dir";
    Path resourcePath = Paths.get(System.getProperty(USER_DIR));
    Path expectedDirPath = RES_DIR.resolve(Paths.get("expected_gen"));
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test Ballerina skeleton generation")
    public void generateSkeleton() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedDirPath, "generateSkeleton.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter,
                    false, false, false);
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
        String definitionPath = RES_DIR.resolve("petstore.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedDirPath, "generate_client.bal");
            generator.generateClient(definitionPath, resourcePath.toString(), filter,
                    false, false);
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

    @Test(description = "Test Ballerina service skeleton generation for OAS 2.0")
    public void generateServiceForOAS2() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore_swagger.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedDirPath,
                    "petstore_service_swagger.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter,
                    false, false, false);
            if (Files.exists(resourcePath.resolve("openapipetstore_service.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore_service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedService.contains(expectedServiceContent),
                        "GeneratedService = " + generatedService);
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore_service.bal");
        }
    }

    @Test(description = "Test Ballerina client generation for OAS 2.0")
    public void generateClientForOAS2() {
        String definitionPath = RES_DIR.resolve("petstore_swagger.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedDirPath,
                    "petstore_client_swagger.bal");
            generator.generateClient(definitionPath, resourcePath.toString(), filter, false, false);
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
        String definitionPath = RES_DIR.resolve("x_init_description.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedDirPath, "x_init_description.bal");
            generator.generateClient(definitionPath, resourcePath.toString(), filter, false, false);

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
        String definitionPath = RES_DIR.resolve("petstore.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedDirPath, "nullable_types.bal");
            generator.generateClient(definitionPath, resourcePath.toString(), filter, true, false);

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
        String definitionPath = RES_DIR.resolve("petstore_nullable_false.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedDirPath, "nullable_false_types.bal");
            generator.generateClient(definitionPath, resourcePath.toString(), filter, true, false);

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
        String definitionPath = RES_DIR.resolve("petstore.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedDirPath, "utils.bal");
            generator.generateClient(definitionPath, resourcePath.toString(), filter, true, false);

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
        String definitionPath = RES_DIR.resolve("petstore_with_apikey_auth.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            String expectedConfigContent = getStringFromGivenBalFile(expectedDirPath, "api_key_config.toml");
            generator.setIncludeTestFiles(true);
            generator.generateClient(definitionPath, resourcePath.toString(), filter, true, false);

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
        String definitionPath = RES_DIR.resolve("petstore_tags.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        List<String> listTags = new ArrayList<>();
        listTags.add("pets");
        listTags.add("dogs");
        Filter filterCustom = new Filter(listTags, list2);
        try {
            String expectedSchemaContent = getStringFromGivenBalFile(expectedDirPath,
                "type_filtered_by_tags.bal");          
            generator.generateClient(definitionPath, resourcePath.toString(), filterCustom,
                    false, false);
            if (Files.exists(resourcePath.resolve("client.bal")) &&
                    Files.exists(resourcePath.resolve("types.bal"))) {
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
        String definitionPath = RES_DIR.resolve("openapi-client-rb.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(expectedDirPath,
                    "generate_client_requestbody.bal");
            generator.generateClient(definitionPath, resourcePath.toString(), filter, false, false);

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
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedDirPath, "generatedRB.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter,
                    false, false, false);
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
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedDirPath, "generated_bal.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter,
                    false, false, false);
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

    @Test(description = "Test Ballerina client generation for catch-all path")
    public void generateClientForResourceWithCatchAllPath() {
        String definitionPath = RES_DIR.resolve("petstore_catch_all_path.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            String expectedClientContent = getStringFromGivenBalFile(
                    expectedDirPath, "petstore_catch_all_path_client.bal");
            generator.generateClient(definitionPath, resourcePath.toString(), filter, false, true);
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

    @Test(description = "Test openapi definition to ballerina source code generation",
            dataProvider = "fileProvider")
    public void openApiToBallerinaCodeGenTest(String yamlFile, String expectedFile) {
        String definitionPath = RES_DIR.resolve(yamlFile).toString();
        Path expectedFilePath = RES_DIR.resolve(Paths.get("expected", expectedFile));

        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            String expectedContent = new String(Files.readAllBytes(expectedFilePath));
            List<GenSrcFile> generatedFileList = generator.generateBallerinaService(
                    Paths.get(definitionPath), "", filter, false, false, false);
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
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedDirPath, "multi_query_para.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter,
                    false, false, false);
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
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list1.add("list");
        Filter filter01 = new Filter(list1, list2);

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedDirPath, "petstoreTag.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter01,
                    false, false, false);
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
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        list1.add("list");
        list2.add("showPetById");
        Filter filter01 = new Filter(list1, list2);

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedDirPath, "petstoreOperation.bal");
            generator.generateService(definitionPath,  serviceName, resourcePath.toString(), filter01,
                    false, false, false);
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

    @Test(description = "Test code generation when no schemas given in service")
    public void testCodeGenerationWithoutSchemasService() {
        final String serviceName = "no_schema";
        String definitionPath = RES_DIR.resolve("no_schema.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            generator.generateService(definitionPath, serviceName, resourcePath.toString(),
                    filter, false, false, false);
            boolean hasTypeFileGenerated = Files.exists(resourcePath.resolve("no_schema_service.bal")) &&
                    Files.notExists(resourcePath.resolve("types.bal"));
            Assert.assertTrue(hasTypeFileGenerated, "Empty types.bal file has been generated");
        } catch (IOException | FormatterException | BallerinaOpenApiException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("no_schema_service.bal");
        }
    }

    @Test(description = "Test Ballerina skeleton generation")
    public void testGenericServiceGeneration() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore_original.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(expectedDirPath,
                    "generic_service_petstore_original.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter,
                    false, false, true);
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

    @Test(description = "Test code generation for no schema in given contract but user created type available in " +
            "service")
    public void testForGeneratingTypeFileWhenNoSchema() {
        final String serviceName = "no_schema";
        String definitionPath = RES_DIR.resolve("no_schema_with_type_bal.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        try {
            generator.generateService(definitionPath, serviceName, resourcePath.toString(),
                    filter, false, false, false);
            boolean hasTypeFileGenerated = Files.exists(resourcePath.resolve("no_schema_service.bal")) &&
                    Files.exists(resourcePath.resolve("types.bal"));
            Assert.assertTrue(hasTypeFileGenerated, "types.bal file has not been generated");
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("no_schema_service.bal");
        }
    }

    @Test(description = "Test service generation for request and responses with wildcard media type")
    public void testServiceGenerationWhenWildCardMediaTypeGiven() {
        final String serviceName = "openapipetstore";
        String definitionPath = RES_DIR.resolve("petstore_wildcard.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();

        try {
            String expectedServiceContent = getStringFromGivenBalFile(
                    expectedDirPath, "petstore_wildcard_service.bal");
            String expectedTypesContent = getStringFromGivenBalFile(
                    expectedDirPath, "petstore_wildcard_types.bal");
            generator.generateService(definitionPath, serviceName, resourcePath.toString(), filter,
                    false, false, false);
            if (Files.exists(resourcePath.resolve("openapipetstore_service.bal")) &&
                    Files.exists(resourcePath.resolve("types.bal"))) {
                String generatedService = getStringFromGivenBalFile(resourcePath, "openapipetstore_service.bal");
                generatedService = (generatedService.trim()).replaceAll("\\s+", "");
                expectedServiceContent = (expectedServiceContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedService.contains(expectedServiceContent),
                        "Expected service and actual generated service is not matching");

                String generatedTypes = getStringFromGivenBalFile(resourcePath, "types.bal");
                generatedTypes = (generatedTypes.trim()).replaceAll("\\s+", "");
                expectedTypesContent = (expectedTypesContent.trim()).replaceAll("\\s+", "");
                Assert.assertTrue(generatedTypes.contains(expectedTypesContent),
                        "Expected types and actual generated types are not matching");

            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException | FormatterException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } finally {
            deleteGeneratedFiles("openapipetstore_service.bal");
        }
    }

    @Test(description = "Functionality tests when invalid OpenAPI definition is given",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "OpenAPI definition has errors: .*")
    public void testForInvalidDefinition() throws IOException, BallerinaOpenApiException,
            FormatterException {
        String definitionPath = RES_DIR.resolve("invalid_petstore.yaml").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        generator.generateClient(definitionPath, "", filter, false, false);
    }

    @Test(description = "Functionality tests when swagger 1.2 contract is given as input",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Provided OpenAPI contract version is not supported in the tool. " +
                    "Use OpenAPI specification version 2 or higher")
    public void testGenerationForUnsupportedOpenAPIVersion() throws IOException, BallerinaOpenApiException,
            FormatterException {
        String definitionPath = RES_DIR.resolve("petstore_swagger_1.2.json").toString();
        BallerinaCodeGenerator generator = new BallerinaCodeGenerator();
        generator.generateClient(definitionPath, "", filter, false, false);
    }

    @Test
    public void escapeIdentifierTest() {
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("abc"), "abc");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("string"), "'string");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("int"), "'int");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("io.foo.bar"), "io\\.foo\\.bar");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("getV1CoreVersion"), "getV1CoreVersion");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("org-invitation"), "org\\-invitation");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("int?"), "int\\?");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("error"), "'error");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("foo_bar$_baz"), "foo_bar\\$_baz");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("foo_bar_baz"), "foo_bar_baz");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("foo'baz"), "foo\\'baz");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("200"), "'200");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("2023-06-28"), "'2023\\-06\\-28");
        Assert.assertEquals(GeneratorUtils.escapeIdentifier("3h"), "'3h");
    }

    @Test
    public void testForSelectMediaType() {
        Assert.assertEquals(ServiceGenerationUtils.selectMediaType("text/json"),
                GeneratorConstants.APPLICATION_JSON);
        Assert.assertEquals(ServiceGenerationUtils.selectMediaType("text/xml"),
                GeneratorConstants.APPLICATION_XML);
    }

    private String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {

        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining(System.lineSeparator()));
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
                {"petstore_catch_all_path.yaml", "petstore_catch_all_path.bal"}
        };
    }

    @AfterTest
    public void clean() throws IOException {
        System.setErr(null);
        System.setOut(null);
        TestUtils.deleteGeneratedFiles();
    }
}
