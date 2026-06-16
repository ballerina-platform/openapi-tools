/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.service.mapper.utils;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

/**
 * Patches the swagger-core YAML mapper so that {@code default} is serialized before {@code enum}
 * in Schema objects. swagger-core 2.2.22+ changed this ordering; this restores the prior behavior.
 */
public final class YamlSchemaOrderPatch {

    private static volatile boolean applied = false;

    private YamlSchemaOrderPatch() {
    }

    public static void applyIfNeeded() {
        if (!applied) {
            applied = true;
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
            if ("default".equals(name)) {
                defaultIdx = i;
            } else if ("enum".equals(name)) {
                enumIdx = i;
            }
        }
        if (enumIdx != -1 && defaultIdx != -1 && enumIdx < defaultIdx) {
            BeanPropertyWriter defaultProp = props.remove(defaultIdx);
            props.add(enumIdx, defaultProp);
        }
    }
}
