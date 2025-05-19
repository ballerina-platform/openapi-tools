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

package io.ballerina.openapi.core.generators.client.mime;

import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.core.generators.client.BallerinaUtilGenerator;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.MediaType;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.JSON_DATA;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.MIME;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.addImport;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.escapeIdentifier;

/**
 * Defines the payload structure of multipart form-data mime type.
 *
 * @since 1.3.0
 */
public class MultipartFormData extends MimeType {

    BallerinaUtilGenerator ballerinaUtilGenerator;
    List<ImportDeclarationNode> imports;

    public MultipartFormData(List<ImportDeclarationNode> imports,
                             BallerinaUtilGenerator ballerinaUtilGenerator) {
        this.imports = imports;
        this.ballerinaUtilGenerator = ballerinaUtilGenerator;
    }

    @Override
    public void setPayload(List<StatementNode> statementsList, Map.Entry<String, MediaType> mediaTypeEntry) {
        ballerinaUtilGenerator.setRequestBodyMultipartFormDatafound(true);
        addImport(imports, MIME);
        addImport(imports, JSON_DATA);
        VariableDeclarationNode encodingMap = getMultipartMap(mediaTypeEntry);

        VariableDeclarationNode bodyPartsVariable;
        if (encodingMap == null) {
            bodyPartsVariable = GeneratorUtils.getSimpleStatement("mime:Entity[]", "bodyParts",
                    "check createBodyParts(check jsondata:toJson(payload).ensureType())");
        } else {
            statementsList.add(encodingMap);
            bodyPartsVariable = GeneratorUtils.getSimpleStatement("mime:Entity[]", "bodyParts",
                    "check createBodyParts(check jsondata:toJson(payload).ensureType(), encodingMap)");
        }
        statementsList.add(bodyPartsVariable);

        ExpressionStatementNode setPayloadExpression = GeneratorUtils.getSimpleExpressionStatementNode(
                "request.setBodyParts(bodyParts)");
        statementsList.add(setPayloadExpression);
    }

    private VariableDeclarationNode getMultipartMap(Map.Entry<String, MediaType> mediaTypeEntry) {
        if (mediaTypeEntry.getValue().getEncoding() != null) {
            List<Node> mapFields = new LinkedList<>();
            BuiltinSimpleNameReferenceNode mapType = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken("map<Encoding>"));
            CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                    createIdentifierToken("encodingMap"));
            TypedBindingPatternNode bindingPatternNode = createTypedBindingPatternNode(mapType, bindingPattern);

            Set<Map.Entry<String, Encoding>> encodingEntries = mediaTypeEntry.getValue().getEncoding().entrySet();
            for (Map.Entry<String, Encoding> entry : encodingEntries) {
                getEncoding(entry, mapFields);
            }

            if (mapFields.isEmpty()) {
                return null;
            }

            mapFields.remove(mapFields.size() - 1);
            MappingConstructorExpressionNode initialize = createMappingConstructorExpressionNode(
                    createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(mapFields), createToken(CLOSE_BRACE_TOKEN));
            return createVariableDeclarationNode(createEmptyNodeList(), null, bindingPatternNode,
                    createToken(EQUAL_TOKEN), initialize, createToken(SEMICOLON_TOKEN));
        }

        return null;
    }

    private void getEncoding(Map.Entry<String, Encoding> entry, List<Node> mapFields) {
        SpecificFieldNode contentField = null;
        SpecificFieldNode headersField = null;
        IdentifierToken fieldName = createIdentifierToken('"' + entry.getKey() + '"');
        SeparatedNodeList<MappingFieldNode> separatedNodeList = createSeparatedNodeList();

        if (entry.getValue().getContentType() != null) {
            contentField = createSpecificFieldNode(null,
                    createIdentifierToken("contentType"), createToken(COLON_TOKEN),
                    createRequiredExpressionNode(createIdentifierToken('"' +
                            entry.getValue().getContentType().split(",")[0] + '"')));
        }

        if (entry.getValue().getHeaders() != null) {
            headersField = getHeaderEncoding(entry);
        }

        if (headersField != null && contentField != null) {
            separatedNodeList = createSeparatedNodeList(contentField, createToken(COMMA_TOKEN), headersField);
        } else if (contentField != null) {
            separatedNodeList = createSeparatedNodeList(contentField);
        } else if (headersField != null) {
            separatedNodeList = createSeparatedNodeList(headersField);
        }

        ExpressionNode expressionNode = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), separatedNodeList, createToken(CLOSE_BRACE_TOKEN));
        SpecificFieldNode specificFieldNode = createSpecificFieldNode(null, fieldName,
                createToken(COLON_TOKEN), expressionNode);
        mapFields.add(specificFieldNode);
        mapFields.add(createToken(COMMA_TOKEN));
    }

    private SpecificFieldNode getHeaderEncoding(Map.Entry<String, Encoding> entry) {
        List<Node> headerMap = new ArrayList<>();
        for (Map.Entry<String, Header> header : entry.getValue().getHeaders().entrySet()) {
            IdentifierToken headerName = createIdentifierToken('"' + header.getKey() + '"');
            SimpleNameReferenceNode valueExpr = createSimpleNameReferenceNode(
                    createIdentifierToken(escapeIdentifier(header.getKey())));
            SpecificFieldNode specificFieldNode = createSpecificFieldNode(null,
                    headerName, createToken(COLON_TOKEN), valueExpr);
            headerMap.add(specificFieldNode);
            headerMap.add(createToken(COMMA_TOKEN));
        }
        if (!headerMap.isEmpty()) {
            headerMap.remove(headerMap.size() - 1);
        }
        MappingConstructorExpressionNode initialize = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(headerMap),
                createToken(CLOSE_BRACE_TOKEN));
        return createSpecificFieldNode(null, createIdentifierToken("headers"),
                createToken(COLON_TOKEN), initialize);
    }

}
