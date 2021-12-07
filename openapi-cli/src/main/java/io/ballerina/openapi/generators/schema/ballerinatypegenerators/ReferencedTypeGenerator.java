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
import io.ballerina.openapi.generators.schema.model.GeneratorMetaData;
import io.swagger.v3.oas.models.media.Schema;

import static io.ballerina.compiler.syntax.tree.NodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.openapi.generators.GeneratorUtils.SINGLE_EMPTY_MINUTIAE;
import static io.ballerina.openapi.generators.GeneratorUtils.SINGLE_WS_MINUTIAE;
import static io.ballerina.openapi.generators.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for referenced schemas.
 * -- ex:
 * Sample OpenAPI :
 *  <pre>
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
 * @since 2.0.0
 */
public class ReferencedTypeGenerator extends TypeGenerator {

    public ReferencedTypeGenerator(Schema schema) {
        super(schema);
    }

    /**
     * Generate TypeDescriptorNode for referenced schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        String typeName = getValidName(extractReferenceType(schema.get$ref()), true);
        Schema<?> refSchema = GeneratorMetaData.getInstance().getOpenAPI().getComponents().getSchemas().get(typeName);
        typeName = TypeGeneratorUtils.getNullableType(refSchema, typeName);
        return createSimpleNameReferenceNode(createIdentifierToken(typeName,
                SINGLE_EMPTY_MINUTIAE, SINGLE_WS_MINUTIAE));
    }
}
