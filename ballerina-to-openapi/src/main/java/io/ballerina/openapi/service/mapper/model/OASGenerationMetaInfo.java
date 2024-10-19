/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.projects.Project;

import java.nio.file.Path;
import java.util.Objects;

/**
 * This {@link OASGenerationMetaInfo} contains details related to openAPI specification.
 *
 * @since 1.6.0
 */
public class OASGenerationMetaInfo {

    private final String openApiFileName;
    private final Path ballerinaFilePath;
    private final SemanticModel semanticModel;
    private final ServiceNode serviceNode;
    private final Project project;
    private final Boolean ballerinaExtensionLevel;

    public OASGenerationMetaInfo(OASGenerationMetaInfoBuilder builder) {
        this.openApiFileName = builder.openApiFileName;
        this.ballerinaFilePath = builder.ballerinaFilePath;
        this.semanticModel = builder.semanticModel;
        ServiceNode serviceNodeFromBuilder = builder.serviceNode;
        if (Objects.isNull(serviceNodeFromBuilder)) {
            serviceNodeFromBuilder = new ServiceDeclaration(builder.serviceDeclarationNode, semanticModel);
        }
        this.serviceNode = serviceNodeFromBuilder;
        this.project = builder.project;
        this.ballerinaExtensionLevel = builder.ballerinaExtension;
    }

    public String getOpenApiFileName() {
        return openApiFileName;
    }

    public Path getBallerinaFilePath() {
        return ballerinaFilePath;
    }

    public SemanticModel getSemanticModel() {
        return semanticModel;
    }

    public ServiceNode getServiceNode() {
        return serviceNode;
    }

    public Project getProject() {
        return project;
    }

    public Boolean getBallerinaExtension() {
        return ballerinaExtensionLevel;
    }

    /**
     * This method is used to create a new {@link OASGenerationMetaInfoBuilder} instance.
     */
    public static class OASGenerationMetaInfoBuilder {

        private String openApiFileName;
        private Path ballerinaFilePath;
        private SemanticModel semanticModel;
        private ServiceDeclarationNode serviceDeclarationNode;
        private ServiceNode serviceNode;
        private Project project;
        private Boolean ballerinaExtension = false;

        public OASGenerationMetaInfoBuilder setBallerinaFilePath(Path ballerinaFilePath) {
            this.ballerinaFilePath = ballerinaFilePath;
            return this;
        }

        public OASGenerationMetaInfoBuilder setSemanticModel(SemanticModel semanticModel) {
            this.semanticModel = semanticModel;
            return this;
        }

        /**
         * @deprecated Construct the {@link ServiceDeclarationNode} instance from the serviceDeclarationNode and use
         * that with {@link #setServiceNode(ServiceNode)} method
         */
        @Deprecated(forRemoval = true, since = "2.1.0")
        public OASGenerationMetaInfoBuilder setServiceDeclarationNode(ServiceDeclarationNode serviceDeclarationNode) {
            this.serviceDeclarationNode = serviceDeclarationNode;
            return this;
        }

        public OASGenerationMetaInfoBuilder setServiceNode(ServiceNode serviceNode) {
            this.serviceNode = serviceNode;
            return this;
        }

        public OASGenerationMetaInfoBuilder setOpenApiFileName(String openApiFileName) {
            this.openApiFileName = openApiFileName;
            return this;
        }

        public OASGenerationMetaInfoBuilder setBallerinaExtension(Boolean balExt) {
            this.ballerinaExtension = balExt;
            return this;
        }

        public void setProject(Project project) {
            this.project = project;
        }

        public OASGenerationMetaInfo build() {
            return new OASGenerationMetaInfo(this);
        }
    }
}
