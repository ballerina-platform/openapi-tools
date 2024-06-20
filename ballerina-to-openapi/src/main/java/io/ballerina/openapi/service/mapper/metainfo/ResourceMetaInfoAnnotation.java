/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.service.mapper.metainfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model for store resource function meta data.
 *
 * @since 2.0.1
 */
public class ResourceMetaInfoAnnotation {
    String summary;
    String operationId;
    List<String> tags;

    //<statusCode, <mediaType, <string, Object>>>
    Map<String, Map<String, Map<String, Object>>> responseExamples;

    private ResourceMetaInfoAnnotation(Builder builder) {
        this.summary = builder.summary;
        this.operationId = builder.operationId;
        this.tags = builder.tags;
        this.responseExamples = builder.responseExamples;
    }

    public String getSummary() {
        return summary;
    }

    public String getOperationId() {
        return operationId;
    }

    public List<String> getTags() {
        return tags;
    }

    public Map<String, Map<String, Map<String, Object>>> getResponseExamples() {
        return responseExamples;
    }

    public static class Builder {
        private String summary;
        private String operationId;
        private List<String> tags = new ArrayList<>();
        private Map<String, Map<String, Map<String, Object>>> responseExamples = new HashMap<>();

        public Builder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public Builder operationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder responseExamples(Map<String, Map<String, Map<String, Object>>> responseExamples) {
            this.responseExamples = responseExamples;
            return this;
        }

        public ResourceMetaInfoAnnotation build() {
            return new ResourceMetaInfoAnnotation(this);
        }
    }
}
