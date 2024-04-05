package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.service.diagnostic.ServiceDiagnostic;
import io.ballerina.openapi.core.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.openapi.core.service.model.OASServiceMetadata;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.STRING;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isArraySchema;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isMapSchema;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isObjectSchema;

public class QueryParameterGenerator extends ParameterGenerator {

    public QueryParameterGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }

    /**
     * This for generate query parameter nodes.
     * <p>
     * Ballerina support query parameter types :
     * type BasicType boolean|int|float|decimal|string ;
     * public type  QueryParamType <map>json|()|BasicType|BasicType[];
     */
    @Override
    public ParameterNode generateParameterNode(Parameter parameter) {
        try {
            Schema<?> schema = parameter.getSchema();
            IdentifierToken parameterName = createIdentifierToken(
                    GeneratorUtils.escapeIdentifier(parameter.getName().trim()),
                    AbstractNodeFactory.createEmptyMinutiaeList(), GeneratorUtils.SINGLE_WS_MINUTIAE);
            boolean isSchemaNotSupported = schema == null || getOpenAPIType(schema) == null;
            //Todo: will enable when header parameter support objects
            //paramSupportedTypes.add(GeneratorConstants.OBJECT);
            if (schema != null && schema.get$ref() != null) {
                String type = getValidName(extractReferenceType(schema.get$ref()), true);
                Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type);
                return handleReferencedQueryParameter(parameter, refSchema, parameterName);
            } else if (parameter.getContent() != null) {
                Content content = parameter.getContent();
                for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
                    return handleMapJsonQueryParameter(parameter, parameterName, mediaTypeEntry);
                }
            } else if (isSchemaNotSupported) {
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_102,
                        getOpenAPIType(parameter.getSchema())));
                OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(
                        createIdentifierToken(STRING), createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(createEmptyNodeList(), optionalNode, parameterName);
            } else if (parameter.getSchema().getDefault() != null) {
                // When query parameter has default value
                return handleDefaultQueryParameter(schema, parameterName);
            } else if (parameter.getRequired() && schema.getNullable() == null) {
                // Required typeDescriptor
                return handleRequiredQueryParameter(schema, parameterName);
            } else {
                // Optional typeDescriptor
                return handleOptionalQueryParameter(schema, parameterName);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handle query parameter for content type which has application/json.
     * example:
     * <pre>
     *     parameters:
     *         - name: petType
     *           in: query
     *           content:
     *             application/json:
     *               schema:
     *                 type: object
     *                 additionalProperties: true
     * </pre>
     */
    private RequiredParameterNode handleMapJsonQueryParameter(Parameter parameter, IdentifierToken parameterName,
                                                              Map.Entry<String, MediaType> mediaTypeEntry)
            throws BallerinaOpenApiException {
        Schema<?> parameterSchema;
        if (mediaTypeEntry.getValue().getSchema() != null && mediaTypeEntry.getValue().getSchema().get$ref() != null) {
            String type = getValidName(extractReferenceType(mediaTypeEntry.getValue()
                    .getSchema().get$ref()), true);
            parameterSchema = (Schema<?>) openAPI.getComponents().getSchemas().get(type.trim());
        } else {
            parameterSchema = mediaTypeEntry.getValue().getSchema();
        }
        if (mediaTypeEntry.getKey().equals(GeneratorConstants.APPLICATION_JSON) && isMapSchema(parameterSchema)) {
            return getMapJsonParameterNode(parameterName, parameter);
        }
        String type = GeneratorUtils.getBallerinaMediaType(mediaTypeEntry.getKey(), false);
        diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_102, type));
        OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(
                createIdentifierToken(STRING), createToken(SyntaxKind.QUESTION_MARK_TOKEN));
        return createRequiredParameterNode(createEmptyNodeList(), optionalNode, parameterName);
    }

    private RequiredParameterNode getMapJsonParameterNode(IdentifierToken parameterName, Parameter parameter) {
        BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(io.ballerina.openapi.core.generators.type.GeneratorConstants.MAP_JSON));
        if (parameter.getRequired()) {
            return createRequiredParameterNode(createEmptyNodeList(), rTypeName, parameterName);
        }
        OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(rTypeName,
                createToken(SyntaxKind.QUESTION_MARK_TOKEN));
        return createRequiredParameterNode(createEmptyNodeList(), optionalNode, parameterName);
    }

    /**
     * This function is to handle query schema which does not have required as true.
     */
    private ParameterNode handleOptionalQueryParameter(Schema<?> schema, IdentifierToken parameterName) {
        if (isArraySchema(schema)) {
            Schema<?> items = schema.getItems();
            if (getOpenAPIType(items) == null && items.get$ref() == null) {
                // Resource function doesn't support to query parameters with array type which doesn't have an
                // item type.
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_101));
                return createStringArrayParameterNode(parameterName);
            } else if ((!(isObjectSchema(items)) && !(getOpenAPIType(items) != null &&
                    getOpenAPIType(items).equals(GeneratorConstants.ARRAY))) || items.get$ref() != null) {
                Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                        .getTypeNodeFromOASSchema(schema, true);
                OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(typeDescriptorNode.get(),
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(createEmptyNodeList(), optionalNode, parameterName);
            } else if (getOpenAPIType(items).equals(GeneratorConstants.ARRAY)) {
                // Resource function doesn't support to the nested array type query parameters.
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_100));
                return createStringArrayParameterNode(parameterName);
            } else {
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_102, "object"));
                OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(
                        createIdentifierToken(STRING), createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(createEmptyNodeList(), optionalNode, parameterName);
            }
        } else {
            Token name;
            if (schema instanceof MapSchema) {
                // handle inline record open
                Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                        .getTypeNodeFromOASSchema(schema, true);
                name = createIdentifierToken(typeDescriptorNode.get().toSourceCode(),
                        GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
            } else {
                try {
                    name = createIdentifierToken(convertOpenAPITypeToBallerina(schema, true),
                            GeneratorUtils.SINGLE_WS_MINUTIAE, GeneratorUtils.SINGLE_WS_MINUTIAE);
                } catch (BallerinaOpenApiException e) {
                    throw new RuntimeException(e);
                }
            }
            TypeDescriptorNode queryParamType = createBuiltinSimpleNameReferenceNode(null, name);
            // If schema has an enum with null value, the type is already nil. Hence, the check.
            if (!name.text().trim().endsWith(GeneratorConstants.NILLABLE)) {
                queryParamType = createOptionalTypeDescriptorNode(queryParamType,
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            }
            return createRequiredParameterNode(createEmptyNodeList(), queryParamType, parameterName);
        }
    }

    private static RequiredParameterNode createStringArrayParameterNode(IdentifierToken parameterName) {
        ArrayDimensionNode arrayDimensionNode = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        TypeDescriptorNode arrayTypedescNode = createArrayTypeDescriptorNode(createSimpleNameReferenceNode(
                createIdentifierToken(STRING)), createNodeList(arrayDimensionNode));
        OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(
                arrayTypedescNode, createToken(SyntaxKind.QUESTION_MARK_TOKEN));
        return createRequiredParameterNode(createEmptyNodeList(), optionalNode, parameterName);
    }

    private ParameterNode handleReferencedQueryParameter(Parameter parameter, Schema<?> refSchema,
                                                         IdentifierToken parameterName) {
        Token refTypeNameNode;
        if (refSchema.getAnyOf() != null || refSchema.getOneOf() != null) {
            refTypeNameNode = createIdentifierToken(STRING);
            diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_107, "object"));
        } else {
            Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                    .getTypeNodeFromOASSchema(parameter.getSchema(), true);
            refTypeNameNode = createIdentifierToken(typeDescriptorNode.get().toSourceCode());
        }
        if (refSchema.getDefault() != null) {
            String defaultValue = getOpenAPIType(refSchema).equals(STRING) ?
                    String.format("\"%s\"", refSchema.getDefault().toString()) : refSchema.getDefault().toString();
            return createDefaultableParameterNode(createEmptyNodeList(), refTypeNameNode, parameterName,
                    createToken(SyntaxKind.EQUAL_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(defaultValue)));
        } else if (parameter.getRequired() && (refSchema.getNullable() == null || (!refSchema.getNullable()))) {
            return createRequiredParameterNode(createEmptyNodeList(), refTypeNameNode, parameterName);
        } else {
            OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(refTypeNameNode,
                    createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            return createRequiredParameterNode(createEmptyNodeList(), optionalNode, parameterName);
        }
    }

    private ParameterNode handleRequiredQueryParameter(Schema<?> schema, IdentifierToken parameterName) {
        if (isArraySchema(schema)) {
            Schema<?> items = schema.getItems();
            if (!(isArraySchema(items)) && (getOpenAPIType(items) != null || (items.get$ref() != null))) {
                Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                        .getTypeNodeFromOASSchema(schema, true);
                Token arrayTypeName = createIdentifierToken(typeDescriptorNode.get().toSourceCode());
                return createRequiredParameterNode(createEmptyNodeList(), arrayTypeName, parameterName);
            } else if (getOpenAPIType(items) == null) {
                // Resource function doesn't support query parameters for array types that doesn't have an item type.
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_101));
                return createStringArrayParameterNode(parameterName);
            } else if (isObjectSchema(items)) {
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_102, "object"));
                OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(
                        createIdentifierToken(STRING), createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(createEmptyNodeList(), optionalNode, parameterName);
            } else {
                // Resource function doesn't support to the nested array type query parameters.
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_100));
                return createStringArrayParameterNode(parameterName);
            }
        } else {
            Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                    .getTypeNodeFromOASSchema(schema, true);
            Token name = createIdentifierToken(typeDescriptorNode.get().toSourceCode());
            BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null, name);
            return createRequiredParameterNode(createEmptyNodeList(), rTypeName, parameterName);
        }
    }

    /**
     * This function generate default query parameter when OAS gives default value.
     * <p>
     * OAS code example:
     * <pre>
     *      parameters:
     *         - name: limit
     *           in: query
     *           schema:
     *             type: integer
     *             default: 10
     *             format: int32
     *  </pre>
     * generated ballerina -> int limit = 10;
     */

    private ParameterNode handleDefaultQueryParameter(Schema<?> schema, IdentifierToken parameterName) {

        if (isArraySchema(schema)) {
            Schema<?> items = schema.getItems();
            if (!isArraySchema(items) && (getOpenAPIType(items) != null || (items.get$ref() != null))) {
                Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                        .getTypeNodeFromOASSchema(schema, true);
                Token arrayTypeName = createIdentifierToken(typeDescriptorNode.get().toSourceCode());
                return createDefaultableParameterNode(createEmptyNodeList(), arrayTypeName, parameterName,
                        createToken(SyntaxKind.EQUAL_TOKEN),
                        createSimpleNameReferenceNode(createIdentifierToken(schema.getDefault().toString())));
            } else if (getOpenAPIType(items) == null) {
                // Resource function doesn't support to query parameters with array type which hasn't item type.
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_101));
                return createStringArrayParameterNode(parameterName);
            } else {
                // Resource function doesn't support to the nested array type query parameters.
                diagnostics.add(new ServiceDiagnostic(ServiceDiagnosticMessages.OAS_SERVICE_100));
                return createStringArrayParameterNode(parameterName);
            }
        } else {
            Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                    .getTypeNodeFromOASSchema(schema, true);
            Token name = createIdentifierToken(typeDescriptorNode.get().toSourceCode());
            BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null, name);
            if (getOpenAPIType(schema).equals(STRING)) {
                return createDefaultableParameterNode(createEmptyNodeList(), rTypeName, parameterName,
                        createToken(SyntaxKind.EQUAL_TOKEN),
                        createSimpleNameReferenceNode(createIdentifierToken('"' +
                                schema.getDefault().toString() + '"')));
            }
            return createDefaultableParameterNode(createEmptyNodeList(), rTypeName, parameterName,
                    createToken(SyntaxKind.EQUAL_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(schema.getDefault().toString())));
        }
    }
}
