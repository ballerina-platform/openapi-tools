/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.validator.error;

import io.ballerina.tools.diagnostics.Location;

/**
 * This model for represent the error model with the service which undocumented with contract yaml file as operation.
 */
public class ResourceValidationError  extends ValidationError {
    public Location position;
    public String resourceMethod;
    public String resourcePath;

    public ResourceValidationError(Location position, String resourceMethod, String resourcePath) {
        this.position = position;
        this.resourceMethod = resourceMethod;
        this.resourcePath = resourcePath;
    }
    public Location getPosition() {
        return position;
    }
    public String getResourceMethod() {
        return resourceMethod;
    }
    public String getResourcePath() {
        return resourcePath;
    }
}
