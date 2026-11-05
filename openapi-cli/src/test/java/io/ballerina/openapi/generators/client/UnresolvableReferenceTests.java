/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.generators.client;

import io.ballerina.openapi.core.generators.client.BallerinaClientGenerator;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.client.model.OASClientConfig;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.Filter;
import io.ballerina.openapi.core.generators.service.ServiceDeclarationGenerator;
import io.ballerina.openapi.core.generators.service.model.OASServiceMetadata;
import io.ballerina.openapi.core.generators.type.TypeGeneratorUtils;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for references which cannot be resolved to a schema under `#/components/schemas`.
 */
public class UnresolvableReferenceTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/generators/client").toAbsolutePath();
    private static final String SPEC = "swagger/json_pointer_reference.yaml";
    private static final String JSON_POINTER_REF = "#/components/schemas/AlbumArray/items";

    @Test(description = "Client generation reports JSON pointer references instead of crashing",
            dataProvider = "clientMethodModes")
    public void testClientGenerationWithJsonPointerReference(boolean isResource) throws IOException,
            BallerinaOpenApiException, ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve(SPEC), true, false);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig oasClientConfig = new OASClientConfig.Builder()
                .withFilters(new Filter(new ArrayList<>(), new ArrayList<>()))
                .withOpenAPI(openAPI)
                .withResourceMode(isResource).build();

        String client = withoutWhitespace(new BallerinaClientGenerator(oasClientConfig).generateSyntaxTree()
                .toSourceCode());

        if (isResource) {
            Assert.assertTrue(client.contains("getalbums(map<string|string[]>headers={})returnsAlbumArray|error"));
            Assert.assertTrue(client.contains("getalbums/[intid](map<string|string[]>headers={})returnsjson|error"));
            Assert.assertTrue(client.contains(
                    "getalbums/[intid]/summary(map<string|string[]>headers={})returnsjson|error"));
        } else {
            Assert.assertTrue(client.contains("getAlbums(map<string|string[]>headers={})returnsAlbumArray|error"));
            Assert.assertTrue(client.contains("getAlbum(intid,map<string|string[]>headers={})returnsjson|error"));
            Assert.assertTrue(client.contains(
                    "getAlbumSummary(intid,map<string|string[]>headers={})returnsjson|error"));
        }
        assertInvalidReferenceDiagnostics(TypeHandler.getInstance().getDiagnostics());
    }

    @DataProvider(name = "clientMethodModes")
    public Object[][] clientMethodModes() {
        return new Object[][]{{false}, {true}};
    }

    @Test(description = "Service generation reports JSON pointer references instead of crashing")
    public void testServiceGenerationWithJsonPointerReference() throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(RES_DIR.resolve(SPEC), true, false);
        TypeHandler.createInstance(openAPI, false);
        OASServiceMetadata oasServiceMetadata = new OASServiceMetadata.Builder()
                .withOpenAPI(openAPI)
                .withFilters(new Filter(new ArrayList<>(), new ArrayList<>()))
                .build();

        String service = withoutWhitespace(new ServiceDeclarationGenerator(oasServiceMetadata).generateSyntaxTree()
                .toSourceCode());

        Assert.assertTrue(service.contains("resourcefunctiongetalbums()returnsAlbumArray"), service);
        Assert.assertTrue(service.contains("resourcefunctiongetalbums/[intid]()returnsjson"), service);
        Assert.assertTrue(service.contains("resourcefunctiongetalbums/[intid]/summary()returnsjson"), service);
        assertInvalidReferenceDiagnostics(TypeHandler.getInstance().getDiagnostics());
    }

    @Test(description = "A JSON pointer reference is reported even when its last segment names another schema")
    public void testJsonPointerReferenceWithMatchingComponentName() throws IOException, BallerinaOpenApiException,
            ClientException {
        OpenAPI openAPI = GeneratorUtils.normalizeOpenAPI(
                RES_DIR.resolve("swagger/json_pointer_reference_collision.yaml"), true, false);
        TypeHandler.createInstance(openAPI, false);
        OASClientConfig oasClientConfig = new OASClientConfig.Builder()
                .withFilters(new Filter(new ArrayList<>(), new ArrayList<>()))
                .withOpenAPI(openAPI)
                .withResourceMode(false).build();

        String client = withoutWhitespace(new BallerinaClientGenerator(oasClientConfig).generateSyntaxTree()
                .toSourceCode());

        // The reference must not resolve to the unrelated `items` schema, including inside `allOf`.
        Assert.assertTrue(client.contains("getAlbum(intid,map<string|string[]>headers={})returnsjson|error"), client);
        Assert.assertTrue(client.contains("getAlbumDetails(intid,map<string|string[]>headers={})returnsjson|error"),
                client);
        assertInvalidReferenceDiagnostics(TypeHandler.getInstance().getDiagnostics());
    }

    @Test(description = "Referenced component schemas are resolved", dataProvider = "componentSchemaReferences")
    public void testGetReferencedComponentSchema(String reference, String expectedSchema) throws OASTypeGenException {
        OpenAPI openAPI = new OpenAPI().components(new Components()
                .addSchemas("Album", new ObjectSchema().description("Album"))
                .addSchemas("items", new ObjectSchema().description("items")));
        GeneratorMetaData.createInstance(openAPI, false);
        Assert.assertEquals(TypeGeneratorUtils.getReferencedComponentSchema(reference).getDescription(),
                expectedSchema);
    }

    @DataProvider(name = "componentSchemaReferences")
    public Object[][] componentSchemaReferences() {
        return new Object[][]{
                {"#/components/schemas/Album", "Album"},
                {"#/components/schemas/items", "items"}
        };
    }

    @Test(description = "References which do not resolve to a component schema are rejected",
            dataProvider = "unresolvableSchemaReferences", expectedExceptions = OASTypeGenException.class)
    public void testGetReferencedComponentSchemaFailure(String reference, boolean hasSchemas)
            throws OASTypeGenException {
        Components components = new Components();
        if (hasSchemas) {
            components.addSchemas("Album", new ObjectSchema()).addSchemas("items", new ObjectSchema());
        }
        GeneratorMetaData.createInstance(new OpenAPI().components(components), false);
        TypeGeneratorUtils.getReferencedComponentSchema(reference);
    }

    @DataProvider(name = "unresolvableSchemaReferences")
    public Object[][] unresolvableSchemaReferences() {
        return new Object[][]{
                // JSON pointer into a schema whose last segment matches another component schema
                {"#/components/schemas/Album/items", true},
                {"#/components/schemas/Missing", true},
                {"./external.yaml#/Album", true},
                // Components section without schemas
                {"#/components/schemas/Album", false}
        };
    }

    private static void assertInvalidReferenceDiagnostics(List<Diagnostic> diagnostics) {
        List<String> messages = diagnostics.stream().map(Diagnostic::message).toList();
        Assert.assertTrue(messages.contains("Invalid reference found " + JSON_POINTER_REF + " in the schema."),
                messages.toString());
        Assert.assertTrue(messages.stream().noneMatch(message -> message.contains("null")), messages.toString());
    }

    private static String withoutWhitespace(String value) {
        return value.replaceAll("\\s+", "");
    }
}
