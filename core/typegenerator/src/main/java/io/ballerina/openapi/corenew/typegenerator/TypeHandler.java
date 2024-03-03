package io.ballerina.openapi.corenew.typegenerator;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TypeHandler {
    public static SimpleNameReferenceNode getSimpleNameReferenceNode(String name) {
        return BallerinaTypesGenerator.getInstance().getSimpleNameReferenceNode(name);
    }

    public static QualifiedNameReferenceNode getQualifiedNameReferenceNode(String modulePrefix, String identifier) {
        return BallerinaTypesGenerator.getInstance().getQualifiedNameReferenceNode(modulePrefix, identifier);
    }

    public static Optional<TypeDescriptorNode> generateTypeDescriptorNodeForOASSchema(Schema<?> schema)
            throws BallerinaOpenApiException {
        return BallerinaTypesGenerator.getInstance().generateTypeDescriptorNodeForOASSchema(schema);
    }

    public static SimpleNameReferenceNode createTypeInclusionRecord(String statusCode, TypeDescriptorNode type) {
        return BallerinaTypesGenerator.getInstance().createTypeInclusionRecord(statusCode, type);
    }

    public static ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> generateTypeDescriptorForMediaTypes(
            String mediaTypeContent, Schema<?> schema, String recordName) throws BallerinaOpenApiException {
        return BallerinaTypesGenerator.getInstance().generateTypeDescriptorForMediaTypes(mediaTypeContent, schema, recordName);
    }

    public static Optional<TypeDescriptorNode> getNodeForPayloadType(Map.Entry<String, MediaType> mediaType) // check
            throws BallerinaOpenApiException {
        return BallerinaTypesGenerator.getInstance().getNodeForPayloadType(mediaType);
    }

    public static ArrayTypeDescriptorNode getArrayTypeDescriptorNode(OpenAPI openAPI, Schema<?> items) throws BallerinaOpenApiException {
        return BallerinaTypesGenerator.getInstance().getArrayTypeDescriptorNode(openAPI, items);
    }

    public static Token getQueryParamTypeToken(Schema<?> schema) throws BallerinaOpenApiException {
        return BallerinaTypesGenerator.getInstance().getQueryParamTypeToken(schema);
    }

    public static RequiredParameterNode getMapJsonParameterNode(IdentifierToken parameterName, Parameter parameter, NodeList<AnnotationNode> annotations) {
        return BallerinaTypesGenerator.getInstance().getMapJsonParameterNode(parameterName, parameter, annotations);
    }

    public static ArrayTypeDescriptorNode getArrayTypeDescriptorNodeFromTypeDescriptorNode(TypeDescriptorNode typeDescriptorNode) {
        return BallerinaTypesGenerator.getInstance().getArrayTypeDescriptorNodeFromTypeDescriptorNode(typeDescriptorNode);
    }
}