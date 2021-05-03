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

package io.ballerina.openapi.validator.tests;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;

/**
 * This test util for extract the symbol type for testing purposes.
 */
public class Inputs {
    private TypeSymbol paramType;
    private SemanticModel semanticModel;
    private SyntaxTree syntaxTree;

    public Inputs() {

    }

    public Inputs(TypeSymbol paramType, SyntaxTree syntaxTree, SemanticModel semanticModel) {
        this.paramType = paramType;
        this.syntaxTree = syntaxTree;
        this.semanticModel = semanticModel;
    }

    public TypeSymbol getParamType() {

        return paramType;
    }

    public void setParamType(TypeSymbol paramType) {

        this.paramType = paramType;
    }

    public  SyntaxTree getSyntaxTree() {

        return syntaxTree;
    }

    public void setSyntaxTree(SyntaxTree syntaxTree) {

        this.syntaxTree = syntaxTree;
    }

    public  SemanticModel getSemanticModel() {

        return semanticModel;
    }

    public void setSemanticModel(SemanticModel semanticModel) {

        this.semanticModel = semanticModel;
    }
}
