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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.ballerina.openapi.build.PluginConstants.OAS_PATH_SEPARATOR;
import static io.ballerina.openapi.build.PluginConstants.OPENAPI;
import static io.ballerina.openapi.converter.utils.CodegenUtils.resolveContractFileName;
import static io.ballerina.openapi.converter.utils.CodegenUtils.writeFile;

/**
 * SyntaxNodeAnalyzer for getting all service node.
 *
 * @since 2.0.0
 */
public class HttpServiceAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {
    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        SemanticModel semanticModel = context.semanticModel();
        SyntaxTree syntaxTree = context.syntaxTree();
        Package currentPackage = context.currentPackage();
        Project project = currentPackage.project();
        //Used build option exportOpenapi() to enable plugin at the build time.
        BuildOptions buildOptions = project.buildOptions();
        if (!buildOptions.exportOpenAPI()) {
            return;
        }
        // Take output path to target directory location in package.
        Path outPath = project.targetDir();
        Optional<Path> path = currentPackage.project().documentPath(context.documentId());
        Path inputPath = path.orElse(null);
        // Traverse the service declaration nodes
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        List<OASResult> openAPIDefinitions = new ArrayList<>();
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            // Load a service declarations
            if (syntaxKind == SyntaxKind.SERVICE_DECLARATION) {
                openAPIDefinitions.addAll(ServiceToOpenAPIConverterUtils.generateOAS3Definition(syntaxTree,
                        semanticModel, null, false, inputPath));
            }
        }
        List<Diagnostic> diagnostics = new ArrayList<>();
        if (!openAPIDefinitions.isEmpty()) {
            extractOpenAPIYamlFromOutputs(outPath, openAPIDefinitions, diagnostics);
        }
        if (!diagnostics.isEmpty()) {
            for (Diagnostic diagnostic : diagnostics) {
                context.reportDiagnostic(diagnostic);
            }
        }
    }

    private void extractOpenAPIYamlFromOutputs(Path outPath, List<OASResult> openAPIDefinitions,
                                               List<Diagnostic> diagnostics) {
        for (OASResult oasResult: openAPIDefinitions) {
            if (oasResult.getYaml().isPresent()) {
                try {
                    // Create openapi directory if not exists in the path. If exists do not throw an error
                    Files.createDirectories(Paths.get(outPath + OAS_PATH_SEPARATOR + OPENAPI));
                    String fileName = resolveContractFileName(outPath.resolve(OPENAPI), oasResult.getServiceName(),
                            false);
                    writeFile(outPath.resolve(OPENAPI + OAS_PATH_SEPARATOR + fileName), oasResult.getYaml().get());
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
}
