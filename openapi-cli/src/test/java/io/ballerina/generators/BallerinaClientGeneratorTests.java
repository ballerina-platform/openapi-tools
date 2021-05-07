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

package io.ballerina.generators;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * All the tests related to the BallerinaClientGenerator util.
 */
public class BallerinaClientGeneratorTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private static final Path clientPath = RES_DIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RES_DIR.resolve("ballerina_project/schema.bal");
    private static final Path testPath = RES_DIR.resolve("ballerina_project/tests/test.bal");

    SyntaxTree syntaxTree;

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);


    @Test(description = "Generate Server URL")
    public void generateServerURL() throws IOException, BallerinaOpenApiException, FormatterException,
            OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_server_with_base_path.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("client_template.bal");
    }

    @Test(description = "Generate Operation Id")
    public void generateOperationId()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_without_operation_id.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("operation_id.bal");
    }

    @Test(description = "Generate Client for GET method")
    public void generateClientForGet()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_get.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("operation_get.bal");
    }

    @Test(description = "Generate Client for POST method")
    public void generateClientForPOST()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/petstore_post.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("operation_post.bal");
    }

    @Test(description = "Generate Client for header Parameter")
    public void generateClientForHeader()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/header_parameter.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("header_parameter.bal");
    }

    @Test(description = "Generate Client for openapi_weather_api method")
    public void generateClientForResponse()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/openapi_weather_api.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("openapi_weather_api.bal");
    }

    @Test(description = "Generate Client for openapi_weather_api method")
    public void generateClientForWeatherAPI()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/openapi_weather_api.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("openapi_weather_api.bal");
    }

    @Test(description = "Generate Client for openapi spec have display annotation method")
    public void generateClientForDisplayAnnotation()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/openapi_display_annotation.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
//        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("openapi_display_annotation.bal");
    }

    @Test(description = "Generate Client for openapi spec UBER")
    public void generateClientForUberAPI()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/uber_openapi.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("uber_openapi.bal");
    }

    @Test(description = "Generate Client for openapi spec COVID19")
    public void generateClientForCovid19API()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/covid19_openapi.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("covid19_openapi.bal");
    }

    @Test(description = "Generate Client for openapi spec JIRA", enabled = false)
    public void generateClientForJIRA()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/jira_openapi.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("jira_openapi.bal");
    }

    @Test(description = "Generate Client for openapi spec world bank")
    public void generateClientForWorldBank()
            throws IOException, BallerinaOpenApiException, FormatterException, OpenApiException {
        Path definitionPath = RES_DIR.resolve("swagger/world_bank_openapi.yaml");
        syntaxTree = BallerinaClientGenerator.generateSyntaxTree(definitionPath, filter);
        List<Diagnostic> diagnostics = getDiagnostics(definitionPath);
        Assert.assertTrue(diagnostics.isEmpty());
        compareGeneratedSyntaxTreeWithExpectedSyntaxTree("world_bank_openapi.bal");
    }

    @AfterTest
    private void deleteGeneratedFiles() {
        try {
            Files.deleteIfExists(clientPath);
            Files.deleteIfExists(schemaPath);
            Files.deleteIfExists(testPath);
            FileUtils.deleteDirectory(new File("ballerina_project/tests"));
        } catch (IOException e) {
            //Ignore the exception
        }
    }
    // Get diagnostics
    private List<Diagnostic> getDiagnostics(Path definitionPath)
            throws OpenApiException, FormatterException, IOException, BallerinaOpenApiException {

        SyntaxTree schemaSyntax = BallerinaSchemaGenerator.generateSyntaxTree(definitionPath);
        writeFile(clientPath, Formatter.format(syntaxTree).toString());
        writeFile(schemaPath, Formatter.format(schemaSyntax).toString());
        SemanticModel semanticModel = getSemanticModel(clientPath);
        return semanticModel.diagnostics();
    }

    //Get string as a content of ballerina file
    private String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    private void compareGeneratedSyntaxTreeWithExpectedSyntaxTree(String s) throws IOException {

        String expectedBallerinaContent = getStringFromGivenBalFile(RES_DIR.resolve("ballerina"), s);
        String generatedSyntaxTree = syntaxTree.toString();

        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedSyntaxTree.contains(expectedBallerinaContent));
    }

    /*
     * Write the generated syntax tree to file
     */
    private static void writeFile(Path filePath, String content) throws IOException {
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(filePath.toString(), "UTF-8");
            writer.print(content);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static SemanticModel getSemanticModel(Path servicePath) {
        // Load project instance for single ballerina file
        Project project = null;
        try {
            project = ProjectLoader.loadProject(servicePath);
        } catch (ProjectException e) {
        }

        Package packageName = project.currentPackage();
        DocumentId docId;

        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
            docId = project.documentId(servicePath);
        } else {
            // Take module instance for traversing the syntax tree
            Module currentModule = packageName.getDefaultModule();
            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
            docId = documentIterator.next();
        }
        return project.currentPackage().getCompilation().getSemanticModel(docId.moduleId());
    }
}
