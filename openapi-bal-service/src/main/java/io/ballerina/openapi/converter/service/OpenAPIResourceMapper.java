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
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.openapi.converter.Constants;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


/**
 * This class will do resource mapping from ballerina to openApi.
 *
 * @since 2.0.0
 */
public class OpenAPIResourceMapper {
    private SemanticModel semanticModel;
    private Paths pathObject = new Paths();
    private Components components = new Components();

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
            useMultiResourceMapper(resource, methods);
//            if (!methods.isEmpty()) {
//                useMultiResourceMapper(resource, methods);
//            }
//            else {
////                useDefaultResourceMapper(resource);
//            }
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

// Enable after refactoring
//    /**
//     * Resource mapper when a resource has default  one http method.
//     * @param resource The ballerina resource.
//     */
//    private void useDefaultResourceMapper(FunctionDefinitionNode resource) {
//        String httpMethod = getHttpMethods(resource, true).get(0);
//        OperationAdaptor operationAdaptor = this.convertResourceToOperation(resource, httpMethod, 1);
//        operationAdaptor.setHttpOperation(httpMethod);
//        String path = getPath(resource);
//        io.swagger.v3.oas.models.Operation operation = operationAdaptor.getOperation();
//        generatePathItem(httpMethod, pathObject, operation, path);
//    }

    private void generatePathItem(String httpMethod, Paths path, Operation operation, String pathName) {
        PathItem pathItem = new PathItem();
        switch (httpMethod.trim().toUpperCase(Locale.ENGLISH)) {
            case Constants.GET:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setGet(operation);
                } else {
                    pathItem.setGet(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.PUT:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPut(operation);
                } else {
                    pathItem.setPut(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.POST:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPost(operation);
                } else {
                    pathItem.setPost(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.DELETE:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setDelete(operation);
                } else {
                    pathItem.setDelete(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.OPTIONS:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setOptions(operation);
                } else {
                    pathItem.setOptions(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.PATCH:
                if (pathObject.containsKey(pathName)) {
                    pathObject.get(pathName).setPatch(operation);
                } else {
                    pathItem.setPatch(operation);
                    path.addPathItem(pathName, pathItem);
                }
                break;
            case Constants.HEAD:
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
            OpenAPIResponseMapper openAPIResponseMapper = new OpenAPIResponseMapper(semanticModel, components);
            openAPIResponseMapper.parseResponsesAnnotationAttachment(resource, op);
//            this.parseResponsesAnnotationAttachment(resource, op);
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
        // Need to check this since ballerina parser issue not generated when `GET` has requestBody
        if (!"GET".toLowerCase(Locale.ENGLISH).equalsIgnoreCase(operationAdaptor.getHttpOperation())) {
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
            httpMethods.add(Constants.GET);
            httpMethods.add(Constants.PUT);
            httpMethods.add(Constants.POST);
            httpMethods.add(Constants.DELETE);
            httpMethods.add(Constants.PATCH);
            httpMethods.add(Constants.OPTIONS);
            httpMethods.add(Constants.HEAD);
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
