package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.MethodDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

public class ResourceFunctionDeclaration implements ResourceFunction {
    MethodDeclarationNode resourceFunction;
    FunctionSignatureNode functionSignature;

    public ResourceFunctionDeclaration(MethodDeclarationNode methodDeclarationNode) {
        resourceFunction = new MethodDeclarationNode(methodDeclarationNode.internalNode(),
                methodDeclarationNode.position(), methodDeclarationNode.parent());
        functionSignature = methodDeclarationNode.methodSignature();
    }

    public NodeList<Node> relativeResourcePath() {
        return resourceFunction.relativeResourcePath();
    }

    public String functionName() {
        return resourceFunction.methodName().text().trim();
    }

    public Location location() {
        return resourceFunction.location();
    }

    public Optional<MetadataNode> metadata() {
        return resourceFunction.metadata();
    }

    public Optional<Symbol> getSymbol(SemanticModel semanticModel) {
        return semanticModel.symbol(resourceFunction);
    }

    public FunctionSignatureNode functionSignature() {
        return new FunctionSignatureNode(functionSignature.internalNode(), functionSignature.position(),
                functionSignature.parent());
    }

    public Optional<MetadataNode> parentMetaData() {
        return ((TypeDefinitionNode) resourceFunction.parent().parent()).metadata();
    }
}
