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

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNilLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.STRING;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;

public class QueryParameterGenerator implements ParameterGenerator {
    OpenAPI openAPI;
    Parameter parameter;

    List<ClientDiagnostic> diagnostics = new ArrayList<>();
    public QueryParameterGenerator(Parameter parameter, OpenAPI openAPI) {
        this.parameter = parameter;
        this.openAPI = openAPI;
    }
    @Override
    public Optional<ParameterNode> generateParameterNode(boolean treatDefaultableAsRequired) {

        TypeDescriptorNode typeNode;

        Schema<?> parameterSchema = parameter.getSchema();
        if (parameterSchema.getType() != null && !isQueryParamTypeSupported(parameterSchema.getType())) {
            DiagnosticMessages unsupportedType = DiagnosticMessages.OAS_CLIENT_102;
            ClientDiagnostic diagnostic = new ClientDiagnosticImp(unsupportedType,
                    unsupportedType.getDescription(), parameter.getName());
            diagnostics.add(diagnostic);
            return Optional.empty();
        }

        //supported type: type BasicType boolean|int|float|decimal|string|map<anydata>|enum;
        //public type QueryParamType ()|BasicType|BasicType[];
        Optional<TypeDescriptorNode> result = TypeHandler.getInstance().getTypeNodeFromOASSchema(parameterSchema,
                true);
        if (result.isEmpty()) {
            DiagnosticMessages unsupportedType = DiagnosticMessages.OAS_CLIENT_102;
            ClientDiagnostic diagnostic = new ClientDiagnosticImp(unsupportedType, parameter.getName());
            diagnostics.add(diagnostic);
            return Optional.empty();
        }
        typeNode = result.get();
        // required parameter
        if (parameter.getRequired() || treatDefaultableAsRequired) {
            IdentifierToken paramName =
                    createIdentifierToken(getValidName(parameter.getName().trim(), false));
            return Optional.of(createRequiredParameterNode(createEmptyNodeList(), typeNode, paramName));
        } else {
            IdentifierToken paramName =
                    createIdentifierToken(getValidName(parameter.getName().trim(), false));
            // Handle given default values in query parameter.
            if (parameterSchema.get$ref() != null) {
                Schema<?> schema = openAPI.getComponents().getSchemas().get(parameterSchema.get$ref().split("/")[3]);
                if (schema != null) {
                    parameterSchema = schema;
                }
            }
            if (parameterSchema.getDefault() != null) {
                LiteralValueToken literalValueToken;
                if (Objects.equals(getOpenAPIType(parameterSchema), STRING)) {
                    literalValueToken = createLiteralValueToken(null,
                            '"' + parameterSchema.getDefault().toString() + '"', createEmptyMinutiaeList(),
                            createEmptyMinutiaeList());
                } else {
                    literalValueToken =
                            createLiteralValueToken(null, parameterSchema.getDefault().toString(),
                                    createEmptyMinutiaeList(),
                                    createEmptyMinutiaeList());

                }
                return Optional.of(createDefaultableParameterNode(createEmptyNodeList(), typeNode, paramName,
                        createToken(EQUAL_TOKEN), literalValueToken));
            } else {
                if (!typeNode.toString().endsWith("?")) {
                    typeNode = createSimpleNameReferenceNode(createIdentifierToken(typeNode.toString() + "?"));
                }
                NilLiteralNode nilLiteralNode =
                        createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
                return Optional.of(createDefaultableParameterNode(createEmptyNodeList(), typeNode, paramName,
                        createToken(EQUAL_TOKEN), nilLiteralNode));
            }
        }
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    private boolean isQueryParamTypeSupported(String type) {
        return type.equals("boolean") || type.equals("integer") || type.equals("number") ||
                type.equals("array") || type.equals("string") || type.equals("object");
    }
}
