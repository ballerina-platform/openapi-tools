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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.openapi.generators.openapi.TestUtils.compareWithGeneratedFile;

/**
 * Tests for openAPI info section mapping.
 */
public class OpenAPIInfoTests {
    private static final Path RES_DIR =
            Paths.get("src/test/resources/ballerina-to-openapi/openapi_info").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "Generate OpenAPI spec with default value with empty Ballerina.toml")
    public void defaultOpenAPIInfo() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("project01/project04.bal");
        compareWithGeneratedFile(ballerinaFilePath, "openapi_info/project01.yaml");
    }

    @Test(description = "Generate OpenAPI spec with default value with version Ballerina.toml")
    public void versionOpenAPIInfo() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("project02/project02.bal");
        compareWithGeneratedFile(ballerinaFilePath, "openapi_info/project02.yaml");
    }

    @Test(description = "Generate OpenAPI spec with default value with version Ballerina.toml")
    public void openAPIAnnotation() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("project03/project03.bal");
        compareWithGeneratedFile(ballerinaFilePath, "openapi_info/project02.yaml");
    }


}
