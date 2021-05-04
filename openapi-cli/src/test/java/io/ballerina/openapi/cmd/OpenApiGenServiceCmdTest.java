/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.cmd;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.generators.GeneratorConstants.USER_DIR;

/**
 * This class contains tests necessary to test OpenApi Generate Service command.
 */
public class OpenApiGenServiceCmdTest extends OpenAPICommandTest {
    Path resourcePath = Paths.get(System.getProperty(USER_DIR));

    @Test(description = "Test openapi gen-service for successful service generation with inline request body type",
            enabled = false)
    public void testInlineRequestBodyServiceGen() throws IOException {

        Path inlineYaml = getExecuteCommand("inline-request-body.yaml", "inlineservice");
        Path expectedServiceFile = resourceDir.resolve(Paths.get("expected_gen", "inline-request-expected.bal"));
        //Read the service file and make string
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile);
        String expectedService = expectedServiceLines.collect(Collectors.joining("\n"));
        if (Files.exists(resourcePath.resolve("inlineservice-service.bal")) &&
                Files.exists(resourcePath.resolve("schema.bal"))) {
            //Convert file content details to one string
            String generatedService = getStringFromFile(resourcePath.resolve("inlineservice-service.bal"));
            expectedService = replaceContractPath(expectedServiceLines, expectedService, generatedService);
            if (replaceWhiteSpace(expectedService).equals(replaceWhiteSpace(generatedService))) {
                Assert.assertTrue(true);
                //Clean the generated files
                deleteGeneratedFiles("inlineservice-service.bal");
            } else {
                deleteGeneratedFiles("inlineservice-service.bal");
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + inlineYaml.toString());
            }
        } else {
            Assert.fail("Service generation for inline request body type failed.");
        }
    }

    @Test(description = "Test open-api genservice for successful service generation with all of schema type",
            enabled = false)
    public void testAllOfSchemaGen() throws IOException {
        Path allOfYaml = getExecuteCommand("allof-petstore.yaml", "allOfYaml");
        Path expectedServiceFile = resourceDir.resolve(Paths.get("expected_gen",
                "allOf-schema-petstore.bal"));
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile);
        String expectedSchema = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        if (Files.exists(resourcePath.resolve("allofyaml-service.bal")) && Files.exists(resourcePath.resolve("schema" +
                ".bal"))) {
            String generatedSchema = getStringFromFile(resourcePath.resolve("schema.bal"));
            if (replaceWhiteSpace(expectedSchema).equals(replaceWhiteSpace(generatedSchema))) {
                Assert.assertTrue(true);
                deleteGeneratedFiles("allofyaml-service.bal");
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + allOfYaml.toString());
            }
        } else {
            Assert.fail("Service generation for All Of Schema type failed.");
        }
    }

    @Test(description = "Test open-api genservice for successful service generation with OneOf schema type", enabled
            = false)
    public void testOneOfSchemaGen() throws IOException {
        Path oneOfYaml = getExecuteCommand("oneof-petstore.yaml", "oneofservice");
        Path expectedServiceFile = resourceDir.resolve(Paths.get("expected_gen",
                "oneof-schema-petstore.bal"));
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile);
        String expectedService = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        if (Files.exists(resourcePath.resolve("oneofservice-service.bal")) &&
                Files.exists(resourcePath.resolve("schema.bal"))) {
            String generatedService = getStringFromFile(resourcePath.resolve("oneofservice-service.bal"));
            expectedService = replaceContractPath(expectedServiceLines, expectedService, generatedService);
            if (replaceWhiteSpace(expectedService).equals(replaceWhiteSpace(generatedService))) {
                Assert.assertTrue(true);
                deleteGeneratedFiles("oneofservice-service.bal");
            } else {
                Assert.fail("Expected content and actual generated content is mismatched for: "
                        + oneOfYaml.toString());
            }
        } else {
            Assert.fail("Service generation for OneOf Schema type failed.");
        }
    }
}
