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
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;

import static io.ballerina.compiler.syntax.tree.NodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.openapi.generators.GeneratorConstants.MAX_ARRAY_LENGTH;
import static io.ballerina.openapi.generators.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.generators.GeneratorConstants.SQUARE_BRACKETS;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for array schemas.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *     Pets:
 *       type: array
 *       items:
 *         $ref: "#/components/schemas/Pet"
 *  </pre>
 * Generated Ballerina type for the schema `Pet` :
 * <pre>
 *      public type Pets Pet[];
 * </pre>
 *
 * @since 2.0.0
 */
public class ArrayTypeGenerator extends TypeGenerator {

    public ArrayTypeGenerator(Schema schema) {
        super(schema);
    }

    /**
     * Generate TypeDescriptorNode for array type schemas. If array type is not given, type will be `AnyData`
     * public type StringArray string[];
     */
    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        assert schema instanceof ArraySchema;
        ArraySchema arraySchema = (ArraySchema) schema;
        TypeGenerator typeGenerator = TypeGeneratorUtils.getTypeGenerator(arraySchema.getItems());
        String arrayType = typeGenerator.generateTypeDescriptorNode().toString();
        if (typeGenerator instanceof UnionTypeGenerator) {
            arrayType = OPEN_PAREN_TOKEN.stringValue() + arrayType + CLOSE_PAREN_TOKEN.stringValue();
        }
        if (arrayType.endsWith(NILLABLE)) {
            arrayType = arrayType.substring(0, arrayType.length() - 1);
        }
        String arrayBrackets = SQUARE_BRACKETS;
        if (arraySchema.getMaxItems() != null) {
            if (arraySchema.getMaxItems() <= MAX_ARRAY_LENGTH) {
                arrayBrackets = OPEN_BRACKET_TOKEN.stringValue() + arraySchema.getMaxItems() +
                        CLOSE_BRACKET_TOKEN.stringValue();
            } else {
                throw new BallerinaOpenApiException("Maximum item count defined in the definition exceeds the " +
                        "maximum ballerina array length.");
            }
        }
        arrayType = TypeGeneratorUtils.getNullableType(arraySchema, arrayType + arrayBrackets);
        return createSimpleNameReferenceNode(createIdentifierToken(arrayType));
    }
}
