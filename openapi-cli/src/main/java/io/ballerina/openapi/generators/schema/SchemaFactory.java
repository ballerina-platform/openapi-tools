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

package io.ballerina.openapi.generators.schema;

import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.schema.schematypes.AnyDataSchemaType;
import io.ballerina.openapi.generators.schema.schematypes.ArraySchemaType;
import io.ballerina.openapi.generators.schema.schematypes.ObjectSchemaType;
import io.ballerina.openapi.generators.schema.schematypes.PrimitiveSchemaType;
import io.ballerina.openapi.generators.schema.schematypes.ReferencedSchemaType;
import io.ballerina.openapi.generators.schema.schematypes.SchemaType;
import io.ballerina.openapi.generators.schema.schematypes.composedschematypes.ComposedSchemaFactory;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.ballerina.openapi.generators.GeneratorConstants.BOOLEAN;
import static io.ballerina.openapi.generators.GeneratorConstants.INTEGER;
import static io.ballerina.openapi.generators.GeneratorConstants.NUMBER;
import static io.ballerina.openapi.generators.GeneratorConstants.STRING;

/**
 * Factory class to identify the type of the schema.
 *
 * @since 2.0.0
 */
public class SchemaFactory {
    private final List<String> primitiveTypeList =
            new ArrayList<>(Arrays.asList(INTEGER, NUMBER, STRING, BOOLEAN));

    /**
     * Get SchemaType object relevant to the schema given.
     *
     * @param openAPI     OpenAPI definition
     * @param schemaValue Schema object
     * @param nullable    Indicates whether the user has given ``nullable command line option
     * @return Relevant SchemaType object
     * @throws BallerinaOpenApiException On type of schema is not supported
     */
    public SchemaType getSchemaType(OpenAPI openAPI, Schema<?> schemaValue, boolean nullable)
            throws BallerinaOpenApiException {
        if (schemaValue instanceof ComposedSchema) {
            ComposedSchema composedSchema = (ComposedSchema) schemaValue;
            ComposedSchemaFactory composedSchemaFactory = new ComposedSchemaFactory();
            return composedSchemaFactory.getComposedSchemaType(openAPI, composedSchema, nullable);
        } else if (schemaValue instanceof ObjectSchema || schemaValue.getProperties() != null) {
            return new ObjectSchemaType(openAPI, nullable);
        } else if (schemaValue instanceof ArraySchema) {
            return new ArraySchemaType(openAPI, nullable);
        } else if (schemaValue.getType() != null && primitiveTypeList.contains(schemaValue.getType())) {
            return new PrimitiveSchemaType(nullable);
        } else if (schemaValue.get$ref() != null) {
            return new ReferencedSchemaType(openAPI, nullable);
        } else { // when schemaValue.type == null
            return new AnyDataSchemaType(nullable);
        }
    }
}
