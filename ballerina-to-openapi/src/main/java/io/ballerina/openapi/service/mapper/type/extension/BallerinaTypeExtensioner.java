/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
package io.ballerina.openapi.service.mapper.type.extension;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This {@link BallerinaTypeExtensioner} class adds ballerina specific extensions to the OpenAPI type schemas.
 *
 * @since 2.1.0
 */
public final class BallerinaTypeExtensioner {

    private static final String X_BALLERINA_TYPE = "x-ballerina-type";

    private BallerinaTypeExtensioner() {
    }

    public static void addExtension(Schema schema, TypeSymbol typeSymbol) {
        getBallerinaPackage(typeSymbol).ifPresent(
                ballerinaPackage -> schema.addExtension(X_BALLERINA_TYPE, ballerinaPackage));
    }

    public static void removeExtensions(OpenAPI openAPI) {
        removeExtensionsFromOpenAPI(openAPI, (extensions, orgName, moduleName) -> true);
    }

    public static void removeCurrentModuleTypeExtensions(OpenAPI openAPI, ModuleID moduleID) {
        String orgName = moduleID.orgName();
        String moduleName = moduleID.moduleName();

        removeExtensionsFromOpenAPI(openAPI, (extensions, org, mod) -> fromSameModule(extensions, orgName,
                moduleName));
    }

    private static void removeExtensionsFromOpenAPI(OpenAPI openAPI, ExtensionRemovalCondition condition) {
        Components components = openAPI.getComponents();
        if (Objects.isNull(components)) {
            return;
        }
        removeExtensionFromComponents(components, condition);
    }

    public static void removeExtensionFromComponents(Components components) {
        removeExtensionFromComponents(components, (extensions, orgName, moduleName) -> true);
    }

    private static void removeExtensionFromComponents(Components components, ExtensionRemovalCondition condition) {
        Map<String, Schema> schemas = components.getSchemas();
        if (Objects.isNull(schemas)) {
            return;
        }

        schemas.forEach((key, schema) -> {
            removeExtensionFromSchema(schema, condition);
        });
    }

    public static void removeExtensionFromSchema(Schema schema) {
        removeExtensionFromSchema(schema, (extensions, orgName, moduleName) -> true);
    }

    private static void removeExtensionFromSchema(Schema schema, ExtensionRemovalCondition condition) {
        if (Objects.isNull(schema)) {
            return;
        }
        Map<?, ?> extensions = schema.getExtensions();
        if (Objects.nonNull(extensions) && condition.shouldRemove(extensions, null, null)) {
            extensions.remove(X_BALLERINA_TYPE);
        }
        Map<String, Schema> properties = schema.getProperties();
        if (Objects.nonNull(properties)) {
            properties.values().forEach(value -> removeExtensionFromSchema(value, condition));
        }
        List<Schema> allOfSchemas = schema.getAllOf();
        if (Objects.nonNull(allOfSchemas)) {
            allOfSchemas.forEach(value -> removeExtensionFromSchema(value, condition));
        }
        List<Schema> oneOfSchemas = schema.getOneOf();
        if (Objects.nonNull(oneOfSchemas)) {
            oneOfSchemas.forEach(value -> removeExtensionFromSchema(value, condition));
        }
        List<Schema> anyOfSchemas = schema.getAnyOf();
        if (Objects.nonNull(anyOfSchemas)) {
            anyOfSchemas.forEach(value -> removeExtensionFromSchema(value, condition));
        }
    }

    @FunctionalInterface
    private interface ExtensionRemovalCondition {
        boolean shouldRemove(Map<?, ?> extensions, String orgName, String moduleName);
    }

    private static boolean fromSameModule(Map<?, ?> extensions, String orgName, String moduleName) {
        return extensions.get(X_BALLERINA_TYPE) instanceof BallerinaPackage ballerinaPkg &&
                orgName.equals(ballerinaPkg.orgName()) && moduleName.equals(ballerinaPkg.moduleName());
    }

    public static Optional<BallerinaPackage> getExtension(Schema schema) {
        if (Objects.isNull(schema)) {
            return Optional.empty();
        }
        Map<?, ?> extensions = schema.getExtensions();
        if (Objects.isNull(extensions)) {
            return Optional.empty();
        }

        Object ballerinaExt = extensions.get(X_BALLERINA_TYPE);
        if (Objects.isNull(ballerinaExt) || !(ballerinaExt instanceof BallerinaPackage ballerinaPkg)) {
            return Optional.empty();
        }
        return Optional.of(ballerinaPkg);
    }

    static Optional<BallerinaPackage> getBallerinaPackage(TypeSymbol typeSymbol) {
        Optional<ModuleSymbol> module = typeSymbol.getModule();
        if (module.isEmpty()) {
            return Optional.empty();
        }
        ModuleID moduleID = module.get().id();
        return Optional.of(new BallerinaPackage(moduleID.orgName(), moduleID.packageName(), moduleID.moduleName(),
                moduleID.version(), moduleID.modulePrefix(), typeSymbol.getName().orElse(null)));
    }
}
