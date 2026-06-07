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
package io.ballerina.openapi.service.mapper.model;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Optional;

/**
 * This {@link OASResult} is used to contain OpenAPI definition in string format and error list.
 *
 * @since 1.0.0
 */
public class OASResult {
    private OpenAPI openAPI;
    private String serviceName; // added base path for key to definition
    private final List<OpenAPIMapperDiagnostic> diagnostics;

    private static boolean yamlMapperPatched = false;

    private static void patchYamlMapperForSchemaOrder() {
        if (!yamlMapperPatched) {
            yamlMapperPatched = true;
            Yaml.mapper().setSerializerFactory(
                    Yaml.mapper().getSerializerFactory().withSerializerModifier(new BeanSerializerModifier() {
                        @Override
                        public List<BeanPropertyWriter> orderProperties(SerializationConfig config,
                                BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                            if (Schema.class.isAssignableFrom(beanDesc.getBeanClass())) {
                                ensureDefaultBeforeEnum(beanProperties);
                            }
                            return beanProperties;
                        }
                    })
            );
        }
    }

    private static void ensureDefaultBeforeEnum(List<BeanPropertyWriter> props) {
        int defaultIdx = -1, enumIdx = -1;
        for (int i = 0; i < props.size(); i++) {
            String name = props.get(i).getName();
            if ("default".equals(name)) { defaultIdx = i; }
            else if ("enum".equals(name)) { enumIdx = i; }
        }
        if (enumIdx != -1 && defaultIdx != -1 && enumIdx < defaultIdx) {
            BeanPropertyWriter defaultProp = props.remove(defaultIdx);
            props.add(enumIdx, defaultProp);
        }
    }

    /**
     * This constructor is used to store the details that Map of {@code OpenAPI} objects and diagnostic list.
     */
    public OASResult(OpenAPI openAPI, List<OpenAPIMapperDiagnostic> diagnostics) {
        this.openAPI = openAPI;
        this.diagnostics = diagnostics;
    }

    public List<OpenAPIMapperDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public Optional<OpenAPI> getOpenAPI() {
        return Optional.ofNullable(openAPI);
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public Optional<String> getYaml() {
        patchYamlMapperForSchemaOrder();
        return Optional.ofNullable(Yaml.pretty(this.openAPI));
    }

    public Optional<String> getJson() {
        return Optional.ofNullable(Json.pretty(this.openAPI));
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setOpenAPI(OpenAPI openAPI) {
        this.openAPI = openAPI;
    }
}
