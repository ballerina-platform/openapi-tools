/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.openapi.converter.service;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.SyntaxKind.BOOLEAN_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.LIST_CONSTRUCTOR;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.MAPPING_CONSTRUCTOR;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NIL_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NUMERIC_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPTIONAL_TYPE_DESC;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.getAnnotationNodesFromServiceNode;

/**
 * This class processes mapping query parameters in between Ballerina and OAS.
 */
public class OpenAPIQueryParameterMapper {
    private final Map<String, String> apidocs;

    public OpenAPIQueryParameterMapper(Map<String, String> apidocs) {
        this.apidocs = apidocs;
    }

    /**
     * Handle function query parameters.
     */
    public Parameter createQueryParameter(RequiredParameterNode queryParam) {
        String queryParamName = queryParam.paramName().get().text();
        boolean isQuery =
                !queryParam.paramName().get().text().equals(Constants.PATH) && queryParam.annotations().isEmpty();
        if (queryParam.typeName() instanceof BuiltinSimpleNameReferenceNode && isQuery) {
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setName(queryParamName);
            Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(queryParam.typeName().toString().trim());
            queryParameter.setSchema(openApiSchema);
            queryParameter.setRequired(true);
            if (!apidocs.isEmpty() && queryParam.paramName().isPresent() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName.trim()));
            }
            return queryParameter;
        } else if (queryParam.typeName().kind() == OPTIONAL_TYPE_DESC && isQuery) {
            // Handle optional query parameter
            NodeList<AnnotationNode> annotations = getAnnotationNodesFromServiceNode(queryParam);
            String isOptional = Constants.TRUE;
            if (!annotations.isEmpty()) {
                Optional<String> values = ConverterCommonUtils.extractServiceAnnotationDetails(annotations,
                        "http:ServiceConfig", "treatNilableAsOptional");
                if (values.isPresent()) {
                    isOptional = values.get();
                }
            }
            return setOptionalQueryParameter(queryParamName, ((OptionalTypeDescriptorNode) queryParam.typeName()),
                    isOptional);
        } else if (queryParam.typeName() instanceof ArrayTypeDescriptorNode && isQuery) {
            // Handle required array type query parameter
            ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) queryParam.typeName();
            return handleArrayTypeQueryParameter(queryParamName, arrayNode);
        } else {
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setName(queryParamName);
            queryParameter.setSchema(new ObjectSchema());
            if (!apidocs.isEmpty() && queryParam.paramName().isPresent() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName.trim()));
            }
            return queryParameter;
        }
    }

    /**
     * Handle function query parameters defualt.
     */
    public Parameter createQueryParameter(DefaultableParameterNode defaultableQueryParam) {

        String queryParamName = defaultableQueryParam.paramName().get().text();
        boolean isQuery = !defaultableQueryParam.paramName().get().text().equals(Constants.PATH) &&
                defaultableQueryParam.annotations().isEmpty();

        QueryParameter queryParameter = new QueryParameter();
        if (defaultableQueryParam.typeName() instanceof BuiltinSimpleNameReferenceNode && isQuery) {
            queryParameter.setName(queryParamName);
            Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(
                    defaultableQueryParam.typeName().toString().trim());
            queryParameter.setSchema(openApiSchema);
            if (!apidocs.isEmpty() && defaultableQueryParam.paramName().isPresent() &&
                    apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName.trim()));
            }
        } else if (defaultableQueryParam.typeName().kind() == OPTIONAL_TYPE_DESC && isQuery) {
            // Handle optional query parameter
            queryParameter = setOptionalQueryParameter(queryParamName,
                    ((OptionalTypeDescriptorNode) defaultableQueryParam.typeName()),
                    Constants.TRUE);
        } else if (defaultableQueryParam.typeName() instanceof ArrayTypeDescriptorNode && isQuery) {
            // Handle required array type query parameter
            ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) defaultableQueryParam.typeName();
            queryParameter = handleArrayTypeQueryParameter(queryParamName, arrayNode);
        } else {
            queryParameter.setName(queryParamName);
            queryParameter.setSchema(new ObjectSchema());
            if (!apidocs.isEmpty() && defaultableQueryParam.paramName().isPresent() &&
                    apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName.trim()));
            }
        }
        // STRING_LITERAL, NUMERIC_LITERAL(int), NUMERIC_LITERAL(decimal), NUMERIC_LITERAL(float), BOOLEAN_LITERAL,
        // LIST_CONSTRUCTER, NIL_LITERAL, MAPPING_CONSTRUCTOR

        SyntaxKind[] invalidExpressionKind = {STRING_LITERAL, NUMERIC_LITERAL, BOOLEAN_LITERAL, LIST_CONSTRUCTOR,
                NIL_LITERAL, MAPPING_CONSTRUCTOR};
        if (Arrays.stream(invalidExpressionKind).anyMatch(syntaxKind -> syntaxKind ==
                defaultableQueryParam.expression().kind())) {
            String defaultValue = defaultableQueryParam.expression().toString().replaceAll("\"", "");
            Schema schema = queryParameter.getSchema();
            schema.setDefault(defaultValue);
            queryParameter.setSchema(schema);
        }
        return queryParameter;
    }

    /**
     * Handle array type query parameter.
     */
    private QueryParameter handleArrayTypeQueryParameter(String queryParamName, ArrayTypeDescriptorNode arrayNode) {
        QueryParameter queryParameter = new QueryParameter();
        ArraySchema arraySchema = new ArraySchema();
        queryParameter.setName(queryParamName);
        TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
        Schema itemSchema = ConverterCommonUtils.getOpenApiSchema(itemTypeNode.toString().trim());
        arraySchema.setItems(itemSchema);
        queryParameter.schema(arraySchema);
        queryParameter.setRequired(true);
        if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
            queryParameter.setDescription(apidocs.get(queryParamName));
        }
        return queryParameter;
    }

    /**
     * Handle optional query parameter.
     */
    private QueryParameter setOptionalQueryParameter(String queryParamName, OptionalTypeDescriptorNode typeNode,
                                                String isOptional) {
        QueryParameter queryParameter = new QueryParameter();
        if (isOptional.equals(Constants.FALSE)) {
            queryParameter.setRequired(true);
        }
        queryParameter.setName(queryParamName);
        Node node = typeNode.typeDescriptor();
        if (node instanceof ArrayTypeDescriptorNode) {
            ArraySchema arraySchema = new ArraySchema();
            arraySchema.setNullable(true);
            ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) node;
            TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
            Schema itemSchema = ConverterCommonUtils.getOpenApiSchema(itemTypeNode.toString().trim());
            arraySchema.setItems(itemSchema);
            queryParameter.schema(arraySchema);
            queryParameter.setName(queryParamName);
            if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName));
            }
            return queryParameter;
        } else {
//            String type = ConverterCommonUtils.convertBallerinaTypeToOpenAPIType(node.toString().trim());
            Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(node.toString().trim());
            openApiSchema.setNullable(true);
            queryParameter.setSchema(openApiSchema);
            if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName));
            }
            return queryParameter;
        }
    }

}
