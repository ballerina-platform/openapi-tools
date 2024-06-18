package io.ballerina.openapi.service.mapper.model;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

public class ResourceFunctionDefinition implements ResourceFunction {
    FunctionDefinitionNode resourceFunction;
    FunctionSignatureNode functionSignature;

    public ResourceFunctionDefinition(FunctionDefinitionNode functionDefinitionNode) {
        resourceFunction = new FunctionDefinitionNode(functionDefinitionNode.internalNode(),
                functionDefinitionNode.position(), functionDefinitionNode.parent());
        functionSignature = functionDefinitionNode.functionSignature();
    }

    public NodeList<Node> relativeResourcePath() {
        return resourceFunction.relativeResourcePath();
    }

    public String functionName() {
        return resourceFunction.functionName().text().trim();
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
        return ((ServiceDeclarationNode) resourceFunction.parent()).metadata();
    }
}
