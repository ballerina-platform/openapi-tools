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

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This test class for the covering the unit tests for constraint
 * {@link io.ballerina.openapi.converter.service.ConstraintAnnotation} scenarios.
 *
 */
public class ConstraintTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/ballerina-to-openapi").toAbsolutePath();

    @Test(description = "When the record field has constraint type")
    public void testMapFiled() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("constraint/record_field.bal");
        //Compare generated yaml file with expected yaml content
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "constraint/record_field.yaml");
    }

    @Test(description = "When the record field has array type")
    public void testArrayType() throws IOException {
        Path ballerinaFilePath = RES_DIR.resolve("constraint/array.bal");
        //Compare generated yaml file with expected yaml content
        TestUtils.compareWithGeneratedFile(ballerinaFilePath, "constraint/array.yaml");
    }
}
