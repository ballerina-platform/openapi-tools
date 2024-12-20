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
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.type.TypeGeneratorUtils;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeDefinitionNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;

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

    public ReferencedTypeGenerator(Schema schema, String typeName, boolean ignoreNullableFlag,
                                   HashMap<String, TypeDefinitionNode> subTypesMap,
                                   HashMap<String, NameReferenceNode> pregeneratedTypeMap) {
        super(schema, typeName, ignoreNullableFlag, subTypesMap, pregeneratedTypeMap);
    }

    /**
     * Generate TypeDescriptorNode for referenced schemas.
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws OASTypeGenException {
        String extractName;
        try {
            extractName = GeneratorUtils.extractReferenceType(schema.get$ref());
        } catch (BallerinaOpenApiException e) {
            throw new OASTypeGenException(e.getMessage());
        }
        String typeName = GeneratorUtils.escapeIdentifier(extractName);
        Schema<?> refSchema = GeneratorMetaData.getInstance().getOpenAPI().getComponents().getSchemas().get(typeName);
        refSchema = refSchema == null ?
                GeneratorMetaData.getInstance().getOpenAPI().getComponents().getSchemas().get(extractName) : refSchema;
        SimpleNameReferenceNode nameReferenceNode = createSimpleNameReferenceNode(createIdentifierToken(typeName));
        TypeGenerator reffredTypeGenerator = TypeGeneratorUtils.getTypeGenerator(refSchema, extractName,
                null,  ignoreNullableFlag, subTypesMap, pregeneratedTypeMap);
        if (!pregeneratedTypeMap.containsKey(typeName)) {
            pregeneratedTypeMap.put(typeName, createSimpleNameReferenceNode(createIdentifierToken(typeName)));
            TypeDescriptorNode typeDescriptorNode = reffredTypeGenerator.generateTypeDescriptorNode();
            subTypesMap.put(typeName, createTypeDefinitionNode(null,
                    createToken(PUBLIC_KEYWORD),
                    createToken(TYPE_KEYWORD),
                    createIdentifierToken(typeName),
                    typeDescriptorNode,
                    createToken(SEMICOLON_TOKEN)));
        }
        return TypeGeneratorUtils.getNullableType(refSchema, nameReferenceNode, ignoreNullableFlag);
    }
}
