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

import java.util.List;

/**
 * {@code OpenApiManager} manages OpenAPI doc generation for HTTP services depending on whether the current project
 * is a ballerina-project or a single ballerina file.
 */
public final class OpenApiManager {
    private final List<OpenApiDocGenerator> docGenerators;

    public OpenApiManager() {
        this.docGenerators = List.of(new SingleFileOpenApiDocGenerator(), new BalProjectOpenApiDocGenerator());
    }

    public void generate(OpenApiDocConfig config) {
        docGenerators.stream()
                .filter(dg -> dg.isSupported(config.getProjectType()))
                .findFirst()
                .ifPresent(dg -> dg.generate(config));
    }
}
