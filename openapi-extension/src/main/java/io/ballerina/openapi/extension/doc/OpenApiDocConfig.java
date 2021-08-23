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

package io.ballerina.openapi.extension.doc;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.ProjectKind;

import java.nio.file.Path;

/**
 * {@code OpenApiDocConfig} contains the configurations related to generate OpenAPI doc.
 */
public class OpenApiDocConfig {
    private final Path projectRoot;
    private final SemanticModel semanticModel;
    private final SyntaxTree syntaxTree;
    private final ServiceDeclarationSymbol serviceSymbol;
    private final ServiceDeclarationNode serviceNode;
    private final ProjectKind projectType;

    public OpenApiDocConfig(Path projectRoot, SemanticModel semanticModel, SyntaxTree syntaxTree,
                            ServiceDeclarationSymbol serviceSymbol, ServiceDeclarationNode serviceNode,
                            ProjectKind projectType) {
        this.projectRoot = projectRoot;
        this.semanticModel = semanticModel;
        this.syntaxTree = syntaxTree;
        this.serviceSymbol = serviceSymbol;
        this.serviceNode = serviceNode;
        this.projectType = projectType;
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public SemanticModel getSemanticModel() {
        return semanticModel;
    }

    public SyntaxTree getSyntaxTree() {
        return syntaxTree;
    }

    public ServiceDeclarationSymbol getServiceSymbol() {
        return serviceSymbol;
    }

    public ServiceDeclarationNode getServiceNode() {
        return serviceNode;
    }

    public ProjectKind getProjectType() {
        return projectType;
    }
}
