///*
// * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * WSO2 Inc. licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package io.ballerina.generators.client;
//
//import io.ballerina.compiler.syntax.tree.Node;
//import io.ballerina.openapi.exception.BallerinaOpenApiException;
//import io.swagger.v3.oas.models.OpenAPI;
//import io.swagger.v3.oas.models.parameters.Parameter;
//import org.testng.Assert;
//import org.testng.annotations.Test;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import static io.ballerina.generators.client.BallerinaClientGenerator.generatePathWithPathParameter;
//import static io.ballerina.generators.client.BallerinaClientGenerator.getPathParameters;
//import static io.ballerina.generators.common.TestUtils.getOpenAPI;
//
///**
// * This tests class for the tests Path parameters in swagger file.
// */
//public class PathParameterTests {
//    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
//
//    @Test(description = "Generate Client for path parameter has parameter name as key word - unit tests for method")
//    public void generatePathWithPathParameterTests() {
//        Assert.assertEquals(generatePathWithPathParameter("/v1/v2"), "/v1/v2");
//        Assert.assertEquals(generatePathWithPathParameter("/v1/{version}/v2/{name}"),
//                "/v1/${'version}/v2/${name}");
//        Assert.assertEquals(generatePathWithPathParameter("/v1/{version}/v2/{limit}"),
//                "/v1/${'version}/v2/${'limit}");
//        Assert.assertEquals(generatePathWithPathParameter("/v1/{age}/v2/{name}"), "/v1/${age}/v2/${name}");
//    }
//
//    @Test(description = "When path parameter has given unmatch data type in ballerina",
//            expectedExceptions = BallerinaOpenApiException.class,
//            expectedExceptionsMessageRegExp = "Invalid path parameter data type for the parameter: .*")
//    public void testInvalidPathParameterType() throws IOException, BallerinaOpenApiException {
//        Path definitionPath = RES_DIR.resolve("swagger/path_parameter.yaml");
//        OpenAPI openAPI = getOpenAPI(definitionPath);
//        Parameter parameter = openAPI.getPaths().get("/v1/{id}").getGet().getParameters().get(0);
//        // int, string, boolean, decimal, float,
//        Node pathParameters = getPathParameters(parameter);
//    }
//}
