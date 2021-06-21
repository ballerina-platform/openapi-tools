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


package org.ballerinalang.ballerina.util.service;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ChildNodeList;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.swagger.models.parameters.Parameter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.CookieParameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.ballerinalang.ballerina.util.Constants;
import org.ballerinalang.net.http.HttpConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;

import static org.ballerinalang.ballerina.util.Constants.HTTP_PAYLOAD;
import static org.ballerinalang.ballerina.util.ConverterUtils.convertBallerinaTypeToOpenAPIType;
import static org.ballerinalang.net.http.HttpConstants.HTTP_METHOD_GET;

/**
 * This class will do resource mapping from ballerina to openApi.
 */
public class OpenApiResourceMapper {
    private SemanticModel semanticModel;
    private Paths pathObject = new Paths();
    private Components components = new Components();

    /**
     * Initializes a resource parser for openApi.
     */
    OpenApiResourceMapper(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public void setSemanticModel(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public Components getComponents() {
        return components;
    }
    /**
     * This method will convert ballerina resource to openApi path objects.
     *
     * @param resources Resource array to be convert.
     * @return map of string and openApi path objects.
     */
    protected Paths convertResourceToPath(List<FunctionDefinitionNode> resources) {
        for (FunctionDefinitionNode resource : resources) {
            List<String> methods = this.getHttpMethods(resource, false);
            if (methods.size() == 0 || methods.size() > 1) {
                useMultiResourceMapper(resource, methods);
            } else {
                useDefaultResourceMapper(resource);
            }
        }
        return pathObject;
    }

    /**
     * Resource mapper when a resource has more than 1 http method.
     * @param resource The ballerina resource.
     */
    private void useMultiResourceMapper(FunctionDefinitionNode resource, List<String> httpMethods) {
        String path = this.getPath(resource);
        Operation operation;
        if (httpMethods.size() > 1) {
            int i = 1;
            for (String httpMethod : httpMethods) {
                //Iterate through http methods and fill path map.
                //If can send a paths it is better
                if (resource.functionName().toString().trim().equals(httpMethod)) {
                    operation = this.convertResourceToOperation(resource, httpMethod, i).getOperation();
                    generatePathItem(httpMethod, pathObject, operation, path);
                    break;
                }
                i++;
            }
        }
    }

    /**
     * Resource mapper when a resource has only one http method.
     * @param resource The ballerina resource.
     */
    private void useDefaultResourceMapper(FunctionDefinitionNode resource) {
        String httpMethod = getHttpMethods(resource, true).get(0);
        OperationAdaptor operationAdaptor = this.convertResourceToOperation(resource, httpMethod, 1);
        operationAdaptor.setHttpOperation(httpMethod);
        String path = getPath(resource);
        io.swagger.v3.oas.models.Operation operation = operationAdaptor.getOperation();
        generatePathItem(httpMethod, pathObject, operation, path);
    }

    private void generatePathItem(String httpMethod, Paths path, Operation operation, String pathName) {
        PathItem pathItem = new PathItem();
        switch (httpMethod.trim().toUpperCase(Locale.ENGLISH)) {
            case HttpConstants.ANNOTATION_METHOD_GET:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setGet(operation);
                } else {
                    pathItem.setGet(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case HttpConstants.ANNOTATION_METHOD_PUT:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPut(operation);
                } else {
                    pathItem.setPut(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case HttpConstants.ANNOTATION_METHOD_POST:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPost(operation);
                } else {
                    pathItem.setPost(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case HttpConstants.ANNOTATION_METHOD_DELETE:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setDelete(operation);
                } else {
                    pathItem.setDelete(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case HttpConstants.ANNOTATION_METHOD_OPTIONS:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setOptions(operation);
                } else {
                    pathItem.setOptions(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case HttpConstants.ANNOTATION_METHOD_PATCH:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPatch(operation);
                } else {
                    pathItem.setPatch(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case HttpConstants.HTTP_METHOD_HEAD:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setHead(operation);
                } else {
                    pathItem.setHead(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            default:
                break;
        }
    }

    /**
     * This method will convert ballerina @Resource to ballerina @OperationAdaptor.
     *
     * @param resource Resource array to be convert.
     * @return Operation Adaptor object of given resource
     */
    private OperationAdaptor convertResourceToOperation(FunctionDefinitionNode resource, String httpMethod,
                                                        int idIncrement) {
        OperationAdaptor op = new OperationAdaptor();
        if (resource != null) {

            String generateRelativePath = generateRelativePath(resource);
            op.setHttpOperation(httpMethod);
            op.setPath(generateRelativePath);

            // @see BallerinaOperation#buildContext
            String resName = (resource.functionName().text() + "_" + generateRelativePath)
                    .replaceAll("\\{///\\}", "_");
            op.getOperation().setOperationId(getOperationId(idIncrement, resName));
            op.getOperation().setParameters(null);

            this.addResourceParameters(resource, op);
            this.parseResponsesAnnotationAttachment(resource, op);
        }
        return op;
    }

    private void parseResponsesAnnotationAttachment(FunctionDefinitionNode resource, OperationAdaptor op) {

        FunctionSignatureNode functionSignatureNode = resource.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptorNode = functionSignatureNode.returnTypeDesc();
        io.swagger.v3.oas.models.Operation operation = op.getOperation();
        ApiResponses apiResponses = new ApiResponses();
        if (returnTypeDescriptorNode.isPresent()) {
            ReturnTypeDescriptorNode returnNode = returnTypeDescriptorNode.orElseThrow();
            NodeList<AnnotationNode> annotations = returnNode.annotations();
            Node typeNode = returnNode.type();
            if (!annotations.isEmpty()) {
                // Handle in further implementation
                //Temporary solution
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.description("Ok");
                apiResponse.content(new Content().addMediaType("text/plain",
                        new io.swagger.v3.oas.models.media.MediaType().schema(new StringSchema())));
                apiResponses.put("200", apiResponse);

            } else {
                if (typeNode.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
                    QualifiedNameReferenceNode qNode = (QualifiedNameReferenceNode) typeNode;
                    if (qNode.modulePrefix().toString().trim().equals("http")) {
                        ApiResponse apiResponse = new ApiResponse();
                        String code = generateApiResponseCode(qNode.identifier().toString().trim());
                        apiResponse.description(qNode.identifier().toString().trim());
                        apiResponses.put(code, apiResponse);
                    }
                } else if (typeNode instanceof BuiltinSimpleNameReferenceNode) {
                    ApiResponse apiResponse = new ApiResponse();
                    String type =  typeNode.toString().toLowerCase(Locale.ENGLISH).trim();
                    Schema schema = getOpenApiSchema(type);
                    io.swagger.v3.oas.models.media.MediaType mediaType =
                            new io.swagger.v3.oas.models.media.MediaType();
                    String media = generateMIMETypeForBallerinaType(type);
                    mediaType.setSchema(schema);
                    apiResponse.description("Successful");
                    apiResponse.content(new Content().addMediaType(media, mediaType));
                    apiResponses.put("200", apiResponse);
                } else if (typeNode.kind().equals(SyntaxKind.JSON_TYPE_DESC)) {
                    ApiResponse apiResponse = new ApiResponse();
                    io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
                    Schema schema = new ObjectSchema();
                    mediaType.setSchema(schema);
                    apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, mediaType));
                    apiResponse.description("Ok");
                    apiResponses.put("200", apiResponse);
                } else if (typeNode.kind().equals(SyntaxKind.XML_TYPE_DESC)) {
                    ApiResponse apiResponse = new ApiResponse();
                    io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
                    Schema schema = new ObjectSchema();
                    mediaType.setSchema(schema);
                    apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_XML, mediaType));
                    apiResponse.description("Ok");
                    apiResponses.put("200", apiResponse);
                } else if (typeNode instanceof SimpleNameReferenceNode) {
                    SimpleNameReferenceNode recordNode  = (SimpleNameReferenceNode) typeNode;
                    Map<String, Schema> schema = components.getSchemas();
                    handleReferencePayload(op, recordNode, schema, MediaType.APPLICATION_JSON, null, apiResponses);
                } else if (typeNode.kind().equals(SyntaxKind.UNION_TYPE_DESC)) {
                    //line-No 304 - 330 commented due to handle advance response scenario in further implementation.
                    //check until right typeDec empty
//                    UnionTypeDescriptorNode unionNode = (UnionTypeDescriptorNode) typeNode;
//                    TypeDescriptorNode rightNode = unionNode.rightTypeDesc();
//                    TypeDescriptorNode leftNode = unionNode.leftTypeDesc();

//                    if (rightNode instanceof UnionTypeDescriptorNode) {
//                        UnionTypeDescriptorNode traversRightNode = (UnionTypeDescriptorNode) rightNode;
//                        while (traversRightNode.rightTypeDesc() != null) {
//                            if (leftNode.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
//                                String identifier = ((QualifiedNameReferenceNode)
//                                leftNode).identifier().text().trim();
//                                String code = generateApiResponseCode(identifier);
//                                ApiResponse apiResponse = new ApiResponse();
//                                apiResponse.description(identifier);
//                                apiResponses.put(code, apiResponse);
//// casting exception return.
////                                rightNode = ((UnionTypeDescriptorNode) rightNode).rightTypeDesc();
////                                leftNode = ((UnionTypeDescriptorNode) rightNode).leftTypeDesc();
//                            }
//                        }
//                    }
//                    if (rightNode.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
//                        String identifier = ((QualifiedNameReferenceNode) rightNode).identifier().text().trim();
//                        String code = generateApiResponseCode(identifier);
//                        ApiResponse apiResponse = new ApiResponse();
//                        apiResponse.description(identifier);
//                        apiResponses.put(code, apiResponse);
//                    }
                    //Return type is union type  generate 200 response with text/plain
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.description("Ok");
                    apiResponse.content(new Content().addMediaType("text/plain",
                            new io.swagger.v3.oas.models.media.MediaType().schema(new StringSchema())));
                    apiResponses.put("200", apiResponse);

                } else if (typeNode.kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
                    ApiResponse apiResponse = new ApiResponse();
                    RecordTypeDescriptorNode record  = (RecordTypeDescriptorNode) typeNode;
                    NodeList<Node> fields = record.fields();
                    String identifier = null;
                    Schema schema = null;
                    String mediaType = null;
                    for (Node field: fields) {
                        if (field.kind().equals(SyntaxKind.TYPE_REFERENCE)) {
                            TypeReferenceNode typefieldNode = (TypeReferenceNode) field;
                            if (typefieldNode.typeName().kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
                                QualifiedNameReferenceNode identiferNode =
                                        (QualifiedNameReferenceNode) typefieldNode.typeName();
                                identifier = generateApiResponseCode(identiferNode.identifier().text().trim());
                                apiResponse.description(identiferNode.identifier().text().trim());
                            }
                        }
                        if (field.kind().equals(SyntaxKind.RECORD_FIELD)) {
                            RecordFieldNode recordField = (RecordFieldNode) field;
                            Node type = recordField.typeName();
                            String nodeType = type.toString().toLowerCase(Locale.ENGLISH).trim();
                            mediaType = generateMIMETypeForBallerinaType(nodeType);
                            if (type.kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                                Map<String, Schema> schemas = components.getSchemas();
                                mediaType = MediaType.APPLICATION_JSON;
                                SimpleNameReferenceNode nameRefNode =  (SimpleNameReferenceNode) type;
                                handleReferencePayload(op, nameRefNode, schemas, mediaType, null,
                                        apiResponses);
                            } else {
                                schema = getOpenApiSchema(nodeType);
                            }
                        }
                    }
                    io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
                    media.setSchema(schema);
                    apiResponse.content(new Content().addMediaType(mediaType, media));
                    apiResponses.put(identifier, apiResponse);

                } else if (typeNode.toString().trim().equals("error") || typeNode.toString().trim().equals("error?")) {
                    //Return type is not given as error or error? in ballerina it will generate 500 response.
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.description("Found unexpected output");
                    apiResponse.content(new Content().addMediaType("text/plain",
                            new io.swagger.v3.oas.models.media.MediaType().schema(new StringSchema())));
                    apiResponses.put("500", apiResponse);
                } else if (typeNode.kind().equals(SyntaxKind.ARRAY_TYPE_DESC)) {
                    ArrayTypeDescriptorNode array = (ArrayTypeDescriptorNode) typeNode;
                    Map<String, Schema> schemas = components.getSchemas();
                    if (array.memberTypeDesc().kind().equals(SyntaxKind.SIMPLE_NAME_REFERENCE)) {
                        handleReferencePayload(op, (SimpleNameReferenceNode) array.memberTypeDesc(), schemas,
                                MediaType.APPLICATION_JSON, null ,
                                apiResponses);
                    } else {
                        ApiResponse apiResponse = new ApiResponse();
                        ArraySchema arraySchema = new ArraySchema();
                        io.swagger.v3.oas.models.media.MediaType mediaType =
                                new io.swagger.v3.oas.models.media.MediaType();
                        String type =
                                array.memberTypeDesc().kind().toString().trim().split("_")[0].
                                        toLowerCase(Locale.ENGLISH);
                        Schema openApiSchema = getOpenApiSchema(type);
                        String mimeType = generateMIMETypeForBallerinaType(type);
                        arraySchema.setItems(openApiSchema);
                        mediaType.setSchema(arraySchema);
                        apiResponse.description("Ok");
                        apiResponse.content(new Content().addMediaType(mimeType, mediaType));
                        apiResponses.put("200", apiResponse);

                    }
                } else {
                    ApiResponse apiResponse = new ApiResponse();
                    apiResponse.description("Ok");
                    apiResponses.put("200", apiResponse);
                }
            }
        } else {
            //Return type is not given generate 200 response with text/plain
            ApiResponse apiResponse = new ApiResponse();
                    apiResponse.description("Ok");
                    apiResponse.content(new Content().addMediaType("text/plain",
                            new io.swagger.v3.oas.models.media.MediaType().schema(new StringSchema())));
                    apiResponses.put("200", apiResponse);
        }
        operation.setResponses(apiResponses);
    }

    private String generateMIMETypeForBallerinaType(String type) {
        switch (type) {
            case Constants.STRING:
                return MediaType.TEXT_PLAIN;
            case Constants.JSON:
                return MediaType.APPLICATION_JSON;
            case Constants.XML:
                return MediaType.APPLICATION_XML;
            case Constants.BYTE_ARRAY:
                return MediaType.APPLICATION_OCTET_STREAM;
            default:
                return MediaType.TEXT_PLAIN;
        }
    }

    private String generateApiResponseCode(String identifier) {
        switch (identifier) {
            case "Continue":
                return "100";
            case "Ok":
                return "200";
            case "Created":
                return "201";
            case "Accepted":
                return "202";
            case "BadRequest":
                return "400";
            case "Unauthorized":
                return "401";
            case "PaymentRequired":
                return "402";
            case "Forbidden":
                return "403";
            case "NotFound":
                return "404";
            case "MethodNotAllowed":
                return "405";
            case "500":
                return "InternalServerError";
            case "InternalServerError":
                return "501";
            case "BadGateway":
                return "502";
            case "ServiceUnavailable":
                return "503";
            default:
                return "200";
        }
    }
    /**
     * get UUID generated with the given post fix.
     *
     * @param postFix string post fix to attach to ID
     * @return {@link String} generated UUID
     */
    private String getOperationId(int idIncrement, String postFix) {
        return "operation" + idIncrement + "_" + postFix;
    }

    /**
     * Creates parameters in the openApi operation using the parameters in the ballerina resource definition.
     *
     * @param resource         The ballerina resource definition.
     * @param operationAdaptor The openApi operation.
     */
    private void addResourceParameters(FunctionDefinitionNode resource, OperationAdaptor operationAdaptor) {
        //Add path parameters if in path and query parameters
        this.createParametersModel(resource, operationAdaptor.getOperation());

        if (!HTTP_METHOD_GET.toLowerCase(Locale.ENGLISH).equalsIgnoreCase(operationAdaptor.getHttpOperation())) {
            // set body parameter
            FunctionSignatureNode functionSignature = resource.functionSignature();
            SeparatedNodeList<ParameterNode> paramExprs = functionSignature.parameters();
            for (ParameterNode expr : paramExprs) {
                if (expr instanceof RequiredParameterNode) {
                    RequiredParameterNode bodyParam = (RequiredParameterNode) expr;
                    if (bodyParam.typeName() instanceof TypeDescriptorNode && !bodyParam.annotations().isEmpty()) {
                        NodeList<AnnotationNode> annotations = bodyParam.annotations();
                        Map<String, Schema> schema = components.getSchemas();
                        for (AnnotationNode annotation: annotations) {
                            handlePayloadAnnotation(operationAdaptor, (RequiredParameterNode) expr, bodyParam,
                                    schema, annotation);
                        }
                    }
                }
            }
        }
    }

    private void handlePayloadAnnotation(OperationAdaptor operationAdaptor, RequiredParameterNode expr,
                                         RequiredParameterNode queryParam, Map<String, Schema> schema,
                                         AnnotationNode annotation) {

        if ((annotation.annotReference().toString()).trim().equals(HTTP_PAYLOAD) &&
                annotation.annotValue().isPresent()) {
            // Creating request body - required.
            RequestBody bodyParameter = new RequestBody();
            MappingConstructorExpressionNode mapMime = annotation.annotValue().orElseThrow();
            SeparatedNodeList<MappingFieldNode> fields = mapMime.fields();
            if (!fields.isEmpty() || fields.size() != 0) {
                handleMultipleMIMETypes(operationAdaptor, bodyParameter, fields, expr, queryParam, schema);
            }  else {
                String consumes = "application/" + expr.typeName().toString().trim();
                switch (consumes) {
                    case MediaType.APPLICATION_JSON:
                        addConsumes(operationAdaptor, bodyParameter, MediaType.APPLICATION_JSON);
                        break;
                    case MediaType.APPLICATION_XML:
                        addConsumes(operationAdaptor, bodyParameter, MediaType.APPLICATION_XML);
                        break;
                    case MediaType.TEXT_PLAIN:
                    case "application/string":
                        addConsumes(operationAdaptor, bodyParameter, MediaType.TEXT_PLAIN);
                        break;
                    case "application/byte[]":
                        addConsumes(operationAdaptor, bodyParameter, MediaType.APPLICATION_OCTET_STREAM);
                        break;
                    default:
                        Node node = queryParam.typeName();
                        if (node instanceof SimpleNameReferenceNode) {
                            SimpleNameReferenceNode record = (SimpleNameReferenceNode) node;
                            handleReferencePayload(operationAdaptor, record, schema,
                                    MediaType.APPLICATION_JSON, new RequestBody(), null);
                        }
                        break;
                }
            }
        }
    }

    private void handleMultipleMIMETypes(OperationAdaptor operationAdaptor, RequestBody bodyParameter,
                                         SeparatedNodeList<MappingFieldNode> fields, RequiredParameterNode expr,
                                         RequiredParameterNode queryParam, Map<String, Schema> schema) {

        for (MappingFieldNode fieldNode: fields) {
            if (fieldNode.children() != null) {
                ChildNodeList nodeList = fieldNode.children();
                Iterator<Node> itNode = nodeList.iterator();
                while (itNode.hasNext()) {
                    Node nextNode = itNode.next();
                    if (nextNode instanceof ListConstructorExpressionNode) {
                        SeparatedNodeList mimeList = ((ListConstructorExpressionNode) nextNode).expressions();
                        if (mimeList.size() != 0) {
                            RequestBody requestBody = new RequestBody();
                            for (Object mime : mimeList) {
                                if (mime instanceof BasicLiteralNode) {
                                    String mimeType = ((BasicLiteralNode) mime).literalToken().text().
                                            replaceAll("\"", "");
                                    if (queryParam.typeName() instanceof SimpleNameReferenceNode) {
                                        SimpleNameReferenceNode record =
                                                (SimpleNameReferenceNode) queryParam.typeName();
                                        handleReferencePayload(operationAdaptor, record, schema, mimeType,
                                                requestBody, null);
                                    } else {

                                        io.swagger.v3.oas.models.media.MediaType media =
                                                new io.swagger.v3.oas.models.media.MediaType();
                                        Schema mimeSchema;
                                        if (bodyParameter.getContent() != null) {
                                            media = new io.swagger.v3.oas.models.media.MediaType();
                                            mimeSchema = getOpenApiSchema(mimeType.split("/")[1]
                                                    .toLowerCase(Locale.ENGLISH));
                                            media.setSchema(mimeSchema);
                                            Content content = bodyParameter.getContent();
                                            content.addMediaType(mimeType, media);
                                        } else {
                                            mimeSchema = getOpenApiSchema(mimeType.split("/")[1].
                                                    toLowerCase(Locale.ENGLISH));
                                            media.setSchema(mimeSchema);
                                            bodyParameter.setContent(new Content().addMediaType(mimeType,
                                                    media));
                                            operationAdaptor.getOperation().setRequestBody(bodyParameter);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleReferencePayload(OperationAdaptor operationAdaptor, SimpleNameReferenceNode recordNode,
                                        Map<String, Schema> schema, String mediaType,
                                        @Nullable RequestBody bodyParameter,
                                        @Nullable ApiResponses apiResponses) {

        // Creating request body - required.
        SimpleNameReferenceNode referenceNode = recordNode;
        Optional<Symbol> symbol = semanticModel.symbol(referenceNode);
        TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();


        //handel record for components
        handleRecordPayload(recordNode, schema, typeSymbol);

        io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
//        media.setSchema(new Schema().$ref(referenceNode.name().toString().trim()));
        if (bodyParameter == null && (apiResponses != null)) {
            if (recordNode.parent().kind().equals(SyntaxKind.ARRAY_TYPE_DESC)) {
                ArraySchema arraySchema = new ArraySchema();
                ApiResponse apiResponse = new ApiResponse();
                arraySchema.setItems(new Schema().$ref(referenceNode.name().toString().trim()));
                media.setSchema(arraySchema);
                apiResponse.description("Ok");
                apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, media));
                apiResponses.put("200", apiResponse);

            } else {
                media.setSchema(new Schema().$ref(referenceNode.name().toString().trim()));
                ApiResponse apiResponse = new ApiResponse();
                apiResponse.content(new Content().addMediaType(MediaType.APPLICATION_JSON, media));
                apiResponse.description("Ok");
                apiResponses.put("200", apiResponse);
            }
            operationAdaptor.getOperation().setResponses(apiResponses);
        } else if (bodyParameter != null) {
            media.setSchema(new Schema().$ref(referenceNode.name().toString().trim()));
            if (bodyParameter.getContent() != null) {
                Content content = bodyParameter.getContent();
                content.addMediaType(mediaType, media);
            } else {
                bodyParameter.setContent(new Content().addMediaType(mediaType, media));
            }
            //  Adding conditional check for http delete operation as it cannot have body
            //  parameter.
            if (!operationAdaptor.getHttpOperation().equalsIgnoreCase("delete")) {
                operationAdaptor.getOperation().setRequestBody(bodyParameter);
            }
        }
    }

    private void handleRecordPayload(SimpleNameReferenceNode queryParam, Map<String, Schema> schema,
                                     TypeSymbol typeSymbol) {
        String componentName = typeSymbol.getName().orElseThrow().trim();
        Schema componentSchema = new Schema();
        componentSchema.setType("object");
        Map<String, Schema> schemaProperties = new HashMap<>();
        if (typeSymbol instanceof TypeReferenceTypeSymbol) {
            TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeSymbol;
            // Handle record type request body
            if (typeRef.typeDescriptor() instanceof RecordTypeSymbol) {
                RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeRef.typeDescriptor();
                Map<String, RecordFieldSymbol> rfields = recordTypeSymbol.fieldDescriptors();
                for (Map.Entry<String, RecordFieldSymbol> field: rfields.entrySet()) {
                    String type = field.getValue().typeDescriptor().typeKind().toString().toLowerCase(Locale.ENGLISH);
                    Schema property = getOpenApiSchema(type);
                    if (type.equals(Constants.TYPE_REFERENCE) && property.get$ref().
                            equals("#/components/schemas/true")) {
                        property.set$ref(field.getValue().typeDescriptor().getName().orElseThrow().trim());
                        Optional<TypeSymbol> recordSymbol = semanticModel.type(field.getValue().location().lineRange());
                        TypeSymbol recordVariable =  recordSymbol.orElseThrow();
                        if (recordVariable.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                            TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) recordVariable;
                            handleRecordPayload(queryParam, schema, typeRecord);
                            schema = components.getSchemas();
                        }
                    }
                    if (property instanceof ArraySchema) {
                        setArrayProperty(queryParam, schema, field.getValue(), (ArraySchema) property);
                        schema = components.getSchemas();
                    }
                    schemaProperties.put(field.getKey(), property);
                }
                componentSchema.setProperties(schemaProperties);
            }
        }
        if (schema != null && !schema.containsKey(componentName)) {
            //Set properties for the schema
            schema.put(componentName, componentSchema);
            this.components.setSchemas(schema);
        } else if (schema == null) {
            schema = new HashMap<>();
            schema.put(componentName, componentSchema);
            this.components.setSchemas(schema);
        }
    }

    private void setArrayProperty(SimpleNameReferenceNode queryParam, Map<String, Schema> schema,
                                  RecordFieldSymbol field, ArraySchema property) {

        TypeSymbol symbol = field.typeDescriptor();
        int arrayDimensions = 0;
        while (symbol instanceof ArrayTypeSymbol) {
            arrayDimensions = arrayDimensions + 1;
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            symbol = arrayTypeSymbol.memberTypeDescriptor();
        }
        //handle record field has nested record array type ex: Tag[] tags
        Schema symbolProperty  = getOpenApiSchema(symbol.typeKind().getName());
        if (symbolProperty.get$ref() != null && symbolProperty.get$ref().equals("#/components/schemas/true")) {
            symbolProperty.set$ref(symbol.getName().orElseThrow().trim());
            //Set the record model to the definition
            if (symbol.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) symbol;
                handleRecordPayload(queryParam, schema, typeRecord);
            }
        }
        //Handle nested array type
        if (arrayDimensions > 1) {
            property.setItems(handleArray(arrayDimensions - 1, symbolProperty, new ArraySchema()));
        } else {
            property.setItems(symbolProperty);
        }
    }

    private ArraySchema handleArray(int arrayDimensions, Schema property, ArraySchema arrayProperty) {
        if (arrayDimensions > 1) {
            ArraySchema narray = new ArraySchema();
            arrayProperty.setItems(handleArray(arrayDimensions - 1, property,  narray));
        } else if (arrayDimensions == 1) {
            arrayProperty.setItems(property);
        }
        return arrayProperty;
    }

    private void addConsumes(OperationAdaptor operationAdaptor, RequestBody bodyParameter, String applicationType) {
        String type = applicationType.split("/")[1];
        Schema schema = getOpenApiSchema(type);
        io.swagger.v3.oas.models.media.MediaType mediaType = new io.swagger.v3.oas.models.media.MediaType();
        mediaType.setSchema(schema);
        bodyParameter.setContent(new Content().addMediaType(applicationType, mediaType));
        operationAdaptor.getOperation().setRequestBody(bodyParameter);
    }

    /**
     * Create {@code Parameters} model for openApi operation.
     *
     * @param resource The resource used to map with operation.
     * @param operation            The openApi operation.
     */
    private void createParametersModel(FunctionDefinitionNode resource, Operation operation) {
        List<io.swagger.v3.oas.models.parameters.Parameter> parameters = new LinkedList<>();

        //Set path parameters
        NodeList<Node> pathParams = resource.relativeResourcePath();
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
        FunctionSignatureNode functionSignature = resource.functionSignature();
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
                            Schema itemSchema = getOpenApiSchema(itemTypeNode.toString().trim());
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
                            Schema itemSchema = getOpenApiSchema(itemTypeNode.toString().trim());
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
                        if ((annotation.annotReference().toString()).trim().equals("http:Header") &&
                                annotation.annotValue().isPresent()) {
                            //Handle with string current header a support with only string and string[]
                            if (queryParam.typeName() instanceof ArrayTypeDescriptorNode)  {
                                ArrayTypeDescriptorNode arrayNode = (ArrayTypeDescriptorNode) queryParam.typeName();
                                if (arrayNode.memberTypeDesc().kind().equals(SyntaxKind.STRING_TYPE_DESC)) {
                                    TypeDescriptorNode itemTypeNode = arrayNode.memberTypeDesc();
                                    Schema itemSchema = getOpenApiSchema(itemTypeNode.toString().trim());
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
     * Gets the http methods of a resource.
     *
     * @param resource    The ballerina resource.
     * @param useDefaults True to add default http methods, else false.
     * @return A list of http methods.
     */
    private List<String> getHttpMethods(FunctionDefinitionNode resource, boolean useDefaults) {
        Set<String> httpMethods = new LinkedHashSet<>();
        ServiceDeclarationNode parentNode = (ServiceDeclarationNode) resource.parent();
        NodeList<Node> siblings = parentNode.members();
        httpMethods.add(resource.functionName().text());
        for (Node function: siblings) {
            SyntaxKind kind = function.kind();
            if (kind.equals(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION)) {
                FunctionDefinitionNode sibling = (FunctionDefinitionNode) function;
                //need to build relative path
                String relativePath = generateRelativePath(resource);
                String siblingRelativePath = generateRelativePath(sibling);
                if (relativePath.equals(siblingRelativePath)) {
                    httpMethods.add(sibling.functionName().text());
                }
            }
        }

        if (httpMethods.isEmpty() && useDefaults) {
            // By default all http methods are supported.
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_GET);
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_PUT);
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_POST);
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_DELETE);
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_PATCH);
            httpMethods.add(HttpConstants.ANNOTATION_METHOD_OPTIONS);
            httpMethods.add("HEAD");
        }
        List<String> httpMethodsAsString = new ArrayList<>(httpMethods);
        Collections.reverse(httpMethodsAsString);
        return httpMethodsAsString;
    }

    private String generateRelativePath(FunctionDefinitionNode resource) {

        String relativePath = "";
        if (!resource.relativeResourcePath().isEmpty()) {
            for (Node node: resource.relativeResourcePath()) {
                if (node instanceof ResourcePathParameterNode) {
                    ResourcePathParameterNode pathNode = (ResourcePathParameterNode) node;
                    relativePath = relativePath + "{" + pathNode.paramName() + "}";
                } else if ((resource.relativeResourcePath().size() == 1) && (node.toString().trim().equals("."))) {
                    return relativePath + "/";
                } else {
                    relativePath = relativePath + node.toString();
                }
            }
        }
        return "/" + relativePath.trim();
    }

    /**
     * Gets the path value of the @http:resourceConfig.
     *
     * @param resource The ballerina resource.
     * @return The path value.
     */
    private String getPath(FunctionDefinitionNode resource) {
        String path = generateRelativePath(resource);
        return path;
    }

    /**
     * Builds an OpenApi {@link Parameter} for provided parameter location.
     *
     * @param in              location of the parameter in the request definition
     * @param paramAttributes parameter attributes for the operation
     * @return OpenApi {@link Parameter} for parameter location {@code in}
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
                type = convertBallerinaTypeToOpenAPIType(queryParam.typeName().toString().trim());
                qParam.schema(getOpenApiSchema(type));
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
                type = convertBallerinaTypeToOpenAPIType(pathParam.typeDescriptor().toString().trim());
                pParam.schema(getOpenApiSchema(type));
                pParam.setName(pathParam.paramName().text());
                param = pParam;
        }

        return param;
    }

    /**
     * Retrieves a matching OpenApi {@link Schema} for a provided ballerina type.
     *
     * @param type ballerina type name as a String
     * @return OpenApi {@link Schema} for type defined by {@code type}
     */
    private Schema getOpenApiSchema(String type) {
        Schema schema;

        switch (type) {
            case Constants.STRING:
            case Constants.PLAIN:
                schema = new StringSchema();
                break;
            case Constants.BOOLEAN:
                schema = new BooleanSchema();
                break;
            case Constants.ARRAY:
                schema = new ArraySchema();
                break;
            case Constants.NUMBER:
            case Constants.INT:
            case Constants.INTEGER:
                schema = new IntegerSchema();
                break;
            case Constants.TYPE_REFERENCE:
            case Constants.TYPEREFERENCE:
                schema = new Schema();
                schema.$ref("true");
                break;
            case Constants.BYTE_ARRAY:
            case Constants.OCTET_STREAM:
                schema = new StringSchema();
                schema.setFormat("uuid");
                break;
            case Constants.XML:
            case Constants.JSON:
            default:
                schema = new ObjectSchema();
                break;
        }

        return schema;
    }
}
