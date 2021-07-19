/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.generators.ballerina.service;

import io.ballerina.generators.ballerina.Constants;
import io.ballerina.generators.ballerina.ConverterUtils;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

import java.util.LinkedList;
import java.util.List;

/**
 * OpenAPIParameterMapper provides functionality for converting ballerina parameter to OAS parameter model.
 */
public class OpenAPIParameterMapper {
    private FunctionDefinitionNode functionDefinitionNode;
    private Operation operation;
    private ConverterUtils converterUtils = new ConverterUtils();

    public OpenAPIParameterMapper(FunctionDefinitionNode functionDefinitionNode,
                                  Operation operation) {

        this.functionDefinitionNode = functionDefinitionNode;
        this.operation = operation;
    }

    /**
     * Create {@code Parameters} model for openAPI operation.
     *
     */
    public void createParametersModel() {
        List<Parameter> parameters = new LinkedList<>();

        //Set path parameters
        NodeList<Node> pathParams = functionDefinitionNode.relativeResourcePath();
        for (Node param: pathParams) {
            if (param instanceof ResourcePathParameterNode) {
                ResourcePathParameterNode pathParam = (ResourcePathParameterNode) param;
                io.swagger.v3.oas.models.parameters.Parameter parameter = buildParameter(Constants.PATH, pathParam);
                parameter.setName(pathParam.paramName().text());

                //Set param description
                parameter.setRequired(true);
                parameters.add(parameter);
            }
        }

        // set query parameter
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();
        SeparatedNodeList<ParameterNode> paramExprs = functionSignature.parameters();
        for (ParameterNode expr : paramExprs) {
            if (expr instanceof RequiredParameterNode) {
                RequiredParameterNode queryParam = (RequiredParameterNode) expr;
                if (queryParam.typeName() instanceof BuiltinSimpleNameReferenceNode &&
                        !queryParam.paramName().orElseThrow().text().equals(Constants.PATH) &&
                        ((RequiredParameterNode) expr).annotations().isEmpty()) {
                    io.swagger.v3.oas.models.parameters.Parameter
                            parameter = buildParameter(Constants.QUERY, queryParam);
                    parameter.setRequired(true);
                    parameters.add(parameter);
                } else if (queryParam.typeName() instanceof OptionalTypeDescriptorNode &&
                        !queryParam.paramName().orElseThrow().text().equals(Constants.PATH) &&
                        ((RequiredParameterNode) expr).annotations().isEmpty()) {
                    Node node = ((OptionalTypeDescriptorNode) queryParam.typeName()).typeDescriptor();
                    if (node instanceof ArrayTypeDescriptorNode) {
                        ArraySchema arraySchema = new ArraySchema();
                        ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) node;
                        TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
                        if (!itemTypeNode.kind().equals(SyntaxKind.TYPE_REFERENCE)) {
                            Schema itemSchema = converterUtils.getOpenApiSchema(itemTypeNode.toString().trim());
                            arraySchema.setItems(itemSchema);
                            QueryParameter queryParameter = new QueryParameter();
                            queryParameter.schema(arraySchema);
                            queryParameter.setName(queryParam.paramName().get().text());
                            queryParameter.setRequired(false);
                            parameters.add(queryParameter);
                        }
                    } else {
                        io.swagger.v3.oas.models.parameters.Parameter
                                parameter = buildParameter(Constants.QUERY, queryParam);
                        parameter.setRequired(false);
                        parameters.add(parameter);
                    }
                } else if (queryParam.typeName() instanceof ArrayTypeDescriptorNode &&
                        !queryParam.paramName().orElseThrow().text().equals(Constants.PATH) &&
                        ((RequiredParameterNode) expr).annotations().isEmpty()) {
                    ArraySchema arraySchema = new ArraySchema();
                    ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) queryParam.typeName();
                    if (!(arrayNode.memberTypeDesc() instanceof ArrayTypeDescriptorNode)) {
                        TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
                        if (!itemTypeNode.kind().equals(SyntaxKind.TYPE_REFERENCE)) {
                            Schema itemSchema = converterUtils.getOpenApiSchema(itemTypeNode.toString().trim());
                            arraySchema.setItems(itemSchema);
                            QueryParameter queryParameter = new QueryParameter();
                            queryParameter.schema(arraySchema);
                            queryParameter.setName(queryParam.paramName().get().text());
                            queryParameter.setRequired(true);
                            parameters.add(queryParameter);
                        }
                    }
                } else if (queryParam.typeName() instanceof TypeDescriptorNode && !queryParam.annotations().isEmpty()) {
                    NodeList<AnnotationNode> annotations = queryParam.annotations();
                    for (AnnotationNode annotation: annotations) {
                        if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_HEADER) &&
                                annotation.annotValue().isPresent()) {
                            //Handle with string current header a support with only string and string[]
                            if (queryParam.typeName() instanceof ArrayTypeDescriptorNode)  {
                                ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) queryParam.typeName();
                                if (arrayNode.memberTypeDesc().kind().equals(SyntaxKind.STRING_TYPE_DESC)) {
                                    TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
                                    Schema itemSchema = converterUtils.getOpenApiSchema(itemTypeNode.toString().trim());
                                    ArraySchema arraySchema = new ArraySchema();
                                    arraySchema.setItems(itemSchema);
                                    io.swagger.v3.oas.models.parameters.HeaderParameter headerParameter =
                                            new io.swagger.v3.oas.models.parameters.HeaderParameter();
                                    headerParameter.schema(arraySchema);
                                    headerParameter.setName(queryParam.paramName().get().text().replaceAll("\\\\",
                                            ""));
                                    parameters.add(headerParameter);
                                }
                            } else {
                                io.swagger.v3.oas.models.parameters.Parameter
                                        parameter = buildParameter(Constants.HEADER, queryParam);
                                parameters.add(parameter);
                            }
                        }
                    }
                }
            }
        }
        if (parameters.isEmpty()) {
            operation.setParameters(null);
        } else {
            operation.setParameters(parameters);
        }
    }

    /**
     * Builds an OpenApi {@link io.swagger.models.parameters.Parameter} for provided parameter location.
     *
     * @param in              location of the parameter in the request definition
     * @param paramAttributes parameter attributes for the operation
     * @return OpenApi {@link io.swagger.models.parameters.Parameter} for parameter location {@code in}
     */
    private io.swagger.v3.oas.models.parameters.Parameter buildParameter(String in, Node paramAttributes) {
        io.swagger.v3.oas.models.parameters.Parameter param = null;
        String type;
        switch (in) {
            case Constants.BODY:
                // TODO : support for inline and other types of schemas
                break;
            case Constants.QUERY:
                io.swagger.v3.oas.models.parameters.QueryParameter qParam = new QueryParameter();
                RequiredParameterNode queryParam = (RequiredParameterNode) paramAttributes;
                qParam.setName(queryParam.paramName().get().text());
                type = converterUtils.convertBallerinaTypeToOpenAPIType(queryParam.typeName().toString().trim());
                qParam.schema(converterUtils.getOpenApiSchema(type));
                param = qParam;
                break;
            case Constants.HEADER:
                param = new io.swagger.v3.oas.models.parameters.HeaderParameter();
                RequiredParameterNode header = (RequiredParameterNode) paramAttributes;
                param.schema(new StringSchema());
                param.setName(header.paramName().get().text().replaceAll("\\\\", ""));
                break;
            case Constants.COOKIE:
                param = new CookieParameter();
                break;
            case Constants.FORM:
                param = new io.swagger.v3.oas.models.parameters.Parameter();
                break;
            case Constants.PATH:
            default:
                io.swagger.v3.oas.models.parameters.PathParameter pParam = new PathParameter();
                ResourcePathParameterNode pathParam = (ResourcePathParameterNode) paramAttributes;
                type = converterUtils.convertBallerinaTypeToOpenAPIType(pathParam.typeDescriptor().toString().trim());
                pParam.schema(converterUtils.getOpenApiSchema(type));
                pParam.setName(pathParam.paramName().text());
                param = pParam;
        }

        return param;
    }

}
