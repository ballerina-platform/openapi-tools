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
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.openapi.core.GeneratorConstants;
import io.ballerina.openapi.core.GeneratorUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

/**
 * This class util for maintain the API doc comment related functions.
 *
 * @since 1.3.0
 */
public class DocCommentsGenerator {

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
            documentation.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
                    "\n# Deprecated", false));
            if (extensions != null && extensions.entrySet().size() > 0) {
                for (Map.Entry<String, Object> next : extensions.entrySet()) {
                    if (next.getKey().equals(GeneratorConstants.X_BALLERINA_DEPRECATED_REASON)) {
                        documentation.addAll(DocCommentsGenerator.createAPIDescriptionDoc(
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
        description = !description.isBlank() ? description.substring(0, 1)
                .toUpperCase(Locale.ENGLISH) + description.substring(1) : description;
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
}
