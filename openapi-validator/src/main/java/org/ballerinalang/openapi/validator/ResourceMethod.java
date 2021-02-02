/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.openapi.validator;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeLocation;
import io.ballerina.tools.diagnostics.Location;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for containing the service details.
 */
public class ResourceMethod {
    private Location resourcePosition;
    private String method;
    private Location methodPosition;
    private Map<String, Node> parameters;
    private String body;

    ResourceMethod() {
        this.method = null;
        this.methodPosition = null;
        this.resourcePosition = null;
        this.parameters = new HashMap<>();
        this.body = null;
    }

    public Map<String, Node> getParameters() {

        return parameters;
    }

    public void setParameters(Map<String, Node> parameters) {

        this.parameters = parameters;
    }

    public Location getMethodPosition() {
        return methodPosition;
    }

    public void setMethodPosition(Location methodsPosition) {
        this.methodPosition = methodsPosition;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return this.method;
    }


    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setResourcePosition(Location position) {
        this.resourcePosition = position;
    }

    public Location getResourcePosition(NodeLocation location) {
        return resourcePosition;
    }

}
