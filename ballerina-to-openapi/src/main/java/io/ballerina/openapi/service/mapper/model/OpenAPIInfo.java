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
package io.ballerina.openapi.service.mapper.model;

import java.util.Optional;

import static io.ballerina.openapi.service.mapper.utils.MapperCommonUtils.normalizeTitle;

/**
 * This {@link OpenAPIInfo} contains details related to openAPI info section.
 *
 * @since 1.0.0
 */
public class OpenAPIInfo {
    private final String title;
    private final String version;
    private final String contractPath;
    private final String description;
    private final String email;
    private final String contactName;
    private final String contactURL;
    private final String termsOfService;
    private final String licenseName;
    private final String licenseURL;

    public OpenAPIInfo(OpenAPIInfoBuilder openAPIInfoBuilder) {
        this.title = openAPIInfoBuilder.title;
        this.version = openAPIInfoBuilder.version;
        this.contractPath = openAPIInfoBuilder.contractPath;
        this.description = openAPIInfoBuilder.description;
        this.email = openAPIInfoBuilder.email;
        this.contactName = openAPIInfoBuilder.contactName;
        this.contactURL = openAPIInfoBuilder.contactURL;
        this.termsOfService = openAPIInfoBuilder.termsOfService;
        this.licenseName = openAPIInfoBuilder.licenseName;
        this.licenseURL = openAPIInfoBuilder.licenseURL;
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(normalizeTitle(this.title));
    }

    public Optional<String> getVersion() {
        return Optional.ofNullable(this.version);
    }

    public Optional<String> getContractPath() {
        return Optional.ofNullable(this.contractPath);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(this.description);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(this.email);
    }

    public Optional<String> getContactName() {
        return Optional.ofNullable(this.contactName);
    }

    public Optional<String> getContactURL() {
        return Optional.ofNullable(this.contactURL);
    }

    public Optional<String> getTermsOfService() {
        return Optional.ofNullable(this.termsOfService);
    }

    public Optional<String> getLicenseName() {
        return Optional.ofNullable(this.licenseName);
    }

    public Optional<String> getLicenseURL() {
        return Optional.ofNullable(this.licenseURL);
    }

    /**
     * This is the builder class for the {@link OpenAPIInfo}.
     */
    public static class OpenAPIInfoBuilder {
        private String title;
        private String version;
        private String contractPath;
        private String description;
        private String email;
        private String contactName;
        private String contactURL;
        private String termsOfService;
        private String licenseName;
        private String licenseURL;

        public OpenAPIInfoBuilder title(String title) {
            this.title = title;
            return this;
        }

        public OpenAPIInfoBuilder version(String version) {
            this.version = version;
            return this;
        }

        public OpenAPIInfoBuilder contractPath(String contractPath) {
            this.contractPath = contractPath;
            return this;
        }

        public OpenAPIInfoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public OpenAPIInfoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public OpenAPIInfoBuilder contactName(String contactName) {
            this.contactName = contactName;
            return this;
        }

        public OpenAPIInfoBuilder contactURL(String contactURL) {
            this.contactURL = contactURL;
            return this;
        }

        public OpenAPIInfoBuilder termsOfService(String termsOfService) {
            this.termsOfService = termsOfService;
            return this;
        }

        public OpenAPIInfoBuilder licenseName(String licenseName) {
            this.licenseName = licenseName;
            return this;
        }

        public OpenAPIInfoBuilder licenseURL(String licenseURL) {
            this.licenseURL = licenseURL;
            return this;
        }

        public OpenAPIInfo build() {
            return new OpenAPIInfo(this);
        }
    }
}
