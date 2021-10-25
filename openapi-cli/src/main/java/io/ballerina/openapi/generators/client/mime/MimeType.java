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

package io.ballerina.openapi.generators.client.mime;

import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.swagger.v3.oas.models.media.MediaType;

import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class MimeType {

    GeneratorUtils generatorUtils;
    String payloadName = "payload";

    public MimeType() {
        this.generatorUtils = new GeneratorUtils();
    }

    /**
     * Generate statements of defining structure and setting payload.
     *
     * @param statementsList    - Previous statements list
     * @param mediaTypeEntry    - Media type entry
     */
    public abstract void setPayload(List<StatementNode> statementsList, Map.Entry<String, MediaType> mediaTypeEntry);

    /**
     * Generate statements for setting payload in the request.
     *
     * @param statementsList    - Previous statements list
     * @param payloadName       - Payload name
     * @param mediaType         - Media type
     */
    public void setPayload(List<StatementNode> statementsList, String payloadName,
                           String mediaType) {
        ExpressionStatementNode setPayloadExpression = generatorUtils.getSimpleExpressionStatementNode(
                String.format("request.setPayload(%s, \"%s\")", payloadName, mediaType));
        statementsList.add(setPayloadExpression);
    }

}
