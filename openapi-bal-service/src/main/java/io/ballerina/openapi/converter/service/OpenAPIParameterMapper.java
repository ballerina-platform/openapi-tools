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

package io.ballerina.openapi.converter.service;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
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
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * OpenAPIParameterMapper provides functionality for converting ballerina parameter to OAS parameter model.
 */
public class OpenAPIParameterMapper {
    private final FunctionDefinitionNode functionDefinitionNode;
    private final OperationAdaptor operationAdaptor;
    private final Map<String, String> apidocs;

    public OpenAPIParameterMapper(FunctionDefinitionNode functionDefinitionNode,
                                  OperationAdaptor operationAdaptor, Map<String, String> apidocs) {

        this.functionDefinitionNode = functionDefinitionNode;
        this.operationAdaptor = operationAdaptor;
        this.apidocs = apidocs;
    }

    /**
     * Create {@code Parameters} model for openAPI operation.
     */
    public void getResourceInputs(Components components, SemanticModel semanticModel) {
        List<Parameter> parameters = new LinkedList<>();
        //Set path parameters
        NodeList<Node> pathParams = functionDefinitionNode.relativeResourcePath();
        createPathParameters(parameters, pathParams);
        // Set query parameters, headers and requestBody
        FunctionSignatureNode functionSignature = functionDefinitionNode.functionSignature();
        SeparatedNodeList<ParameterNode> parameterList = functionSignature.parameters();
        for (ParameterNode parameterNode : parameterList) {
            if (parameterNode instanceof RequiredParameterNode) {
                RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                // Handle query parameter
                parameters.add(createQueryParameter(requiredParameterNode));
                // Handle header, payload parameter
                if (requiredParameterNode.typeName() instanceof TypeDescriptorNode &&
                        !requiredParameterNode.annotations().isEmpty()) {
                    handleAnnotationParameters(components, semanticModel, parameters, requiredParameterNode);
                }
            } else if (parameterNode instanceof DefaultableParameterNode) {
                DefaultableParameterNode defaultableParameterNode = (DefaultableParameterNode) parameterNode;
                // Handle header parameter
                if (defaultableParameterNode.typeName() instanceof TypeDescriptorNode &&
                        !defaultableParameterNode.annotations().isEmpty()) {
                    parameters.addAll(handleDefaultableAnnotationParameters(defaultableParameterNode));
                }
            }
        }
        if (parameters.isEmpty()) {
            operationAdaptor.getOperation().setParameters(null);
        } else {
            operationAdaptor.getOperation().setParameters(parameters);
        }
    }

    /**
     * Map path parameter data to OAS path parameter.
     */
    private void createPathParameters(List<Parameter> parameters, NodeList<Node> pathParams) {

        for (Node param: pathParams) {
            if (param instanceof ResourcePathParameterNode) {
                PathParameter pathParameterOAS = new PathParameter();
                ResourcePathParameterNode pathParam = (ResourcePathParameterNode) param;
                String type = ConverterCommonUtils
                        .convertBallerinaTypeToOpenAPIType(pathParam.typeDescriptor().toString().trim());
                pathParameterOAS.schema(ConverterCommonUtils.getOpenApiSchema(type));
                pathParameterOAS.setName(pathParam.paramName().text());

                // Check the parameter has doc
                if (!apidocs.isEmpty() && apidocs.containsKey(pathParam.paramName().text().trim())) {
                    pathParameterOAS.setDescription(apidocs.get(pathParam.paramName().text().trim()));
                }
                // Set param description
                pathParameterOAS.setRequired(true);
                parameters.add(pathParameterOAS);
            }
        }
    }

    /**
     * Handle function query parameters.
     */
    private Parameter createQueryParameter(RequiredParameterNode queryParam) {
        String queryParamName = queryParam.paramName().get().text();
        boolean noAnnotation = queryParam.annotations().isEmpty();
        boolean isPath = queryParam.paramName().get().text().equals(Constants.PATH);
        if (queryParam.typeName() instanceof BuiltinSimpleNameReferenceNode && !isPath && noAnnotation) {
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setName(queryParamName);
            String type = ConverterCommonUtils.convertBallerinaTypeToOpenAPIType(
                    queryParam.typeName().toString().trim());
            Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(type);
            queryParameter.setSchema(openApiSchema);
            queryParameter.setRequired(true);
            if (!apidocs.isEmpty() && queryParam.paramName().isPresent() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName.trim()));
            }
            return queryParameter;
        } else if (queryParam.typeName() instanceof OptionalTypeDescriptorNode && !isPath && noAnnotation) {
            // Handle optional query parameter
            return setOptionalQueryParameter(queryParamName, ((OptionalTypeDescriptorNode) queryParam.typeName()));
        } else if (queryParam.typeName() instanceof ArrayTypeDescriptorNode && !isPath && noAnnotation) {
            // Handle required array type query parameter
            ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) queryParam.typeName();
            return handleArrayTypeQueryParameter(queryParamName, arrayNode);
        } else {
            QueryParameter queryParameter = new QueryParameter();
            queryParameter.setName(queryParamName);
            queryParameter.setSchema(new ObjectSchema());
            return queryParameter;
        }
    }

    /**
     * Handle array type query parameter.
     */
    private Parameter handleArrayTypeQueryParameter(String queryParamName, ArrayTypeDescriptorNode arrayNode) {
        QueryParameter queryParameter = new QueryParameter();
        ArraySchema arraySchema = new ArraySchema();
        queryParameter.setName(queryParamName);
        TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
        Schema itemSchema = ConverterCommonUtils.getOpenApiSchema(itemTypeNode.toString().trim());
        arraySchema.setItems(itemSchema);
        queryParameter.schema(arraySchema);
        queryParameter.setRequired(true);
        return queryParameter;
    }

    /**
     * Handle optional query parameter.
     */
    private Parameter setOptionalQueryParameter(String queryParamName, OptionalTypeDescriptorNode typeNode) {
        QueryParameter queryParameter = new QueryParameter();
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
            queryParameter.setRequired(false);
            if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName));
            }
            return queryParameter;
        } else {
            String type = ConverterCommonUtils.convertBallerinaTypeToOpenAPIType(node.toString().trim());
            Schema openApiSchema = ConverterCommonUtils.getOpenApiSchema(type);
            openApiSchema.setNullable(true);
            queryParameter.setSchema(openApiSchema);
            if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                queryParameter.setDescription(apidocs.get(queryParamName));
            }
            return queryParameter;
        }
    }

    /**
     * This function for handle the payload and header parameters with annotation @http:Payload, @http:Header.
     */
    private void handleAnnotationParameters(Components components,
                                            SemanticModel semanticModel,
                                            List<Parameter> parameters,
                                            RequiredParameterNode requiredParameterNode) {

        NodeList<AnnotationNode> annotations = requiredParameterNode.annotations();
        for (AnnotationNode annotation: annotations) {
            if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_HEADER)) {
                // Handle headers.
                OpenAPIHeaderMapper openAPIHeaderMapper = new OpenAPIHeaderMapper();
                parameters.addAll(openAPIHeaderMapper.setHeaderParameter(requiredParameterNode));
            } else if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_PAYLOAD) &&
                    (!"GET".toLowerCase(Locale.ENGLISH).equalsIgnoreCase(operationAdaptor.getHttpOperation()))) {
                Map<String, Schema> schema = components.getSchemas();
                // Handle request payload.
                OpenAPIRequestBodyMapper openAPIRequestBodyMapper = new OpenAPIRequestBodyMapper(components,
                        operationAdaptor, semanticModel);
                openAPIRequestBodyMapper.handlePayloadAnnotation(requiredParameterNode, schema, annotation, apidocs);
            }
        }
    }

    /**
     * This function for handle the payload and header parameters with annotation @http:Payload, @http:Header.
     */
    private List<Parameter> handleDefaultableAnnotationParameters(DefaultableParameterNode defaultableParameterNode) {
        List<Parameter> parameters = new ArrayList<>();
        NodeList<AnnotationNode> annotations = defaultableParameterNode.annotations();
        for (AnnotationNode annotation: annotations) {
            if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_HEADER)) {
                // Handle headers.
                OpenAPIHeaderMapper openAPIHeaderMapper = new OpenAPIHeaderMapper();
                parameters = openAPIHeaderMapper.setHeaderParameter(defaultableParameterNode);
            }
        }
        return parameters;
    }
}
