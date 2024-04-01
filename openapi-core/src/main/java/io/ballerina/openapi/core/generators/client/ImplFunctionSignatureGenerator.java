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

public class ImplFunctionSignatureGenerator extends RemoteExternalFunctionSignatureGenerator {

    public ImplFunctionSignatureGenerator(Operation operation, OpenAPI openAPI) {
        super(operation, openAPI);
        setTreatDefaultableAsRequired();
    }

    @Override
    protected FunctionReturnTypeGeneratorImp getFunctionReturnTypeGenerator() {
        return new FunctionStatusCodeReturnTypeGenerator(operation, openAPI);
    }

    @Override
    protected ParametersInfo populateTargetTypeParam(TypeDescriptorNode targetType, ParametersInfo parametersInfo) {
        ParameterNode targetTypeParam = createRequiredParameterNode(createEmptyNodeList(), targetType, createIdentifierToken("targetType"));
        parametersInfo.parameterList().add(targetTypeParam);
        parametersInfo.parameterList().add(createToken(COMMA_TOKEN));
        return parametersInfo;
    }
}
