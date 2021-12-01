package io.ballerina.openapi.generators.schema.model;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Stores metadata related to Ballerina types generation.
 */
public class GeneratorMetaData {
    private final OpenAPI openAPI;
    private final boolean nullable;
    private static GeneratorMetaData generatorMetaData = null;

    private GeneratorMetaData(OpenAPI openAPI, boolean nullable) {
        this.openAPI = openAPI;
        this.nullable = nullable;
    }

    public static void createInstance(OpenAPI openAPI, boolean nullable) {
        generatorMetaData = new GeneratorMetaData(openAPI, nullable);
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
