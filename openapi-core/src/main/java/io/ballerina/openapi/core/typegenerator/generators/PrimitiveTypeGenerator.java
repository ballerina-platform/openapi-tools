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
import io.ballerina.openapi.core.typegenerator.GeneratorUtils;
import io.ballerina.openapi.core.typegenerator.TypeGeneratorUtils;
import io.ballerina.openapi.core.typegenerator.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for primitive type schemas.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *      components:
 *          schemas:
 *              PetName:
 *                  type: string
 *  </pre>
 * Generated Ballerina type for the schema `PetName` :
 * <pre>
 *     public type PetName string;
 * </pre>
 *
 * @since 1.3.0
 */
public class PrimitiveTypeGenerator extends TypeGenerator {

    public PrimitiveTypeGenerator(Schema schema, String typeName, HashMap<String, TypeDefinitionNode> subTypesMap, HashMap<String, NameReferenceNode> pregeneratedTypeMap) {
        super(schema, typeName, subTypesMap, pregeneratedTypeMap);
    }

    /**
     * Generate TypeDescriptorNode for primitive type schemas.
     * public type PetName string;
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        String typeDescriptorName = GeneratorUtils.convertOpenAPITypeToBallerina(schema);
        // TODO: Need to the format of other primitive types too
        if (schema.getEnum() != null && schema.getEnum().size() > 0) {
            EnumGenerator enumGenerator = new EnumGenerator(schema, typeName, subTypesMap, pregeneratedTypeMap);
            typeDescriptorName = enumGenerator.generateTypeDescriptorNode().toString();
            return createSimpleNameReferenceNode(
                    createIdentifierToken(typeDescriptorName));
        } else if (GeneratorUtils.getOpenAPIType(schema).equals(GeneratorConstants.STRING) &&
                schema.getFormat() != null &&
                schema.getFormat().equals(GeneratorConstants.BINARY)) {
            typeDescriptorName = "record {byte[] fileContent; string fileName;}";
        }
        TypeDescriptorNode typeDescriptorNode = createSimpleNameReferenceNode(
                createIdentifierToken(typeDescriptorName));
        return TypeGeneratorUtils.getNullableType(schema, typeDescriptorNode);
    }
}
