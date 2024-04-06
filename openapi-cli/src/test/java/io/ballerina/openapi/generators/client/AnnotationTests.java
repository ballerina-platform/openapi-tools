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

package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.openapi.generators.common.GeneratorTestUtils.compareGeneratedSyntaxTreeWithExpectedSyntaxTree;
import static io.ballerina.openapi.generators.common.GeneratorTestUtils.getOpenAPI;

/**
 * All the tests related to the Display Annotation in the generated code related to the
 * {{@link io.ballerina.openapi.core.generators.client.BallerinaClientGenerator}}
 * util.
 */
public class AnnotationTests {
    private static final Path RESDIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    @Test(description = "Display Annotation tests for parameters")
    public void extractDisplayAnnotationTests() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/openapi_display_annotation.yaml");
        OpenAPI display = getOpenAPI(definitionPath);
        Map<String, Object> param01 =
                display.getPaths().get("/weather").getGet().getParameters().get(0).getExtensions();
        Map<String, Object> param02 =
                display.getPaths().get("/weather").getGet().getParameters().get(1).getExtensions();
        List<AnnotationNode> annotationNodes  = new ArrayList<>();
        DocCommentsGeneratorUtil.extractDisplayAnnotation(param01, annotationNodes);
        DocCommentsGeneratorUtil.extractDisplayAnnotation(param02, annotationNodes);
        Assert.assertEquals(annotationNodes.get(0).annotValue().orElseThrow().toString().trim(),
                "{label:\"City name\"}");
        Assert.assertEquals(annotationNodes.size(), 1);
    }

    @Test(description = "Deprecated Annotation tests for parameters with x-ballerina-deprecated-reason")
    public void extractDisplayAnnotationInParametersWithReasonTest() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/deprecated_parameter.yaml");
        OpenAPI openAPI = getOpenAPI(definitionPath);
        Map<String, Object> param01 =
                openAPI.getPaths().get("/pets").getGet().getParameters().get(0).getExtensions();
        List<AnnotationNode> annotationNodes  = new ArrayList<>();
        List<Node> documentaion = new ArrayList<>();
        DocCommentsGeneratorUtil.extractDeprecatedAnnotation(param01, documentaion, annotationNodes);
        Assert.assertEquals(annotationNodes.get(0).annotReference().toString(), "deprecated");
    }

    @Test(description = "Deprecated Annotation tests for parameters without deprecated reason")
    public void extractDisplayAnnotationInParametersTest() throws IOException, BallerinaOpenApiException {
        Path definitionPath = RESDIR.resolve("swagger/deprecated_parameter.yaml");
        OpenAPI openAPI = getOpenAPI(definitionPath);
        Map<String, Object> param01 =
                openAPI.getPaths().get("/pets").getGet().getParameters().get(1).getExtensions();
        List<AnnotationNode> annotationNodes  = new ArrayList<>();
        List<Node> documentaion = new ArrayList<>();
        DocCommentsGeneratorUtil.extractDeprecatedAnnotation(param01, documentaion, annotationNodes);
        Assert.assertEquals(annotationNodes.get(0).annotReference().toString(), "deprecated");
    }

    @Test(description = "Test openAPI definition to ballerina client source code generation with deprecated annotation",
            dataProvider = "fileProviderForFilesComparison")
    public void  openApiToBallerinaClientGenWithAnnotation(String yamlFile, String expectedFile)
            throws IOException, BallerinaOpenApiException, ClientException {
        Path definitionPath = RESDIR.resolve("swagger/" + yamlFile);
        Path expectedPath = RESDIR.resolve("ballerina/" + expectedFile);
        List<String> list1 = new ArrayList<>();
        List<String> list2 = new ArrayList<>();
        Filter filter = new Filter(list1, list2);
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(definitionPath, true);
        OASClientConfig.Builder clientMetaDataBuilder = new OASClientConfig.Builder();
        OASClientConfig oasClientConfig = clientMetaDataBuilder
                .withFilters(filter)
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();
        BallerinaClientGenerator ballerinaClientGenerator = new BallerinaClientGenerator(oasClientConfig);
        SyntaxTree syntaxTree = ballerinaClientGenerator.generateSyntaxTree();
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree(expectedPath, syntaxTree);
    }

    @DataProvider(name = "fileProviderForFilesComparison")
    public Object[][] fileProviderForFilesComparison() {
        return new Object[][]{
                {"deprecated_functions.yaml", "deprecated_functions.bal"},
                {"display_and_deprecated.yaml", "display_and_deprecated.bal"},
                {"deprecated_mix_params.yaml", "deprecated_mix_params.bal"}
        };
    }
}
