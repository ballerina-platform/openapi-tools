/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.openapi.generators.openapi;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.converter.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.converter.model.OASResult;
import io.ballerina.openapi.converter.utils.CodegenUtils;
import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.openapi.converter.Constants.JSON_EXTENSION;
import static io.ballerina.openapi.converter.Constants.YAML_EXTENSION;

/**
 * OpenApi related utility classes.
 */

public class OpenApiConverter {
    private SyntaxTree syntaxTree;
    private SemanticModel semanticModel;
    private Project project;
    private List<OpenAPIConverterDiagnostic> errors = new ArrayList<>();

    /**
     * Initialize constructor.
     */
    public OpenApiConverter() {

    }

    public List<OpenAPIConverterDiagnostic> getErrors() {
        return errors;
    }

    /**
     * This util for generating OAS files.
     *
     * @param servicePath The path to a single ballerina file.
     * @param outPath     The output directory to which the OpenAPI specifications should be generated to.
     * @param serviceName Filter the services to generate OpenAPI specification for service with this name.
     */
    public void generateOAS3DefinitionsAllService(Path servicePath, Path outPath, String serviceName,
                                                  Boolean needJson) {
        // Load project instance for single ballerina file
        project = ProjectLoader.loadProject(servicePath);
        Package packageName = project.currentPackage();
        DocumentId docId;
        Document doc;
        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
            docId = project.documentId(servicePath);
            ModuleId moduleId = docId.moduleId();
            doc = project.currentPackage().module(moduleId).document(docId);
        } else {
            // Take module instance for traversing the syntax tree
            Module currentModule = packageName.getDefaultModule();
            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();

            docId = documentIterator.next();
            doc = currentModule.document(docId);
        }
        Optional<Path> path = project.documentPath(docId);
        Path inputPath = path.orElse(null);

        syntaxTree = doc.syntaxTree();
        semanticModel = project.currentPackage().getCompilation().getSemanticModel(docId.moduleId());
        List<OASResult> openAPIDefinitions = ServiceToOpenAPIConverterUtils.generateOAS3Definition(syntaxTree,
                semanticModel, serviceName, needJson, outPath, inputPath);

        if (!openAPIDefinitions.isEmpty()) {
            for (OASResult definition : openAPIDefinitions) {
                try {
                    this.errors.addAll(definition.getDiagnostics());
                    if (definition.getOpenAPI().isPresent()) {
                        Optional<String> content;
                        if (needJson) {
                            content = definition.getJson();
                        } else {
                            content = definition.getYaml();
                        }
                        String fileName = checkDuplicateFiles(outPath, definition.getServiceName(), needJson);
                        CodegenUtils.writeFile(outPath.resolve(fileName), content.get());
                    }
                } catch (IOException e) {
                    DiagnosticMessages message = DiagnosticMessages.OAS_CONVERTOR_108;
                    ExceptionDiagnostic error = new ExceptionDiagnostic(message.getCode(),
                            message.getDescription() + e.getLocalizedMessage(),
                            null);
                    this.errors.add(error);
                }
            }
        }
    }

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
}
