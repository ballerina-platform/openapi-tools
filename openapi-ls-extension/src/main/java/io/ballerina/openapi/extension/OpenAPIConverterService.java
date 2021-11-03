
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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.openapi.converter.service.OASResult;
import io.ballerina.openapi.converter.utils.ServiceToOpenAPIConverterUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private WorkspaceManager workspaceManager;

    public OpenAPIConverterService(WorkspaceManager workspaceManager) {
        this.workspaceManager = workspaceManager;
    }

    public OpenAPIConverterService() {
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
            if (semanticModel.isEmpty() || syntaxTree.isEmpty()) {
                StringBuilder errorString = getErrorMessage(syntaxTree, semanticModel);
                response.setError(errorString.toString());
            } else {
                response.setError(null);
                List<OASResult> yamlContent = ServiceToOpenAPIConverterUtils.generateOAS3Definition(
                        syntaxTree.orElseThrow(), semanticModel.orElseThrow(), null, false,
                        null);
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
     * This API is used to return the List of {@code OASResult} that containing all the service node generated yaml
     * content, json content, openapi content and all diagnostics.
     */
    @JsonRequest
    public CompletableFuture<OpenAPIConverterResponse> generateOpenAPI(OpenAPIConverterRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            OpenAPIConverterResponse response = new OpenAPIConverterResponse();
            String fileUri = request.getDocumentFilePath();
            Optional<SyntaxTree> syntaxTree = getPathFromURI(fileUri).flatMap(workspaceManager::syntaxTree);
            Optional<SemanticModel> semanticModel = getPathFromURI(fileUri).flatMap(workspaceManager::semanticModel);
            if (semanticModel.isEmpty() || syntaxTree.isEmpty()) {
                StringBuilder errorString = getErrorMessage(syntaxTree, semanticModel);
                response.setError(errorString.toString());
            } else {
                response.setError(null);
                List<OASResult> oasResult = ServiceToOpenAPIConverterUtils.generateOAS3Definition(syntaxTree.get(),
                        semanticModel.get(), null, false, null);
                //Response handle with returning list of {@code OASResult} model.
                response.setContent(oasResult);
            }
            return response;
        });
    }

    // Generate error message.
    private StringBuilder getErrorMessage(Optional<SyntaxTree> syntaxTree, Optional<SemanticModel> semanticModel) {
        StringBuilder errorString = new StringBuilder();
        if (syntaxTree.isEmpty()) {
            errorString.append("Error while generating syntax tree.").append(System.lineSeparator());
        }
        if (semanticModel.isEmpty()) {
            errorString.append("Error while generating semantic model.");
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
