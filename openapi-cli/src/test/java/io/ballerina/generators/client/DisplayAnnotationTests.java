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

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static io.ballerina.generators.client.BallerinaClientGenerator.extractDisplayAnnotation;
import static io.ballerina.generators.common.TestUtils.getOpenAPI;

/**
 * All the tests related to the Display Annotation in the generated code related to the
 * {@link BallerinaClientGenerator}
 * util.
 */
public class DisplayAnnotationTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();

    @Test(description = "Display Annotation tests for parameters")
    public void extractDisplayAnnotationTests() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/openapi_display_annotation.yaml");
        OpenAPI display = getOpenAPI(definitionPath);
        Map<String, Object> param01 =
                display.getPaths().get("/weather").getGet().getParameters().get(0).getExtensions();
        Map<String, Object> param02 =
                display.getPaths().get("/weather").getGet().getParameters().get(1).getExtensions();
        NodeList<AnnotationNode> annotationNodes01 = extractDisplayAnnotation(param01);
        NodeList<AnnotationNode> annotationNodes02 = extractDisplayAnnotation(param02);
        Assert.assertEquals(annotationNodes01.get(0).annotValue().orElseThrow().toString().trim(),
                "{label:\"City name\"}");
        Assert.assertTrue(annotationNodes02.isEmpty());
    }
}
