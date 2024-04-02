/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.openapi.core.generators.type.model;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Stores metadata related to Ballerina types generation.
 *
 * @since 1.3.0
 */
public class GeneratorMetaData {

    private final OpenAPI openAPI;
    private final boolean nullable;
    private static GeneratorMetaData generatorMetaData = null;

    private GeneratorMetaData(OpenAPI openAPI, boolean nullable) {
        this.openAPI = openAPI;
        this.nullable = nullable;
    }

    public static GeneratorMetaData createInstance(OpenAPI openAPI, boolean nullable) {
        generatorMetaData = new GeneratorMetaData(openAPI, nullable);
        return generatorMetaData;
    }

    public static GeneratorMetaData getInstance() {
        return generatorMetaData;
    }

    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    public boolean isNullable() {
        return nullable;
    }

}
