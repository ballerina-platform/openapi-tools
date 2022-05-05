/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.validator.model;

import io.ballerina.tools.diagnostics.DiagnosticSeverity;

import java.util.List;

/**
 * This for model the all tag, operations, excludeTags and excludeOperations filters.
 *
 * @since 2201.1.0
 */
public class Filter {
    private final List<String> tag;
    private final List<String> excludeTag;
    private final List<String> operation;
    private final List<String> excludeOperation;
    private final DiagnosticSeverity kind;

    public Filter(FilterBuilder filterBuilder) {
        this.tag = filterBuilder.tag;
        this.excludeTag = filterBuilder.excludeTag;
        this.operation = filterBuilder.operation;
        this.excludeOperation = filterBuilder.excludeOperation;
        this.kind = filterBuilder.kind;
    }

    public List<String> getTag() {
        return tag;
    }

    public List<String> getExcludeTag() {
        return excludeTag;
    }

    public List<String> getOperation() {
        return operation;
    }

    public List<String> getExcludeOperation() {
        return excludeOperation;
    }

    public DiagnosticSeverity getKind() {
        return kind;
    }

    /**
     * This is the builder class for the {@link Filter}.
     */
    public static class FilterBuilder {
        private List<String> tag;
        private List<String> excludeTag;
        private List<String> operation;
        private List<String> excludeOperation;
        private DiagnosticSeverity kind = DiagnosticSeverity.ERROR;

        public FilterBuilder tag(List<String> tag) {
            this.tag = tag;
            return this;
        }

        public FilterBuilder excludeTag(List<String> excludeTag) {
            this.excludeTag = excludeTag;
            return this;
        }

        public FilterBuilder operation(List<String> operation) {
            this.operation = operation;
            return this;
        }

        public FilterBuilder excludeOperation(List<String> excludeOperation) {
            this.excludeOperation = excludeOperation;
            return this;
        }

        public FilterBuilder kind(DiagnosticSeverity kind) {
            this.kind = kind;
            return this;
        }
        public Filter build() {
            Filter filter = new Filter(this);
            return filter;
        }
    }
}
