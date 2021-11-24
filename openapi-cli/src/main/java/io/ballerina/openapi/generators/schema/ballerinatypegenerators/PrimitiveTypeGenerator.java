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

import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.schema.TypeGeneratorUtils;
import io.swagger.v3.oas.models.media.Schema;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.generators.GeneratorConstants.NUMBER;
import static io.ballerina.openapi.generators.GeneratorUtils.convertOpenAPITypeToBallerina;

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
 * @since 2.0.0
 */
public class PrimitiveTypeGenerator extends TypeGenerator {

    public PrimitiveTypeGenerator(Schema schema) {
        super(schema);
    }

    /**
     * Generate TypeDescriptorNode for primitive type schemas.
     * public type PetName string;
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        String typeDescriptorName = convertOpenAPITypeToBallerina(schema.getType().trim());
        // TODO: Need to the format of other primitive types too
        if (schema.getType().equals(NUMBER)) {
            if (schema.getFormat() != null) {
                typeDescriptorName = convertOpenAPITypeToBallerina(schema.getFormat().trim());
            }
        }
        typeDescriptorName = TypeGeneratorUtils.getNullableType(schema, typeDescriptorName);
        return createSimpleNameReferenceNode(createIdentifierToken(typeDescriptorName));
    }
}
