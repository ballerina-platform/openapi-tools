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
package io.ballerina.openapi.core.service;

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
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.TypeHandler;
import io.ballerina.openapi.core.generators.type.GeneratorUtils;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
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
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;

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
     * @throws OASTypeGenException - Throws an exception if the reference string is incompatible.
     *                                   Note : Current implementation will not support external links a references.
     */
    public static String extractReferenceType(String referenceVariable) throws OASTypeGenException {

        if (referenceVariable.startsWith("#") && referenceVariable.contains("/")) {
            String[] refArray = referenceVariable.split("/");
            return GeneratorUtils.escapeIdentifier(refArray[refArray.length - 1]);
        } else {
            throw new OASTypeGenException("Invalid reference value : " + referenceVariable
                    + "\nBallerina only supports local reference values.");
        }
    }

    public static AnnotationNode getAnnotationNode(String identifier, MappingConstructorExpressionNode annotValue) {

        Token atToken = createIdentifierToken("@");
        QualifiedNameReferenceNode annotReference = GeneratorUtils.getQualifiedNameReferenceNode(
                GeneratorConstants.HTTP, identifier);
        return createAnnotationNode(atToken, annotReference, annotValue);
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

    public static QualifiedNameReferenceNode getQualifiedNameReferenceNode(String modulePrefix, String identifier) {
        Token modulePrefixToken = AbstractNodeFactory.createIdentifierToken(modulePrefix);
        Token colon = AbstractNodeFactory.createIdentifierToken(":");
        IdentifierToken identifierToken = AbstractNodeFactory.createIdentifierToken(identifier);
        return NodeFactory.createQualifiedNameReferenceNode(modulePrefixToken, colon, identifierToken);
    }

    public static TypeDescriptorNode generateTypeDescriptorForXMLContent() {
        return getSimpleNameReferenceNode(GeneratorConstants.XML);
    }

    public static TypeDescriptorNode generateTypeDescriptorForMapStringContent() {
        return getSimpleNameReferenceNode(GeneratorConstants.MAP_STRING);
    }

    public static TypeDescriptorNode generateTypeDescriptorForTextContent() {
        return getSimpleNameReferenceNode(GeneratorConstants.STRING);
    }

    public static TypeDescriptorNode generateTypeDescriptorForOctetStreamContent() {
        ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null, createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        return createArrayTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.BYTE)), NodeFactory.createNodeList(dimensionNode));
    }

    public static TypeDescriptorNode generateTypeDescriptorForJsonContent(Map.Entry<String, MediaType> mediaType) throws
            OASTypeGenException {
        Schema<?> schema = mediaType.getValue().getSchema();
        Optional<TypeDescriptorNode> typeDescriptorNode = TypeHandler.getInstance().getTypeNodeFromOASSchema(schema);
        return typeDescriptorNode.orElse(null);
    }

    private static TypeDescriptorNode getSimpleNameReferenceNode(String typeName) {
        return createSimpleNameReferenceNode(createIdentifierToken(typeName));
    }
}
