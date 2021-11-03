package io.ballerina.openapi.extension;

/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.openapi.converter.service.OASResult;

import java.util.List;
import java.util.Optional;

/**
 * The extended service for the OpenAPIConverter endpoint.
 *
 * @since 2.0.0
 */
public class OpenAPIConverterResponse {
    @Deprecated
    private String yamlContent;

    private List<OASResult> content;
    private String error;

    public OpenAPIConverterResponse() {
    }

    @Deprecated
    public String getYamlContent() {
        return yamlContent;
    }

    @Deprecated
    public void setYamlContent(String yamlContent) {
        this.yamlContent = yamlContent;
    }

    public List<OASResult> getContent() {
        return content;
    }

    public void setContent(List<OASResult> content) {
        this.content = content;
    }

    public Optional<String> getError() {
        return Optional.ofNullable(this.error);
    }

    public void setError(String error) {
        this.error = error;
    }
}
