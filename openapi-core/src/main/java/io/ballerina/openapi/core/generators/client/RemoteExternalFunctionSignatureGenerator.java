package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.InferredTypedescDefaultNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createInferredTypedescDefaultNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParameterizedTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeParameterNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.GT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.LT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPEDESC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPEDESC_TYPE_DESC;

public class RemoteExternalFunctionSignatureGenerator extends RemoteFunctionSignatureGenerator {
    public RemoteExternalFunctionSignatureGenerator(Operation operation, OpenAPI openAPI, String httpMethod) {
        super(operation, openAPI, httpMethod);
    }

    @Override
    protected FunctionReturnTypeGeneratorImp getFunctionReturnTypeGenerator() {
        return new FunctionExternalReturnTypeGenerator(operation, openAPI, httpMethod);
    }

    @Override
    protected ParametersInfo getParametersInfo(List<Parameter> parameters) {
        ParametersInfo parametersInfo = super.getParametersInfo(parameters);

        if (parametersInfo == null) {
            return null;
        }

        FunctionReturnTypeGeneratorImp functionReturnTypeGenerator = getFunctionReturnTypeGenerator();
        FunctionReturnTypeGeneratorImp.ReturnTypesInfo returnTypeInfo = functionReturnTypeGenerator.getReturnTypeInfo();
        List<TypeDescriptorNode> returnTypes = returnTypeInfo.types();
        boolean noContentResponseFound = returnTypeInfo.noContentResponseFound();
        if (noContentResponseFound) {
            TypeDescriptorNode lastReturnType = returnTypes.remove(returnTypes.size() - 1);
            createOptionalTypeDescriptorNode(lastReturnType, createToken(QUESTION_MARK_TOKEN));
            returnTypes.add(lastReturnType);
        }
        TypeDescriptorNode returnType = FunctionReturnTypeGeneratorImp.createUnionReturnType(returnTypes);

        TypeParameterNode returnTypeParam = createTypeParameterNode(createToken(LT_TOKEN), returnType,
                createToken(GT_TOKEN));
        TypeDescriptorNode targetType = createParameterizedTypeDescriptorNode(TYPEDESC_TYPE_DESC,
                createToken(TYPEDESC_KEYWORD), returnTypeParam);
        return populateTargetTypeParam(targetType, parametersInfo);
    }

    protected ParametersInfo populateTargetTypeParam(TypeDescriptorNode targetType, ParametersInfo parametersInfo) {
        InferredTypedescDefaultNode inferredToken = createInferredTypedescDefaultNode(createToken(LT_TOKEN),
                createToken(GT_TOKEN));
        ParameterNode targetTypeParam = createDefaultableParameterNode(createEmptyNodeList(), targetType, createIdentifierToken("targetType"),
                createToken(EQUAL_TOKEN), inferredToken);
        parametersInfo.defaultable().add(targetTypeParam);
        parametersInfo.defaultable().add(createToken(COMMA_TOKEN));
        return parametersInfo;
    }
}
