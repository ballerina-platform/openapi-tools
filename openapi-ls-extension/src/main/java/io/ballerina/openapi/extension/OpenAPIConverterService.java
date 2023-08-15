
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

package io.ballerina.openapi.extension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.converter.model.OASResult;
import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;
import org.eclipse.lsp4j.services.LanguageServer;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The extended service for the Ballerina to OpenAPI LS extension endpoint.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("openAPILSExtension")
public class OpenAPIConverterService implements ExtendedLanguageServerService {
    private static final String SERVICE_NAME = "serviceName";
    private static final String SPEC = "spec";
    private static final String DIAGNOSTICS = "diagnostics";
    private static final String MESSAGE = "message";
    private static final String SEVERITY = "severity";
    private static final String LOCATION = "location";
    private static final String FILE = "file";
    private WorkspaceManager workspaceManager;

    @Override
    public void init(LanguageServer langServer, WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    @Override
    public Class<?> getRemoteInterface() {
        return getClass();
    }

    /**
     * @deprecated This API deprecated due to providing only output yaml string as response. Please make use of this
     * new API {@link #generateOpenAPI(OpenAPIConverterRequest)} for replacement to this. It will provide list of {@code
     * OASResult} that containing OpenAPI model with its diagnostics.
     */
    @JsonRequest
    @Deprecated
    public CompletableFuture<OpenAPIConverterResponse> generateOpenAPIFile(OpenAPIConverterRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            OpenAPIConverterResponse response = new OpenAPIConverterResponse();
            String fileUri = request.getDocumentFilePath();
            Optional<SyntaxTree> syntaxTree = getPathFromURI(fileUri).flatMap(workspaceManager::syntaxTree);
            Optional<SemanticModel> semanticModel = getPathFromURI(fileUri).flatMap(workspaceManager::semanticModel);
            Optional<Project> ballerinaPackage = getPathFromURI(fileUri).flatMap(workspaceManager::project);

            if (semanticModel.isEmpty() || syntaxTree.isEmpty() || ballerinaPackage.isEmpty()) {
                StringBuilder errorString = getErrorMessage(syntaxTree, semanticModel, ballerinaPackage);
                response.setError(errorString.toString());
            } else {
                response.setError(null);
                List<OASResult> yamlContent = ServiceToOpenAPIConverterUtils.generateOAS3Definition(
                        ballerinaPackage.get(), syntaxTree.get(), semanticModel.get(), null, false,
                        Path.of(request.getDocumentFilePath()));
                //Response should handle
                if (!yamlContent.isEmpty() && (yamlContent.get(0).getOpenAPI().isPresent())) {
                    Optional<String> yaml = yamlContent.get(0).getYaml();
                    yaml.ifPresent(response::setYamlContent);
                } else {
                    response.setError("Error occurred while generating yaml.");
                }
            }
            return response;
        });
    }

    /**
     * This API is used to return the Json Array of {@code OASResult} that containing all the service node generated
     * yaml content, json content, openapi content and all diagnostics and error message if generation failed.
     */
    @JsonRequest
    public CompletableFuture<OpenAPIConverterResponse> generateOpenAPI(OpenAPIConverterRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            OpenAPIConverterResponse response = new OpenAPIConverterResponse();
            Path filePath = Path.of(request.getDocumentFilePath());
            Optional<SemanticModel> semanticModel = workspaceManager.semanticModel(filePath);
            Optional<Project> module = workspaceManager.project(filePath);
            if (semanticModel.isEmpty()) {
                response.setError("Error while generating semantic model.");
                return response;
            }
            if (module.isEmpty()) {
                response.setError("Error while getting the project.");
                return response;
            }
            module = Optional.of(module.get().duplicate());
            DiagnosticResult diagnosticsFromCodeGenAndModify = module.get()
                    .currentPackage()
                    .runCodeGenAndModifyPlugins();
            boolean hasErrorsFromCodeGenAndModify = diagnosticsFromCodeGenAndModify.diagnostics().stream()
                    .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
            Collection<Diagnostic> compilationDiagnostics = module.get().currentPackage()
                    .getCompilation().diagnosticResult().diagnostics();
            boolean hasCompilationErrors = compilationDiagnostics.stream()
                    .anyMatch(d -> DiagnosticSeverity.ERROR.equals(d.diagnosticInfo().severity()));
            if (hasCompilationErrors || hasErrorsFromCodeGenAndModify) {
                // if there are any compilation errors, do not proceed
                response.setError("Given Ballerina file contains compilation error(s).");
                return response;
            }
            Module defaultModule = module.get().currentPackage().getDefaultModule();

            JsonArray specs = new JsonArray();
            for (DocumentId currentDocumentID : defaultModule.documentIds()) {
                Document document = defaultModule.document(currentDocumentID);
                Optional<Path> path = defaultModule.project().documentPath(currentDocumentID);
                Path inputPath = path.orElse(null);
                SyntaxTree syntaxTree = document.syntaxTree();
                List<OASResult> oasResults = ServiceToOpenAPIConverterUtils.generateOAS3Definition(module.get(),
                        syntaxTree, semanticModel.get(), null, false, inputPath);

                generateServiceJson(response, document.syntaxTree().filePath(), oasResults, specs);
            }
            response.setContent(specs);
            return response;
        });
    }

    /**
     * Generate openAPI json for a service.
     *
     * @param response     OpenAPIConverterResponse to set error message
     * @param document     Ballerina document path
     * @param oasResults   OAS Results list
     * @param specs        Specs array to add the service
     */
    private void generateServiceJson(OpenAPIConverterResponse response, String document,
                                     List<OASResult> oasResults, JsonArray specs) {
        for (OASResult oasResult : oasResults) {
            if (oasResult.getOpenAPI().isEmpty()) {
                response.setError("Error occurred while generating yaml.");
                continue;
            }
            if (oasResult.getJson().isEmpty()) {
                continue;
            }
            JsonObject spec = new JsonObject();
            JsonElement json = JsonParser.parseString(oasResult.getJson().get()).getAsJsonObject();
            JsonArray diagnosticsJson = getDiagnosticsJson(oasResult);

            spec.addProperty(SERVICE_NAME, oasResult.getOpenAPI().get().getInfo().getTitle());
            spec.addProperty(FILE, document);
            spec.add(SPEC, json);
            spec.add(DIAGNOSTICS, diagnosticsJson);
            specs.add(spec);
        }
    }

    /**
     * Generate and returns the diagnostics data in OAS Result.
     *
     * @param oasResult OASResult
     * @return Json Array of diagnostics
     */
    private JsonArray getDiagnosticsJson(OASResult oasResult) {
        List<OpenAPIConverterDiagnostic> diagnostics = oasResult.getDiagnostics();
        JsonArray diagnosticsJson = new JsonArray();
        for (OpenAPIConverterDiagnostic diagnostic : diagnostics) {
            JsonObject diagnosticJson = new JsonObject();
            diagnosticJson.addProperty(MESSAGE, diagnostic.getMessage());
            diagnosticJson.addProperty(SEVERITY, diagnostic.getDiagnosticSeverity().name());
            Optional<Location> diagnosticLocation = diagnostic.getLocation();
            if (diagnosticLocation.isPresent()) {
                GsonBuilder builder = new GsonBuilder();
                builder.serializeNulls();
                Gson gson = builder.create();
                diagnosticJson.add(LOCATION, gson.toJsonTree(diagnosticLocation.get().lineRange()));
            }
            diagnosticsJson.add(diagnosticJson);
        }
        return diagnosticsJson;
    }

    // Generate error message.
    private StringBuilder getErrorMessage(Optional<SyntaxTree> syntaxTree, Optional<SemanticModel> semanticModel,
                                          Optional<Project> project) {
        StringBuilder errorString = new StringBuilder();
        if (syntaxTree.isEmpty()) {
            errorString.append("Error while generating syntax tree.").append(System.lineSeparator());
        }
        if (semanticModel.isEmpty()) {
            errorString.append("Error while generating semantic model.").append(System.lineSeparator());
        }
        if (project.isEmpty()) {
            errorString.append("Error while generating ballerina package.").append(System.lineSeparator());
        }
        return errorString;
    }

    // Refactor documentation path
    public static Optional<Path> getPathFromURI(String uri) {
        try {
            return Optional.of(Paths.get(new URL(uri).toURI()));
        } catch (URISyntaxException | MalformedURLException ignore) {
        }
        return Optional.empty();
    }
}
