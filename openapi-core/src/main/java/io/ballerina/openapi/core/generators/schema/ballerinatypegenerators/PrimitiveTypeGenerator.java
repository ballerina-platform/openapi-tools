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

package io.ballerina.openapi.core.generators.schema.ballerinatypegenerators;

import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.GeneratorConstants;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.schema.TypeGeneratorUtils;
import io.swagger.v3.oas.models.media.Schema;

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

    public PrimitiveTypeGenerator(Schema schema, String typeName) {
        super(schema, typeName);
    }

    /**
     * Generate TypeDescriptorNode for primitive type schemas.
     * public type PetName string;
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        String typeDescriptorName = GeneratorUtils.convertOpenAPITypeToBallerina(schema.getType().trim());
        // TODO: Need to the format of other primitive types too
        if (schema.getType().equals(GeneratorConstants.NUMBER)) {
            if (schema.getFormat() != null) {
                typeDescriptorName = GeneratorUtils.convertOpenAPITypeToBallerina(schema.getFormat().trim());
            }
        } else if (schema.getType().equals(GeneratorConstants.STRING) && schema.getFormat() != null &&
                schema.getFormat().equals(GeneratorConstants.BINARY)) {
            typeDescriptorName = "record {byte[] fileContent; string fileName;}";
        }
        TypeDescriptorNode typeDescriptorNode = createSimpleNameReferenceNode(
                createIdentifierToken(typeDescriptorName));
        return TypeGeneratorUtils.getNullableType(schema, typeDescriptorNode);
    }
}
