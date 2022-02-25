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

package io.ballerina.openapi.validator.error;

import io.ballerina.tools.diagnostics.Location;

public class TypeMismatchParameter extends ValidationError {
    private final String parameterName;
    private final String jsonType;
    private final String ballerinaType;

    public TypeMismatchParameter(String parameterName, String jsonType, String ballerinaType, Location location) {
        super(location);
        this.parameterName = parameterName;
        this.jsonType = jsonType;
        this.ballerinaType = ballerinaType;
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getJsonType() {
        return jsonType;
    }

    public String getBallerinaType() {
        return ballerinaType;
    }
}
