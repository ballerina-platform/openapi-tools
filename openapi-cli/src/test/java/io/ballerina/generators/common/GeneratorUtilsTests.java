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

package io.ballerina.generators.common;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.generators.BallerinaSchemaGenerator;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.generators.GeneratorUtils.extractReferenceType;
import static io.ballerina.generators.GeneratorUtils.getBallerinaOpenApiType;
import static io.ballerina.generators.GeneratorUtils.getValidName;
import static io.ballerina.generators.common.TestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;

/**
 * This util class for testing functionality for {@GeneratorUtils.java}.
 */
public class GeneratorUtilsTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators").toAbsolutePath();
    private  static BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator();

    @Test(description = "Functionality tests for getBallerinaOpenApiType",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Couldn't read or parse the definition from file: .*")
    public static void getIncorrectYamlContract() throws IOException, BallerinaOpenApiException {
        Path path = RES_DIR.resolve("swagger/invalid/petstore_without_info.yaml");
        OpenAPI ballerinaOpenApiType = getBallerinaOpenApiType(path);
    }

    @Test(description = "Functionality tests for When info section null",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Couldn't read or parse the definition from file: .*")
    public static void testForInfoNull() throws IOException, BallerinaOpenApiException {
        Path path = RES_DIR.resolve("swagger/invalid/petstore_without_info.yaml");
        OpenAPI ballerinaOpenApiType = getBallerinaOpenApiType(path);
    }

    @Test(description = "Functionality negative tests for extractReferenceType",
            expectedExceptions = BallerinaOpenApiException.class,
            expectedExceptionsMessageRegExp = "Invalid reference value : .*")
    public static void testForReferenceLinkInvalid() throws BallerinaOpenApiException {
        String recordName = extractReferenceType("/components/schemas/Error");
    }

    @Test(description = "Add valid reference path for extract")
    public static void testForReferenceLinkValid() throws BallerinaOpenApiException {
        Assert.assertEquals(extractReferenceType("#/components/schemas/Error"), "Error");
        Assert.assertEquals(extractReferenceType("#/components/schemas/Pet.-id"), "PetId");
        Assert.assertEquals(extractReferenceType("#/components/schemas/Pet."), "Pet");
        Assert.assertEquals(extractReferenceType("#/components/schemas/200"), "200");
        Assert.assertEquals(extractReferenceType("#/components/schemas/worker"), "Worker");
        Assert.assertEquals(extractReferenceType("#/components/schemas/worker abc"), "WorkerAbc");
    }

    @Test(description = "Generate the readable function, record name removing special characters")
    public static void testGenerateReadableName() {
        Assert.assertEquals(getValidName("endpoint-remove-shows-user", true), "EndpointRemoveShowsUser");
    }

    @Test(description = "Set record name with removing special Characters")
    public static void testRecordName() throws IOException, BallerinaOpenApiException {
        SyntaxTree syntaxTree = ballerinaSchemaGenerator.generateSyntaxTree(RES_DIR.resolve("schema/swagger" +
                "/recordName.yaml"));
        Path expectedPath = RES_DIR.resolve("schema/ballerina/recordName.bal");
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }
}
