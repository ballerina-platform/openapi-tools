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

package io.ballerina.openapi.generators.schema.ballerinatypegenerators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.schema.TypeGeneratorUtils;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for object type schema.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *      components:
 *          schemas:
 *              Pet:
 *                  required:
 *                      - id
 *                      - name
 *                  properties:
 *                      id:
 *                          type: integer
 *                          format: int64
 *                      name:
 *                          type: string
 *                      tag:
 *                          type: string
 *                      type:
 *                          type: string
 *  </pre>
 * Generated Ballerina type for the schema `Pet` :
 * <pre>
 * public type Pet record {
 *      int id;
 *      string name;
 *      string tag?;
 *      string 'type?;
 * };
 * </pre>
 *
 * @since 2.0.0
 */
public class RecordTypeGenerator extends TypeGenerator {

    public RecordTypeGenerator(Schema schema) {
        super(schema);
    }

    /**
     * Generate TypeDescriptorNode for object type schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        if (schema.getProperties() != null) {
            Map<String, Schema> properties = schema.getProperties();
            List<String> required = schema.getRequired();
            List<Node> recordFList = TypeGeneratorUtils.addRecordFields(required, properties.entrySet(), true);
            NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFList);
            return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                    createToken(OPEN_BRACE_TOKEN), fieldNodes, null, createToken(CLOSE_BRACE_TOKEN));
        } else {
            NodeList<Node> fieldNodes = createEmptyNodeList();
            return NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                    createToken(OPEN_BRACE_TOKEN), fieldNodes, null, createToken(CLOSE_BRACE_TOKEN));
        }
    }
}
