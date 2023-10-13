/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This test class is used to maintain test for datatype in ballerina.
 */
public class DataTypeTests {
    private static final Path RES_DIR =
            Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }

    @Test(description = "When the record field has type definitions")
    public void testForAllTypeDefinitions() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("data_type/type_def.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "data_type/type_def.yaml");
    }

    @Test(description = "When the record field has type definitions with nullable")
    public void testForAllTypeDefinitionWithNullableValue() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("data_type/nullable_type_def.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "data_type/nullable_type_def.yaml");
    }

    @Test(description = "test for tuple type scenarios")
    public void testForTupleType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("data_type/tuple_types.bal");
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "data_type/tuple_type.yaml");
    }

    @AfterMethod
    public void cleanUp() {
        TestUtils.deleteDirectory(this.tempDir);
    }

    @AfterTest
    public void clean() {
        System.setErr(null);
        System.setOut(null);
    }
}
