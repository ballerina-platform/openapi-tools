package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.generators.type.model.TypeDescriptorReturnType;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNilLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.STRING;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;

public class QueryParameterGenerator implements ParameterGenerator {
    OpenAPI openAPI;
    Parameter parameter;

    List<ClientDiagnostic> diagnostics;
    public QueryParameterGenerator(Parameter parameter, OpenAPI openAPI) {
        this.parameter = parameter;
        this.openAPI = openAPI;
    }
    @Override
    public Optional<ParameterNode> generateParameterNode() {

        TypeDescriptorNode typeNode = null;

        Schema<?> parameterSchema = parameter.getSchema();
        // parameter type is  typedescriptor node

        //supported type: type BasicType boolean|int|float|decimal|string|map<anydata>|enum;
        //public type QueryParamType ()|BasicType|BasicType[];
        TypeDescriptorReturnType result = TypeHandler.getInstance().generateTypeDescriptorNodeForOASSchema(parameterSchema);
        typeNode = result.typeDescriptorNode().get();
        SyntaxKind kind = typeNode.kind();
        if (!isQueryParamTypeSupported(kind.stringValue())) {
            //TODO diagnostic message unsupported and early return
            DiagnosticMessages unsupportedType = DiagnosticMessages.OAS_CLIENT_102;
            ClientDiagnostic diagnostic = new ClientDiagnosticImp(unsupportedType.getCode(),
                    unsupportedType.getDescription(), parameter.getName());
            diagnostics.add(diagnostic);
            return Optional.empty();
        }

        // required parameter- done
        // default parameter- done
        // nullable parameter - done

        // unsupported content type in query parameter
        // generate type node from type handler

        // todo handle required parameter
        if (parameter.getRequired()) {
            // todo type handler node
            IdentifierToken paramName =
                    createIdentifierToken(getValidName(parameter.getName().trim(), false));
            //todo doc comments separate handle
            return Optional.of(createRequiredParameterNode(null, typeNode, paramName));
        } else {
            IdentifierToken paramName =
                    createIdentifierToken(getValidName(parameter.getName().trim(), false));
            // Handle given default values in query parameter.
            if (parameterSchema.getDefault() != null) {
                LiteralValueToken literalValueToken;
                if (getOpenAPIType(parameterSchema).equals(STRING)) {
                    literalValueToken = createLiteralValueToken(null,
                            '"' + parameterSchema.getDefault().toString() + '"', createEmptyMinutiaeList(),
                            createEmptyMinutiaeList());
                } else {
                    literalValueToken =
                            createLiteralValueToken(null, parameterSchema.getDefault().toString(),
                                    createEmptyMinutiaeList(),
                                    createEmptyMinutiaeList());

                }
                return Optional.of(createDefaultableParameterNode(null, typeNode, paramName,
                        createToken(EQUAL_TOKEN), literalValueToken));
            } else {
//                paramType = paramType.endsWith(NILLABLE) ? paramType : paramType + NILLABLE;
//                typeName = createBuiltinSimpleNameReferenceNode(null,
//                        createIdentifierToken(paramType));
//
                NilLiteralNode nilLiteralNode =
                        createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
                return Optional.of(createDefaultableParameterNode(null, typeNode, paramName,
                        createToken(EQUAL_TOKEN), nilLiteralNode));
            }
        }
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    private boolean isQueryParamTypeSupported(String type) {
        return type.equals("boolean") || type.equals("int") || type.equals("float") || type.equals("decimal") ||
                type.equals("string") || type.equals("map<anydata>") || type.equals("enum") || type.equals("array");
    }
}
