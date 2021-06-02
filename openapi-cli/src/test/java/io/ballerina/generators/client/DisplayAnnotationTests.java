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

import static io.ballerina.generators.BallerinaClientGenerator.extractDisplayAnnotation;
import static io.ballerina.generators.common.TestUtils.getOpenAPI;

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
