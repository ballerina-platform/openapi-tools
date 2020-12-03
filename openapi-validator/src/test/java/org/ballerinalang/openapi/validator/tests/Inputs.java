package org.ballerinalang.openapi.validator.tests;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxTree;

public class Inputs {
    private   TypeSymbol paramType;
    private   SemanticModel semanticModel;
    private   SyntaxTree syntaxTree;

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
