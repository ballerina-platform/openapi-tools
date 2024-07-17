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
package io.ballerina.openapi.service.mapper.type;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

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

    public static void  addExtension(Schema schema, TypeSymbol typeSymbol) {
        getBallerinaPackage(typeSymbol).ifPresent(
                ballerinaPackage -> schema.addExtension(X_BALLERINA_TYPE, ballerinaPackage));
    }

    public static void removeExtensions(OpenAPI openAPI) {
        Components components = openAPI.getComponents();
        if (Objects.isNull(components)) {
            return;
        }

        Map<String, Schema> schemas = components.getSchemas();
        if (Objects.isNull(schemas)) {
            return;
        }

        schemas.forEach((key, schema) -> schema.getExtensions().remove(X_BALLERINA_TYPE));
    }

    public static Optional<BallerinaPackage> getExtension(Schema schema) {
        Map extensions = schema.getExtensions();
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
                moduleID.version(), moduleID.modulePrefix()));
    }
}
