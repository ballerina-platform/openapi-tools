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

package io.ballerina.openapi.extension.doc.gen;

import io.ballerina.compiler.syntax.tree.NodeLocation;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;

import java.util.List;

/**
 * {@code DocGeneratorManager} manages OpenAPI doc generation for HTTP services depending on whether the current project
 * is a ballerina-project or a single ballerina file.
 */
public final class DocGeneratorManager {
    private final List<OpenApiDocGenerator> docGenerators;

    public DocGeneratorManager() {
        this.docGenerators = List.of(new SingleFileOpenApiDocGenerator(), new BalProjectOpenApiDocGenerator());
    }

    public void generate(OpenApiDocConfig config, SyntaxNodeAnalysisContext context, NodeLocation location) {
        docGenerators.stream()
                .filter(dg -> dg.isSupported(config.getProjectType()))
                .findFirst()
                .ifPresent(dg -> dg.generate(config, context, location));
    }
}
