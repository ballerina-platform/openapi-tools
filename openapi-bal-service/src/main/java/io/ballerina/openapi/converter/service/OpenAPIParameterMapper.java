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
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;

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
                createQueryParameter(parameters, requiredParameterNode);
                // Handle header, payload parameter
                if (requiredParameterNode.typeName() instanceof TypeDescriptorNode &&
                        !requiredParameterNode.annotations().isEmpty()) {
                    handleAnnotationParameters(components, semanticModel, parameters, requiredParameterNode);
                }
            }
            //TODO: query other scenarios
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
                ResourcePathParameterNode pathParam = (ResourcePathParameterNode) param;
                Parameter parameter = buildParameter(Constants.PATH, pathParam);
                parameter.setName(pathParam.paramName().text());
                // Check the parameter has doc
                if (!apidocs.isEmpty() && apidocs.containsKey(pathParam.paramName().text().trim())) {
                    parameter.setDescription(apidocs.get(pathParam.paramName().text().trim()));
                }
                // Set param description
                parameter.setRequired(true);
                parameters.add(parameter);
            }
        }
    }

    /**
     * Handle function query parameters.
     */
    private void createQueryParameter(List<Parameter> parameters, RequiredParameterNode queryParam) {

        String queryParamName = queryParam.paramName().get().text();
        if (queryParam.typeName() instanceof BuiltinSimpleNameReferenceNode &&
                !queryParam.paramName().orElseThrow().text().equals(Constants.PATH) &&
                queryParam.annotations().isEmpty()) {
            Parameter parameter = buildParameter(Constants.QUERY, queryParam);
            parameter.setRequired(true);
            // Handle required query parameter
            if (!apidocs.isEmpty() && queryParam.paramName().isPresent() && apidocs.containsKey(queryParamName)) {
                parameter.setDescription(apidocs.get(queryParamName.trim()));
            }
            parameters.add(parameter);
        } else if (queryParam.typeName() instanceof OptionalTypeDescriptorNode &&
                !queryParam.paramName().orElseThrow().text().equals(Constants.PATH) &&
                queryParam.annotations().isEmpty()) {
            // Handle optional query parameter
            setOptionalQueryParameter(parameters, queryParamName, queryParam);
        } else if (queryParam.typeName() instanceof ArrayTypeDescriptorNode &&
                !queryParam.paramName().orElseThrow().text().equals(Constants.PATH) &&
                queryParam.annotations().isEmpty()) {
            // Handle required array type query parameter
            handleArrayTypeQueryParameter(parameters, queryParam);
        }
    }

    /**
     * Handle array type query parameter.
     */
    private void handleArrayTypeQueryParameter(List<Parameter> parameters, RequiredParameterNode queryParam) {

        ArraySchema arraySchema = new ArraySchema();
        String queryParamName = queryParam.paramName().get().text();
        ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) queryParam.typeName();
        if (!(arrayNode.memberTypeDesc() instanceof ArrayTypeDescriptorNode)) {
            TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
            if (!itemTypeNode.kind().equals(SyntaxKind.TYPE_REFERENCE)) {
                Schema itemSchema = ConverterCommonUtils.getOpenApiSchema(itemTypeNode.toString().trim());
                arraySchema.setItems(itemSchema);
                QueryParameter queryParameter = new QueryParameter();
                queryParameter.schema(arraySchema);
                queryParameter.setName(queryParamName);
                queryParameter.setRequired(true);
                if (!apidocs.isEmpty() && queryParam.paramName().isPresent() && apidocs.containsKey(queryParamName)) {
                    queryParameter.setDescription(apidocs.get(queryParamName.trim()));
                }
                parameters.add(queryParameter);
            }
        }
    }

    /**
     * Handle optional query parameter.
     */
    private void setOptionalQueryParameter(List<Parameter> parameters, String queryParamName,
                                           RequiredParameterNode queryParam) {
        Node node = ((OptionalTypeDescriptorNode) queryParam.typeName()).typeDescriptor();
        if (node instanceof ArrayTypeDescriptorNode) {
            ArraySchema arraySchema = new ArraySchema();
            ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) node;
            TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
            if (!itemTypeNode.kind().equals(SyntaxKind.TYPE_REFERENCE)) {
                Schema itemSchema = ConverterCommonUtils.getOpenApiSchema(itemTypeNode.toString().trim());
                arraySchema.setItems(itemSchema);
                QueryParameter queryParameter = new QueryParameter();
                queryParameter.schema(arraySchema);
                queryParameter.setName(queryParamName);
                queryParameter.setRequired(false);
                if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                    queryParameter.setDescription(apidocs.get(queryParamName));
                }
                parameters.add(queryParameter);
            }
        } else {
            Parameter parameter = buildParameter(Constants.QUERY, queryParam);
            parameter.setRequired(false);
            if (!apidocs.isEmpty() && apidocs.containsKey(queryParamName)) {
                parameter.setDescription(apidocs.get(queryParamName));
            }
            parameters.add(parameter);
        }
    }

    /**
     * Handle header parameters in ballerina data type.
     *
     * @param parameters    -  OAS Parameters
     * @param queryParam    -  Resource function parameter list
     */
    private void setHeaderParameter(List<Parameter> parameters, RequiredParameterNode queryParam) {
        //Handle with string current header a support with only string and string[]
        String headerName = queryParam.paramName().get().text().replaceAll("\\\\", "");
        HeaderParameter headerParameter = new HeaderParameter();
        headerParameter.setRequired(true);
        if (!queryParam.annotations().isEmpty()) {
            AnnotationNode annotationNode = queryParam.annotations().get(0);
            headerName = getHeaderName(headerName, annotationNode);
        }
        if (queryParam.typeName() instanceof ArrayTypeDescriptorNode)  {
            ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) queryParam.typeName();
            if (arrayNode.memberTypeDesc().kind().equals(SyntaxKind.STRING_TYPE_DESC)) {
                TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
                Schema itemSchema = ConverterCommonUtils.getOpenApiSchema(itemTypeNode.toString().trim());
                ArraySchema arraySchema = new ArraySchema();
                arraySchema.setItems(itemSchema);
                headerParameter.schema(arraySchema);
                headerParameter.setName(headerName.replaceAll("\\\\", ""));
                parameters.add(headerParameter);
            }
        } else {
            headerParameter.schema(new StringSchema());
            headerParameter.setName(headerName.replaceAll("\\\\", ""));
            parameters.add(headerParameter);
        }
    }

    /*Extract header name from header annotation value */
    private String getHeaderName(String headerName, AnnotationNode annotationNode) {
        if (annotationNode.annotValue().isPresent()) {
            MappingConstructorExpressionNode fieldNode = annotationNode.annotValue().get();
            SeparatedNodeList<MappingFieldNode> fields = fieldNode.fields();
            for (MappingFieldNode field: fields) {
                SpecificFieldNode sField = (SpecificFieldNode) field;
                if (sField.fieldName().toString().trim().equals("name") && sField.valueExpr().isPresent()) {
                    headerName = sField.valueExpr().get().toString().trim().replaceAll("\"", "");
                }
            }
        }
        return headerName;
    }

    /**
     * Builds an OpenApi {@link io.swagger.models.parameters.Parameter} for provided parameter location.
     *
     * @param in              location of the parameter in the request definition
     * @param paramAttributes parameter attributes for the operation
     * @return OpenApi {@link io.swagger.models.parameters.Parameter} for parameter location {@code in}
     */
    private Parameter buildParameter(String in, Node paramAttributes) {
        Parameter param = new Parameter();
        String type;
        switch (in) {
            case Constants.BODY:
                // TODO : support for inline and other types of schemas
                break;
            case Constants.QUERY:
                QueryParameter qParam = new QueryParameter();
                RequiredParameterNode queryParam = (RequiredParameterNode) paramAttributes;
                qParam.setName(queryParam.paramName().get().text());
                Schema openApiSchema;
                if (queryParam.typeName().kind() == SyntaxKind.OPTIONAL_TYPE_DESC) {
                    OptionalTypeDescriptorNode optional = (OptionalTypeDescriptorNode) queryParam.typeName();
                    type = ConverterCommonUtils.convertBallerinaTypeToOpenAPIType(
                                    optional.typeDescriptor().toString().trim());
                    openApiSchema = ConverterCommonUtils.getOpenApiSchema(type);
                    openApiSchema.setNullable(true);
                } else {
                    type = ConverterCommonUtils.convertBallerinaTypeToOpenAPIType(
                            queryParam.typeName().toString().trim());
                    openApiSchema = ConverterCommonUtils.getOpenApiSchema(type);
                }
                qParam.schema(openApiSchema);
                param = qParam;
                break;
            case Constants.COOKIE:
                param = new CookieParameter();
                break;
            case Constants.FORM:
                break;
            case Constants.PATH:
            default:
                PathParameter pParam = new PathParameter();
                ResourcePathParameterNode pathParam = (ResourcePathParameterNode) paramAttributes;
                type = ConverterCommonUtils
                        .convertBallerinaTypeToOpenAPIType(pathParam.typeDescriptor().toString().trim());
                pParam.schema(ConverterCommonUtils.getOpenApiSchema(type));
                pParam.setName(pathParam.paramName().text());
                param = pParam;
        }

        return param;
    }

    /**
     * This function for handle the payload and header parameters with annotation @http:Payload, @http:Header.
     */
    private void handleAnnotationParameters(Components components, SemanticModel semanticModel,
                                            List<Parameter> parameters, RequiredParameterNode requiredParameterNode) {

        NodeList<AnnotationNode> annotations = requiredParameterNode.annotations();
        for (AnnotationNode annotation: annotations) {
            if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_HEADER)) {
                // Handle headers.
                setHeaderParameter(parameters, requiredParameterNode);
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
}
