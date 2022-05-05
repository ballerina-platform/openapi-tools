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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.tools.diagnostics.Location;

import java.util.Map;

/**
 * This is for containing the resource functions details. This includes all path, parameters, headers, return types,
 * request body details.
 *
 * @since 2201.1.0
 */
public class ResourceMethod {
    private final String path;
    private final Location location;
    private final String method;
    private final Map<String, Node> parameters;
    private final RequiredParameterNode body;
    private final Map<String, Node> headers;
    private final ReturnTypeDescriptorNode returnNode;

    public ResourceMethod(ResourceMethodBuilder resourceMethodBuilder) {
        this.path = resourceMethodBuilder.path;
        this.location = resourceMethodBuilder.location;
        this.method = resourceMethodBuilder.method;
        this.parameters = resourceMethodBuilder.parameters;
        this.body = resourceMethodBuilder.body;
        this.headers = resourceMethodBuilder.headers;
        this.returnNode = resourceMethodBuilder.returnNode;
    }

    public String getPath() {
        return path;
    }

    public Location getLocation() {
        return location;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, Node> getParameters() {
        return parameters;
    }

    public RequiredParameterNode getBody() {
        return body;
    }

    public Map<String, Node> getHeaders() {
        return headers;
    }

    public ReturnTypeDescriptorNode getReturnNode() {
        return returnNode;
    }

    /**
     * This is the builder class for the {@link ResourceMethod}.
     */
    public static  class ResourceMethodBuilder {
        private String path;
        private Location location;
        private String method;
        private Map<String, Node> parameters;
        private RequiredParameterNode body;
        private Map<String, Node> headers;
        private ReturnTypeDescriptorNode returnNode;

        public ResourceMethodBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ResourceMethodBuilder location(Location resourcePosition) {
            this.location = resourcePosition;
            return this;
        }

        public ResourceMethodBuilder method(String method) {
            this.method = method;
            return this;
        }

        public ResourceMethodBuilder parameters(Map<String, Node> parameters) {
            this.parameters = parameters;
            return this;
        }

        public ResourceMethodBuilder body(RequiredParameterNode body) {
            this.body = body;
            return this;
        }

        public ResourceMethodBuilder headers(Map<String, Node> headers) {
            this.headers = headers;
            return this;
        }

        public ResourceMethodBuilder returnNode(ReturnTypeDescriptorNode returnNode) {
            this.returnNode = returnNode;
            return this;
        }

        public ResourceMethod build() {
            ResourceMethod resourceMethod = new ResourceMethod(this);
            return resourceMethod;
        }
    }
}
