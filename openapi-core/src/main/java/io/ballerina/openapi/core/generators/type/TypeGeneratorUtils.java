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

package io.ballerina.openapi.core.generators.type;

import io.ballerina.compiler.syntax.tree.NameReferenceNode;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.type.generators.AllOfRecordTypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.AnyDataTypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.ArrayTypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.PrimitiveTypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.RecordTypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.ReferencedTypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.TypeGenerator;
import io.ballerina.openapi.core.generators.type.generators.UnionTypeGenerator;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;

/**
 * Contains util functions needed for schema generation.
 *
 * @since 1.3.0
 */
public class TypeGeneratorUtils {
    public static final List<String> PRIMITIVE_TYPE_LIST =
            Collections.unmodifiableList(Arrays.asList(
                    GeneratorConstants.INTEGER, GeneratorConstants.NUMBER,
                    GeneratorConstants.STRING, GeneratorConstants.BOOLEAN));

    /**
     * Get SchemaType object relevant to the schema given.
     *
     * @param schemaValue Schema object
     * @param typeName    parameter name
     * @return Relevant SchemaType object
     */
    public static TypeGenerator getTypeGenerator(Schema<?> schemaValue, String typeName, String parentName,
                                                 boolean ignoreNullableFlag,
                                                 HashMap<String, TypeDefinitionNode> subTypesMap,
                                                 HashMap<String, NameReferenceNode> pregeneratedTypeMap) {
        if (schemaValue.get$ref() != null) {
            return new ReferencedTypeGenerator(schemaValue, typeName, ignoreNullableFlag,
                    subTypesMap, pregeneratedTypeMap);
        } else if (GeneratorUtils.isComposedSchema(schemaValue)) {
            if (schemaValue.getAllOf() != null) {
                return new AllOfRecordTypeGenerator(schemaValue, typeName, ignoreNullableFlag,
                        subTypesMap, pregeneratedTypeMap);
            } else {
                return new UnionTypeGenerator(schemaValue, typeName, ignoreNullableFlag,
                        subTypesMap, pregeneratedTypeMap);
            }
        } else if ((GeneratorUtils.getOpenAPIType(schemaValue) != null &&
                GeneratorUtils.getOpenAPIType(schemaValue).equals(GeneratorConstants.OBJECT)) ||
                GeneratorUtils.isObjectSchema(schemaValue) || schemaValue.getProperties() != null ||
                GeneratorUtils.isMapSchema(schemaValue)) {
            return new RecordTypeGenerator(schemaValue, typeName, ignoreNullableFlag,
                    subTypesMap, pregeneratedTypeMap);
        } else if (GeneratorUtils.isArraySchema(schemaValue)) {
            return new ArrayTypeGenerator(schemaValue, typeName, ignoreNullableFlag, parentName, subTypesMap,
                    pregeneratedTypeMap);
        } else if (GeneratorUtils.getOpenAPIType(schemaValue) != null &&
                PRIMITIVE_TYPE_LIST.contains(GeneratorUtils.getOpenAPIType(schemaValue))) {
            return new PrimitiveTypeGenerator(schemaValue, typeName, ignoreNullableFlag,
                    subTypesMap, pregeneratedTypeMap);
        } else { // when schemaValue.type == null
            return new AnyDataTypeGenerator(schemaValue, typeName, ignoreNullableFlag, subTypesMap, pregeneratedTypeMap);
        }
    }

    /**
     * Generate proper type name considering the nullable configurations.
     * Scenario 1 : schema.getNullable() != null && schema.getNullable() == true && nullable == true -> string?
     * Scenario 2 : schema.getNullable() != null && schema.getNullable() == true && nullable == false -> string?
     * Scenario 3 : schema.getNullable() != null && schema.getNullable() == false && nullable == false -> string
     * Scenario 4 : schema.getNullable() != null && schema.getNullable() == false && nullable == true -> string
     * Scenario 5 : schema.getNullable() == null && nullable == true -> string?
     * Scenario 6 : schema.getNullable() == null && nullable == false -> string
     *
     * @param schema           Schema of the property
     * @param originalTypeDesc Type name
     * @return Final type of the field
     */
    public static TypeDescriptorNode getNullableType(Schema schema, TypeDescriptorNode originalTypeDesc,
                                                     boolean ignoreNullableFlag) {
        TypeDescriptorNode nillableType = originalTypeDesc;
        boolean nullable = GeneratorMetaData.getInstance().isNullable();
        if (schema.getNullable() != null) {
            if (schema.getNullable()) {
                nillableType = createOptionalTypeDescriptorNode(originalTypeDesc, createToken(QUESTION_MARK_TOKEN));
            }
        } else if (schema.getTypes() != null && schema.getTypes().contains(GeneratorConstants.NULL)) {
            nillableType = createOptionalTypeDescriptorNode(originalTypeDesc, createToken(QUESTION_MARK_TOKEN));
        } else if (!ignoreNullableFlag && nullable) {
            nillableType = createOptionalTypeDescriptorNode(originalTypeDesc, createToken(QUESTION_MARK_TOKEN));
        }
        return nillableType;
    }
}
