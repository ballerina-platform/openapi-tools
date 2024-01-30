/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.service.mapper.response;

import org.ballerinalang.model.symbols.TypeSymbol;

import java.util.List;

public class Interceptor {
    public enum InterceptorType {
        REQUEST, REQUEST_ERROR, RESPONSE, RESPONSE_ERROR
    }

    private InterceptorType type;
    private String relativeResourcePath;
    private List<TypeSymbol> returnTypes;

    public InterceptorType getType() {
        return type;
    }

    public void setType(InterceptorType type) {
        this.type = type;
    }

    public String getRelativeResourcePath() {
        return relativeResourcePath;
    }

    public void setRelativeResourcePath(String relativeResourcePath) {
        this.relativeResourcePath = relativeResourcePath;
    }

    public List<TypeSymbol> getReturnTypes() {
        return returnTypes;
    }

    public void setReturnTypes(List<TypeSymbol> returnTypes) {
        this.returnTypes = returnTypes;
    }
}
