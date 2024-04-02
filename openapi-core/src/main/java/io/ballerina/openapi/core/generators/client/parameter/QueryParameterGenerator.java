package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.model.GeneratorMetaData;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNilLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ARRAY_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BOOLEAN_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DECIMAL_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ENUM_DECLARATION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FLOAT_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.INT_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.MAP_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SIMPLE_NAME_REFERENCE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SINGLETON_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.UNION_TYPE_DESC;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.STRING;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;

public class QueryParameterGenerator implements ParameterGenerator {
    OpenAPI openAPI;
    Parameter parameter;

    List<ClientDiagnostic> diagnostics = new ArrayList<>();
    public QueryParameterGenerator(Parameter parameter, OpenAPI openAPI) {
        this.parameter = parameter;
        this.openAPI = openAPI;
    }
    @Override
    public Optional<ParameterNode> generateParameterNode() {

        TypeDescriptorNode typeNode;

        Schema<?> parameterSchema = parameter.getSchema();
        if (!isQueryParamTypeSupported(parameterSchema.getType())) {
            //TODO diagnostic message unsupported and early return
            DiagnosticMessages unsupportedType = DiagnosticMessages.OAS_CLIENT_102;
            ClientDiagnostic diagnostic = new ClientDiagnosticImp(unsupportedType.getCode(),
                    unsupportedType.getDescription(), parameter.getName());
            diagnostics.add(diagnostic);
            return Optional.empty();
        }

        //supported type: type BasicType boolean|int|float|decimal|string|map<anydata>|enum;
        //public type QueryParamType ()|BasicType|BasicType[];
        Optional<TypeDescriptorNode> result = TypeHandler.getInstance().getTypeNodeFromOASSchema(parameterSchema);
        if (result.isEmpty()) {
            //TODO diagnostic message unsupported and early return
            DiagnosticMessages unsupportedType = DiagnosticMessages.OAS_CLIENT_102;
            ClientDiagnostic diagnostic = new ClientDiagnosticImp(unsupportedType.getCode(),
                    unsupportedType.getDescription(), parameter.getName());
            diagnostics.add(diagnostic);
            return Optional.empty();
        }
        typeNode = result.get();

        // required parameter- done
        // default parameter- done
        // nullable parameter - done

        // unsupported content type in query parameter
        // generate type node from type handler

        // Todo handle required parameter
        if (parameter.getRequired()) {
            // to remove the ? from the type node this code is a hack for now ,
            // further implementation need to do modify the typeNode
            if (parameterSchema.getNullable() == null  ||
                    (parameterSchema.getNullable() != null && !parameterSchema.getNullable()) ||
                    (parameterSchema.getTypes() != null && parameterSchema.getTypes().contains(null))) {
                if (typeNode.toString().endsWith("?")) {
                    typeNode = createSimpleNameReferenceNode(createIdentifierToken(
                            typeNode.toString().substring(0, typeNode.toString().length() - 1)));
                }
            }
            IdentifierToken paramName =
                    createIdentifierToken(getValidName(parameter.getName().trim(), false));
            return Optional.of(createRequiredParameterNode(createEmptyNodeList(), typeNode, paramName));
        } else {
            IdentifierToken paramName =
                    createIdentifierToken(getValidName(parameter.getName().trim(), false));
            // Handle given default values in query parameter.
            if (parameterSchema.getDefault() != null) {
                LiteralValueToken literalValueToken;
                if (Objects.equals(getOpenAPIType(parameterSchema), STRING)) {
                    literalValueToken = createLiteralValueToken(null,
                            '"' + parameterSchema.getDefault().toString() + '"', createEmptyMinutiaeList(),
                            createEmptyMinutiaeList());
                } else {
                    literalValueToken =
                            createLiteralValueToken(null, parameterSchema.getDefault().toString(),
                                    createEmptyMinutiaeList(),
                                    createEmptyMinutiaeList());

                }
                // to remove the ? from the type node this code is a hack for now ,
                // further implementation need to do modify the typeNode
                if (parameterSchema.getNullable() == null  ||
                        (parameterSchema.getNullable() != null && !parameterSchema.getNullable()) ||
                        (parameterSchema.getTypes() != null && parameterSchema.getTypes().contains(null))) {
                    if (typeNode.toString().endsWith("?")) {
                        typeNode = createSimpleNameReferenceNode(createIdentifierToken(
                                typeNode.toString().substring(0, typeNode.toString().length() - 1)));
                    }
                }
                return Optional.of(createDefaultableParameterNode(createEmptyNodeList(), typeNode, paramName,
                        createToken(EQUAL_TOKEN), literalValueToken));
            } else {
                if (!typeNode.toString().endsWith("?")) {
                    typeNode = createSimpleNameReferenceNode(createIdentifierToken(typeNode.toString() + "?"));
                }
                NilLiteralNode nilLiteralNode =
                        createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
                return Optional.of(createDefaultableParameterNode(createEmptyNodeList(), typeNode, paramName,
                        createToken(EQUAL_TOKEN), nilLiteralNode));
            }
        }
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }

//    private boolean isQueryParamTypeSupported(SyntaxKind type) {
//        return type.equals("boolean") || type.equals("integer") || type.equals("number") ||
//                type.equals("array") ||
//                type.equals("string") || type.equals(MAP_TYPE_DESC) || type.equals(ENUM_DECLARATION) || type.equals(ARRAY_TYPE_DESC);
//    }

    private boolean isQueryParamTypeSupported(String type) {
        return type.equals("boolean") || type.equals("integer") || type.equals("number") ||
                type.equals("array") || type.equals("string") || type.equals("object");
    }
}
