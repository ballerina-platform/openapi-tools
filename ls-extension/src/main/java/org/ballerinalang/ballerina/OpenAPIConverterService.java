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

package org.ballerinalang.ballerina;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.ballerina.util.OpenApiConverterException;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import java.util.concurrent.CompletableFuture;

/**
 * The extended service for the JsonToBalRecord endpoint.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.service.spi.ExtendedLanguageServerService")
@JsonSegment("openAPILSExtension")
public class OpenAPIConverterService implements ExtendedLanguageServerService {
    private LanguageServer languageServer;
    private WorkspaceManager workspaceManager;

    @Override
    public void init(languageServer, workspaceManager) {
        this.languageServer = languageServer;
        this.workspaceManager = workspaceManager;
    }

    @Override
    public class <?> remoteInterface() {
        return getClass();
    }

    @JsonRequest
    public CompletableFuture<OpenAPIConverter> generateOpenAPIFile(OpenAPIConverterRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            OpenAPIConverterResponse response = new OpenAPIConverterResponse();
            try {
                String documentPath = request.getDocumentFilePath();
                SyntaxTree syntaxTree = workspaceManager.syntaxTree(documentPath);
                SemanticModel semanticModel = workspaceManager.semanticModel(documentPath);
                OpenAPIConverter openAPIConverter = new OpenAPIConverter(syntaxTree, semanticModel);
                String yamlContent = openAPIConverter.generateOAS3Definition(syntaxTree, false);
                //Response should handle
                response.setYamlContent(yamlContent);
            } catch (Throwable e) {
//                response.setCodeBlock("");
                // Throw a exceptions.
            }
            return response;
        });
    }




}
