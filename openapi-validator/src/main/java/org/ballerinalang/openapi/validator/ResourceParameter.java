/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.openapi.validator;

import io.ballerina.compiler.syntax.tree.ParameterNode;

/**
 * Container to hold the resource parameter details needed for the Resource param.
 */
public class ResourceParameter {
    private String name;
    private String type;
    private ParameterNode parameter;

    ResourceParameter() {
        name = null;
        type = null;
        parameter = null;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    void setType(String type) {
        this.type = type;
    }

    ParameterNode getParameter() {
        return parameter;
    }

    void setParameter(ParameterNode parameter) {
        this.parameter = parameter;
    }

    public String getType() {
        return type;
    }
}
