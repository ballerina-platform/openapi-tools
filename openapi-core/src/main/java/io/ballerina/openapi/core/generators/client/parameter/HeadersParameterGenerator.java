package io.ballerina.openapi.core.generators.client.parameter;

import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.LiteralValueToken;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeParameterNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnosticImp;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMapTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.LT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.MAP_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.openapi.core.generators.client.diagnostic.DiagnosticMessages.OAS_CLIENT_108;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HEADERS;

public class HeadersParameterGenerator implements ParameterGenerator {
    private final List<Parameter> parameters;
    private final List<ClientDiagnostic> diagnostics = new ArrayList<>();
    private final OpenAPI openAPI;
    private final Operation operation;
    private final String httpMethod;
    private final String path;
    private boolean hasErrors = false;

    public HeadersParameterGenerator(List<Parameter> parameters, OpenAPI openAPI, Operation operation,
                                     String httpMethod, String path) {
        this.parameters = parameters;
        this.openAPI = openAPI;
        this.operation = operation;
        this.httpMethod = httpMethod;
        this.path = path;
    }

    @Override
    public Optional<ParameterNode> generateParameterNode() {
        if (parameters.isEmpty()) {
            return Optional.empty();
        }

        ObjectSchema headersSchema = getHeadersSchema();
        if (Objects.isNull(headersSchema)) {
            return Optional.empty();
        }

        String operationId = GeneratorUtils.generateOperationUniqueId(operation, path, httpMethod);
        headersSchema.setDescription("Represents the Headers record for the operation: " + operationId);
        String headersName = GeneratorUtils.getValidName(operationId, true) + "Headers";
        openAPI.getComponents().addSchemas(headersName, headersSchema);

        Schema headersRefSchema = new ObjectSchema().$ref(headersName);
        Optional<TypeDescriptorNode> headersType =  TypeHandler.getInstance().getTypeNodeFromOASSchema(
                headersRefSchema, true);

        if (headersType.isEmpty()) {
            // This should be some error scenario
            return Optional.empty();
        }

        boolean isDefaultable = isDefaultable(headersSchema);
        ParameterNode parameterNode = createParameterNode(headersType.get(), isDefaultable);

        return Optional.of(parameterNode);
    }

    private boolean isDefaultable(ObjectSchema headersSchema) {
        List<String> requiredHeaders = headersSchema.getRequired();
        Map<String, Schema> headers = headersSchema.getProperties();

        return Objects.isNull(requiredHeaders) || requiredHeaders.isEmpty() || requiredHeaders.stream()
                .allMatch(header -> Objects.nonNull(headers.get(header).getDefault()));
    }

    private ParameterNode createParameterNode(TypeDescriptorNode headersType, boolean isDefaultable) {
        if (isDefaultable) {
            LiteralValueToken defaultMapVal = createLiteralValueToken(null, "{}", createEmptyMinutiaeList(),
                    createEmptyMinutiaeList());
            BasicLiteralNode defaultMapExp = createBasicLiteralNode(null, defaultMapVal);
            return createDefaultableParameterNode(createEmptyNodeList(), headersType,
                    createIdentifierToken(HEADERS), createToken(EQUAL_TOKEN), defaultMapExp);
        } else {
            return createRequiredParameterNode(createEmptyNodeList(), headersType,
                    createIdentifierToken(HEADERS));
        }
    }

    public static Optional<ParameterNode> getDefaultParameterNode() {
        TypeDescriptorNode stringType = createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.STRING));

        ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        TypeDescriptorNode stringArrType = createArrayTypeDescriptorNode(stringType,
                createNodeList(dimensionNode));

        UnionTypeDescriptorNode unionType = createUnionTypeDescriptorNode(stringType, createToken(PIPE_TOKEN),
                stringArrType);
        TypeParameterNode headerParamNode = createTypeParameterNode(createToken(LT_TOKEN), unionType,
                createToken(SyntaxKind.GT_TOKEN));
        TypeDescriptorNode defaultHeadersType = createMapTypeDescriptorNode(createToken(MAP_KEYWORD), headerParamNode);

        LiteralValueToken defaultMapVal = createLiteralValueToken(null, "{}", createEmptyMinutiaeList(),
                createEmptyMinutiaeList());
        BasicLiteralNode defaultMapExp = createBasicLiteralNode(null, defaultMapVal);
        return Optional.of(createDefaultableParameterNode(createEmptyNodeList(), defaultHeadersType,
                createIdentifierToken(HEADERS), createToken(EQUAL_TOKEN), defaultMapExp));
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    private ObjectSchema getHeadersSchema() {
        Map<String, Schema> properties = new HashMap<>();
        for (Parameter parameter : parameters) {
            properties.put(parameter.getName(), getSchemaWithDetails(parameter));
        }

        properties.entrySet().removeIf(entry -> {
            if (Objects.isNull(entry.getValue())) {
                ClientDiagnostic diagnostic = new ClientDiagnosticImp(OAS_CLIENT_108, entry.getKey());
                diagnostics.add(diagnostic);
                hasErrors = true;
                return true;
            }
            return false;
        });

        if (properties.isEmpty()) {
            return null;
        }

        List<String> requiredFields = new ArrayList<>(parameters.stream()
                .filter(parameter -> Boolean.TRUE.equals(parameter.getRequired()))
                .map(Parameter::getName)
                .toList());

        requiredFields.removeIf(field -> !properties.containsKey(field));

        ObjectSchema headersSchema = new ObjectSchema();
        headersSchema.setProperties(properties);
        if (!requiredFields.isEmpty()) {
            headersSchema.setRequired(requiredFields);
        }
        return headersSchema;
    }
}
