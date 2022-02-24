/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Location;

import java.nio.file.Path;
import java.util.Optional;

/**
 * This model used to filter and validate all the operations according to the given filter and filter the service
 * resource in the resource file.
 *
 * @since 2.0.0
 */
public class Validator implements AnalysisTask<SyntaxNodeAnalysisContext> {

    @Override
    public void perform(SyntaxNodeAnalysisContext syntaxContext) {
        // Checking receive service node has compilation issue
        SemanticModel semanticModel = syntaxContext.semanticModel();
        SyntaxTree syntaxTree = syntaxContext.syntaxTree();

        // Generate ballerina file path
        Package aPackage = syntaxContext.currentPackage();
        DocumentId documentId = syntaxContext.documentId();
        Optional<Path> path = aPackage.project().documentPath(documentId);
        Path ballerinaFilePath = path.orElse(null);

        ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) syntaxContext.node();
        Optional<MetadataNode> metadata = serviceNode.metadata();
        if (metadata.isPresent()) {
            Location location = serviceNode.location();
            // Check annotation is available
            getDiagnosticFromServiceNode(functions, kind, filters, semanticModel, syntaxTree,
                    ballerinaFilePath, serviceNode, validations);
        }


    }
}
