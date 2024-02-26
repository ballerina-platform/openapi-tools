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

package io.ballerina.openapi.corenew.typegenerator.generators;

import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.corenew.typegenerator.GeneratorUtils;
import io.ballerina.openapi.corenew.typegenerator.TypeGeneratorUtils;
import io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException;
import io.ballerina.openapi.corenew.typegenerator.model.GeneratorMetaData;
import io.swagger.v3.oas.models.media.Schema;

import static io.ballerina.compiler.syntax.tree.NodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for referenced schemas.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *      components:
 *          schemas:
 *              PetName:
 *                  type: string
 *              DogName:
 *                  $ref: "#/components/schemas/PetName"
 *  </pre>
 * Generated Ballerina type for the schema `DogName` :
 * <pre>
 *     public type DogName PetName;
 * </pre>
 *
 * @since 1.3.0
 */
public class ReferencedTypeGenerator extends TypeGenerator {

    public ReferencedTypeGenerator(Schema schema, String typeName) {
        super(schema, typeName);
    }

    /**
     * Generate TypeDescriptorNode for referenced schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {

        String extractName = GeneratorUtils.extractReferenceType(schema.get$ref());
        String typeName = GeneratorUtils.getValidName(extractName, true);
        Schema<?> refSchema = GeneratorMetaData.getInstance().getOpenAPI().getComponents().getSchemas().get(typeName);
        refSchema = refSchema == null ?
                GeneratorMetaData.getInstance().getOpenAPI().getComponents().getSchemas().get(extractName) : refSchema;
        TypeDescriptorNode typeDescriptorNode = createSimpleNameReferenceNode(createIdentifierToken(typeName));
        if (refSchema == null) {
            throw new BallerinaOpenApiException(String.format("Undefined $ref: '%s' in openAPI contract.",
                    schema.get$ref()));
        }
        return TypeGeneratorUtils.getNullableType(refSchema, typeDescriptorNode);
    }
}