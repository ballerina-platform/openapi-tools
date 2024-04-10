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

import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNilLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;

public class RequestBodyHeaderParameter implements ParameterGenerator {
    Map.Entry<String, Header> header;
    List<ClientDiagnostic> diagnostics = new ArrayList<>();

    RequestBodyHeaderParameter(Map.Entry<String, Header> header) {
        this.header = header;
    }

    @Override
    public Optional<ParameterNode> generateParameterNode() {
        Schema<?> schema = header.getValue().getSchema();
        Boolean required = header.getValue().getRequired();
        if (required == null) {
            required = false;
            schema.setNullable(true);
        }
        Optional<TypeDescriptorNode> typeNodeResult = TypeHandler.getInstance()
                .getTypeNodeFromOASSchema(schema, true);
        if (typeNodeResult.isEmpty()) {
            ClientDiagnosticImp diagnosticImp = new ClientDiagnosticImp(DiagnosticMessages.OAS_CLIENT_108,
                    header.getKey());
            diagnostics.add(diagnosticImp);
            return Optional.empty();
        }
        if (required) {
            RequiredParameterNode requiredParameterNode = createRequiredParameterNode(createEmptyNodeList(),
                    typeNodeResult.get(), createIdentifierToken(getValidName(header.getKey(), false)));
            return Optional.of(requiredParameterNode);
        } else {
            NilLiteralNode nilLiteralNode =
                    createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
            return Optional.of(createDefaultableParameterNode(createEmptyNodeList(), typeNodeResult.get(),
                    createIdentifierToken(getValidName(header.getKey(), false)), createToken(EQUAL_TOKEN),
                    nilLiteralNode));
        }
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }

}
