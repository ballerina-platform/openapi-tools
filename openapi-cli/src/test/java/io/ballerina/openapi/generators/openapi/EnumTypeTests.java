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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This test class for the covering the unit tests for record scenarios.
 */
public class EnumTypeTests {
    private static final Path RES_DIR =
            Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();
    private Path tempDir;

    @BeforeMethod
    public void setup() throws IOException {
        this.tempDir = Files.createTempDirectory("bal-to-openapi-test-out-" + System.nanoTime());
    }
    @Test(description = "When the record field has reference to enum type")
    public void testEnumType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("data_type/enum.bal");
        //Compare generated yaml file with expected yaml content
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "data_type/enum.yaml");
    }

    @Test(description = "When the record field has reference to enum type array")
    public void testEnumTypeInArray() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("data_type/enum_array.bal");
        //Compare generated yaml file with expected yaml content
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "data_type/enum_array_type.yaml");
    }

    @Test(description = "Test for the record field has reference to enum with constant value")
    public void testEnumWithConstantValue() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("data_type/enum_with_constant_value.bal");
        //Compare generated yaml file with expected yaml content
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "data_type/enum_with_value.yaml");
    }

    @Test(description = "Test for query and path parameters having references to enums with constant value")
    public void testEnumsWithinQueryParameters() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("data_type/enum_query.bal");
        //Compare generated yaml file with expected yaml content
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "data_type/enum_query.yaml");
    }

    @Test(description = "Test for header parameters having references to enums with constant values")
    public void testEnumsWithinHeaderParameters() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("data_type/enum_header.bal");
        //Compare generated yaml file with expected yaml content
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "data_type/enum_header.yaml");
    }

    //TODO:Enable this test after fixing issue : https://github.com/ballerina-platform/openapi-tools/issues/1472
    @Test(description = "Test for enum that defined as ballerina types", enabled = false)
    public void testEnumsDefinedAsBalTypes() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("data_type/bal_type_enum.bal");
        //Compare generated yaml file with expected yaml content
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "data_type/bal_type_enum.yaml");
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
