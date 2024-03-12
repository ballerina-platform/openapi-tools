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

package io.ballerina.openapi.core.typegenerator.generators;

import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.typegenerator.GeneratorConstants;
import io.ballerina.openapi.core.typegenerator.TypeGeneratorUtils;
import io.ballerina.openapi.core.typegenerator.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for schemas without type.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *     Pet:
 *       description: Details of the pet.
 *  </pre>
 * Generated Ballerina type for the schema `Pet` :
 * <pre>
 *      public type Pet anydata;
 * </pre>
 *
 * @since 1.3.0
 */
public class AnyDataTypeGenerator extends TypeGenerator {

    public AnyDataTypeGenerator(Schema schema, String typeName, HashMap<String, TypeDefinitionNode> subTypesMap, HashMap<String, NameReferenceNode> pregeneratedTypeMap) {
        super(schema, typeName, subTypesMap, pregeneratedTypeMap);
    }

    /**
     * Generate TypeDescriptorNode schemas with no type.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        return TypeGeneratorUtils.getNullableType(schema, createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.ANY_DATA)));
    }
}
