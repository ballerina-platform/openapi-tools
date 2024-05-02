/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIncludedRecordParameterNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ASTERISK_TOKEN;
import static io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages.OAS_CLIENT_102;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.QUERIES;

public class QueriesParameterGenerator implements ParameterGenerator {

    private final List<Parameter> parameters;
    private final OpenAPI openAPI;
    private final List<ClientDiagnostic> diagnostics = new ArrayList<>();
    private final Operation operation;
    private final String httpMethod;
    private final String path;

    public QueriesParameterGenerator(List<Parameter> parameters, OpenAPI openAPI, Operation operation,
                                     String httpMethod, String path) {
        this.parameters = parameters;
        this.openAPI = openAPI;
        this.operation = operation;
        this.httpMethod = httpMethod;
        this.path = path;
    }

    @Override
    public Optional<ParameterNode> generateParameterNode() {
        if (parameters.isEmpty()) {
            return Optional.empty();
        }

        ObjectSchema queriesSchema = getQueriesSchema();
        if (Objects.isNull(queriesSchema)) {
            return Optional.empty();
        }

        String operationId = GeneratorUtils.generateOperationUniqueId(operation, path, httpMethod);
        queriesSchema.setDescription("Represents the Queries record for the operation: " + operationId);
        String queriesName = GeneratorUtils.getValidName(operationId, true) + "Queries";
        openAPI.getComponents().addSchemas(queriesName, queriesSchema);
        Schema queriesRefSchema = new ObjectSchema().$ref(queriesName);
        Optional<TypeDescriptorNode> queriesType =  TypeHandler.getInstance().getTypeNodeFromOASSchema(queriesRefSchema,
                true);

        return queriesType.map(typeDescriptorNode -> createIncludedRecordParameterNode(createEmptyNodeList(),
                createToken(ASTERISK_TOKEN), typeDescriptorNode, createIdentifierToken(QUERIES)));

    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    private ObjectSchema getQueriesSchema() {
        Map<String, Schema> properties = new HashMap<>();
        for (Parameter parameter : parameters) {
            properties.put(parameter.getName(), getSchemaWithDetails(parameter));
        }

        properties.entrySet().removeIf(entry -> {
            if (Objects.isNull(entry.getValue())) {
                ClientDiagnostic diagnostic = new ClientDiagnosticImp(OAS_CLIENT_102, entry.getKey());
                diagnostics.add(diagnostic);
                return true;
            }
            return false;
        });

        if (properties.isEmpty()) {
            return null;
        }

        List<String> requiredFields = new ArrayList<>(parameters.stream()
                .filter(parameter -> Boolean.TRUE.equals(parameter.getRequired()))
                .map(Parameter::getName)
                .toList());

        requiredFields.removeIf(field -> !properties.containsKey(field));

        ObjectSchema queriesSchema = new ObjectSchema();
        queriesSchema.setProperties(properties);
        if (!requiredFields.isEmpty()) {
            queriesSchema.setRequired(requiredFields);
        }
        return queriesSchema;
    }

    private Schema getSchemaWithDetails(Parameter parameter) {
        Schema schema = parameter.getSchema();
        if (Objects.isNull(schema)) {
            return null;
        }
        schema.setDescription(parameter.getDescription());
        schema.setDeprecated(parameter.getDeprecated());
        schema.extensions(parameter.getExtensions());
        return schema;
    }
}
