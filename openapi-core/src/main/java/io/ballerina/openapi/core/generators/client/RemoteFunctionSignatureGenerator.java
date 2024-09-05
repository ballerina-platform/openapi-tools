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

package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.generators.client.parameter.PathParameterGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;

public class RemoteFunctionSignatureGenerator extends AbstractFunctionSignatureGenerator {

    public RemoteFunctionSignatureGenerator(Operation operation, OpenAPI openAPI, String httpMethod,
                                            String path) {
        super(operation, openAPI, httpMethod, path);
    }

    protected ParametersInfo getParametersInfo(List<Parameter> parameters) {
        List<Node> requiredParams;
        List<Node> defaultableParams;
        List<Node> includedParam;
        Token comma = createToken(COMMA_TOKEN);

        ParametersInfo parametersInfo = super.getParametersInfo(parameters);

        if (Objects.isNull(parameters)) {
            return parametersInfo;
        }

        if (Objects.isNull(parametersInfo)) {
            requiredParams = new ArrayList<>();
            defaultableParams = new ArrayList<>();
            includedParam = new ArrayList<>();
        } else {
            requiredParams = parametersInfo.requiredParams();
            defaultableParams = parametersInfo.defaultableParams();
            includedParam = parametersInfo.includedParam();
        }

        // path parameters
        for (Parameter parameter : parameters) {
            if (parameter.getIn().equals("path")) {
                PathParameterGenerator paramGenerator = new PathParameterGenerator(parameter, openAPI);
                Optional<ParameterNode> param = paramGenerator.generateParameterNode();
                if (param.isEmpty()) {
                    diagnostics.addAll(paramGenerator.getDiagnostics());
                    return null;
                }
                // Path parameters are always required.
                requiredParams.add(param.get());
                requiredParams.add(comma);
            }
        }
        return new ParametersInfo(requiredParams, defaultableParams, includedParam);
    }
}
