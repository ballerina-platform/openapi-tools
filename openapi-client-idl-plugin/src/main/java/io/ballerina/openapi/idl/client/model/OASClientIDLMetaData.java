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

package io.ballerina.openapi.idl.client.model;

import java.util.List;
import java.util.Optional;

/**
 * This class is used to contain all the metadata that use for generate client.
 */
public class OASClientIDLMetaData {

    private final List<String> tags;
    private final List<String> operations;
    private final boolean nullable;
    private final boolean isResource;
    private final boolean withTest;

    public OASClientIDLMetaData(OASClientIDLMetaDataBuilder oasClientIDLMetaDataBuilder) {
        this.tags = oasClientIDLMetaDataBuilder.tags;
        this.operations = oasClientIDLMetaDataBuilder.operations;
        this.nullable = oasClientIDLMetaDataBuilder.nullable;
        this.isResource = oasClientIDLMetaDataBuilder.isResource;
        this.withTest = oasClientIDLMetaDataBuilder.withTest;
    }

    public Optional<List<String>> getTags() {
        return Optional.ofNullable(this.tags);
    }

    public Optional<List<String>> getOperations() {
        return Optional.ofNullable(this.operations);
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isResource() {
        return isResource;
    }

    public boolean isWithTest() {
        return withTest;
    }

    /**
     * This is the builder class for the {@link OASClientIDLMetaData}.
     */
    public static class OASClientIDLMetaDataBuilder {
        private List<String> tags;
        private List<String> operations;
        private boolean nullable = true;
        private boolean isResource = true;
        private boolean withTest = false;

        public OASClientIDLMetaDataBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public OASClientIDLMetaDataBuilder withOperations(List<String> operations) {
            this.operations = operations;
            return this;
        }

        public OASClientIDLMetaDataBuilder withNullable(boolean nullable) {
            this.nullable = nullable;
            return this;
        }

        public OASClientIDLMetaDataBuilder withTest(boolean withTest) {
            this.withTest = withTest;
            return this;
        }

        public OASClientIDLMetaDataBuilder withIsResource(boolean isResource) {
            this.isResource = isResource;
            return this;
        }

        public OASClientIDLMetaData build() {
            return new OASClientIDLMetaData(this);
        }
    }
}
