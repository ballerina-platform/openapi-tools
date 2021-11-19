/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.generators.schema.schematypes.composedschematypes;

import io.ballerina.openapi.generators.schema.schematypes.SchemaType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ComposedSchema;

/**
 * Factory class to identify the type of the composed schemas.
 *
 * @since 2.0.0
 */
public class ComposedSchemaFactory {
    /**
     * Get SchemaType object relevant to the composed schema given.
     *
     * @param openAPI        OpenAPI definition
     * @param composedSchema Composed schema object
     * @param nullable       Indicates whether the user has given ``nullable command line option
     * @return Relevant SchemaType object
     */
    public SchemaType getComposedSchemaType(OpenAPI openAPI, ComposedSchema composedSchema, boolean nullable) {
        if (composedSchema.getAllOf() != null) {
            return new AllOfSchemaType(openAPI, nullable);
        } else if (composedSchema.getAnyOf() != null) {
            return new AnyOfSchemaType(nullable);
        } else {
            return new OneOfSchemaType(nullable);
        }
    }
}
