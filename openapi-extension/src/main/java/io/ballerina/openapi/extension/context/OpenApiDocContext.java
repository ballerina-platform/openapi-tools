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

import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * {@code OpenApiDocContext} contains details related to open-api doc generation.
 */
public class OpenApiDocContext {
    private final ModuleId moduleId;
    private final DocumentId documentId;
    private final List<OpenApiDefinition> definitions = new ArrayList<>();

    public OpenApiDocContext(ModuleId moduleId, DocumentId documentId) {
        this.moduleId = moduleId;
        this.documentId = documentId;
    }

    ModuleId getModuleId() {
        return moduleId;
    }

    DocumentId getDocumentId() {
        return documentId;
    }

    public List<OpenApiDefinition> getOpenApiDetails() {
        return Collections.unmodifiableList(definitions);
    }

    void updateOpenApiDetails(OpenApiDefinition definition) {
        this.definitions.add(definition);
    }

    /**
     * {@code OpenApiDefinition} contains details related to generated open-api definition.
     */
    public static class OpenApiDefinition {
        private final int serviceId;
        private final String definition;
        private final boolean embed;

        public OpenApiDefinition(int serviceId, String definition, boolean embed) {
            this.serviceId = serviceId;
            this.definition = definition;
            this.embed = embed;
        }

        public int getServiceId() {
            return serviceId;
        }

        public String getDefinition() {
            return definition;
        }

        public boolean isAutoEmbedToService() {
            return embed;
        }
    }
}
