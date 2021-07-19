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

package io.ballerina.generators.client;

import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.generators.BallerinaSchemaGenerator;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.ballerina.generators.common.TestUtils.getOpenAPI;

/**
 * All the tests related to the functionSignatureNode in {@link BallerinaClientGenerator} util.
 */
public class FunctionSignatureNodeTests {
    private static final Path RESDIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private static final Path clientPath = RESDIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RESDIR.resolve("ballerina_project/types.bal");
    SyntaxTree syntaxTree;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    @Test(description = "Test for generate function signature for given operations")
    public void getFunctionSignatureNodeTests() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPI(RESDIR.resolve("swagger/valid_operation.yaml"));
        FunctionSignatureGenerator functionSignatureGenerator = new FunctionSignatureGenerator(openAPI,
                new BallerinaSchemaGenerator(openAPI, false), new ArrayList<>());
        FunctionSignatureNode signature = functionSignatureGenerator.getFunctionSignatureNode(openAPI.getPaths()
                .get("/products/{country}").getGet(), new ArrayList<>());
        SeparatedNodeList<ParameterNode> parameters = signature.parameters();
        Assert.assertFalse(parameters.isEmpty());
        RequiredParameterNode param01 = (RequiredParameterNode) parameters.get(0);
        RequiredParameterNode param02 = (RequiredParameterNode) parameters.get(1);
        RequiredParameterNode param03 = (RequiredParameterNode) parameters.get(2);

        Assert.assertEquals(param01.paramName().orElseThrow().text(), "latitude");
        Assert.assertEquals(param01.typeName().toString(), "float");

        Assert.assertEquals(param02.paramName().orElseThrow().text(), "longitude");
        Assert.assertEquals(param02.typeName().toString(), "float");

        Assert.assertEquals(param03.paramName().orElseThrow().text(), "country");
        Assert.assertEquals(param03.typeName().toString(), "string");

        ReturnTypeDescriptorNode returnTypeNode = signature.returnTypeDesc().orElseThrow();
        Assert.assertEquals(returnTypeNode.type().toString(), "Product[]|error");
    }

    @AfterTest
    private void deleteGeneratedFiles() {
        try {
            Files.deleteIfExists(clientPath);
            Files.deleteIfExists(schemaPath);
        } catch (IOException e) {
            //Ignore the exception
        }
    }

}
