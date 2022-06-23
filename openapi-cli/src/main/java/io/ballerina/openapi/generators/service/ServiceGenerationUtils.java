/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.openapi.generators.service;

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
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.openapi.generators.GeneratorConstants.APPLICATION_JSON;
import static io.ballerina.openapi.generators.GeneratorConstants.APPLICATION_OCTET_STREAM;
import static io.ballerina.openapi.generators.GeneratorConstants.APPLICATION_URL_ENCODE;
import static io.ballerina.openapi.generators.GeneratorConstants.APPLICATION_XML;
import static io.ballerina.openapi.generators.GeneratorConstants.FALSE;
import static io.ballerina.openapi.generators.GeneratorConstants.SERVICE_CONFIG;
import static io.ballerina.openapi.generators.GeneratorConstants.TEXT;
import static io.ballerina.openapi.generators.GeneratorConstants.TREAT_NILABLE_AS_OPTIONAL;
import static io.ballerina.openapi.generators.GeneratorUtils.SINGLE_WS_MINUTIAE;
import static io.ballerina.openapi.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.generators.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.generators.GeneratorUtils.getQualifiedNameReferenceNode;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;
import static io.ballerina.openapi.generators.GeneratorUtils.isConstraint;

/**
 * This store all the util functions related service generation process.
 *
 * @since 2.0.0
 */
public class ServiceGenerationUtils {

    /**
     * This method will extract reference type by splitting the reference string.
     *
     * @param referenceVariable - Reference String
     * @return Reference variable name
     * @throws BallerinaOpenApiException - Throws an exception if the reference string is incompatible.
     *                                     Note : Current implementation will not support external links a references.
     */
    public  static String extractReferenceType(String referenceVariable) throws BallerinaOpenApiException {
        if (referenceVariable.startsWith("#") && referenceVariable.contains("/")) {
            String[] refArray = referenceVariable.split("/");
            return escapeIdentifier(refArray[refArray.length - 1]);
        } else {
            throw new BallerinaOpenApiException("Invalid reference value : " + referenceVariable
                    + "\nBallerina only supports local reference values.");
        }
    }

    public static AnnotationNode getAnnotationNode(String identifier, MappingConstructorExpressionNode annotValue) {
        Token atToken = createIdentifierToken("@");
        QualifiedNameReferenceNode annotReference = getQualifiedNameReferenceNode(GeneratorConstants.HTTP, identifier);
        return createAnnotationNode(atToken, annotReference, annotValue);
    }

    public static UnionTypeDescriptorNode getUnionNodeForOneOf(Iterator<Schema> iterator)
            throws BallerinaOpenApiException {

        List<SimpleNameReferenceNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = createIdentifierToken("|");
        while (iterator.hasNext()) {
            Schema contentType = iterator.next();
            TypeDescriptorNode node = generateNodeForOASSchema(contentType);
            qualifiedNodes.add((SimpleNameReferenceNode) node);
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
    public static TypeDescriptorNode generateNodeForOASSchema(Schema<?> schema) throws BallerinaOpenApiException {
        IdentifierToken identifierToken =  createIdentifierToken(GeneratorConstants.JSON,
                AbstractNodeFactory.createEmptyMinutiaeList(), SINGLE_WS_MINUTIAE);
        if (schema == null) {
            return createSimpleNameReferenceNode(identifierToken);
        }
        if (schema.get$ref() != null) {
            String schemaName = getValidName(extractReferenceType(schema.get$ref()), true);
            return createSimpleNameReferenceNode(createIdentifierToken(schemaName));
        } else if (schema.getType() != null) {
            if (schema instanceof ArraySchema) {
                TypeDescriptorNode member;
                if (((ArraySchema) schema).getItems().get$ref() != null) {
                    member = createBuiltinSimpleNameReferenceNode(null,
                            createIdentifierToken(getValidName(extractReferenceType(((ArraySchema) schema).
                                    getItems().get$ref()), true)));
                } else if (!(((ArraySchema) schema).getItems() instanceof ArraySchema)) {
                    member = createBuiltinSimpleNameReferenceNode(null,
                            createIdentifierToken(GeneratorConstants.JSON));
                } else {
                    member = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(
                            convertOpenAPITypeToBallerina(((ArraySchema) schema).getItems().getType())));
                }
                ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                        createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                        createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
                NodeList<ArrayDimensionNode> nodeList = createNodeList(dimensionNode);
                return  createArrayTypeDescriptorNode(member, nodeList);
            } else {
                identifierToken =  createIdentifierToken(schema.getType(),
                        AbstractNodeFactory.createEmptyMinutiaeList(), SINGLE_WS_MINUTIAE);
                return createSimpleNameReferenceNode(identifierToken);
            }
        } else if (schema instanceof ComposedSchema && (((ComposedSchema) schema).getOneOf() != null)) {
            Iterator<Schema> iterator = ((ComposedSchema) schema).getOneOf().iterator();
            return getUnionNodeForOneOf(iterator);
        }
        return createSimpleNameReferenceNode(identifierToken);
    }

    /**
     * Generate TypeDescriptor for all the mediaTypes.
     */
    public static TypeDescriptorNode getMediaTypeToken(Map.Entry<String, MediaType> mediaType)
            throws BallerinaOpenApiException {
        String mediaTypeContent = mediaType.getKey().trim();
        if (mediaTypeContent.matches("text/.*")) {
            mediaTypeContent = TEXT;
        }
        MediaType value = mediaType.getValue();
        Schema<?> schema = value.getSchema();
        IdentifierToken identifierToken;
        switch (mediaTypeContent) {
            case APPLICATION_JSON:
                return generateNodeForOASSchema(schema);
            case APPLICATION_XML:
                identifierToken = createIdentifierToken(GeneratorConstants.XML);
                return createSimpleNameReferenceNode(identifierToken);
            case APPLICATION_URL_ENCODE:
                identifierToken = createIdentifierToken(GeneratorConstants.MAP_STRING);
                return createSimpleNameReferenceNode(identifierToken);
            case TEXT:
                identifierToken = createIdentifierToken(GeneratorConstants.STRING);
                return createSimpleNameReferenceNode(identifierToken);
            case APPLICATION_OCTET_STREAM:
                ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                        createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                        createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
                return createArrayTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(
                        null, createIdentifierToken(GeneratorConstants.BYTE)),
                        NodeFactory.createNodeList(dimensionNode));
            default:
                identifierToken = createIdentifierToken(GeneratorConstants.JSON);
                return createSimpleNameReferenceNode(identifierToken);
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
                createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN, FALSE, createEmptyMinutiaeList(),
                        createEmptyMinutiaeList()));
        SpecificFieldNode fields = createSpecificFieldNode(null,
                createIdentifierToken(TREAT_NILABLE_AS_OPTIONAL), createToken(COLON_TOKEN), valueExpr);
        AnnotationNode annotationNode = createAnnotationNode(createToken(SyntaxKind.AT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(SERVICE_CONFIG, SINGLE_WS_MINUTIAE,
                        SINGLE_WS_MINUTIAE)), createMappingConstructorExpressionNode(
                        createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(fields),
                        createToken(CLOSE_BRACE_TOKEN)));
        metadataNode = createMetadataNode(null, createSeparatedNodeList(annotationNode));
        return metadataNode;
    }

    /**
     * This util function is for generating the import node for http module.
     */
    public static NodeList<ImportDeclarationNode> createImportDeclarationNodes(OpenAPI openAPI) {
        List<ImportDeclarationNode> imports = new ArrayList<>();
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode(GeneratorConstants.BALLERINA
                , GeneratorConstants.HTTP);
        imports.add(importForHttp);
        if (isConstraint(openAPI)) {
            ImportDeclarationNode importForConstraint = GeneratorUtils.getImportDeclarationNode(
                    GeneratorConstants.BALLERINA, GeneratorConstants.CONSTRAINT);
            imports.add(importForConstraint);

        }
        return AbstractNodeFactory.createNodeList(imports);
    }
}
