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
package io.ballerina.openapi.generators.service;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.openapi.generators.GeneratorConstants.HTTP;
import static io.ballerina.openapi.generators.GeneratorConstants.STRING;
import static io.ballerina.openapi.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.generators.GeneratorUtils.getQualifiedNameReferenceNode;
import static io.ballerina.openapi.generators.service.ServiceGenerationUtils.escapeIdentifier;

/**
 * This class uses for generating all resource function parameters.
 *
 * @since 2.0.0
 */
public class ParametersGenerator {

    public List<Node> generateResourcesInputs(Map.Entry<PathItem.HttpMethod, Operation> operation)
            throws BallerinaOpenApiException {
        List<Node> params = new ArrayList<>();
        Token comma = createToken(SyntaxKind.COMMA_TOKEN);
        // Handle header and query parameters
        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
            for (Parameter parameter: parameters) {
                if (parameter.getIn().trim().equals("header")) {
                    RequiredParameterNode param = handleHeader(parameter);
                    params.add(param);
                    params.add(comma);
                } else if (parameter.getIn().trim().equals("query")) {
                    // type​ ​ BasicType​ ​ boolean|int|float|decimal|string​ ;
                    //public​ ​ type​ ​​ ()​ |BasicType|BasicType​ [];
                    Node param = createNodeForQueryParam(parameter);
                    params.add(param);
                    params.add(comma);
                }
            }
        }
        // Handle request Body (Payload)
        // type​ ​ CustomRecord​ ​ record {| anydata...; |};
        //public type​ ​ PayloadType​ ​ string|json|xml|byte[]|CustomRecord|CustomRecord[]​ ;
        if (operation.getValue().getRequestBody() != null) {
            RequestBody requestBody = operation.getValue().getRequestBody();
            if (requestBody.getContent() != null) {
                RequestBodyGenerator requestBodyGen = new RequestBodyGenerator();
                List<Node> requestBodyNode = requestBodyGen.createNodeForRequestBody(requestBody);
            }
        }
        if (params.size() > 1) {
            params.remove(params.size() - 1);
        }
        return params;
    }

    /**
     * This function for generating parameter ST node for header.
     * @param parameter
     * @return
     * @throws BallerinaOpenApiException
     */
    private RequiredParameterNode handleHeader(Parameter parameter)throws BallerinaOpenApiException {
        Schema schema = parameter.getSchema();
        TypeDescriptorNode headerTypeName;
        IdentifierToken parameterName = createIdentifierToken(" " + escapeIdentifier(parameter.getName()
                .toLowerCase(Locale.ENGLISH)));
        if (schema == null) {
            /**
             *01.<pre>in: header
             *       name: X-Request-ID
             *       schema: {}
             * </pre>
             */
            return createRequiredParameterNode(createEmptyNodeList(), createIdentifierToken(STRING), parameterName);
        } else {
            if (!schema.getType().equals(STRING) && !(schema instanceof ArraySchema)) {
                //TO-DO: Generate diagnostic about to error type
                headerTypeName = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(STRING));
            } else if (schema instanceof ArraySchema) {
                String arrayType = ((ArraySchema) schema).getItems().getType();
                BuiltinSimpleNameReferenceNode headerArrayItemTypeName = createBuiltinSimpleNameReferenceNode(null,
                                createIdentifierToken(arrayType));
                headerTypeName = createArrayTypeDescriptorNode(headerArrayItemTypeName,
                        createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                        createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            } else {
                headerTypeName = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(
                        convertOpenAPITypeToBallerina(schema.getType().trim())));
            }

            // Create annotation
            MappingConstructorExpressionNode annotValue = NodeFactory.createMappingConstructorExpressionNode(
                    createToken(SyntaxKind.OPEN_BRACE_TOKEN), NodeFactory.createSeparatedNodeList(),
                    createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
            AnnotationNode headerNode = getAnnotationNode("Header", annotValue);
            NodeList<AnnotationNode> headerAnnotations = NodeFactory.createNodeList(headerNode);

            return createRequiredParameterNode(headerAnnotations, headerTypeName, parameterName);

        }
    }

    private  AnnotationNode getAnnotationNode(String identifier, MappingConstructorExpressionNode annotValue) {
        // Create annotation
        Token atToken = createIdentifierToken("@");
        QualifiedNameReferenceNode annotReference = getQualifiedNameReferenceNode(HTTP, identifier);
        return createAnnotationNode(atToken, annotReference, annotValue);
    }


    /**
     * This for generate query parameter nodes.
     */
    private Node createNodeForQueryParam(Parameter parameter) throws BallerinaOpenApiException {
        Schema schema = parameter.getSchema();
        NodeList<AnnotationNode> annotations = createEmptyNodeList();
        IdentifierToken parameterName = createIdentifierToken(escapeIdentifier(parameter.getName().trim()));
        if (schema == null || parameter.getContent() != null) {
            //TODO throw a error by mentioning support query type
            return createRequiredParameterNode(createEmptyNodeList(), createIdentifierToken("string"),
                    parameterName);
        } else {
            if (parameter.getRequired()) {
                //Required without typeDescriptor
                //When it has arrayType
                return handleRequiredQueryParameter(schema, annotations, parameterName);
            } else {
                //Optional TypeDescriptor
                //Array type
                //When it has arrayType
                return handleOptionalQueryParameter(schema, annotations, parameterName);
            }
        }
    }

    private Node handleOptionalQueryParameter(Schema schema, NodeList<AnnotationNode> annotations,
                                              IdentifierToken parameterName) throws BallerinaOpenApiException {

        if (schema instanceof ArraySchema) {
            Schema<?> items = ((ArraySchema) schema).getItems();
            if (!(items instanceof ObjectSchema) && !(items.getType().equals("array"))) {
                // create arrayTypeDescriptor
                ArrayTypeDescriptorNode arrayTypeName = getArrayTypeDescriptorNode(items);
                // create Optional type descriptor
                OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(arrayTypeName,
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(annotations,optionalNode, parameterName);
            } else {
                // handle in case swagger has nested array or record type
                // TODO create diagnostic after checking with team or map to map<json>.
                Token name = createIdentifierToken("map<json>");
                BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null, name);
                OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(rTypeName,
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(annotations, optionalNode, parameterName);
            }
        } else {
            Token name = createIdentifierToken(convertOpenAPITypeToBallerina(
                    schema.getType().toLowerCase(Locale.ENGLISH).trim()));
            BuiltinSimpleNameReferenceNode rTypeName = createBuiltinSimpleNameReferenceNode(null, name);
            OptionalTypeDescriptorNode optionalNode = createOptionalTypeDescriptorNode(rTypeName,
                    createToken(SyntaxKind.QUESTION_MARK_TOKEN));
            return createRequiredParameterNode(annotations, optionalNode, parameterName);
        }
    }

    private Node handleRequiredQueryParameter(Schema schema, NodeList<AnnotationNode> annotations,
                                              IdentifierToken parameterName) throws BallerinaOpenApiException {

        if (schema instanceof ArraySchema) {
            Schema<?> items = ((ArraySchema) schema).getItems();
            if (!(items instanceof ArraySchema)) {
                // create arrayTypeDescriptor
                //1. memberTypeDescriptor
                ArrayTypeDescriptorNode arrayTypeName = getArrayTypeDescriptorNode(items);
                return createRequiredParameterNode(annotations, arrayTypeName, parameterName);
            } else {
                // handle in case swagger has nested array or record type
                //create optional query parameter
                Token arrayName = createIdentifierToken("string");
                BuiltinSimpleNameReferenceNode memberTypeDesc =
                        createBuiltinSimpleNameReferenceNode(null, arrayName);
                ArrayTypeDescriptorNode arrayTypeName = createArrayTypeDescriptorNode(
                        memberTypeDesc, createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                        createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
                // create Optional type descriptor
                OptionalTypeDescriptorNode optionalNode =createOptionalTypeDescriptorNode(arrayTypeName,
                        createToken(SyntaxKind.QUESTION_MARK_TOKEN));
                return createRequiredParameterNode(annotations, optionalNode, parameterName);
            }
        } else {
            Token name =
                    createIdentifierToken(convertOpenAPITypeToBallerina(
                            schema.getType().toLowerCase(Locale.ENGLISH).trim()));
            BuiltinSimpleNameReferenceNode rTypeName =
                    createBuiltinSimpleNameReferenceNode(null, name);
            return createRequiredParameterNode(annotations, rTypeName, parameterName);
        }
    }

    // Create ArrayTypeDescriptorNode using Schema
    private  ArrayTypeDescriptorNode getArrayTypeDescriptorNode(Schema<?> items) {

        Token arrayName = createIdentifierToken(items.getType().trim());
        BuiltinSimpleNameReferenceNode memberTypeDesc =
                createBuiltinSimpleNameReferenceNode(null, arrayName);
        return createArrayTypeDescriptorNode(memberTypeDesc, createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
    }


}
