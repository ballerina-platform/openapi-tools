/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
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

import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.corenew.typegenerator.GeneratorConstants;
import io.ballerina.openapi.corenew.typegenerator.GeneratorUtils;
import io.ballerina.openapi.corenew.typegenerator.TypeGeneratorUtils;
import io.ballerina.openapi.corenew.typegenerator.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;

/**
 * Generate TypeDefinitionNode and TypeDescriptorNode for schemas with enums.
 * -- ex:
 * Sample OpenAPI :
 * <pre>
 *    components:
 *     schemas:
 *       MeetingType:
 *         type: string
 *         default: live
 *         enum:
 *         - "scheduled"
 *         - "live"
 *         - "upcoming"
 *  </pre>
 * Generated Ballerina type for the schema `MeetingTypes` :
 * <pre>
 *     public type MeetingTypes "scheduled"|"live"|"upcoming";
 * </pre>
 *
 * @since 1.6.0
 */
public class EnumGenerator extends TypeGenerator {

    public EnumGenerator(Schema schema, String typeName) {
        super(schema, typeName);
    }

    @Override
    public TypeDescriptorNode generateTypeDescriptorNode() throws BallerinaOpenApiException {
        List<?> enumList = schema.getEnum();
        boolean isNull = false;
        StringBuilder enumBuilder = new StringBuilder();
        if (TypeGeneratorUtils.PRIMITIVE_TYPE_LIST.contains(GeneratorUtils.getOpenAPIType(schema))) {
            for (Object enumValue : enumList) {
                isNull = enumValue == null;
                if (isNull) {
                    continue;
                }
                if (enumValue instanceof String) {
                    enumBuilder.append("\"").append(enumValue).append("\"").append("|");
                } else {
                    enumBuilder.append(enumValue).append("|");
                }
            }
            if (enumBuilder.length() > 0) {
                enumBuilder.deleteCharAt(enumBuilder.length() - 1);
                String enumString = isNull ? enumBuilder.toString() + GeneratorConstants.NILLABLE : enumBuilder.toString();
                addToTypeListAndRemoveFromTempList(null);
                return NodeParser.parseTypeDescriptor(enumString);
            } else {
                String typeDescriptorName;
                if (GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.containsKey(schema.getType().trim())) {
                    typeDescriptorName = GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.get(
                            GeneratorUtils.getOpenAPIType(schema));
                } else {
                    throw new BallerinaOpenApiException("Unsupported OAS data type `" + schema.getType().trim() + "`");
                }
                if (isNull) {
                    addToTypeListAndRemoveFromTempList(null);
                    return createSimpleNameReferenceNode(createIdentifierToken(typeDescriptorName + GeneratorConstants.NILLABLE));
                } else {
                    TypeDescriptorNode typeDescriptorNode = createSimpleNameReferenceNode(
                            createIdentifierToken(typeDescriptorName));
                    addToTypeListAndRemoveFromTempList(null);
                    return TypeGeneratorUtils.getNullableType(schema, typeDescriptorNode);
                }
            }
        } else {
            throw new BallerinaOpenApiException(String.format("The data type '%s' is not a valid enum type." +
                    "The supported types are string, integer, number and boolean",
                    GeneratorUtils.getOpenAPIType(schema)));
        }
    }
}
