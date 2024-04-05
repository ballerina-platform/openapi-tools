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

package io.ballerina.openapi.core.generators.type.generators;

import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.type.GeneratorConstants;
import io.ballerina.openapi.core.generators.type.TypeGeneratorUtils;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;

import static io.ballerina.openapi.core.generators.common.GeneratorUtils.convertOpenAPITypeToBallerina;

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

    public PrimitiveTypeGenerator(Schema schema, String typeName, boolean overrideNullable,
                                  HashMap<String, TypeDefinitionNode> subTypesMap,
                                  HashMap<String, NameReferenceNode> pregeneratedTypeMap) {
        super(schema, typeName, overrideNullable, subTypesMap, pregeneratedTypeMap);
    }

    /**
     * Generate TypeDescriptorNode for primitive type schemas.
     * public type PetName string;
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws OASTypeGenException {
        String typeDescriptorName = null;
        try {
            typeDescriptorName = convertOpenAPITypeToBallerina(schema, overrideNullable);
        } catch (BallerinaOpenApiException e) {
            throw new RuntimeException(e);
        }
        // TODO: Need to the format of other primitive types too
        if (schema.getEnum() != null && schema.getEnum().size() > 0) {
            EnumGenerator enumGenerator = new EnumGenerator(schema, typeName, overrideNullable,
                    subTypesMap, pregeneratedTypeMap);
            return enumGenerator.generateTypeDescriptorNode();
        } else if (GeneratorUtils.getOpenAPIType(schema).equals(GeneratorConstants.STRING) &&
                schema.getFormat() != null &&
                schema.getFormat().equals(GeneratorConstants.BINARY)) {
            typeDescriptorName = "record {byte[] fileContent; string fileName;}";
        }
        TypeDescriptorNode typeDescriptorNode = NodeParser.parseTypeDescriptor(typeDescriptorName);
        return TypeGeneratorUtils.getNullableType(schema, typeDescriptorNode, overrideNullable);
    }
}
