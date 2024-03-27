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

package io.ballerina.openapi.core.service.parameter;

import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.service.GeneratorConstants;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;

/**
 * This class for generating request body payload for OAS requestBody section.
 *
 * @since 1.3.0
 */
public class LowResourceRequestBodyGenerator extends RequestBodyGenerator {

    /**
     * This for creating request Body for given request object.
     */
    public RequiredParameterNode createRequestBodyNode() {
        BuiltinSimpleNameReferenceNode typeNameRequest = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.HTTP_REQUEST));
        IdentifierToken paramNameRequest = createIdentifierToken(GeneratorConstants.REQUEST);
        return createRequiredParameterNode(createEmptyNodeList(), typeNameRequest, paramNameRequest);
    }
}
