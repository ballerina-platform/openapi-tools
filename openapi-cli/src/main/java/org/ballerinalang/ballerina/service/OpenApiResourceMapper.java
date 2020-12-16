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

import com.google.common.collect.Lists;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
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
import io.swagger.models.properties.StringProperty;
import org.ballerinalang.ballerina.openapi.convertor.ConverterUtils;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.net.http.HttpConstants;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import static org.ballerinalang.ballerina.ConverterUtils.convertBallerinaTypeToOpenAPIType;
import static org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValueField;

/**
 * This class will do resource mapping from ballerina to openApi.
 */
public class OpenApiResourceMapper {
    private final String httpAlias;
    private final String openApiAlias;
    private final Swagger openApiDefinition;

    /**
     * Initializes a resource parser for openApi.
     *
     * @param openApi      The OpenAPI definition.
     * @param httpAlias    The alias for ballerina/http module.
     * @param openApiAlias The alias for ballerina.openapi module.
     */
    OpenApiResourceMapper(Swagger openApi, String httpAlias, String openApiAlias) {
        this.httpAlias = httpAlias;
        this.openApiAlias = openApiAlias;
        this.openApiDefinition = openApi;
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
        switch (httpMethod.trim().toUpperCase()) {
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
            Response response = new Response().description("Successful").
                    example(MediaType.APPLICATION_JSON, "Ok");
            op.getOperation().response(200, response);

            // @see BallerinaOperation#buildContext
            String resName = (resource.functionName().text() + "_" + generateRelativePath(resource))
                    .replaceAll("\\{///\\}", "_");
            op.getOperation().setOperationId(getOperationId(idIncrement, resName));
            op.getOperation().setParameters(null);

            // Parsing annotations.
            this.parseResourceConfigAnnotationAttachment(resource, op);
            this.addResourceParameters(resource, op);
//            this.parseResponsesAnnotationAttachment(resource, op.getOperation());
        }
        return op;
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
     * Creates headers definitions for openApi response.
     *
     * @param annotationExpression The annotation attribute value which has the headers.
     * @param response             The openApi response.
     */
    private void createHeadersModel(BLangExpression annotationExpression, Response response) {
        if (null != annotationExpression) {
            BLangListConstructorExpr headerArray = (BLangListConstructorExpr) annotationExpression;

            for (ExpressionNode headersValue : headerArray.getExpressions()) {
                Map<String, BLangExpression> headersAttributes =
                        ConverterUtils.listToMap(((BLangRecordLiteral) headersValue).getFields());
                Map<String, Property> headers = new HashMap<>();

                if (headersAttributes.containsKey(ConverterConstants.ATTR_NAME) && headersAttributes
                        .containsKey(ConverterConstants.ATTR_HEADER_TYPE)) {
                    String headerName = ConverterUtils
                            .getStringLiteralValue(headersAttributes.get(ConverterConstants.ATTR_NAME));
                    String type = ConverterUtils
                            .getStringLiteralValue(headersAttributes.get(ConverterConstants.ATTR_HEADER_TYPE));
                    Property property = getOpenApiProperty(type);

                    if (headersAttributes.containsKey(ConverterConstants.ATTR_DESCRIPTION)) {
                        property.setDescription(ConverterUtils
                                .getStringLiteralValue(headersAttributes.get(ConverterConstants.ATTR_DESCRIPTION)));
                    }
                    headers.put(headerName, property);
                }
                response.setHeaders(headers);
            }
        }
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
        //Add path parameters if in path
        this.createParametersModel(resource, operationAdaptor.getOperation());

        if (!"get".equalsIgnoreCase(operationAdaptor.getHttpOperation())) {

            // Creating request body - required.
            ModelImpl messageModel = new ModelImpl();
            messageModel.setType("object");
            Map<String, Model> definitions = new HashMap<>();
            if (!definitions.containsKey(ConverterConstants.ATTR_REQUEST)) {
                definitions.put(ConverterConstants.ATTR_REQUEST, messageModel);
                this.openApiDefinition.setDefinitions(definitions);
            }

            // Creating "Request rq" parameter
            //TODO request param
//            BodyParameter messageParameter = new BodyParameter();
//            messageParameter.setName(resource.getParameters().get(0).getName().getValue());
//            RefModel refModel = new RefModel();
//            refModel.setReference(ConverterConstants.ATTR_REQUEST);
//            messageParameter.setSchema(refModel);
            //Adding conditional check for http delete operation as it cannot have body parameter.
//            if (!operationAdaptor.getHttpOperation().equalsIgnoreCase("delete")) {
//                operationAdaptor.getOperation().addParameter(messageParameter);
//            }
        }
    }

    /**
     * Create {@code Parameters} model for openApi operation.
     *
     * @param resource The resource used to map with operation.
     * @param operation            The openApi operation.
     */
    private void createParametersModel(FunctionDefinitionNode resource, Operation operation) {
        List<Parameter> parameters = new LinkedList<>();
        String in;

        //Set path parameters
        NodeList<Node> pathParams = resource.relativeResourcePath();
        for (Node param: pathParams) {
            if (param instanceof ResourcePathParameterNode) {
                in = "path";
                ResourcePathParameterNode pathParam = (ResourcePathParameterNode) param;
                Parameter parameter = buildParameter(in, pathParam);
                parameter.setName(pathParam.paramName().text());

                //Set param description
//                parameter.setDescription();
                parameter.setRequired(true);
                parameters.add(parameter);
            }
        }

        // set query parameter
        FunctionSignatureNode functionSignature = (FunctionSignatureNode) resource.functionSignature();
        SeparatedNodeList<ParameterNode> paramExprs = functionSignature.parameters();
        for (ParameterNode expr : paramExprs) {
            if (expr instanceof RequiredParameterNode) {
                RequiredParameterNode queryParam = (RequiredParameterNode) expr;
                if (queryParam.typeName() instanceof BuiltinSimpleNameReferenceNode) {
                    in = "query";
                    Parameter parameter = buildParameter(in, queryParam);
                    parameter.setRequired(true);
                    parameters.add(parameter);
                }
            }
        }
        operation.setParameters(parameters);
    }

    private void parseMultiResourceInfoAnnotationAttachment(AnnotationAttachmentNode multiResourceInfoAnnotation,
                                                            Operation operation, String httpMethod) {
        // Get multi resource information
        if (multiResourceInfoAnnotation != null) {
            BLangRecordLiteral bLiteral = (BLangRecordLiteral) ((BLangAnnotationAttachment) multiResourceInfoAnnotation)
                    .getExpression();
            // In multi resource information there is only one key exist that is `resource information`.
            BLangRecordKeyValueField resourceInformationAttr = bLiteral.fields.size() == 1
                    ? (BLangRecordKeyValueField) bLiteral.fields.get(0)
                    : null;
            if (resourceInformationAttr != null) {
                for (RecordLiteralNode.RecordField resourceInfo :
                        ((BLangRecordLiteral) resourceInformationAttr.valueExpr).getFields()) {
                    BLangRecordKeyValueField resourceInfoKeyValue = (BLangRecordKeyValueField) resourceInfo;
                    if (((BLangLiteral) resourceInfoKeyValue.key.expr).value.equals(httpMethod)) {
//                        addResourceInfoToOperation(((BLangRecordLiteral) resourceInfoKeyValue.valueExpr), operation);
                    }
                }
            }
        }
    }

    private void addResourceInfoToOperation(FunctionDefinitionNode resource, Operation operation) {
        //TODO createTags
        //TODO addResource Summery
        //TODO addDescription
        //Parameter Maps
        if (resource != null) {
            this.createParametersModel(resource, operation);
        }
    }

    /**
     * Parse 'ResourceConfig' annotation attachment and build a resource operation.
     *
     * @param resource  The ballerina resource definition.
     * @param operation The openApi operation.
     */
    private void parseResourceConfigAnnotationAttachment(FunctionDefinitionNode resource, OperationAdaptor operation) {

            operation.setPath(resource.relativeResourcePath().get(0).toString());
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
        return Lists.reverse(new ArrayList<>(httpMethods));
    }

    private String generateRelativePath(FunctionDefinitionNode resource) {

        String relativePath = "";
        if (!resource.relativeResourcePath().isEmpty()) {
            for (Node node: resource.relativeResourcePath()) {
                if (node instanceof ResourcePathParameterNode) {
                    ResourcePathParameterNode pathNode = (ResourcePathParameterNode) node;
                    relativePath = relativePath + "{" + pathNode.paramName() + "}";
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
        String path = "/" + resource.relativeResourcePath().get(0).toString();

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
            case "body":
                // TODO : support for inline and other types of schemas
//                BodyParameter bParam = new BodyParameter();
//                RefModel m = new RefModel();
//                m.set$ref(ConverterUtils
//                        .getStringLiteralValue(paramAttributes.get(ConverterConstants.ATTR_TYPE)));
//                bParam.setSchema(m);
//                param = bParam;
                break;
            case "query":
                QueryParameter qParam = new QueryParameter();
                RequiredParameterNode queryParam = (RequiredParameterNode) paramAttributes;
                qParam.setName(queryParam.paramName().get().text());
                qParam.setType(convertBallerinaTypeToOpenAPIType(queryParam.typeName().toString().trim()));
                param = qParam;
                break;
            case "header":
                param = new HeaderParameter();
                break;
            case "cookie":
                param = new CookieParameter();
                break;
            case "form":
                param = new FormParameter();
                break;
            case "path":
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
            case "string":
                property = new StringProperty();
                break;
            case "boolean":
                property = new BooleanProperty();
                break;
            case "array":
                property = new ArrayProperty();
                break;
            case "number":
            case "integer":
                property = new IntegerProperty();
                break;
            default:
                property = new ObjectProperty();
                break;
        }

        return property;
    }
}
