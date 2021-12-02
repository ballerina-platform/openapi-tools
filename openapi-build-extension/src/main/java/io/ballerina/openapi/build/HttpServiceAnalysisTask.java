/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.openapi.build;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.converter.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.converter.model.OASResult;
import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.converter.Constants.JSON_EXTENSION;
import static io.ballerina.openapi.converter.Constants.YAML_EXTENSION;

/**
 * SyntaxNodeAnalyzer for getting all service node.
 */
public class HttpServiceAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {
    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        SemanticModel semanticModel = context.semanticModel();
        SyntaxTree syntaxTree = context.syntaxTree();
        Package currentPackage = context.currentPackage();
        Project project = currentPackage.project();
        BuildOptions buildOptions = project.buildOptions();
        if (buildOptions.exportOpenapi()) {
            // Take output path to target directory location in package.
            Path outPath = project.targetDir();
            Optional<Path> path = currentPackage.project().documentPath(context.documentId());
            Path inputPath = path.orElse(null);
            // Traverse the service declaration nodes
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            List<OASResult> openAPIDefinitions = new ArrayList<>();
            for (Node node : modulePartNode.members()) {
                SyntaxKind syntaxKind = node.kind();
                // Load a service declarations for the path part in the yaml spec
                if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                    openAPIDefinitions.addAll(ServiceToOpenAPIConverterUtils.generateOAS3Definition(syntaxTree,
                            semanticModel, null, false, outPath, inputPath));
                }
            }
            List<Diagnostic> diagnostics = new ArrayList<>();
            if (!openAPIDefinitions.isEmpty()) {
                for (OASResult oasResult: openAPIDefinitions) {
                    if (oasResult.getYaml().isPresent()) {
                        try {
                            String fileName = checkDuplicateFiles(outPath, oasResult.getServiceName(), false);
                            writeFile(outPath.resolve(fileName), oasResult.getYaml().get());
                        } catch (IOException e) {
                            DiagnosticMessages error = DiagnosticMessages.OAS_CONVERTOR_108;
                            ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(error.getCode(),
                                    error.getDescription(), null, e.toString());
                            diagnostics.add(BuildExtensionUtil.getDiagnostics(diagnostic));
                        }
                    }
                    if (!oasResult.getDiagnostics().isEmpty()) {
                        for (OpenAPIConverterDiagnostic diagnostic: oasResult.getDiagnostics()) {
                            diagnostics.add(BuildExtensionUtil.getDiagnostics(diagnostic));
                        }
                    }
                }
            }
            if (!diagnostics.isEmpty()) {
                for (Diagnostic diagnostic : diagnostics) {
                    context.reportDiagnostic(diagnostic);
                }
            }
        }
    }

    //TODO: use already define function in bal-service module for write file after merging #766 in openapi-tools
    /**
     * This method use for checking the duplicate files.
     *
     * @param outPath     output path for file generated
     * @param openApiName given file name
     * @return file name with duplicate number tag
     */
    private static String checkDuplicateFiles(Path outPath, String openApiName, Boolean isJson) {
        if (outPath != null && Files.exists(outPath)) {
            final File[] listFiles = new File(String.valueOf(outPath)).listFiles();
            if (listFiles != null) {
                openApiName = checkAvailabilityOfGivenName(openApiName, listFiles, isJson);
            }
        }
        return openApiName;
    }

    private static String checkAvailabilityOfGivenName(String openApiName, File[] listFiles, Boolean isJson) {
        for (File file : listFiles) {
            if (System.console() != null && file.getName().equals(openApiName)) {
                String userInput = System.console().readLine("There is already a/an " + file.getName() +
                        " in the location. Do you want to override the file? [y/N] ");
                if (!Objects.equals(userInput.toLowerCase(Locale.ENGLISH), "y")) {
                    int duplicateCount = 0;
                    openApiName = setGeneratedFileName(listFiles, openApiName, duplicateCount, isJson);
                }
            }
        }
        return openApiName;
    }

    /**
     * This method for setting the file name for generated file.
     *
     * @param listFiles      generated files
     * @param fileName       File name
     * @param duplicateCount add the tag with duplicate number if file already exist
     */
    private static String setGeneratedFileName(File[] listFiles, String fileName, int duplicateCount, boolean isJson) {
        for (File listFile : listFiles) {
            String listFileName = listFile.getName();
            if (listFileName.contains(".") && ((listFileName.split("\\.")).length >= 2)
                    && (listFileName.split("\\.")[0]
                    .equals(fileName.split("\\.")[0]))) {
                duplicateCount = 1 + duplicateCount;
            }
        }
        if (isJson) {
            return fileName.split("\\.")[0] + "." + (duplicateCount) + JSON_EXTENSION;
        }
        return fileName.split("\\.")[0] + "." + (duplicateCount) + YAML_EXTENSION;
    }

    /**
     * Writes a file with content to specified {@code filePath}.
     *
     * @param filePath valid file path to write the content
     * @param content  content of the file
     * @throws IOException when a file operation fails
     */
    public static void writeFile(Path filePath, String content) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toString(), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
}
