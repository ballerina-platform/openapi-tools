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
package io.ballerina.openapi.service.mapper.example;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.openapi.service.mapper.diagnostic.OpenAPIMapperDiagnostic;
import io.ballerina.openapi.service.mapper.example.field.RecordFieldExampleMapper;
import io.ballerina.openapi.service.mapper.example.type.TypeExampleMapper;
import io.ballerina.openapi.service.mapper.model.AdditionalData;
import io.ballerina.openapi.service.mapper.model.ModuleMemberVisitor;
import io.ballerina.openapi.service.mapper.type.BallerinaPackage;
import io.ballerina.openapi.service.mapper.type.BallerinaTypeExtensioner;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * This {@link OpenAPIExampleMapper} class is the implementation of the {@link OpenAPIExampleMapper} interface.
 * This class is responsible for mapping all the examples in the OpenAPI specification.
 *
 * @since 2.1.0
 */
public class OpenAPIExampleMapperImpl implements OpenAPIExampleMapper {

    private final OpenAPI openAPI;
    private final List<OpenAPIMapperDiagnostic> diagnostics;
    private final ServiceDeclarationNode serviceDeclarationNode;
    private final SemanticModel semanticModel;

    public OpenAPIExampleMapperImpl(OpenAPI openAPI, ServiceDeclarationNode serviceDeclarationNode,
                                    AdditionalData additionalData) {
        this.openAPI = openAPI;
        this.diagnostics = additionalData.diagnostics();
        this.serviceDeclarationNode = serviceDeclarationNode;
        this.semanticModel = additionalData.semanticModel();
    }

    @Override
    public void setExamples() {
        Components components = openAPI.getComponents();
        if (Objects.isNull(components)) {
            return;
        }
        Map<String, Schema> schemas = components.getSchemas();
        schemas.forEach((key, schema) -> {
            setExamplesForTypes(key, schema);
        });
    }

    private void setExamplesForTypes(String key, Schema schema) {
        Optional<BallerinaPackage> ballerinaExt = BallerinaTypeExtensioner.getExtension(schema);
        if (ballerinaExt.isEmpty()) {
            return;
        }
        BallerinaPackage ballerinaPkg = ballerinaExt.get();
        Optional<Symbol> ballerinaType = semanticModel.types().getTypeByName(ballerinaPkg.orgName(),
                ballerinaPkg.moduleName(), ballerinaPkg.version(), key);
        if (ballerinaType.isEmpty() || !(ballerinaType.get() instanceof TypeDefinitionSymbol typeDefSymbol)) {
            return;
        }

        // Map examples from types
        ExampleMapper typeExampleMapper = new TypeExampleMapper(typeDefSymbol, schema, semanticModel, diagnostics);
        typeExampleMapper.setExample();

        if (typeDefSymbol.typeDescriptor() instanceof RecordTypeSymbol recordTypeSymbol
                && schema instanceof ObjectSchema objectSchema) {
            // Map examples from record fields
            ExampleMapper recordFieldExampleMapper = new RecordFieldExampleMapper(recordTypeSymbol, objectSchema,
                    semanticModel, diagnostics);
            recordFieldExampleMapper.setExample();
        }
    }
}
