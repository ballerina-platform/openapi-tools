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
import static io.ballerina.openapi.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static io.ballerina.openapi.generators.GeneratorUtils.getQualifiedNameReferenceNode;

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
                    // Handle header parameters
                    // type string
                    createNodeForHeaderParameter(params, comma, parameter);
                } else {
                    // Handle query parameter
                    // type​ ​ BasicType​ ​ boolean|int|float|decimal|string​ ;
                    //public​ ​ type​ ​ QueryParamType​ ()​ |BasicType|BasicType​ [];
                    createNodeForQueryParam(params, comma, parameter);
                }
            }
        }
        // Handle request Body (Payload)
        // type​ ​ CustomRecord​ ​ record {| anydata...; |};
        //public type​ ​ PayloadType​ ​ string|json|xml|byte[]|CustomRecord|CustomRecord[]​ ;
        if (operation.getValue().getRequestBody() != null) {
            RequestBody requestBody = operation.getValue().getRequestBody();
            if (requestBody.getContent() != null) {
                createNodeForRequestBody(params, comma, requestBody);
            }
        }
        if (params.size() > 1) {
            params.remove(params.size() - 1);
        }
        return params;
    }

    private void createNodeForHeaderParameter(List<Node> params, Token comma, Parameter parameter)
            throws BallerinaOpenApiException {

        Schema schema = parameter.getSchema();
        String type = "string";
        TypeDescriptorNode headerTypeName;
        IdentifierToken parameterName =
                createIdentifierToken(" " + escapeIdentifier(parameter.getName().toLowerCase(
                        Locale.ENGLISH)));

        if (schema == null || parameter.getContent() != null) {
            RequiredParameterNode param =
                    createRequiredParameterNode(createEmptyNodeList(),
                            createIdentifierToken("string"), parameterName);
            params.add(param);
            //Diagnostic for null schema
        } else {
            if (!schema.getType().equals(type) && !(schema instanceof ArraySchema)) {
                //TO-DO: Generate diagnostic about to error type
                headerTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(type));
            } else if (schema instanceof ArraySchema) {
                String arrayType = ((ArraySchema) schema).getItems().getType();
                BuiltinSimpleNameReferenceNode headerArrayItemTypeName =
                        createBuiltinSimpleNameReferenceNode(null,
                                createIdentifierToken(arrayType));
                headerTypeName = createArrayTypeDescriptorNode(headerArrayItemTypeName, createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null,
                        createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
            } else {
                headerTypeName = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken(
                        convertOpenAPITypeToBallerina(schema.getType().trim())));
            }

            // Create annotation
            MappingConstructorExpressionNode annotValue =
                    NodeFactory.createMappingConstructorExpressionNode(createToken(SyntaxKind.OPEN_BRACE_TOKEN),
                            NodeFactory.createSeparatedNodeList(), createToken(SyntaxKind.CLOSE_BRACE_TOKEN));
            AnnotationNode headerNode = getAnnotationNode("Header", annotValue);
            NodeList<AnnotationNode> headerAnnotations = NodeFactory.createNodeList(headerNode);

            RequiredParameterNode param =  createRequiredParameterNode(headerAnnotations,
                    headerTypeName, parameterName);
            params.add(param);
            params.add(comma);
        }
    }

    private  AnnotationNode getAnnotationNode(String identifier, MappingConstructorExpressionNode annotValue) {
        // Create annotation
        Token atToken = createIdentifierToken("@");
        QualifiedNameReferenceNode annotReference = getQualifiedNameReferenceNode("http", identifier);
        return createAnnotationNode(atToken, annotReference, annotValue);
    }


    /**
     * This for generate query parameter nodes.
     */
    private void createNodeForQueryParam(List<Node> params, Token comma, Parameter parameter)
            throws BallerinaOpenApiException {
        if (parameter.getIn().trim().equals("query")) {
            Schema schema = parameter.getSchema();
            NodeList<AnnotationNode> annotations = createEmptyNodeList();
            IdentifierToken parameterName =
                    createIdentifierToken(" " + escapeIdentifier(parameter.getName().trim()));
            if (schema == null || parameter.getContent() != null) {
                RequiredParameterNode param =
                        createRequiredParameterNode(createEmptyNodeList(),
                                createIdentifierToken("string"), parameterName);
                params.add(param);
                params.add(comma);
                //Diagnostic for null schema
            } else {
                if (parameter.getRequired()) {
                    //Required without typeDescriptor
                    //When it has arrayType
                    if (schema instanceof ArraySchema) {
                        Schema<?> items = ((ArraySchema) schema).getItems();
                        if (!(items instanceof ArraySchema)) {
                            // create arrayTypeDescriptor
                            //1. memberTypeDescriptor
                            ArrayTypeDescriptorNode arrayTypeName = getArrayTypeDescriptorNode(items);
                            RequiredParameterNode arrayRparam = createRequiredParameterNode(annotations, arrayTypeName,
                                    parameterName);
                            params.add(arrayRparam);

                        } else {
                            // handle in case swagger has nested array or record type
                            //create optional query parameter
                            Token arrayName = createIdentifierToken("string");
                            BuiltinSimpleNameReferenceNode memberTypeDesc =
                                    createBuiltinSimpleNameReferenceNode(null, arrayName);
                            ArrayTypeDescriptorNode arrayTypeName = createArrayTypeDescriptorNode(
                                    memberTypeDesc, createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null, createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
                            // create Optional type descriptor
                            OptionalTypeDescriptorNode optionalTypeDescriptorNode =
                                    createOptionalTypeDescriptorNode(arrayTypeName, questionMark);
                            RequiredParameterNode arrayRparam = createRequiredParameterNode(annotations,
                                    optionalTypeDescriptorNode, parameterName);
                            params.add(arrayRparam);
                        }
                    } else {
                        Token name =
                                createIdentifierToken(convertOpenAPITypeToBallerina(
                                        schema.getType().toLowerCase(Locale.ENGLISH).trim()));
                        BuiltinSimpleNameReferenceNode rTypeName =
                                createBuiltinSimpleNameReferenceNode(null, name);
                        RequiredParameterNode param1 =
                                createRequiredParameterNode(annotations, rTypeName, parameterName);
                        params.add(param1);
                    }
                    params.add(comma);
                } else {
                    //Optional TypeDescriptor
                    //Array type
                    //When it has arrayType
                    if (schema instanceof ArraySchema) {
                        Schema<?> items = ((ArraySchema) schema).getItems();
                        if (!(items instanceof ObjectSchema) && !(items.getType().equals("array"))) {
                            // create arrayTypeDescriptor
                            ArrayTypeDescriptorNode arrayTypeName = getArrayTypeDescriptorNode(items);
                            // create Optional type descriptor
                            OptionalTypeDescriptorNode optionalTypeDescriptorNode =
                                    createOptionalTypeDescriptorNode(arrayTypeName, questionMark);
                            RequiredParameterNode arrayRparam = createRequiredParameterNode(annotations,
                                    optionalTypeDescriptorNode, parameterName);
                            params.add(arrayRparam);
                            params.add(comma);

                        } else {
                            // handle in case swagger has nested array or record type
                            // create diagnostic after checking with team.
                        }
                    } else {
                        Token name =
                                createIdentifierToken(convertOpenAPITypeToBallerina(
                                        schema.getType().toLowerCase(Locale.ENGLISH).trim()));
                        BuiltinSimpleNameReferenceNode rTypeName =
                                createBuiltinSimpleNameReferenceNode(null, name);
                        OptionalTypeDescriptorNode optionalTypeDescriptorNode =
                                createOptionalTypeDescriptorNode(rTypeName, questionMark);
                        RequiredParameterNode param1 =
                                createRequiredParameterNode(annotations, optionalTypeDescriptorNode,
                                        parameterName);
                        params.add(param1);
                        params.add(comma);
                    }
                }
            }
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
