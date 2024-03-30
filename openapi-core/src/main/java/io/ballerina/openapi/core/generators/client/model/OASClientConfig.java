/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
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
package io.ballerina.openapi.core.generators.client.model;

import io.ballerina.openapi.core.generators.common.model.Filter;
import io.swagger.v3.oas.models.OpenAPI;

import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DO_NOT_MODIFY_FILE_HEADER;

/**
 * This class stores metadata that related to client code generations.
 *
 * @since 1.3.0
 */
public class OASClientConfig {
    private final OpenAPI openAPI;
    private final Filter filter;
    private final boolean nullable;
    private final boolean resourceMode;
    private final boolean isPlugin;
    private final String license;
    private final boolean statusCodeBinding;


    private OASClientConfig(Builder clientConfigBuilder) {
        this.openAPI = clientConfigBuilder.openAPI;
        this.filter = clientConfigBuilder.filter;
        this.nullable = clientConfigBuilder.nullable;
        this.isPlugin = clientConfigBuilder.isPlugin;
        this.resourceMode = clientConfigBuilder.resourceMode;
        this.license = clientConfigBuilder.license;
        this.statusCodeBinding = clientConfigBuilder.statusCodeBinding;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    public Filter getFilter() {
        return filter;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isResourceMode() {
        return resourceMode;
    }

    public boolean isPlugin() {
        return isPlugin;
    }
    public String getLicense() {
        return license;
    }
    public boolean isStatusCodeBinding() {
        return statusCodeBinding;
    }

    /**
     * Client IDL plugin meta data builder class.
     */
    public static class Builder {
        private OpenAPI openAPI;
        private Filter filter;
        private boolean nullable = false;
        private boolean resourceMode = true;
        private boolean isPlugin = false;
        private String license = DO_NOT_MODIFY_FILE_HEADER;
        private boolean statusCodeBinding = false;

        public Builder withOpenAPI(OpenAPI openAPI) {
            this.openAPI = openAPI;
            return this;
        }

        public Builder withFilters(Filter filter) {
            this.filter = filter;
            return this;
        }

        public Builder withNullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public Builder withResourceMode(boolean resourceMode) {
            this.resourceMode = resourceMode;
            return this;
        }

        public Builder withPlugin(boolean isPlugin) {
            this.isPlugin = isPlugin;
            return this;
        }

        public Builder withLicense(String license) {
            this.license = license;
            return this;
        }

        public Builder withStatusCodeBinding(boolean statusCodeBinding) {
            this.statusCodeBinding = statusCodeBinding;
            return this;
        }

        public OASClientConfig build() {
            return new OASClientConfig(this);
        }
    }
}
