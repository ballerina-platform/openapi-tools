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
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
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
import io.ballerina.stdlib.http.api.HttpConstants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import static io.ballerina.stdlib.http.api.HttpConstants.HTTP_METHOD_GET;

/**
 * This class will do resource mapping from ballerina to openApi.
 */
public class OpenAPIResourceMapper {
    private SemanticModel semanticModel;
    private Paths pathObject = new Paths();
    private Components components = new Components();
    private ConverterUtils converterUtils = new ConverterUtils();

    /**
     * Initializes a resource parser for openApi.
     */
    OpenAPIResourceMapper(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
    }

    public Components getComponents() {
        return components;
    }
    /**
     * This method will convert ballerina resource to openApi Paths objects.
     *
     * @param resources Resource list to be convert.
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
                    Schema schema = converterUtils.getOpenApiSchema(type);
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
                    handleReferenceInResponse(op, recordNode, schema, apiResponses);
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
                                handleReferenceInResponse(op, nameRefNode, schemas, apiResponses);
                            } else {
                                schema = converterUtils.getOpenApiSchema(nodeType);
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
                        handleReferenceInResponse(op, (SimpleNameReferenceNode) array.memberTypeDesc(), schemas,
                                apiResponses);
                    } else {
                        ApiResponse apiResponse = new ApiResponse();
                        ArraySchema arraySchema = new ArraySchema();
                        io.swagger.v3.oas.models.media.MediaType mediaType =
                                new io.swagger.v3.oas.models.media.MediaType();
                        String type =
                                array.memberTypeDesc().kind().toString().trim().split("_")[0].
                                        toLowerCase(Locale.ENGLISH);
                        Schema openApiSchema = converterUtils.getOpenApiSchema(type);
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

    private void handleReferenceInResponse(OperationAdaptor operationAdaptor, SimpleNameReferenceNode recordNode,
                                        Map<String, Schema> schema, ApiResponses apiResponses) {

        // Creating request body - required.
        SimpleNameReferenceNode referenceNode = recordNode;
        Optional<Symbol> symbol = semanticModel.symbol(referenceNode);
        TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();
        //handel record for components
        OpenAPIComponentMapper componentMapper = new OpenAPIComponentMapper(components, semanticModel);
        componentMapper.handleRecordNode(recordNode, schema, typeSymbol);
        io.swagger.v3.oas.models.media.MediaType media = new io.swagger.v3.oas.models.media.MediaType();
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
        OpenAPIParameterMapper openAPIParameterMapper = new OpenAPIParameterMapper(resource,
                operationAdaptor.getOperation());
        openAPIParameterMapper.createParametersModel();

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
                            OpenAPIRequestBodyMapper openAPIRequestBodyMapper =
                                    new OpenAPIRequestBodyMapper(components, operationAdaptor, semanticModel);
                            openAPIRequestBodyMapper.handlePayloadAnnotation(bodyParam, schema, annotation);
                        }
                    }
                }
            }
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
}
