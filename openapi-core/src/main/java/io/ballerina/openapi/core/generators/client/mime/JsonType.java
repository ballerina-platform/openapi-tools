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

package io.ballerina.openapi.core.generators.client.mime;

import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.core.GeneratorUtils;
import io.swagger.v3.oas.models.media.MediaType;

import java.util.List;
import java.util.Map;

/**
 * Defines the payload structure of json mime type.
 *
 * @since 2.0.0
 */
public class JsonType extends MimeType {

    @Override
    public void setPayload(List<StatementNode> statementsList, Map.Entry<String, MediaType> mediaTypeEntry) {
        payloadName = "jsonBody";
        VariableDeclarationNode jsonVariable = GeneratorUtils.getSimpleStatement("json", payloadName
                , "check payload.cloneWithType(json)");
        statementsList.add(jsonVariable);
        setPayload(statementsList, payloadName, mediaTypeEntry.getKey());
    }
}
