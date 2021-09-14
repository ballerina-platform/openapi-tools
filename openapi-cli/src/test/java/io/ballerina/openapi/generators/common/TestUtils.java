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

package io.ballerina.openapi.generators.common;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.cmd.Filter;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.generators.schema.BallerinaSchemaGenerator;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;

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
 * This util class for keeping all the common functions that use to tests.
 */
public class TestUtils {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/").toAbsolutePath();
    private static final Path clientPath = RES_DIR.resolve("ballerina_project/client.bal");
    private static final Path schemaPath = RES_DIR.resolve("ballerina_project/types.bal");
    private static final Path utilPath = RES_DIR.resolve("ballerina_project/utils.bal");
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);

    // Get diagnostics
    public static List<Diagnostic> getDiagnostics(SyntaxTree syntaxTree, OpenAPI openAPI,
                                                  BallerinaClientGenerator ballerinaClientGenerator)
            throws FormatterException, IOException, BallerinaOpenApiException {
        BallerinaSchemaGenerator ballerinaSchemaGenerator = new BallerinaSchemaGenerator(openAPI);
        ballerinaSchemaGenerator.setTypeDefinitionNodeList(ballerinaClientGenerator.getTypeDefinitionNodeList());
        SyntaxTree schemaSyntax = ballerinaSchemaGenerator.generateSyntaxTree();
        SyntaxTree utilSyntaxTree = ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree();
        writeFile(clientPath, Formatter.format(syntaxTree).toString());
        writeFile(schemaPath, Formatter.format(schemaSyntax).toString());
        writeFile(utilPath, Formatter.format(utilSyntaxTree).toString());
        SemanticModel semanticModel = getSemanticModel(clientPath);
        return semanticModel.diagnostics();
    }

    //Get string as a content of ballerina file
    public static String getStringFromGivenBalFile(Path expectedServiceFile) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile);
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent.replaceAll("\n", "");
    }

    public static void compareGeneratedSyntaxTreeWithExpectedSyntaxTree(Path path, SyntaxTree syntaxTree)
            throws IOException {

        String expectedBallerinaContent = getStringFromGivenBalFile(path);
        String generatedSyntaxTree = syntaxTree.toString();
        generatedSyntaxTree = generatedSyntaxTree.replaceAll("\n", "");
        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedSyntaxTree.equals(expectedBallerinaContent));
    }

    /*
     * Write the generated syntax tree to file.
     */
    public static void writeFile(Path filePath, String content) throws IOException {
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

    public static SemanticModel getSemanticModel(Path servicePath) {
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

    public static OpenAPI getOpenAPI(Path definitionPath) throws IOException, BallerinaOpenApiException {
        String openAPIFileContent = Files.readString(definitionPath);
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openAPIFileContent);
        OpenAPI api = parseResult.getOpenAPI();
        return api;
    }

    public static String  getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining("\n"));
        expectedServiceLines.close();
        return expectedServiceContent;
    }

    public static void compareGeneratedSyntaxTreewithExpectedSyntaxTree(String s, SyntaxTree syntaxTree)
            throws IOException {

        String expectedBallerinaContent = getStringFromGivenBalFile(RES_DIR.resolve(s));
        String generatedSyntaxTree = syntaxTree.toString();

        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertTrue(generatedSyntaxTree.contains(expectedBallerinaContent));
    }

}
