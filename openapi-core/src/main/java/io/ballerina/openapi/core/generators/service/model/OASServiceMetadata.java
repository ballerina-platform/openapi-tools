/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package io.ballerina.openapi.core.generators.service.model;

import io.ballerina.openapi.core.generators.common.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * This class stores metadata that related to service code generations.
 *
 * @since 1.4.0
 */
public class OASServiceMetadata {

    private final OpenAPI openAPI;
    private final Filter filters;
    private final boolean nullable;
    private final boolean generateServiceType;
    private final boolean generateWithoutDataBinding;
    private final boolean generateOnlyServiceType;
    private final String licenseHeader;
    private final String srcPackage;
    private final String srcFile;

    private OASServiceMetadata(Builder serviceMetadataBuilder) {
        this.openAPI = serviceMetadataBuilder.openAPI;
        this.filters = serviceMetadataBuilder.filters;
        this.nullable = serviceMetadataBuilder.nullable;
        this.generateServiceType = serviceMetadataBuilder.generateServiceType;
        this.generateWithoutDataBinding = serviceMetadataBuilder.generateWithoutDataBinding;
        this.generateOnlyServiceType = false;
        this.licenseHeader = serviceMetadataBuilder.licenseHeader;
        this.srcPackage = serviceMetadataBuilder.srcPackage;
        this.srcFile = serviceMetadataBuilder.srcFile;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    public Filter getFilters() {
        return filters;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isServiceTypeRequired() {
        return generateServiceType;
    }

    public boolean generateWithoutDataBinding() {
        return generateWithoutDataBinding;
    }

    public boolean isGenerateOnlyServiceType() {
        return generateOnlyServiceType;
    }

    public String getLicenseHeader() {
        return licenseHeader;
    }

    public String getSrcPackage() {
        return srcPackage;
    }

    public String getSrcFile() {
        return srcFile;
    }

    /**
     * Service generation meta data builder class.
     */
    public static class Builder {

        private OpenAPI openAPI;
        private Filter filters;
        private boolean nullable = false;

        private boolean generateServiceType = false;

        private boolean generateWithoutDataBinding = false;
        private String licenseHeader = "";
        private String srcPackage = "";
        private String srcFile = "";

        public Builder withOpenAPI(OpenAPI openAPI) {
            this.openAPI = openAPI;
            return this;
        }

        public Builder withFilters(Filter filters) {
            this.filters = filters;
            return this;
        }

        public Builder withNullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public Builder withGenerateServiceType(boolean generateServiceType) {
            this.generateServiceType = generateServiceType;
            return this;
        }

        public Builder withGenerateWithoutDataBinding(boolean generateWithoutDataBinding) {
            this.generateWithoutDataBinding = generateWithoutDataBinding;
            return this;
        }

        public Builder withLicenseHeader(String licenseHeader) {
            this.licenseHeader = licenseHeader;
            return this;
        }

        public Builder withSrcPackage(String srcPackage) {
            this.srcPackage = srcPackage;
            return this;
        }

        public Builder withSrcFile(String srcFile) {
            this.srcFile = srcFile;
            return this;
        }

        public OASServiceMetadata build() {
            return new OASServiceMetadata(this);
        }
    }
}
