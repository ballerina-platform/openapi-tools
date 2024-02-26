package io.ballerina.openapi.corenew.typegenerator;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException;
import io.ballerina.openapi.corenew.typegenerator.generators.PrimitiveTypeGenerator;
import io.ballerina.openapi.corenew.typegenerator.generators.RecordTypeGenerator;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TypeHandler {

    private final static SyntaxTree typesSyntaxTree = SyntaxTree.from(TextDocuments.from(""));
    private static BallerinaTypesGenerator typesGenerator = new BallerinaTypesGenerator(null);

    public static SimpleNameReferenceNode getSimpleNameReferenceNode(String name) {
        return BallerinaTypesGenerator.getSimpleNameReferenceNode(name);
    }

    public static QualifiedNameReferenceNode getQualifiedNameReferenceNode(String modulePrefix, String identifier) {
        return BallerinaTypesGenerator.getQualifiedNameReferenceNode(modulePrefix, identifier);
    }

    public static Optional<TypeDescriptorNode> generateTypeDescNodeForOASSchema(Schema<?> schema)
            throws BallerinaOpenApiException {
        return BallerinaTypesGenerator.generateTypeDescNodeForOASSchema(schema);
    }

    public static SimpleNameReferenceNode createReturnTypeInclusionRecord(String statusCode, TypeDescriptorNode type) {
        return BallerinaTypesGenerator.createReturnTypeInclusionRecord(statusCode, type);
    }

    public static ImmutablePair<Optional<TypeDescriptorNode>, Optional<TypeDefinitionNode>> generateTypeDescriptorForMediaTypes(
            Map.Entry<String, MediaType> mediaType, String recordName) throws BallerinaOpenApiException {
        return BallerinaTypesGenerator.generateTypeDescriptorForMediaTypes(mediaType, recordName);
    }

    public static TypeDescriptorNode getReturnNodeForSchemaType(Set<Map.Entry<String, MediaType>> contentEntries, String recordName)
            throws BallerinaOpenApiException {
        return BallerinaTypesGenerator.getReturnNodeForSchemaType(contentEntries, recordName);
    }

    public static Optional<TypeDescriptorNode> getNodeForPayloadType(Map.Entry<String, MediaType> mediaType)
            throws BallerinaOpenApiException {
        return BallerinaTypesGenerator.getNodeForPayloadType(mediaType);
    }

    public static ArrayTypeDescriptorNode getArrayTypeDescriptorNode(OpenAPI openAPI, Schema<?> items) throws BallerinaOpenApiException {
        return BallerinaTypesGenerator.getArrayTypeDescriptorNode(openAPI, items);
    }

    public static Token getQueryParamTypeToken(Schema<?> schema) throws BallerinaOpenApiException {
        return BallerinaTypesGenerator.getQueryParamTypeToken(schema);
    }
}