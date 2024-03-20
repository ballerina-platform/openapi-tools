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
        String paramType = "";
        if (parameterSchema.get$ref() != null) {
            try {
                paramType = getValidName(extractReferenceType(parameterSchema.get$ref()), true);
            } catch (BallerinaOpenApiException e) {
                DiagnosticMessages diagMessages = DiagnosticMessages.OAS_CLIENT_100;
                ClientDiagnostic diagnostic = new ClientDiagnosticImp(diagMessages.getCode(),
                        diagMessages.getDescription(), parameter.getName());
                diagnostics.add(diagnostic);
            }
            parameterSchema = openAPI.getComponents().getSchemas().get(paramType.trim());
            //handle the reference type

        } else {
            //supported type: type BasicType boolean|int|float|decimal|string|map<anydata>|enum;
            //public type QueryParamType ()|BasicType|BasicType[];
            try {
                typeNode = TypeHandler.getInstance().getTypeNodeForQueryParam(parameterSchema);
                SyntaxKind kind = typeNode.kind();
                if (!isQueryParamTypeSupported(kind.stringValue())) {
                    //TODO diagnostic message unsupported and early return
                    DiagnosticMessages unsupportedType = DiagnosticMessages.OAS_CLIENT_102;
                    ClientDiagnostic diagnostic = new ClientDiagnosticImp(unsupportedType.getCode(),
                            unsupportedType.getDescription(), parameter.getName());
                    diagnostics.add(diagnostic);
                    return Optional.empty();
                } else if () {
                    //handle
                }

            } catch (OASTypeGenException e) {
                //todo diagnostic message with error occurred
                DiagnosticMessages diagMessages = DiagnosticMessages.OAS_CLIENT_104;
                ClientDiagnostic diagnostic = new ClientDiagnosticImp(diagMessages.getCode(),
                        diagMessages.getDescription());
                diagnostics.add(diagnostic);
            }

            // required parameter- done
            // default parameter- done
            // nullable parameter - done

            // unsupported content type in query parameter
            // generate type node from type handler

//            paramType = convertOpenAPITypeToBallerina(parameterSchema);
//            if (getOpenAPIType(parameterSchema).equals(ARRAY)) {
//                if (getOpenAPIType(parameterSchema.getItems()) != null) {
//                    String itemType = getOpenAPIType(parameterSchema.getItems());
//                    if (itemType.equals(STRING) || itemType.equals(INTEGER) || itemType.equals(BOOLEAN) ||
//                            itemType.equals(NUMBER)) {
//                        if (parameterSchema.getItems().getEnum() != null &&
//                                !parameterSchema.getItems().getEnum().isEmpty()) {
//                            paramType = OPEN_PAREN_TOKEN.stringValue() +
//                                    convertOpenAPITypeToBallerina(parameterSchema.getItems()) +
//                                    CLOSE_PAREN_TOKEN.stringValue() + SQUARE_BRACKETS;
//                        } else {
//                            paramType = convertOpenAPITypeToBallerina(parameterSchema.getItems()) + SQUARE_BRACKETS;
//                        }
//                    } else {
//                        //unsupported query parameter type
//                        DiagnosticMessages diagMessages = DiagnosticMessages.OAS_CLIENT_102;
//                        ClientDiagnosticImp diagnostic = new ClientDiagnosticImp(diagMessages.getCode(),
//                                diagMessages.getDescription(), parameter.getName());
//                        diagnostics.add(diagnostic);
//                    }
//                } else if (parameterSchema.getItems().get$ref() != null) {
//                    paramType = getValidName(extractReferenceType(
//                            parameterSchema.getItems().get$ref().trim()), true) + SQUARE_BRACKETS;
//                } else {
//                    //OAS_CLIENT_103 diagnostic message
//                    throw new BallerinaOpenApiException("Please define the array item type of the parameter : " +
//                            parameter.getName());
//                }
//            }
        }

        // todo handle required parameter
        if (parameter.getRequired()) {
            // todo type handler node
//            typeName = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(paramType));
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
                type.equals("string") || type.equals("map<anydata>") || type.equals("enum");
    }
}
