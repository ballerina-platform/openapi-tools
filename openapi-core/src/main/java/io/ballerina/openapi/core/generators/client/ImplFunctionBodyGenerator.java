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

import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;

import java.util.List;
import java.util.Map;

import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getSimpleExpressionStatementNode;

/**
 * This class is used to generate the function body of the client external method's implementation function.
 *
 * @since 1.9.0
 */
public class ImplFunctionBodyGenerator extends FunctionBodyGeneratorImp {
    private final boolean hasDefaultStatusResponse;
    private final List<String> nonDefaultStatusCodes;

    public ImplFunctionBodyGenerator(String path, Map.Entry<PathItem.HttpMethod, Operation> operation, OpenAPI openAPI,
                                     AuthConfigGeneratorImp ballerinaAuthConfigGeneratorImp,
                                     BallerinaUtilGenerator ballerinaUtilGenerator,
                                     List<ImportDeclarationNode> imports, boolean hasHeaders,
                                     boolean hasDefaultHeaders, boolean hasQueries, boolean hasDefaultResponse,
                                     List<String> nonDefaultStatusCodes) {
        super(path, operation, openAPI, ballerinaAuthConfigGeneratorImp, ballerinaUtilGenerator, imports,
                hasHeaders, hasDefaultHeaders, hasQueries);
        this.hasDefaultStatusResponse = hasDefaultResponse;
        this.nonDefaultStatusCodes = nonDefaultStatusCodes;
    }

    @Override
    protected String getClientCallWithHeadersParam() {
        return addTargetTypeParam(super.getClientCallWithHeadersParam());
    }

    @Override
    protected String getClientCallWithRequestAndHeaders() {
        return addTargetTypeParam(super.getClientCallWithRequestAndHeaders());
    }

    @Override
    protected String getClientCallWithHeaders() {
        return addTargetTypeParam(super.getClientCallWithHeaders());
    }

    @Override
    protected String getClientCallWithRequest() {
        return addTargetTypeParam(super.getClientCallWithRequest());
    }

    @Override
    protected String getSimpleClientCall() {
        return addTargetTypeParam(super.getSimpleClientCall());
    }

    @Override
    protected void generateReturnStatement(List<StatementNode> statementsList, String returnStatement) {
        if (hasDefaultStatusResponse) {
            String responseVarExp = "var response = " + returnStatement;
            ExpressionStatementNode responseStatement = getSimpleExpressionStatementNode(responseVarExp);
            statementsList.add(responseStatement);

            String nonDefaultStatusCodeList = nonDefaultStatusCodes.isEmpty() ? "[]" : "[" + String.join(", ",
                    nonDefaultStatusCodes) + "]";
            String nonDefaultStatusCodesExp = "int[] nonDefaultStatusCodes = " + nonDefaultStatusCodeList;
            ExpressionStatementNode nonDefaultStatusCodesStatement = getSimpleExpressionStatementNode(
                    nonDefaultStatusCodesExp);
            statementsList.add(nonDefaultStatusCodesStatement);

            returnStatement = "getValidatedResponseForDefaultMapping(response, nonDefaultStatusCodes)";
        }
        super.generateReturnStatement(statementsList, returnStatement);
    }

    private String addTargetTypeParam(String clientCall) {
        String substring = clientCall.substring(0, clientCall.length() - 1);
        return substring + ", targetType = targetType)";
    }
}
