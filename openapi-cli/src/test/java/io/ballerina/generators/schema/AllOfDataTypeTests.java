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

package io.ballerina.generators.schema;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.generators.BallerinaSchemaGenerator;
import io.ballerina.generators.common.TestUtils;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The tests are related to the allOF data type in the swagger.
 */
public class AllOfDataTypeTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/schema").toAbsolutePath();
    SyntaxTree syntaxTree;

    @Test(description = "Scenario09-Generate record for schema has allOf reference")
    public void generateScenario09() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/scenario09.yaml");
        syntaxTree = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        TestUtils.compareGeneratedSyntaxTreewithExpectedSyntaxTree("schema/ballerina/schema09.bal", syntaxTree);
    }
}
