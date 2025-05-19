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

package io.ballerina.openapi.core.generators.document;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.InvalidReferenceException;
import io.ballerina.runtime.api.utils.IdentifierUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationLineNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownParameterDocumentationLineNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.AT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOCUMENTATION_DESCRIPTION;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.HASH_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.MARKDOWN_DOCUMENTATION_LINE;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.X_BALLERINA_DEPRECATED_REASON;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.escapeIdentifier;

/**
 * This class util for maintain the API doc comment related functions.
 *
 * @since 1.3.0
 */
public class DocCommentsGeneratorUtil {

    /**
     * Extract extension for find the display annotation.
     *
     * @param extensions OpenAPI extension.
     */
    public static void extractDisplayAnnotation(Map<String, Object> extensions,
                                                List<AnnotationNode> annotationNodes) {

        if (extensions != null) {
            for (Map.Entry<String, Object> extension : extensions.entrySet()) {
                if (extension.getKey().trim().equals(GeneratorConstants.X_BALLERINA_DISPLAY)) {
                    annotationNodes.add(getAnnotationNode(extension));
                }
            }
        }
    }

    /**
     * Extract annotation and documentation related for deprecated records and schemas.
     *
     * @param extensions      OpenAPI extensions
     * @param documentation   List of documentation nodes to be updated with documentation related to deprecation
     * @param annotationNodes List of annotation nodes to be updated with deprecated annotation
     */
    public static void extractDeprecatedAnnotation(Map<String, Object> extensions, List<Node> documentation,
                                                   List<AnnotationNode> annotationNodes) {

        AnnotationNode deprecatedAnnotation = createAnnotationNode(createToken(AT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken("deprecated")), null);
        if (documentation.size() > 0) {
            documentation.addAll(DocCommentsGeneratorUtil.createAPIDescriptionDoc(
                    "\n# Deprecated", false));
            if (extensions != null && extensions.entrySet().size() > 0) {
                for (Map.Entry<String, Object> next : extensions.entrySet()) {
                    if (next.getKey().equals(GeneratorConstants.X_BALLERINA_DEPRECATED_REASON)) {
                        documentation.addAll(DocCommentsGeneratorUtil.createAPIDescriptionDoc(
                                next.getValue().toString(), false));
                    }
                }
            }
        }
        annotationNodes.add(deprecatedAnnotation);
    }

    private static AnnotationNode getAnnotationNode(Map.Entry<String, Object> extension) {

        LinkedHashMap<String, String> extFields = (LinkedHashMap<String, String>) extension.getValue();
        List<Node> annotFields = new ArrayList<>();
        if (!extFields.isEmpty()) {
            for (Map.Entry<String, String> field : extFields.entrySet()) {

                BasicLiteralNode valueExpr = createBasicLiteralNode(STRING_LITERAL,
                        createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                                '"' + field.getValue().trim() + '"',
                                createEmptyMinutiaeList(),
                                createEmptyMinutiaeList()));
                SpecificFieldNode fields = createSpecificFieldNode(null,
                        createIdentifierToken(field.getKey().trim()),
                        createToken(COLON_TOKEN), valueExpr);
                annotFields.add(fields);
                annotFields.add(createToken(COMMA_TOKEN));
            }
            annotFields.remove(annotFields.size() - 1);
        }

        MappingConstructorExpressionNode annotValue = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(annotFields),
                createToken(CLOSE_BRACE_TOKEN));

        SimpleNameReferenceNode annotateReference =
                createSimpleNameReferenceNode(createIdentifierToken("display"));

        return createAnnotationNode(createToken(SyntaxKind.AT_TOKEN)
                , annotateReference, annotValue);
    }

    public static List<MarkdownDocumentationLineNode> createAPIDescriptionDoc(String description,
                                                                              boolean addExtraLine) {
        // Capitalize the first letter of the description. This is to maintain consistency
        String[] descriptionLines = description.split("\n");
        List<MarkdownDocumentationLineNode> documentElements = new ArrayList<>();
        Token hashToken = createToken(HASH_TOKEN, createEmptyMinutiaeList(), GeneratorUtils.SINGLE_WS_MINUTIAE);
        for (String line : descriptionLines) {
            MarkdownDocumentationLineNode documentationLineNode =
                    createMarkdownDocumentationLineNode(MARKDOWN_DOCUMENTATION_LINE, hashToken,
                            createNodeList(createLiteralValueToken(DOCUMENTATION_DESCRIPTION, line,
                                    createEmptyMinutiaeList(),
                                    GeneratorUtils.SINGLE_END_OF_LINE_MINUTIAE)));
            documentElements.add(documentationLineNode);
        }
        if (addExtraLine) {
            MarkdownDocumentationLineNode newLine = createMarkdownDocumentationLineNode(MARKDOWN_DOCUMENTATION_LINE,
                    createToken(SyntaxKind.HASH_TOKEN), createEmptyNodeList());
            documentElements.add(newLine);
        }
        return documentElements;
    }

    public static MarkdownDocumentationNode createAPIParamDocFromString(String paramName, String description) {
        String[] paramDescriptionLines = description.split("\n");
        StringBuilder docComment = new StringBuilder("# + " + paramName + " - " +
                paramDescriptionLines[0] + System.lineSeparator());
        for (int i = 1; i < paramDescriptionLines.length; i++) {
            String line = paramDescriptionLines[i].replaceAll("[\\r\\n\\t]", "");
            if (!line.isBlank()) {
                docComment.append("# ").append(line).append(System.lineSeparator());
            }
        }
        docComment.append(System.lineSeparator()).append("type a A;");
        ModuleMemberDeclarationNode moduleMemberDeclarationNode =
                NodeParser.parseModuleMemberDeclaration(docComment.toString());
        TypeDefinitionNode typeDefinitionNode = (TypeDefinitionNode) moduleMemberDeclarationNode;
        MetadataNode metadataNode = typeDefinitionNode.metadata().get();
        return (MarkdownDocumentationNode) metadataNode.children().get(0);
    }

    public static MarkdownParameterDocumentationLineNode createAPIParamDoc(String paramName, String description) {
        String[] paramDescriptionLines = description.split("\n");
        List<Node> documentElements = new ArrayList<>();
        for (String line : paramDescriptionLines) {
            if (!line.isBlank()) {
                documentElements.add(createIdentifierToken(line + " "));
            }
        }
        return createMarkdownParameterDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                createToken(SyntaxKind.PLUS_TOKEN), createIdentifierToken(paramName),
                createToken(SyntaxKind.MINUS_TOKEN), createNodeList(documentElements));
    }

    public static void extractDeprecatedAnnotationDetails(List<Node> deprecatedParamDocComments,
                                                          Parameter parameter, List<AnnotationNode> paramAnnot) {
        deprecatedParamDocComments.addAll(DocCommentsGeneratorUtil.createAPIDescriptionDoc(
                "# Deprecated parameters", false));

        String deprecatedDescription = "";
        if (parameter.getExtensions() != null) {
            for (Map.Entry<String, Object> extension : parameter.getExtensions().entrySet()) {
                if (extension.getKey().trim().equals(X_BALLERINA_DEPRECATED_REASON)) {
                    deprecatedDescription = extension.getValue().toString();
                    break;
                }
            }
        }
        MarkdownParameterDocumentationLineNode paramAPIDoc =
                DocCommentsGeneratorUtil.createAPIParamDoc(escapeIdentifier(
                        parameter.getName()), deprecatedDescription);
        deprecatedParamDocComments.add(paramAPIDoc);
        paramAnnot.add(createAnnotationNode(createToken(AT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken("deprecated")), null));
    }

    public static void updatedAnnotationInParameterNode(List<Node> updatedParamsRequired,
                                                         List<Node> updatedParamsDefault,
                                                         List<AnnotationNode> paramAnnot,
                                                         ParameterNode parameterNode) {
        if (parameterNode != null) {
            if (parameterNode instanceof RequiredParameterNode reParam) {
                updatedParamsRequired.add(reParam.modify(
                        reParam.annotations().isEmpty() ? createNodeList(paramAnnot) :
                                reParam.annotations().addAll(paramAnnot),
                        reParam.typeName(),
                        reParam.paramName().orElse(null)));
                updatedParamsRequired.add(createToken(SyntaxKind.COMMA_TOKEN));
            } else if (parameterNode instanceof DefaultableParameterNode deParam) {
                updatedParamsDefault.add(deParam.modify(
                        deParam.annotations().isEmpty() ? createNodeList(paramAnnot) :
                                deParam.annotations().addAll(paramAnnot),
                        deParam.typeName(),
                        deParam.paramName().orElse(null),
                        deParam.equalsToken(),
                        deParam.expression()));
                updatedParamsDefault.add(createToken(SyntaxKind.COMMA_TOKEN));
            }
        }
    }

    public static Optional<String> getSchemaDescription(Schema<?> schema, Components components) {
        if (Objects.nonNull(schema.getDescription()) && !schema.getDescription().isBlank()) {
            return Optional.of(schema.getDescription());
        }
        if (Objects.isNull(schema.get$ref())) {
            return Optional.empty();
        }
        try {
            String refName = GeneratorUtils.extractReferenceType(schema.get$ref());
            String refSection = GeneratorUtils.extractReferenceSection(schema.get$ref());
            return getDescriptionFromReference(refName, refSection, components);
        } catch (InvalidReferenceException exp) {
            return Optional.empty();
        }
    }

    public static Optional<String> getParameterDescription(Parameter parameter, Components components) {
        if (Objects.nonNull(parameter.getDescription()) && !parameter.getDescription().isBlank()) {
            return Optional.of(parameter.getDescription());
        }
        if (Objects.isNull(parameter.get$ref())) {
            return Optional.empty();
        }
        try {
            String refName = GeneratorUtils.extractReferenceType(parameter.get$ref());
            String refSection = GeneratorUtils.extractReferenceSection(parameter.get$ref());
            return getDescriptionFromReference(refName, refSection, components);
        } catch (InvalidReferenceException exp) {
            return Optional.empty();
        }
    }

    public static Optional<String> getResponseDescription(ApiResponse response, Components components) {
        if (Objects.nonNull(response.getDescription()) && !response.getDescription().isBlank()) {
            return Optional.of(response.getDescription());
        }
        if (Objects.isNull(response.get$ref())) {
            return Optional.empty();
        }
        try {
            String refName = GeneratorUtils.extractReferenceType(response.get$ref());
            String refSection = GeneratorUtils.extractReferenceSection(response.get$ref());
            return getDescriptionFromReference(refName, refSection, components);
        } catch (InvalidReferenceException exp) {
            return Optional.empty();
        }
    }

    public static Optional<String> getRequestBodyDescription(RequestBody requestBody, Components components) {
        if (Objects.nonNull(requestBody.getDescription()) && !requestBody.getDescription().isBlank()) {
            return Optional.of(requestBody.getDescription());
        }
        if (Objects.isNull(requestBody.get$ref())) {
            return Optional.empty();
        }
        try {
            String refName = GeneratorUtils.extractReferenceType(requestBody.get$ref());
            String refSection = GeneratorUtils.extractReferenceSection(requestBody.get$ref());
            return getDescriptionFromReference(refName, refSection, components);
        } catch (InvalidReferenceException exp) {
            return Optional.empty();
        }
    }

    private static Optional<String> getDescriptionFromReference(String refName, String refSection,
                                                                Components components)
            throws InvalidReferenceException {
        if (Objects.isNull(components)) {
            return Optional.empty();
        }
        switch (refSection) {
            case "schemas" -> {
                if (Objects.nonNull(components.getSchemas()) && components.getSchemas().containsKey(refName)) {
                    return getSchemaDescription(components.getSchemas().get(refName), components);
                }
            }
            case "parameters" -> {
                if (Objects.nonNull(components.getParameters()) &&
                        components.getParameters().containsKey(refName)) {
                    return getParameterDescription(components.getParameters().get(refName), components);
                }
            }
            case "responses" -> {
                if (Objects.nonNull(components.getResponses()) &&
                        components.getResponses().containsKey(refName)) {
                    return getResponseDescription(components.getResponses().get(refName), components);
                }
            }
            case "requestBodies" -> {
                if (Objects.nonNull(components.getRequestBodies()) &&
                        components.getRequestBodies().containsKey(refName)) {
                    return getRequestBodyDescription(components.getRequestBodies().get(refName), components);
                }
            }
        }
        return Optional.empty();
    }

    public static String unescapeIdentifier(String parameterName) {
        String unescapedParamName = IdentifierUtils.unescapeBallerina(parameterName);
        return unescapedParamName.replaceAll("\\\\", "").replaceAll("'", "");
    }
}
