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


package org.ballerinalang.ballerina.service;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
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
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.ballerinalang.ballerina.Constants;
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

import javax.ws.rs.core.MediaType;

import static org.ballerinalang.ballerina.Constants.HTTP_PAYLOAD;
import static org.ballerinalang.ballerina.ConverterUtils.convertBallerinaTypeToOpenAPIType;
import static org.ballerinalang.net.http.HttpConstants.HTTP_METHOD_GET;

/**
 * This class will do resource mapping from ballerina to openApi.
 */
public class OpenApiResourceMapper {
    private final Swagger openApiDefinition;
    private final SemanticModel semanticModel;

    /**
     * Initializes a resource parser for openApi.
     *
     * @param openApi      The OpenAPI definition.
     * @param semanticModel
     */
    OpenApiResourceMapper(Swagger openApi, SemanticModel semanticModel) {
        this.openApiDefinition = openApi;
        this.semanticModel = semanticModel;
    }

    /**
     * This method will convert ballerina resource to openApi path objects.
     *
     * @param resources Resource array to be convert.
     * @return map of string and openApi path objects.
     */
    protected Map<String, Path> convertResourceToPath(List<FunctionDefinitionNode> resources) {
        Map<String, Path> pathMap = new HashMap<>();
        for (FunctionDefinitionNode resource : resources) {
            List<String> methods = this.getHttpMethods(resource, false);
            if (methods.size() == 0 || methods.size() > 1) {
                useMultiResourceMapper(pathMap, resource);
            } else {
                useDefaultResourceMapper(pathMap, resource);
            }
        }
        return pathMap;
    }

    /**
     * Resource mapper when a resource has more than 1 http method.
     *
     * @param pathMap  The map with paths that should be updated.
     * @param resource The ballerina resource.
     */
    private void useMultiResourceMapper(Map<String, Path> pathMap, FunctionDefinitionNode resource) {
        List<String> httpMethods = this.getHttpMethods(resource, false);
        String path = this.getPath(resource);
        Path pathObject = new Path();
        Operation operation;
        if (httpMethods.size() > 1) {
            int i = 1;
            for (String httpMethod : httpMethods) {
                //Iterate through http methods and fill path map.
                operation = this.convertResourceToOperation(resource, httpMethod, i).getOperation();
                pathObject.set(httpMethod.toLowerCase(Locale.ENGLISH), operation);
                i++;
            }
        }
        pathMap.put(path, pathObject);
    }

    /**
     * Resource mapper when a resource has only one http method.
     *
     * @param pathMap  The map with paths that should be updated.
     * @param resource The ballerina resource.
     */
    private void useDefaultResourceMapper(Map<String, Path> pathMap, FunctionDefinitionNode resource) {
        String httpMethod = getHttpMethods(resource, true).get(0);
        OperationAdaptor operationAdaptor = this.convertResourceToOperation(resource, httpMethod, 1);
        operationAdaptor.setHttpOperation(httpMethod);
        Path path = pathMap.get(operationAdaptor.getPath());
        if (path == null) {
            path = new Path();
            pathMap.put(operationAdaptor.getPath(), path);
        }
        Operation operation = operationAdaptor.getOperation();
        switch (httpMethod.trim().toUpperCase(Locale.ENGLISH)) {
            case HttpConstants.ANNOTATION_METHOD_GET:
                path.get(operation);
                break;
            case HttpConstants.ANNOTATION_METHOD_PUT:
                path.put(operation);
                break;
            case HttpConstants.ANNOTATION_METHOD_POST:
                path.post(operation);
                break;
            case HttpConstants.ANNOTATION_METHOD_DELETE:
                path.delete(operation);
                break;
            case HttpConstants.ANNOTATION_METHOD_OPTIONS:
                path.options(operation);
                break;
            case HttpConstants.ANNOTATION_METHOD_PATCH:
                path.patch(operation);
                break;
            case "HEAD":
                path.head(operation);
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
            op.setHttpOperation(httpMethod);
            op.setPath('/' + generateRelativePath(resource));

            // @see BallerinaOperation#buildContext
            String resName = (resource.functionName().text() + "_" + generateRelativePath(resource))
                    .replaceAll("\\{///\\}", "_");
            op.getOperation().setOperationId(getOperationId(idIncrement, resName));
            op.getOperation().setParameters(null);

            // Parsing annotations.
            this.parseResourceConfigAnnotationAttachment(resource, op);
            this.addResourceParameters(resource, op);
            this.parseResponsesAnnotationAttachment(resource, op);
        }
        return op;
    }

    private void parseResponsesAnnotationAttachment(FunctionDefinitionNode resource, OperationAdaptor op) {

        FunctionSignatureNode functionSignatureNode = resource.functionSignature();
        Optional<ReturnTypeDescriptorNode> returnTypeDescriptorNode = functionSignatureNode.returnTypeDesc();
        Operation operation = op.getOperation();
        Response response = new Response();
        if (returnTypeDescriptorNode.isPresent()) {
            ReturnTypeDescriptorNode returnNode = returnTypeDescriptorNode.orElseThrow();
            NodeList<AnnotationNode> annotations = returnNode.annotations();
            Node typeNode = returnNode.type();
            if (!annotations.isEmpty()) {

            } else {
                if (typeNode.kind().equals(SyntaxKind.QUALIFIED_NAME_REFERENCE)) {
                    QualifiedNameReferenceNode qNode = (QualifiedNameReferenceNode) typeNode;
                    if (qNode.modulePrefix().toString().trim().equals("http") && qNode.identifier().toString().trim().equals("Ok")) {
                        response.description(qNode.identifier().toString());
                        operation.response(200, response);
                    }
                } else if (typeNode.kind().equals(SyntaxKind.STRING_TYPE_DESC)) {
//                    ApiResponse apiResponse = new ApiResponse();
//                    apiResponse.description("Successful");
//                    apiResponse.content(new Content().addMediaType("text/plain",
//                            new io.swagger.v3.oas.models.media.MediaType().schema(new StringSchema())));
                    HeaderParameter responseBody = new HeaderParameter();
//                    operationAdaptor.getOperation().addConsumes(applicationJson);
//                    bodyParameter.description(applicationJson);
//                    bodyParameter.name(Constants.PAYLOAD);
//                    operationAdaptor.getOperation().addParameter(bodyParameter);
                    operation.addProduces("text/plain");
//                    responseBody.description("text/plain");
//                    responseBody.name("response");
//                    operation.addParameter(responseBody);
                    response.setDescription("test/plain");
                    operation.addResponse("200", response);


                }
            }
        } else {

            response.description("Successful").
                    example(MediaType.APPLICATION_JSON, "Ok");
            operation.response(200, response);
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
        //Set Path
         if (generateRelativePath(resource) != null) {
            String path = generateRelativePath(resource).trim();
            operationAdaptor.setPath(path);
         } else {
            operationAdaptor.setPath("/");
         }
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
                        Map<String, Model> definitions = new HashMap<>();
                        for (AnnotationNode annotation: annotations) {
                            handlePayloadAnnotation(operationAdaptor, (RequiredParameterNode) expr, bodyParam,
                                    definitions, annotation);
                        }
                    }
                }
            }
        }
    }

    private void handlePayloadAnnotation(OperationAdaptor operationAdaptor, RequiredParameterNode expr,
                                         RequiredParameterNode queryParam, Map<String, Model> definitions,
                                         AnnotationNode annotation) {

        if ((annotation.annotReference().toString()).trim().equals(HTTP_PAYLOAD) &&
                annotation.annotValue().isPresent()) {
            // Creating request body - required.
            BodyParameter bodyParameter = new BodyParameter();
            MappingConstructorExpressionNode mapMime = annotation.annotValue().orElseThrow();
            SeparatedNodeList<MappingFieldNode> fields = mapMime.fields();
            if (!fields.isEmpty() || fields.size() != 0) {
                handleMultipleMIMETypes(operationAdaptor, bodyParameter, fields, expr, queryParam, definitions);
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
                        addConsumes(operationAdaptor, bodyParameter, MediaType.TEXT_PLAIN);
                        break;
                    default:
                        if (queryParam.typeName() instanceof SimpleNameReferenceNode) {
                            handleReferencePayload(operationAdaptor,
                                    expr, queryParam, definitions, MediaType.APPLICATION_JSON);
                        }
                        break;
                }
            }

        }
    }

    private void handleMultipleMIMETypes(OperationAdaptor operationAdaptor, BodyParameter bodyParameter,
                                         SeparatedNodeList<MappingFieldNode> fields, RequiredParameterNode expr,
                                         RequiredParameterNode queryParam, Map<String, Model> definitions) {

        for (MappingFieldNode fieldNode: fields) {
            if (fieldNode.children() != null) {
                ChildNodeList nodeList = fieldNode.children();
                Iterator<Node> itNode = nodeList.iterator();
                while (itNode.hasNext()) {
                    Node nextNode = itNode.next();
                    if (nextNode instanceof ListConstructorExpressionNode) {
                        SeparatedNodeList mimeList = ((ListConstructorExpressionNode) nextNode).expressions();
                        if (mimeList.size() != 0) {
                            for (Object mime : mimeList) {
                                if (mime instanceof BasicLiteralNode) {
                                    String mimeType = ((BasicLiteralNode) mime).literalToken().text().
                                            replaceAll("\"", "");
                                    if (queryParam.typeName() instanceof SimpleNameReferenceNode) {
                                        handleReferencePayload(operationAdaptor, expr, queryParam, definitions,
                                                mimeType);
                                    } else {
                                        operationAdaptor.getOperation().addConsumes(mimeType);
                                        bodyParameter.description("Optional description");
                                        bodyParameter.name(Constants.PAYLOAD);
                                        operationAdaptor.getOperation().addParameter(bodyParameter);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleReferencePayload(OperationAdaptor operationAdaptor, RequiredParameterNode expr,
                                         RequiredParameterNode queryParam, Map<String, Model> definitions,
                                        String mediaType) {

        // Creating request body - required.
        SimpleNameReferenceNode referenceNode = (SimpleNameReferenceNode) queryParam.typeName();
        Optional<Symbol> symbol = semanticModel.symbol(referenceNode);
        TypeSymbol typeSymbol = (TypeSymbol) symbol.orElseThrow();
        handleRecordPayload(queryParam, definitions, typeSymbol);
        // Creating "Request rq" parameter
        BodyParameter messageParameter = new BodyParameter();
        messageParameter.setName(expr.paramName().get().text());
        RefModel refModel = new RefModel();
        refModel.setReference(queryParam.typeName().toString().trim());
        messageParameter.setSchema(refModel);
        //  Adding conditional check for http delete operation as it cannot have body
        //  parameter.
        if (!operationAdaptor.getHttpOperation().equalsIgnoreCase("delete")) {
            operationAdaptor.getOperation().addConsumes(mediaType);
            operationAdaptor.getOperation().addParameter(messageParameter);
        }
    }

    private void handleRecordPayload(RequiredParameterNode queryParam, Map<String, Model> definitions,
                                     TypeSymbol typeSymbol) {
        String componentName = typeSymbol.getName().orElseThrow().trim();
        ModelImpl messageModel = new ModelImpl();
        if (typeSymbol instanceof TypeReferenceTypeSymbol) {
            TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeSymbol;
            // Handle record type request body
            if (typeRef.typeDescriptor() instanceof RecordTypeSymbol) {
                RecordTypeSymbol recordTypeSymbol = (RecordTypeSymbol) typeRef.typeDescriptor();
                Map<String, RecordFieldSymbol> rfields = recordTypeSymbol.fieldDescriptors();
                for (Map.Entry<String, RecordFieldSymbol> field: rfields.entrySet()) {
                    String type = field.getValue().typeDescriptor().typeKind().toString().toLowerCase(Locale.ENGLISH);
                    Property property = getOpenApiProperty(type);
                    if (type.equals(Constants.TYPE_REFERENCE) && property instanceof RefProperty) {
                        RefModel refModel = new RefModel();
                        refModel.setReference(field.getValue().typeDescriptor().getName().orElseThrow().trim());
                        ((RefProperty) property).set$ref(refModel.get$ref());
                        Optional<TypeSymbol> recordSymbol = semanticModel.type(field.getValue().location().lineRange());
                        TypeSymbol recordVariable =  recordSymbol.orElseThrow();
                        if (recordVariable.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                            TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) recordVariable;
                            handleRecordPayload(queryParam, definitions, typeRecord);
                        }
                    }
                    if (property instanceof ArrayProperty) {
                        setArrayProperty(queryParam, definitions, field.getValue(), (ArrayProperty) property);
                    }
                    messageModel.property(field.getKey(), property);
                }
            }
        }

        if (!definitions.containsKey(componentName)) {
            //Set properties for the schema
            definitions.put(componentName, messageModel);
            this.openApiDefinition.setDefinitions(definitions);
        }
    }

    private void setArrayProperty(RequiredParameterNode queryParam, Map<String, Model> definitions,
                                  RecordFieldSymbol field, ArrayProperty property) {

        TypeSymbol symbol = field.typeDescriptor();
        int arrayDimensions = 0;
        while (symbol instanceof ArrayTypeSymbol) {
            arrayDimensions = arrayDimensions + 1;
            ArrayTypeSymbol arrayTypeSymbol = (ArrayTypeSymbol) symbol;
            symbol = arrayTypeSymbol.memberTypeDescriptor();
        }
        //handle record field has nested record array type ex: Tag[] tags
        Property symbolProperty  = getOpenApiProperty(symbol.typeKind().getName());
        if (symbolProperty instanceof RefProperty) {
            RefModel refModel = new RefModel();
            refModel.setReference(symbol.getName().orElseThrow().trim());
            ((RefProperty) symbolProperty).set$ref(refModel.get$ref());

            //Set the record model to the definition
            if (symbol.typeKind().equals(TypeDescKind.TYPE_REFERENCE)) {
                TypeReferenceTypeSymbol typeRecord = (TypeReferenceTypeSymbol) symbol;
                handleRecordPayload(queryParam, definitions, typeRecord);
            }
        }
        //Handle nested array type
        if (arrayDimensions > 1) {
            property.setItems(handleArray(arrayDimensions - 1, symbolProperty, new ArrayProperty()));
        } else {
            property.setItems(symbolProperty);
        }
    }

    private ArrayProperty handleArray(int arrayDimensions, Property property, ArrayProperty arrayProperty) {
        if (arrayDimensions > 1) {
            ArrayProperty narray = new ArrayProperty();
            arrayProperty.setItems(handleArray(arrayDimensions - 1, property,  narray));
        } else if (arrayDimensions == 1) {
            arrayProperty.setItems(property);
        }
        return arrayProperty;
    }

    private void addConsumes(OperationAdaptor operationAdaptor, BodyParameter bodyParameter, String applicationJson) {
        operationAdaptor.getOperation().addConsumes(applicationJson);
        bodyParameter.description(applicationJson);
        bodyParameter.name(Constants.PAYLOAD);
        operationAdaptor.getOperation().addParameter(bodyParameter);
    }

    /**
     * Create {@code Parameters} model for openApi operation.
     *
     * @param resource The resource used to map with operation.
     * @param operation            The openApi operation.
     */
    private void createParametersModel(FunctionDefinitionNode resource, Operation operation) {
        List<Parameter> parameters = new LinkedList<>();

        //Set path parameters
        NodeList<Node> pathParams = resource.relativeResourcePath();
        for (Node param: pathParams) {
            if (param instanceof ResourcePathParameterNode) {
                ResourcePathParameterNode pathParam = (ResourcePathParameterNode) param;
                Parameter parameter = buildParameter(Constants.PATH, pathParam);
                parameter.setName(pathParam.paramName().text());

                //Set param description
//                parameter.setDescription();
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
                        !queryParam.paramName().orElseThrow().text().equals(Constants.PATH)) {
                    Parameter parameter = buildParameter(Constants.QUERY, queryParam);
                    parameter.setRequired(true);
                    parameters.add(parameter);
                }
            }
        }
        operation.setParameters(parameters);
    }

    /**
     * Parse 'ResourceConfig' annotation attachment and build a resource operation.
     *
     * @param resource  The ballerina resource definition.
     * @param operation The openApi operation.
     */
    private void parseResourceConfigAnnotationAttachment(FunctionDefinitionNode resource, OperationAdaptor operation) {

        String path = resource.relativeResourcePath().get(0).toString();
        if (path.equals(".")) {
            operation.setPath("/");
        } else {
            operation.setPath(path);
        }

        // TODO: Implement consumer definitions.
        // TODO: Implement producer definitions.
        // TODO: Implement security definitions.

            // TODO: Implement security definitions.
            //this.createSecurityDefinitions(resourceConfigAnnotation.get().getAttributeNameValuePairs()
            // .get("authorizations"), operation);
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
                    relativePath = relativePath + "/";
                } else {
                    relativePath = relativePath + node.toString();
                }
            }
        }
        return relativePath;
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
    private Parameter buildParameter(String in, Node paramAttributes) {
        Parameter param = null;

        switch (in) {
            case Constants.BODY:
                // TODO : support for inline and other types of schemas
                break;
            case Constants.QUERY:
                QueryParameter qParam = new QueryParameter();
                RequiredParameterNode queryParam = (RequiredParameterNode) paramAttributes;
                qParam.setName(queryParam.paramName().get().text());
                qParam.setType(convertBallerinaTypeToOpenAPIType(queryParam.typeName().toString().trim()));
                param = qParam;
                break;
            case Constants.HEADER:
                param = new HeaderParameter();
                break;
            case Constants.COOKIE:
                param = new CookieParameter();
                break;
            case Constants.FORM:
                param = new FormParameter();
                break;
            case Constants.PATH:
            default:
                PathParameter pParam = new PathParameter();
                ResourcePathParameterNode pathParam = (ResourcePathParameterNode) paramAttributes;
                pParam.setType(convertBallerinaTypeToOpenAPIType(pathParam.typeDescriptor().toString().trim()));
                pParam.setName(pathParam.paramName().text());
                param = pParam;
        }

        return param;
    }

    /**
     * Retrieves a matching OpenApi {@link Property} for a provided ballerina type.
     *
     * @param type ballerina type name as a String
     * @return OpenApi {@link Property} for type defined by {@code type}
     */
    private Property getOpenApiProperty(String type) {
        Property property;

        switch (type) {
            case Constants.STRING:
                property = new StringProperty();
                break;
            case Constants.BOOLEAN:
                property = new BooleanProperty();
                break;
            case Constants.ARRAY:
                property = new ArrayProperty();
                break;
            case Constants.NUMBER:
            case Constants.INT:
            case Constants.INTEGER:
                property = new IntegerProperty();
                break;
            case Constants.TYPE_REFERENCE:
            case Constants.TYPEREFERENCE:
                property = new RefProperty();
                break;
            default:
                property = new ObjectProperty();
                break;
        }

        return property;
    }
}
