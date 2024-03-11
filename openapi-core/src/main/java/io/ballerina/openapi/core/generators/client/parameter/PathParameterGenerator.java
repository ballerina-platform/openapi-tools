package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.openapi.core.GeneratorConstants.SQUARE_BRACKETS;
import static io.ballerina.openapi.core.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.core.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages.OAS_CLIENT_100;

public class PathParameterGenerator implements ParameterGenerator {
    OpenAPI openAPI;
    Parameter parameter;

    List<ClientDiagnostic> diagnostics;
    public PathParameterGenerator(Parameter parameter, OpenAPI openAPI) {
        this.parameter = parameter;
        this.openAPI = openAPI;
    }


    @Override
    public ParameterNode generateParameter() {
        IdentifierToken paramName = createIdentifierToken(getValidName(parameter.getName(), false));
        // type should be a any type node.
        Schema parameterSchema = parameter.getSchema();
        if (parameterSchema.get$ref() != null) {
            String type = "";
            try {
                type = getValidName(extractReferenceType(parameterSchema.get$ref()), true);
            } catch (BallerinaOpenApiException e) {
                DiagnosticMessages diagnostic = OAS_CLIENT_100;
                ClientDiagnosticImp clientDiagnostic = new ClientDiagnosticImp(diagnostic.getCode(),
                        diagnostic.getDescription(), parameterSchema.get$ref());
                diagnostics.add(clientDiagnostic);
            }
            //todo 1. call type handler get type node

            Schema schema = openAPI.getComponents().getSchemas().get(type.trim());
//            TypeDefinitionNode typeDefinitionNode = ballerinaSchemaGenerator.getTypeDefinitionNode
//                    (schema, type, new ArrayList<>());
//            if (typeDefinitionNode.typeDescriptor().kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
//                throw new BallerinaOpenApiException(String.format(
//                        "Path parameter: '%s' is invalid. Ballerina does not support object type path parameters.",
//                        parameter.getName()));
//            }
        } else {
            String type = convertOpenAPITypeToBallerina(parameter.getSchema());
            if (type.equals("anydata") || type.equals(SQUARE_BRACKETS) || type.equals("record {}")) {
                DiagnosticMessages diagMessages = DiagnosticMessages.OAS_CLIENT_101;
                ClientDiagnosticImp diagnostic = new ClientDiagnosticImp(diagMessages.getCode(),
                        diagMessages.getDescription(), parameter.getName());
                diagnostics.add(diagnostic);
//                throw new BallerinaOpenApiException(invalidPathParamType(parameter.getName().trim()));
            }
        }

        BuiltinSimpleNameReferenceNode typeNode = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(type));
        // remove annotation
        return createRequiredParameterNode(null, typeNode, paramName);
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }
}
