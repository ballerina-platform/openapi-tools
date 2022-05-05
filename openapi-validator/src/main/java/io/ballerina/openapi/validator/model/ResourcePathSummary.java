/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.validator.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Summarized details of a Ballerina resource to be validated against OpenAPI document.
 *
 * @since 2201.1.0
 */
public class ResourcePathSummary {
    private final String path;
    private final List<String> availableMethods;
    private final Map<String, ResourceMethod> methods;

    public ResourcePathSummary(String path) {
        this.methods = new HashMap<>();
        this.availableMethods = new ArrayList<>();
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public Map<String, ResourceMethod> getMethods() {
        return methods;
    }

    public void addMethod(String method, ResourceMethod resourceMethod) {
        this.methods.put(method, resourceMethod);
    }

    public void addAvailableMethod(String method) {
        this.availableMethods.add(method);
    }
}
