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
package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;

/**
 * This class is used to generate the function signature of the client external method's implementation function.
 *
 * @since 1.9.0
 */
public class ImplFunctionSignatureGenerator extends RemoteExternalFunctionSignatureGenerator {
    private final String path;

    public ImplFunctionSignatureGenerator(Operation operation, OpenAPI openAPI, String httpMethod, String path) {
        super(operation, openAPI, httpMethod, path);
        this.treatDefaultableAsRequired = true;
        this.path = path;
    }

    @Override
    protected FunctionReturnTypeGeneratorImp getFunctionReturnTypeGenerator() {
        return new FunctionStatusCodeReturnTypeGenerator(operation, openAPI, httpMethod, path);
    }

    @Override
    protected ParametersInfo populateTargetTypeParam(TypeDescriptorNode targetType, ParametersInfo parametersInfo) {
        ParameterNode targetTypeParam = createRequiredParameterNode(createEmptyNodeList(), targetType,
                createIdentifierToken("targetType"));
        parametersInfo.parameterList().add(targetTypeParam);
        parametersInfo.parameterList().add(createToken(COMMA_TOKEN));
        return parametersInfo;
    }
}
