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

package io.ballerina.openapi.generators.schema.schematypes;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.media.Schema;

import java.util.List;

/**
 * Abstract class for schema types.
 */
public abstract class SchemaType {

    /**
     * Create Type Definition Node for a given OpenAPI schema.
     *
     * @param schemaValue     OpenAPI schema
     * @param typeName        IdentifierToken of the name of the type
     * @param schemaDoc       Documentation of the type
     * @param typeAnnotations Annotations related to the type. Currently, only includes `Deprecated` annotation
     * @return {@link TypeDefinitionNode}
     * @throws BallerinaOpenApiException when unsupported schema type is found
     */
    public abstract TypeDefinitionNode generateTypeDefinitionNode(Schema<Object> schemaValue, IdentifierToken typeName,
                                                                  List<Node> schemaDoc,
                                                                  List<AnnotationNode> typeAnnotations)
            throws BallerinaOpenApiException;

    /**
     * Create Type Descriptor Node for a given OpenAPI schema.
     *
     * @param schema OpenAPI schema
     * @return {@link TypeDescriptorNode}
     * @throws BallerinaOpenApiException when unsupported schema type is found
     */
    public abstract TypeDescriptorNode generateTypeDescriptorNode(Schema schema) throws BallerinaOpenApiException;
}
