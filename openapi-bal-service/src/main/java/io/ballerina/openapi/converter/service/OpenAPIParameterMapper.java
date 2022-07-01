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
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.converter.Constants;
import io.ballerina.openapi.converter.diagnostic.DiagnosticMessages;
import io.ballerina.openapi.converter.diagnostic.IncompatibleResourceDiagnostic;
import io.ballerina.openapi.converter.diagnostic.OpenAPIConverterDiagnostic;
import io.ballerina.openapi.converter.utils.ConverterCommonUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.converter.Constants.HTTP_REQUEST;
import static io.ballerina.openapi.converter.Constants.WILD_CARD_CONTENT_KEY;
import static io.ballerina.openapi.converter.Constants.WILD_CARD_SUMMARY;
import static io.ballerina.openapi.converter.utils.ConverterCommonUtils.extractCustomMediaType;

/**
 * OpenAPIParameterMapper provides functionality for converting ballerina parameter to OAS parameter model.
 */
public class OpenAPIParameterMapper {
    private final FunctionDefinitionNode functionDefinitionNode;
    private final OperationAdaptor operationAdaptor;
    private final Map<String, String> apidocs;
    private final List<OpenAPIConverterDiagnostic> errors = new ArrayList<>();
    private final Components components;
    private final SemanticModel semanticModel;

    public List<OpenAPIConverterDiagnostic> getErrors() {
        return errors;
    }

    public OpenAPIParameterMapper(FunctionDefinitionNode functionDefinitionNode,
                                  OperationAdaptor operationAdaptor, Map<String, String> apidocs,
                                  Components components, SemanticModel semanticModel) {

        this.functionDefinitionNode = functionDefinitionNode;
        this.operationAdaptor = operationAdaptor;
        this.apidocs = apidocs;
        this.components = components;
        this.semanticModel = semanticModel;
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
            OpenAPIQueryParameterMapper queryParameterMapper = new OpenAPIQueryParameterMapper(apidocs, components,
                    semanticModel);
            if (parameterNode.kind() == SyntaxKind.REQUIRED_PARAM) {
                RequiredParameterNode requiredParameterNode = (RequiredParameterNode) parameterNode;
                // Handle query parameter
                if (requiredParameterNode.typeName().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
                    QualifiedNameReferenceNode referenceNode =
                            (QualifiedNameReferenceNode) requiredParameterNode.typeName();
                    String typeName = (referenceNode).modulePrefix().text() + ":" + (referenceNode).identifier().text();
                    if (typeName.equals(HTTP_REQUEST) && (!Constants.GET.toLowerCase(Locale.ENGLISH).equalsIgnoreCase(
                            operationAdaptor.getHttpOperation()))) {
                        RequestBody requestBody = new RequestBody();
                        MediaType mediaType = new MediaType();
                        mediaType.setSchema(new Schema<>().description(WILD_CARD_SUMMARY));
                        requestBody.setContent(new Content().addMediaType(WILD_CARD_CONTENT_KEY, mediaType));
                        operationAdaptor.getOperation().setRequestBody(requestBody);
                    }
                }
                if (requiredParameterNode.typeName().kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE &&
                        requiredParameterNode.annotations().isEmpty()) {
                    parameters.add(queryParameterMapper.createQueryParameter(requiredParameterNode));
                }
                // Handle header, payload parameter
                if (requiredParameterNode.typeName() instanceof TypeDescriptorNode &&
                        !requiredParameterNode.annotations().isEmpty()) {
                    handleAnnotationParameters(components, semanticModel, parameters, requiredParameterNode);
                }
            } else if (parameterNode.kind() == SyntaxKind.DEFAULTABLE_PARAM) {
                DefaultableParameterNode defaultableParameterNode = (DefaultableParameterNode) parameterNode;
                // Handle header parameter
                if (defaultableParameterNode.typeName() instanceof TypeDescriptorNode &&
                        !defaultableParameterNode.annotations().isEmpty()) {
                    parameters.addAll(handleDefaultableAnnotationParameters(defaultableParameterNode));
                } else {
                    parameters.add(queryParameterMapper.createQueryParameter(defaultableParameterNode));
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
                if (pathParam.typeDescriptor().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    SimpleNameReferenceNode queryNode = (SimpleNameReferenceNode) pathParam.typeDescriptor();
                    OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components);
                    TypeSymbol typeSymbol = (TypeSymbol) semanticModel.symbol(queryNode).orElseThrow();
                    componentMapper.createComponentSchema(components.getSchemas(), typeSymbol);
                    Schema schema = new Schema();
                    schema.set$ref(ConverterCommonUtils.unescapeIdentifier(queryNode.name().text().trim()));
                    pathParameterOAS.setSchema(schema);
                } else {
                    pathParameterOAS.schema(ConverterCommonUtils.getOpenApiSchema(
                            pathParam.typeDescriptor().toString().trim()));
                }

                pathParameterOAS.setName(ConverterCommonUtils.unescapeIdentifier(pathParam.paramName().text()));

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
                OpenAPIHeaderMapper openAPIHeaderMapper = new OpenAPIHeaderMapper(apidocs);
                parameters.addAll(openAPIHeaderMapper.setHeaderParameter(requiredParameterNode));
            } else if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_PAYLOAD) &&
                    (!Constants.GET.toLowerCase(Locale.ENGLISH).equalsIgnoreCase(
                            operationAdaptor.getHttpOperation()))) {
                Map<String, Schema> schema = components.getSchemas();
                // Handle request payload.
                Optional<String> customMediaType = extractCustomMediaType(functionDefinitionNode);
                OpenAPIRequestBodyMapper openAPIRequestBodyMapper = customMediaType.map(
                        value -> new OpenAPIRequestBodyMapper(components,
                        operationAdaptor, semanticModel, value)).orElse(new OpenAPIRequestBodyMapper(components,
                        operationAdaptor, semanticModel));
                openAPIRequestBodyMapper.handlePayloadAnnotation(requiredParameterNode, schema, annotation, apidocs);
                errors.addAll(openAPIRequestBodyMapper.getDiagnostics());
            } else if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_PAYLOAD) &&
                    (Constants.GET.toLowerCase(Locale.ENGLISH).equalsIgnoreCase(operationAdaptor.getHttpOperation()))) {
                DiagnosticMessages errorMessage = DiagnosticMessages.OAS_CONVERTOR_113;
                IncompatibleResourceDiagnostic error = new IncompatibleResourceDiagnostic(errorMessage,
                        annotation.location());
                errors.add(error);
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
                OpenAPIHeaderMapper openAPIHeaderMapper = new OpenAPIHeaderMapper(apidocs);
                parameters = openAPIHeaderMapper.setHeaderParameter(defaultableParameterNode);
            }
        }
        return parameters;
    }
}
