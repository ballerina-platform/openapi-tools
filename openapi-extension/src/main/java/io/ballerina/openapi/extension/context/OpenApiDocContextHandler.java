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

package io.ballerina.openapi.extension.context;

import io.ballerina.projects.PackageId;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * {@code OpenApiDocContextHandler} will manage the shared context among compiler plugin tasks.
 */
public final class OpenApiDocContextHandler {
    private static OpenApiDocContextHandler INSTANCE;

    private final List<OpenApiDocContext> contexts;

    private OpenApiDocContextHandler() {
        this.contexts = new ArrayList<>();
    }

    public static OpenApiDocContextHandler getContextHandler() {
        synchronized (OpenApiDocContextHandler.class) {
            if (Objects.isNull(INSTANCE)) {
                INSTANCE = new OpenApiDocContextHandler();
            }
        }
        return INSTANCE;
    }

    public void addContext(OpenApiDocContext context) {
        synchronized (this.contexts) {
            this.contexts.add(context);
        }
    }

    /**
     * Update the shared context for open-api doc generation.
     * @param packageId of the current project
     * @param srcRoot of the current project
     * @param definition to be added to the context
     */
    public void updateContext(PackageId packageId, Path srcRoot, OpenApiDocContext.OpenApiDefinition definition) {
        Optional<OpenApiDocContext> contextOpt = retrieveContext(packageId, srcRoot);
        if (contextOpt.isPresent()) {
            OpenApiDocContext context = contextOpt.get();
            synchronized (context) {
                context.updateOpenApiDetails(definition);
            }
            return;
        }
        OpenApiDocContext context = new OpenApiDocContext(packageId, srcRoot);
        context.updateOpenApiDetails(definition);
        addContext(context);
    }

    public Optional<OpenApiDocContext> retrieveContext(PackageId packageId, Path srcRoot) {
        return this.contexts.stream()
                .filter(ctx -> equals(ctx, packageId, srcRoot))
                .findFirst();
    }

    private boolean equals(OpenApiDocContext context, PackageId packageId, Path srcRoot) {
        int hashCodeForCurrentContext = Objects.hash(context.getPackageId(), context.getSourcePath());
        return hashCodeForCurrentContext == Objects.hash(packageId, srcRoot);
    }
}
