/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.core.generators.service;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.core.GeneratorConstants;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.RecordTypeGenerator;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.core.GeneratorConstants.BOOLEAN;
import static io.ballerina.openapi.core.GeneratorConstants.INTEGER;
import static io.ballerina.openapi.core.GeneratorConstants.NUMBER;
import static io.ballerina.openapi.core.GeneratorConstants.STRING;

/**
 * This store all the util functions related service generation process.
 *
 * @since 1.3.0
 */
public class ServiceGenerationUtils {

    /**
     * This method will extract reference type by splitting the reference string.
     *
     * @param referenceVariable - Reference String
     * @return Reference variable name
     * @throws BallerinaOpenApiException - Throws an exception if the reference string is incompatible.
     *                                   Note : Current implementation will not support external links a references.
     */
    public static String extractReferenceType(String referenceVariable) throws BallerinaOpenApiException {

        if (referenceVariable.startsWith("#") && referenceVariable.contains("/")) {
            String[] refArray = referenceVariable.split("/");
            return GeneratorUtils.escapeIdentifier(refArray[refArray.length - 1]);
        } else {
            throw new BallerinaOpenApiException("Invalid reference value : " + referenceVariable
                    + "\nBallerina only supports local reference values.");
        }
    }

    public static AnnotationNode getAnnotationNode(String identifier, MappingConstructorExpressionNode annotValue) {

        Token atToken = createIdentifierToken("@");
        QualifiedNameReferenceNode annotReference = GeneratorUtils.getQualifiedNameReferenceNode(
                GeneratorConstants.HTTP, identifier);
        return createAnnotationNode(atToken, annotReference, annotValue);
    }

    public static UnionTypeDescriptorNode getUnionNodeForOneOf(Iterator<Schema> iterator)
            throws BallerinaOpenApiException {

        List<SimpleNameReferenceNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = createIdentifierToken("|");
        while (iterator.hasNext()) {
            Schema contentType = iterator.next();
            Optional<TypeDescriptorNode> qualifiedNodeType = generateTypeDescNodeForOASSchema(contentType);
            if (qualifiedNodeType.isEmpty()) {
                continue;
            }
            qualifiedNodes.add((SimpleNameReferenceNode) qualifiedNodeType.get());
        }
        SimpleNameReferenceNode right = qualifiedNodes.get(qualifiedNodes.size() - 1);
        SimpleNameReferenceNode traversRight = qualifiedNodes.get(qualifiedNodes.size() - 2);
        UnionTypeDescriptorNode traversUnion = createUnionTypeDescriptorNode(traversRight, pipeToken,
                right);
        if (qualifiedNodes.size() >= 3) {
            for (int i = qualifiedNodes.size() - 3; i >= 0; i--) {
                traversUnion = createUnionTypeDescriptorNode(qualifiedNodes.get(i), pipeToken,
                        traversUnion);
            }
        }
        return traversUnion;
    }

    /**
     * Generate typeDescriptor for given schema.
     */
    public static Optional<TypeDescriptorNode> generateTypeDescNodeForOASSchema(Schema<?> schema)
            throws BallerinaOpenApiException {
        if (schema == null) {
            return Optional.empty();
        }

        if (schema.get$ref() != null) {
            String schemaName = GeneratorUtils.getValidName(extractReferenceType(schema.get$ref()), true);
            return Optional.ofNullable(createSimpleNameReferenceNode(createIdentifierToken(schemaName)));
        } else if (schema instanceof MapSchema) {
            RecordTypeGenerator recordTypeGenerator = new RecordTypeGenerator(schema, null);
            TypeDescriptorNode record = recordTypeGenerator.generateTypeDescriptorNode();
            return Optional.ofNullable(record);
        } else if (schema.getType() != null) {
            String schemaType = schema.getType();
            boolean isPrimitiveType = schemaType.equals(INTEGER) || schemaType.equals(NUMBER) ||
                    schemaType.equals(BOOLEAN) || schemaType.equals(STRING);
            if (schema instanceof ArraySchema) {
                return getTypeDescNodeForArraySchema((ArraySchema) schema);
            } else if (isPrimitiveType) {
                //This returns identifier node for the types: int, float, decimal, boolean, string
                IdentifierToken identifierToken = createIdentifierToken(schemaType,
                        AbstractNodeFactory.createEmptyMinutiaeList(), GeneratorUtils.SINGLE_WS_MINUTIAE);
                return Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
            } else {
                return Optional.empty();
            }
        } else if (schema instanceof ComposedSchema && (((ComposedSchema) schema).getOneOf() != null)) {
            Iterator<Schema> iterator = ((ComposedSchema) schema).getOneOf().iterator();
            return Optional.ofNullable(getUnionNodeForOneOf(iterator));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Generate {@code TypeDescriptorNode} for ArraySchema in OAS.
     */
    private static Optional<TypeDescriptorNode> getTypeDescNodeForArraySchema(ArraySchema schema)
            throws BallerinaOpenApiException {
        TypeDescriptorNode member;
        if (schema.getItems().get$ref() != null) {
            member = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(GeneratorUtils.getValidName(
                            extractReferenceType(schema.getItems().get$ref()), true)));
        } else if (!(schema.getItems() instanceof ArraySchema)) {
            member = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(GeneratorConstants.JSON));
        } else {
            member = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(
                    GeneratorUtils.convertOpenAPITypeToBallerina(schema.getItems().getType())));
        }
        ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        NodeList<ArrayDimensionNode> nodeList = createNodeList(dimensionNode);
        return Optional.ofNullable(createArrayTypeDescriptorNode(member, nodeList));
    }

    /**
     * Generate TypeDescriptor for all the mediaTypes.
     */
    public static Optional<TypeDescriptorNode> getMediaTypeToken(Map.Entry<String, MediaType> mediaType)
            throws BallerinaOpenApiException {

        String mediaTypeContent = mediaType.getKey().trim();
        if (mediaTypeContent.matches("text/.*")) {
            mediaTypeContent = GeneratorConstants.TEXT;
        }
        MediaType value = mediaType.getValue();
        Schema<?> schema = value.getSchema();
        IdentifierToken identifierToken;
        switch (mediaTypeContent) {
            case GeneratorConstants.APPLICATION_JSON:
                Optional<TypeDescriptorNode> returnTypeDecNode = generateTypeDescNodeForOASSchema(schema);
                return returnTypeDecNode.isEmpty() ?
                        Optional.ofNullable(createSimpleNameReferenceNode(createIdentifierToken("json"))) :
                        returnTypeDecNode;
            case GeneratorConstants.APPLICATION_XML:
                identifierToken = createIdentifierToken(GeneratorConstants.XML);
                return Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
            case GeneratorConstants.APPLICATION_URL_ENCODE:
                identifierToken = createIdentifierToken(GeneratorConstants.MAP_STRING);
                return Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
            case GeneratorConstants.TEXT:
                identifierToken = createIdentifierToken(GeneratorConstants.STRING);
                return Optional.ofNullable(createSimpleNameReferenceNode(identifierToken));
            case GeneratorConstants.APPLICATION_OCTET_STREAM:
                ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                        createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                        createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
                return Optional.ofNullable(createArrayTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(
                                null, createIdentifierToken(GeneratorConstants.BYTE)),
                        NodeFactory.createNodeList(dimensionNode)));
            default:
                return Optional.empty();
        }
    }

    /**
     * This util function is for generating service config annotation.
     * <pre>
     *     @http:ServiceConfig {
     *          treatNilableAsOptional : false
     *      }
     * </pre>
     */
    public static MetadataNode generateServiceConfigAnnotation() {

        MetadataNode metadataNode;
        BasicLiteralNode valueExpr = createBasicLiteralNode(STRING_LITERAL,
                createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN, GeneratorConstants.FALSE,
                        createEmptyMinutiaeList(),
                        createEmptyMinutiaeList()));
        SpecificFieldNode fields = createSpecificFieldNode(null,
                createIdentifierToken(
                        GeneratorConstants.TREAT_NILABLE_AS_OPTIONAL), createToken(COLON_TOKEN), valueExpr);
        AnnotationNode annotationNode = createAnnotationNode(createToken(SyntaxKind.AT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(
                        GeneratorConstants.SERVICE_CONFIG, GeneratorUtils.SINGLE_WS_MINUTIAE,
                        GeneratorUtils.SINGLE_WS_MINUTIAE)), createMappingConstructorExpressionNode(
                        createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(fields),
                        createToken(CLOSE_BRACE_TOKEN)));
        metadataNode = createMetadataNode(null, createSeparatedNodeList(annotationNode));
        return metadataNode;
    }

    /**
     * This util function is for generating the import node for http module.
     */
    public static NodeList<ImportDeclarationNode> createImportDeclarationNodes() {

        List<ImportDeclarationNode> imports = new ArrayList<>();
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA
                , GeneratorConstants.HTTP);
        imports.add(importForHttp);
        return AbstractNodeFactory.createNodeList(imports);
    }
}
