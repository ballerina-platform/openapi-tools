/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.generators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;

import java.io.IOException;
import java.nio.file.Path;

import static org.ballerinalang.generators.GeneratorUtils.getBallerinaOpenApiType;

/**
 * This Util class use for generating ballerina client file according to given yaml file.
 */
public class BallerinaClientGenerator {
    public static SyntaxTree generateSyntaxTree(Path definitionPath, Filter filter)
            throws IOException, BallerinaOpenApiException {
        // 1. Load client template syntax tree
        SyntaxTree syntaxTree = null;
        // Create imports http and openapi
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode("ballerina"
                , "http");
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createNodeList(importForHttp);
        // Generate client config record
        Token visibilityQualifier = AbstractNodeFactory.createIdentifierToken("public");
        Token typeKeyWord = AbstractNodeFactory.createIdentifierToken("type");
        IdentifierToken typeName = AbstractNodeFactory.createIdentifierToken("clientConfig");

        Token recordKeyWord = AbstractNodeFactory.createIdentifierToken("record ");
        Token bodyStartDelimiter = AbstractNodeFactory.createIdentifierToken("{|");


        // Summaries OpenAPI details
        OpenAPI openApi = getBallerinaOpenApiType(definitionPath);



        return syntaxTree;
    }

}
