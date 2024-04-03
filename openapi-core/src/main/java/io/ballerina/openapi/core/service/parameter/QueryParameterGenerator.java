package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.type.GeneratorUtils;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.ServiceGenerationUtils;
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
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.core.generators.type.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.generators.type.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.generators.type.GeneratorUtils.isArraySchema;
import static io.ballerina.openapi.core.generators.type.GeneratorUtils.isMapSchema;
import static io.ballerina.openapi.core.generators.type.GeneratorUtils.isObjectSchema;

public class QueryParameterGenerator extends ParameterGenerator {

    public QueryParameterGenerator(OASServiceMetadata oasServiceMetadata) {
        super(oasServiceMetadata);
    }

    /**
     * This for generate query parameter nodes.
     * <p>
     * Ballerina support query parameter types :
     * type BasicType boolean|int|float|decimal|string ;
     * public type  QueryParamType <map>json | () |BasicType|BasicType[];
     */
    @Override
    public ParameterNode generateParameterNode(Parameter parameter) {
        try {
            Schema<?> schema = parameter.getSchema();
            NodeList<AnnotationNode> annotations = createEmptyNodeList();
            IdentifierToken parameterName = createIdentifierToken(
                    GeneratorUtils.escapeIdentifier(parameter.getName().trim()),
                    AbstractNodeFactory.createEmptyMinutiaeList(), GeneratorUtils.SINGLE_WS_MINUTIAE);
            boolean isSchemaNotSupported = schema == null || getOpenAPIType(schema) == null;
            //Todo: will enable when header parameter support objects
            //paramSupportedTypes.add(GeneratorConstants.OBJECT);
            if (schema != null && schema.get$ref() != null) {
                String type = getValidName(ServiceGenerationUtils.extractReferenceType(schema.get$ref()), true);
                Schema<?> refSchema = openAPI.getComponents().getSchemas().get(type);
                return handleReferencedQueryParameter(parameter, refSchema, annotations, parameterName);
            } else if (parameter.getContent() != null) {
                Content content = parameter.getContent();
                for (Map.Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
                    return handleMapJsonQueryParameter(parameter, annotations, parameterName, mediaTypeEntry);
                }
            } else if (isSchemaNotSupported) {
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
                throw new OASTypeGenException(String.format(messages.getDescription(),
                        getOpenAPIType(parameter.getSchema())));
            } else if (parameter.getSchema().getDefault() != null) {
                // When query parameter has default value
                return handleDefaultQueryParameter(schema, annotations, parameterName);
            } else if (parameter.getRequired() && schema.getNullable() == null) {
                // Required typeDescriptor
                return handleRequiredQueryParameter(schema, annotations, parameterName);
            } else {
                // Optional typeDescriptor
                return handleOptionalQueryParameter(schema, annotations, parameterName);
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
    private RequiredParameterNode handleMapJsonQueryParameter(Parameter parameter, NodeList<AnnotationNode> annotations,
                                                              IdentifierToken parameterName,
                                                              Map.Entry<String, MediaType> mediaTypeEntry)
            throws OASTypeGenException {

        Schema<?> parameterSchema;
        if (mediaTypeEntry.getValue().getSchema() != null && mediaTypeEntry.getValue().getSchema().get$ref() != null) {
            String type = getValidName(ServiceGenerationUtils.extractReferenceType(mediaTypeEntry.getValue()
                    .getSchema().get$ref()), true);
            parameterSchema = (Schema<?>) openAPI.getComponents().getSchemas().get(type.trim());
        } else {
            parameterSchema = mediaTypeEntry.getValue().getSchema();
        }
        if (mediaTypeEntry.getKey().equals(GeneratorConstants.APPLICATION_JSON) && isMapSchema(parameterSchema)) {
            return getMapJsonParameterNode(parameterName, parameter, annotations);
        }
        String type = GeneratorUtils.getBallerinaMediaType(mediaTypeEntry.getKey(), false);
        throw new OASTypeGenException(String.format(ServiceDiagnosticMessages.OAS_SERVICE_102.getDescription(),
                type));
    }

    private RequiredParameterNode getMapJsonParameterNode(IdentifierToken parameterName, Parameter parameter,
                                                          NodeList<AnnotationNode> annotations) {
        BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(io.ballerina.openapi.core.generators.type.GeneratorConstants.MAP_JSON));
        if (parameter.getRequired()) {
            return createRequiredParameterNode(annotations, rTypeName, parameterName);
        }
        OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(rTypeName,
                createToken(SyntaxKind.QUESTION_MARK_TOKEN));
        return createRequiredParameterNode(annotations, optionalNode, parameterName);
    }

    /**
     * This function is to handle query schema which does not have required as true.
     */
    private ParameterNode handleOptionalQueryParameter(Schema<?> schema, NodeList<AnnotationNode> annotations,
                                              IdentifierToken parameterName) throws OASTypeGenException {
        if (isArraySchema(schema)) {
            Schema<?> items = schema.getItems();
            if (getOpenAPIType(items) == null && items.get$ref() == null) {
                // Resource function doesn't support to query parameters with array type which doesn't have an
                // item type.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_101;
                throw new OASTypeGenException(messages.getDescription());
            } else if ((!(isObjectSchema(items)) && !(getOpenAPIType(items) != null &&
                    getOpenAPIType(items).equals(GeneratorConstants.ARRAY))) || items.get$ref() != null) {
                Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                        .getTypeNodeFromOASSchema(schema, true);
                OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(typeDescriptorNode.get(),
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(annotations, optionalNode, parameterName);
            } else if (getOpenAPIType(items).equals(GeneratorConstants.ARRAY)) {
                // Resource function doesn't support to the nested array type query parameters.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_100;
                throw new OASTypeGenException(messages.getDescription());
            } else {
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
                throw new OASTypeGenException(String.format(messages.getDescription(), "object"));
            }
        } else {
            Token name;
            if (schema instanceof MapSchema) {
                // handle inline record open
                Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                        .getTypeNodeFromOASSchema(schema, true);
                name = createIdentifierToken(typeDescriptorNode.get().toSourceCode(),
                        GeneratorUtils.SINGLE_WS_MINUTIAE,
                        GeneratorUtils.SINGLE_WS_MINUTIAE);
            } else {
                try {
                    name = createIdentifierToken(convertOpenAPITypeToBallerina(schema, true),
                            GeneratorUtils.SINGLE_WS_MINUTIAE,
                            GeneratorUtils.SINGLE_WS_MINUTIAE);
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
            return createRequiredParameterNode(annotations, queryParamType, parameterName);
        }
    }

    private ParameterNode handleReferencedQueryParameter(Parameter parameter, Schema<?> refSchema,
                                                NodeList<AnnotationNode> annotations, IdentifierToken parameterName) {
        Token refTypeNameNode;
        if (refSchema.getAnyOf() != null || refSchema.getOneOf() != null) {
            refTypeNameNode = createIdentifierToken(GeneratorConstants.STRING);
            // todo : add a warning message
        } else {
            Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                    .getTypeNodeFromOASSchema(parameter.getSchema(), true);
            refTypeNameNode = createIdentifierToken(typeDescriptorNode.get().toSourceCode());
        }
        if (refSchema.getDefault() != null) {
            String defaultValue = getOpenAPIType(refSchema).equals(GeneratorConstants.STRING) ?
                    String.format("\"%s\"", refSchema.getDefault().toString()) : refSchema.getDefault().toString();
            return createDefaultableParameterNode(annotations, refTypeNameNode, parameterName,
                    createToken(SyntaxKind.EQUAL_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(defaultValue)));
        } else if (parameter.getRequired() && (refSchema.getNullable() == null || (!refSchema.getNullable()))) {
            return createRequiredParameterNode(annotations, refTypeNameNode, parameterName);
        } else {
            OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(refTypeNameNode,
                    createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            return createRequiredParameterNode(annotations, optionalNode, parameterName);
        }
    }

    private ParameterNode handleRequiredQueryParameter(Schema<?> schema, NodeList<AnnotationNode> annotations,
                                              IdentifierToken parameterName) throws OASTypeGenException {
        if (isArraySchema(schema)) {
            Schema<?> items = schema.getItems();
            if (!(isArraySchema(items)) && (getOpenAPIType(items) != null || (items.get$ref() != null))) {
                Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                        .getTypeNodeFromOASSchema(schema, true);
                Token arrayTypeName = createIdentifierToken(typeDescriptorNode.get().toSourceCode());
                return createRequiredParameterNode(annotations, arrayTypeName, parameterName);
            } else if (getOpenAPIType(items) == null) {
                // Resource function doesn't support query parameters for array types that doesn't have an item type.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_101;
                throw new OASTypeGenException(messages.getDescription());
            } else if (isObjectSchema(items)) {
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_102;
                throw new OASTypeGenException(String.format(messages.getDescription(), "object"));
            } else {
                // Resource function doesn't support to the nested array type query parameters.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_100;
                throw new OASTypeGenException(messages.getDescription());
            }
        } else {
            Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                    .getTypeNodeFromOASSchema(schema, true);
            Token name = createIdentifierToken(typeDescriptorNode.get().toSourceCode());
            BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null, name);
            return createRequiredParameterNode(annotations, rTypeName, parameterName);
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

    private ParameterNode handleDefaultQueryParameter(Schema<?> schema, NodeList<AnnotationNode> annotations,
                                             IdentifierToken parameterName) throws OASTypeGenException {

        if (isArraySchema(schema)) {
            Schema<?> items = schema.getItems();
            if (!isArraySchema(items) && (getOpenAPIType(items) != null || (items.get$ref() != null))) {
                // todo : update this part
//                TypeDescriptorNode arrayTypeName = null;
//                TypeDescriptorNode arrayTypeName = TypeHandler.getInstance().getArrayTypeDescriptorNode(items);

                Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                        .getTypeNodeFromOASSchema(schema, true);
                Token arrayTypeName = createIdentifierToken(typeDescriptorNode.get().toSourceCode());

//
                return createDefaultableParameterNode(annotations, arrayTypeName, parameterName,
                        createToken(SyntaxKind.EQUAL_TOKEN),
                        createSimpleNameReferenceNode(createIdentifierToken(schema.getDefault().toString())));
            } else if (getOpenAPIType(items) == null) {
                // Resource function doesn't support to query parameters with array type which hasn't item type.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_101;
                throw new OASTypeGenException(messages.getDescription());
            } else {
                // Resource function doesn't support to the nested array type query parameters.
                ServiceDiagnosticMessages messages = ServiceDiagnosticMessages.OAS_SERVICE_100;
                throw new OASTypeGenException(messages.getDescription());
            }
        } else {
            // todo : update this part
            Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance()
                    .getTypeNodeFromOASSchema(schema, true);
            Token name = createIdentifierToken(typeDescriptorNode.get().toSourceCode());


            BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null, name);
            if (getOpenAPIType(schema).equals(GeneratorConstants.STRING)) {
                return createDefaultableParameterNode(annotations, rTypeName, parameterName,
                        createToken(SyntaxKind.EQUAL_TOKEN),
                        createSimpleNameReferenceNode(createIdentifierToken('"' +
                                schema.getDefault().toString() + '"')));
            }
            return createDefaultableParameterNode(annotations, rTypeName, parameterName,
                    createToken(SyntaxKind.EQUAL_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(schema.getDefault().toString())));
        }
    }
}
