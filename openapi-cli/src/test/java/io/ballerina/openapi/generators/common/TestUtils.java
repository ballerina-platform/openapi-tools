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
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
import io.ballerina.openapi.core.generators.service.BallerinaServiceGenerator;
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
    private static final Path servicePath = RES_DIR.resolve("ballerina_project/service.bal");
    private static final String LINE_SEPARATOR = System.lineSeparator();

    // Get diagnostics
    public static List<Diagnostic> getDiagnostics(SyntaxTree syntaxTree, OpenAPI openAPI,
                                                  BallerinaClientGenerator ballerinaClientGenerator)
            throws FormatterException, IOException, BallerinaOpenApiException {
        List<TypeDefinitionNode> preGeneratedTypeDefinitionNodes = new LinkedList<>();
        preGeneratedTypeDefinitionNodes.addAll(ballerinaClientGenerator.
                getBallerinaAuthConfigGenerator().getAuthRelatedTypeDefinitionNodes());
        preGeneratedTypeDefinitionNodes.addAll(ballerinaClientGenerator.getTypeDefinitionNodeList());
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(
                openAPI, false, preGeneratedTypeDefinitionNodes);
        SyntaxTree schemaSyntax = ballerinaSchemaGenerator.generateSyntaxTree();
        SyntaxTree utilSyntaxTree = ballerinaClientGenerator.getBallerinaUtilGenerator().generateUtilSyntaxTree();
        writeFile(clientPath, Formatter.format(syntaxTree).toSourceCode());
        writeFile(schemaPath, Formatter.format(schemaSyntax).toSourceCode());
        writeFile(utilPath, Formatter.format(utilSyntaxTree).toSourceCode());
        SemanticModel semanticModel = getSemanticModel(clientPath);
        return semanticModel.diagnostics();
    }

    public static List<Diagnostic> getDiagnostics(SyntaxTree syntaxTree) throws FormatterException, IOException {
        writeFile(schemaPath, Formatter.format(syntaxTree).toSourceCode());
        SemanticModel semanticModel = getSemanticModel(schemaPath);
        return semanticModel.diagnostics();
    }

    public static List<Diagnostic> getDiagnosticsForGenericService(SyntaxTree serviceSyntaxTree)
            throws FormatterException, IOException {
        writeFile(servicePath, Formatter.format(serviceSyntaxTree).toSourceCode());
        SemanticModel semanticModel = getSemanticModel(servicePath);
        return semanticModel.diagnostics();
    }

    public static List<Diagnostic> getDiagnosticsForService(SyntaxTree serviceSyntaxTree, OpenAPI openAPI,
                                                            BallerinaServiceGenerator ballerinaServiceGenerator)
            throws FormatterException, IOException, BallerinaOpenApiException {
        List<TypeDefinitionNode> preGeneratedTypeDefNodes = new ArrayList<>(
                ballerinaServiceGenerator.getTypeInclusionRecords());
        BallerinaTypesGenerator ballerinaSchemaGenerator = new BallerinaTypesGenerator(
                openAPI, false, preGeneratedTypeDefNodes);
        String schemaContent = Formatter.format(
                ballerinaSchemaGenerator.generateSyntaxTree()).toSourceCode();
        String serviceContent = Formatter.format(serviceSyntaxTree).toSourceCode();
        serviceContent = serviceContent.replaceAll(
                "\\{" + System.lineSeparator() + "\\s*\\}", "\\{panic error(\"Tests\");\\}");
        writeFile(servicePath, serviceContent);
        writeFile(schemaPath, schemaContent);
        SemanticModel semanticModel = getSemanticModel(servicePath);
        return semanticModel.diagnostics();
    }

    //Get string as a content of ballerina file
    public static String getStringFromGivenBalFile(Path expectedServiceFile) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile);
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining(LINE_SEPARATOR));
        expectedServiceLines.close();
        return expectedServiceContent.replaceAll(LINE_SEPARATOR, "");
    }

    public static void compareGeneratedSyntaxTreeWithExpectedSyntaxTree(Path path, SyntaxTree syntaxTree)
            throws IOException {

        String expectedBallerinaContent = getStringFromGivenBalFile(path);
        String generatedSyntaxTree = syntaxTree.toSourceCode();
        generatedSyntaxTree = generatedSyntaxTree.replaceAll(LINE_SEPARATOR, "");
        generatedSyntaxTree = (generatedSyntaxTree.trim()).replaceAll("\\s+", "");
        expectedBallerinaContent = (expectedBallerinaContent.trim()).replaceAll("\\s+", "");
        Assert.assertEquals(expectedBallerinaContent, generatedSyntaxTree);
    }

    /*
     * Write the generated syntax tree to file.
     */
    public static void writeFile(Path filePath, String content) throws IOException {
        try (PrintWriter writer = new PrintWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.print(content);
        }
    }

    public static SemanticModel getSemanticModel(Path servicePath) {
        // Load project instance for single ballerina file
        Project project = null;
        try {
            project = ProjectLoader.loadProject(servicePath);
        } catch (ProjectException ignored) {
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
        return parseResult.getOpenAPI();
    }

    public static String getStringFromGivenBalFile(Path expectedServiceFile, String s) throws IOException {
        Stream<String> expectedServiceLines = Files.lines(expectedServiceFile.resolve(s));
        String expectedServiceContent = expectedServiceLines.collect(Collectors.joining(LINE_SEPARATOR));
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

    /**
     * Delete generated ballerina files.
     */
    public static void deleteGeneratedFiles() throws IOException {
        Path resourcesPath = RES_DIR.resolve("ballerina_project");
        if (Files.exists(resourcesPath)) {
            File[] listFiles = Objects.requireNonNull(new File(String.valueOf(resourcesPath)).listFiles());
            for (File existsFile : listFiles) {
                String fileName = existsFile.getName();
                if (fileName.endsWith(".bal")) {
                    existsFile.delete();
                }
            }
        }
    }
}
