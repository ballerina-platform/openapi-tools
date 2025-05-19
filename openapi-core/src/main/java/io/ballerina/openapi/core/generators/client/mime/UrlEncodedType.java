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
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.core.generators.client.BallerinaUtilGenerator;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.swagger.v3.oas.models.media.Encoding;
import io.swagger.v3.oas.models.media.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.ENCODING;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.JSON_DATA;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.STRING;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.addImport;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.createEncodingMap;

/**
 * Defines the payload structure of "application/x-www-form-urlencoded" mime type.
 *
 * @since 1.3.0
 */
public class UrlEncodedType extends MimeType {

    private final BallerinaUtilGenerator ballerinaUtilGenerator;
    private final List<ImportDeclarationNode> imports;

    public UrlEncodedType(BallerinaUtilGenerator ballerinaUtilGenerator, List<ImportDeclarationNode> imports) {
        this.ballerinaUtilGenerator = ballerinaUtilGenerator;
        this.imports = imports;
    }

    @Override
    public void setPayload(List<StatementNode> statementsList, Map.Entry<String, MediaType> mediaTypeEntry) {
        addImport(imports, JSON_DATA);
        ballerinaUtilGenerator.setRequestBodyEncodingFound(true);
        VariableDeclarationNode requestBodyEncodingMap = getRequestBodyEncodingMap(
                mediaTypeEntry.getValue().getEncoding());
        String payloadName = "encodedRequestBody";
        if (requestBodyEncodingMap != null) {
            statementsList.add(requestBodyEncodingMap);
            VariableDeclarationNode requestBodyVariable = GeneratorUtils.getSimpleStatement(STRING, payloadName,
                    "createFormURLEncodedRequestBody(check jsondata:toJson(payload).ensureType(), " +
                            "requestBodyEncoding)");
            statementsList.add(requestBodyVariable);
        } else {
            VariableDeclarationNode requestBodyVariable = GeneratorUtils.getSimpleStatement(STRING, payloadName,
                    "createFormURLEncodedRequestBody(check jsondata:toJson(payload).ensureType())");
            statementsList.add(requestBodyVariable);
        }
        setPayload(statementsList, payloadName, mediaTypeEntry.getKey());
    }

    /**
     * Generate VariableDeclarationNode for request body encoding map which includes the data related serialization
     * mechanism that needs to be used in each array or object type property. These data is given in the `style` and
     * `explode` sections under the requestBody encoding section of the OpenAPI definition. Style defines how multiple
     * values are delimited and explode specifies whether arrays and objects should generate separate parameters.
     * <p>
     * --ex: {@code map<[string, boolean]> requestBodyEncoding =
     * {"address": ["deepObject", true], "bank_account": ["deepObject", true]}}
     *
     * @param encodingMap Encoding details of the `application/x-www-form-urlencoded` type request body
     * @return {@link VariableDeclarationNode}
     */
    public VariableDeclarationNode getRequestBodyEncodingMap(Map<String, Encoding> encodingMap) {

        List<Node> filedOfMap = new ArrayList<>();
        BuiltinSimpleNameReferenceNode mapType = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("map<" + ENCODING + ">"));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken("requestBodyEncoding"));
        TypedBindingPatternNode bindingPatternNode = createTypedBindingPatternNode(mapType, bindingPattern);
        if (encodingMap != null && encodingMap.size() > 0) {
            for (Map.Entry<String, Encoding> encoding : encodingMap.entrySet()) {
                if (encoding.getValue().getStyle() != null || encoding.getValue().getExplode() != null) {
                    createEncodingMap(filedOfMap, encoding.getValue().getStyle().toString(),
                            encoding.getValue().getExplode(), encoding.getKey());
                }
            }
            if (!filedOfMap.isEmpty()) {
                filedOfMap.remove(filedOfMap.size() - 1);
                MappingConstructorExpressionNode initialize = createMappingConstructorExpressionNode(
                        createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(filedOfMap),
                        createToken(CLOSE_BRACE_TOKEN));
                return createVariableDeclarationNode(createEmptyNodeList(),
                        null, bindingPatternNode, createToken(EQUAL_TOKEN), initialize,
                        createToken(SEMICOLON_TOKEN));
            }
        }
        return null;
    }
}
