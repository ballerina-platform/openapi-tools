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
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.service.mapper.ServersMapper;
import io.ballerina.openapi.service.mapper.ServiceToOpenAPIMapper;
import io.ballerina.openapi.service.mapper.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.service.mapper.diagnostic.ExceptionDiagnostic;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.model.OASGenerationMetaInfo;
import io.ballerina.openapi.service.mapper.model.OASResult;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.build.PluginConstants.OAS_PATH_SEPARATOR;
import static io.ballerina.openapi.build.PluginConstants.OPENAPI;
import static io.ballerina.openapi.build.PluginConstants.UNDERSCORE;
import static io.ballerina.openapi.service.mapper.Constants.HYPHEN;
import static io.ballerina.openapi.service.mapper.Constants.OPENAPI_SUFFIX;
import static io.ballerina.openapi.service.mapper.Constants.SLASH;
import static io.ballerina.openapi.service.mapper.Constants.YAML_EXTENSION;
import static io.ballerina.openapi.service.mapper.utils.CodegenUtils.resolveContractFileName;
import static io.ballerina.openapi.service.mapper.utils.CodegenUtils.writeFile;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.containErrors;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.getNormalizedFileName;
import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.isHttpService;

/**
 * SyntaxNodeAnalyzer for getting all service node.
 *
 * @since 2.0.0
 */
public class HttpServiceAnalysisTask implements AnalysisTask<SyntaxNodeAnalysisContext> {
    static boolean isWarningPrinted = false;

    static void setIsWarningPrinted(boolean isWarningPrinted) {
        HttpServiceAnalysisTask.isWarningPrinted = isWarningPrinted;
    }

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
        // if there are any compilation errors, do not proceed
        if (!isWarningPrinted) {
            setIsWarningPrinted(true);
            PrintStream outStream = System.out;
            outStream.println("openapi contract generation is skipped because the compilation error(s) in the ballerina package");
            return;
        }
        boolean hasErrors = context.compilation().diagnosticResult()
                .diagnostics().stream()
                .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));

        if (hasErrors) {
            return;
        }
        Path outPath = project.targetDir();
        Optional<Path> path = currentPackage.project().documentPath(context.documentId());
        Path inputPath = path.orElse(null);
        ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) context.node();
        Map<Integer, String> services = new HashMap<>();
        List<Diagnostic> diagnostics = new ArrayList<>();

        // Spec generation won't proceed, If diagnostic includes error type.
        if (containErrors(semanticModel.diagnostics())) {
            diagnostics.addAll(semanticModel.diagnostics());
        } else if (isHttpService(serviceNode, semanticModel)) {
            Optional<Symbol> serviceSymbol = semanticModel.symbol(serviceNode);
            if (serviceSymbol.isPresent() && serviceSymbol.get() instanceof ServiceDeclarationSymbol) {
                extractServiceNodes(syntaxTree.rootNode(), services, semanticModel);
                OASGenerationMetaInfo.OASGenerationMetaInfoBuilder builder =
                        new OASGenerationMetaInfo.OASGenerationMetaInfoBuilder();
                builder.setServiceDeclarationNode(serviceNode).setSemanticModel(semanticModel)
                        .setOpenApiFileName(services.get(serviceSymbol.get().hashCode()))
                        .setBallerinaFilePath(inputPath).setProject(project);
                OASResult oasResult = ServiceToOpenAPIMapper.generateOAS(builder.build());
                oasResult.setServiceName(constructFileName(syntaxTree, services, serviceSymbol.get()));
                writeOpenAPIYaml(outPath, oasResult, diagnostics);
            }
        }
        if (!diagnostics.isEmpty()) {
            for (Diagnostic diagnostic : diagnostics) {
                context.reportDiagnostic(diagnostic);
            }
        }
    }

    /**
     * This util function is to construct the generated file name.
     *
     * @param syntaxTree syntax tree for check the multiple services
     * @param services   service map for maintain the file name with updated name
     * @param serviceSymbol symbol for taking the hash code of services
     */
    private String constructFileName(SyntaxTree syntaxTree, Map<Integer, String> services, Symbol serviceSymbol) {
        String fileName = getNormalizedFileName(services.get(serviceSymbol.hashCode()));
        String balFileName = syntaxTree.filePath().split("\\.")[0];
        if (fileName.equals(SLASH)) {
            return balFileName + OPENAPI_SUFFIX + YAML_EXTENSION;
        } else if (fileName.contains(HYPHEN) && fileName.split(HYPHEN)[0].equals(SLASH) || fileName.isBlank()) {
            return balFileName + UNDERSCORE + serviceSymbol.hashCode() + OPENAPI_SUFFIX + YAML_EXTENSION;
        }
        return fileName + OPENAPI_SUFFIX + YAML_EXTENSION;
    }

    private void writeOpenAPIYaml(Path outPath, OASResult oasResult, List<Diagnostic> diagnostics) {
        if (oasResult.getYaml().isPresent()) {
            try {
                // Create openapi directory if not exists in the path. If exists do not throw an error
                Files.createDirectories(Paths.get(outPath + OAS_PATH_SEPARATOR + OPENAPI));
                String serviceName = oasResult.getServiceName();
                String fileName = resolveContractFileName(outPath.resolve(OPENAPI),
                        serviceName, false);
                writeFile(outPath.resolve(OPENAPI + OAS_PATH_SEPARATOR + fileName), oasResult.getYaml().get());
            } catch (IOException e) {
                DiagnosticMessages error = DiagnosticMessages.OAS_CONVERTOR_108;
                ExceptionDiagnostic diagnostic = new ExceptionDiagnostic(error.getCode(),
                        error.getDescription(), null, e.toString());
                diagnostics.add(BuildExtensionUtil.getDiagnostics(diagnostic));
            }
        }
        if (!oasResult.getDiagnostics().isEmpty()) {
            for (OpenAPIMapperDiagnostic diagnostic : oasResult.getDiagnostics()) {
                diagnostics.add(BuildExtensionUtil.getDiagnostics(diagnostic));
            }
        }
    }

    /**
     * Filter all the end points and service nodes for avoiding the generated file name conflicts.
     */
    private static void extractServiceNodes(ModulePartNode modulePartNode, Map<Integer, String> services,
                                            SemanticModel semanticModel) {
        List<String> allServices = new ArrayList<>();
        for (Node node : modulePartNode.members()) {
            SyntaxKind syntaxKind = node.kind();
            if (syntaxKind.equals(SyntaxKind.SERVICE_DECLARATION)) {
                ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) node;
                if (isHttpService(serviceNode, semanticModel)) {
                    // Here check the service is related to the http
                    // module by checking listener type that attached to service endpoints.
                    Optional<Symbol> serviceSymbol = semanticModel.symbol(serviceNode);
                    if (serviceSymbol.isPresent() && serviceSymbol.get() instanceof ServiceDeclarationSymbol) {
                        String service = ServersMapper.getServiceBasePath(serviceNode);
                        String updateServiceName = service;
                        if (allServices.contains(service)) {
                            updateServiceName = service + HYPHEN + serviceSymbol.get().hashCode();
                        } else {
                            // To generate for all services
                            allServices.add(service);
                        }
                        services.put(serviceSymbol.get().hashCode(), updateServiceName);
                    }
                }
            }
        }
    }
}
