package io.ballerina.openapi.validator.example;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ObjectTypeDescriptorNode;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;

import java.util.Optional;

import static io.ballerina.openapi.validator.Constants.BALLERINA;
import static io.ballerina.openapi.validator.Constants.EMPTY;
import static io.ballerina.openapi.validator.Constants.HTTP;
import static io.ballerina.openapi.validator.Constants.SERVICE_CONTRACT_TYPE;

public class ContractParamExampleAnalyzer extends ParameterExampleAnalyzer {

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        if (diagnosticContainsErrors(context)) {
            return;
        }

        Node node = context.node();
        SemanticModel semanticModel = context.semanticModel();
        Optional<ObjectTypeSymbol> serviceContractObjType = getServiceContractObjType(semanticModel, node);

        if (serviceContractObjType.isEmpty()) {
            return;
        }
        serviceContractObjType.get().methods().values().forEach(method ->
                validateAnnotationOnResources(context, method, false, semanticModel));
    }

    private static Optional<ObjectTypeSymbol> getServiceContractObjType(SemanticModel semanticModel, Node node) {
        if (!(node instanceof ObjectTypeDescriptorNode objectTypeNode)) {
            return Optional.empty();
        }

        Optional<Symbol> serviceObjSymbol = semanticModel.symbol(objectTypeNode.parent());
        if (serviceObjSymbol.isEmpty() ||
                (!(serviceObjSymbol.get() instanceof TypeDefinitionSymbol serviceObjTypeDef))) {
            return Optional.empty();
        }

        Optional<Symbol> serviceContractType = semanticModel.types().getTypeByName(BALLERINA, HTTP, EMPTY,
                SERVICE_CONTRACT_TYPE);
        if (serviceContractType.isEmpty() ||
                !(serviceContractType.get() instanceof TypeDefinitionSymbol serviceContractTypeDef)) {
            return Optional.empty();
        }

        TypeSymbol serviceType = serviceObjTypeDef.typeDescriptor();
        if (serviceType.subtypeOf(serviceContractTypeDef.typeDescriptor()) &&
                serviceType instanceof ObjectTypeSymbol serviceObjType) {
            return Optional.of(serviceObjType);
        }

        return Optional.empty();
    }
}
