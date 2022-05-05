/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.openapi.validator;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
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
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.openapi.validator.error.CompilationError;
import io.ballerina.openapi.validator.model.Filter;
import io.ballerina.openapi.validator.model.OpenAPIPathSummary;
import io.ballerina.openapi.validator.model.ResourceMethod;
import io.ballerina.openapi.validator.model.ResourcePathSummary;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.openapi.validator.Constants.BALLERINA;
import static io.ballerina.openapi.validator.Constants.FULL_STOP;
import static io.ballerina.openapi.validator.Constants.HEADER_NAME;
import static io.ballerina.openapi.validator.Constants.HTTP;
import static io.ballerina.openapi.validator.Constants.HTTP_CALLER;
import static io.ballerina.openapi.validator.Constants.HTTP_HEADER;
import static io.ballerina.openapi.validator.Constants.HTTP_REQUEST;
import static io.ballerina.openapi.validator.Constants.JSON;
import static io.ballerina.openapi.validator.Constants.SLASH;
import static io.ballerina.openapi.validator.Constants.YAML;
import static io.ballerina.openapi.validator.Constants.YML;

/**
 * This util class contain the common util functions that use in validator process.
 *
 * @since 2.0.0
 */
public class ValidatorUtils {

    /**
     * This util method is to refactor the OAS path when it includes the curly brace.
     *
     * @param path OAS path
     * @return string with refactored path
     */
    public static String getNormalizedPath(String path) {
        return path;
    }

    /**
     * Parse and get the {@link OpenAPI} for the given OpenAPI contract.
     *
     * @param definitionURI     URI for the OpenAPI contract
     * @return {@link OpenAPI}  OpenAPI model
     * @throws IOException in case of exception
     */
    public static OpenAPI parseOpenAPIFile(SyntaxNodeAnalysisContext context, String definitionURI, Location location)
            throws  IOException {
        Path contractPath = Paths.get(definitionURI);
        ParseOptions parseOptions = new ParseOptions();

        if (!Files.exists(contractPath)) {
            updateContext(context, CompilationError.INVALID_CONTRACT_PATH, location, DiagnosticSeverity.ERROR);
        }
        if (!(definitionURI.endsWith(YAML) || definitionURI.endsWith(JSON) || definitionURI.endsWith(YML))) {
            updateContext(context, CompilationError.INVALID_CONTRACT_FORMAT, location, DiagnosticSeverity.ERROR);
        }
        String openAPIFileContent = Files.readString(Paths.get(definitionURI));
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openAPIFileContent, null,
                parseOptions);
        OpenAPI api = parseResult.getOpenAPI();
        if (api == null) {
            updateContext(context, CompilationError.PARSER_EXCEPTION, location, DiagnosticSeverity.ERROR,
                    definitionURI);
        }
        return api;
    }

    /**
     * This util function is to check the given service is http service.
     *
     * @param serviceNode    Service node for analyse
     * @param semanticModel  Semantic model
     * @return  boolean output
     */
    public static boolean isHttpService(ServiceDeclarationNode serviceNode, SemanticModel semanticModel) {
        Optional<Symbol> serviceSymbol = semanticModel.symbol(serviceNode);
        ServiceDeclarationSymbol serviceNodeSymbol = (ServiceDeclarationSymbol) serviceSymbol.get();
        List<TypeSymbol> listenerTypes = (serviceNodeSymbol).listenerTypes();
        for (TypeSymbol listenerType : listenerTypes) {
            if (isHttpListener(listenerType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHttpListener(TypeSymbol listenerType) {
        if (listenerType.typeKind() == TypeDescKind.UNION) {
            return ((UnionTypeSymbol) listenerType).memberTypeDescriptors().stream()
                    .filter(typeDescriptor -> typeDescriptor instanceof TypeReferenceTypeSymbol)
                    .map(typeReferenceTypeSymbol -> (TypeReferenceTypeSymbol) typeReferenceTypeSymbol)
                    .anyMatch(typeReferenceTypeSymbol -> isHttpModule(typeReferenceTypeSymbol.getModule().get()));
        }

        if (listenerType.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            return isHttpModule(((TypeReferenceTypeSymbol) listenerType).typeDescriptor().getModule().get());
        }
        return false;
    }

    private static boolean isHttpModule(ModuleSymbol moduleSymbol) {
        if (moduleSymbol.getName().isPresent()) {
            return HTTP.equals(moduleSymbol.getName().get()) && BALLERINA.equals(moduleSymbol.id().orgName());
        } else {
            return false;
        }
    }

    public static void updateContext(SyntaxNodeAnalysisContext context, CompilationError error, Location location,
                                     DiagnosticSeverity severity) {

        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(error.getCode(),
                error.getDescription(), severity);
        Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo
                , location);
        context.reportDiagnostic(diagnostic);
    }

    public static void updateContext(SyntaxNodeAnalysisContext context, CompilationError error,
                                     Location location, DiagnosticSeverity severity, Object... args) {
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(error.getCode(), error.getDescription(), severity);
        Diagnostic diagnostic = DiagnosticFactory.createDiagnostic(diagnosticInfo, location, args);
        context.reportDiagnostic(diagnostic);
    }

    /**
     * This util function uses to filter and summaries the operations in OpenAPI.
     *
     * @param filter  Filters that need to be applied the select the operation for validations
     * @return List with {@link OpenAPIPathSummary} .
     */
    private static boolean applyFilter(Filter filter, List<String> tags, String operationId,
                                       SyntaxNodeAnalysisContext context) {
        boolean tagEnabled = filter.getTag() != null;
        boolean operationEnabled = filter.getOperation() != null;
        boolean excludeTagsEnabled = filter.getExcludeTag() != null;
        boolean excludeOperationEnable = filter.getExcludeOperation() != null;

        if (excludeTagsEnabled && excludeOperationEnable) {
            return tags == null || Collections.disjoint(tags, filter.getExcludeTag())  &&
                    operationId != null && !filter.getExcludeOperation().contains(operationId);
        }

        if (tagEnabled && operationEnabled) {
            return operationId != null && filter.getOperation().contains(operationId)  ||
                    tags != null && !Collections.disjoint(tags, filter.getTag());
        }

        if (operationEnabled) {
            if (excludeTagsEnabled) {
                return tags == null || Collections.disjoint(tags, filter.getExcludeTag());
            }
            return operationId != null && filter.getOperation().contains(operationId);
        }

        if (tagEnabled) {
            if (tags != null && !Collections.disjoint(tags, filter.getTag())) {
                if (excludeOperationEnable) {
                    return operationId != null && !filter.getExcludeOperation().contains(operationId);
                }
            }
            return tags != null && !Collections.disjoint(tags, filter.getTag());
        }

        if (excludeOperationEnable) {
            return operationId != null && !filter.getExcludeOperation().contains(operationId);
        }

        return tags == null || Collections.disjoint(tags, filter.getExcludeTag());
        // if some user give the operations for filter ,
        // operation filter takes the first priority of filtering process.
    }

    /**
     * Summarize openAPI contract paths to easily access details to validate.
     * @param contract                openAPI contract
     * @return List of summarized OpenAPIPathSummary
     */
    public static List<OpenAPIPathSummary> summarizeOpenAPI(OpenAPI contract, SyntaxNodeAnalysisContext context,
                                                             Filter filter) {
        List<OpenAPIPathSummary> openAPISummaries = new ArrayList<>();
        io.swagger.v3.oas.models.Paths paths = contract.getPaths();
        paths.forEach((path, value) -> {
            OpenAPIPathSummary openAPISummary = new OpenAPIPathSummary();
            if (value != null) {
                openAPISummary.setPath(path);
                if (value.getGet() != null) {
                    addOpenAPISummary(openAPISummary, Constants.GET, value.getGet(), filter, context);
                }
                if (value.getPost() != null) {
                    addOpenAPISummary(openAPISummary, Constants.POST, value.getPost(), filter, context);
                }
                if (value.getPut() != null) {
                    addOpenAPISummary(openAPISummary, Constants.PUT, value.getPut(), filter, context);
                }
                if (value.getDelete() != null) {
                    addOpenAPISummary(openAPISummary, Constants.DELETE, value.getDelete(), filter, context);
                }
                if (value.getHead() != null) {
                    addOpenAPISummary(openAPISummary, Constants.HEAD, value.getHead(), filter, context);
                }
                if (value.getPatch() != null) {
                    addOpenAPISummary(openAPISummary, Constants.PATCH, value.getPatch(), filter, context);
                }
                if (value.getOptions() != null) {
                    addOpenAPISummary(openAPISummary, Constants.OPTIONS, value.getOptions(), filter, context);
                }
                if (value.getTrace() != null) {
                    addOpenAPISummary(openAPISummary, Constants.TRACE, value.getTrace(), filter, context);
                }
            }
            if (openAPISummary.getOperations().size() > 0) {
                openAPISummaries.add(openAPISummary);
            }
        });
        return openAPISummaries;
    }

    private static void addOpenAPISummary(OpenAPIPathSummary openAPISummary, String httpMethod, Operation operation,
                                          Filter filter, SyntaxNodeAnalysisContext context) {
        openAPISummary.addAvailableOperation(httpMethod);
        if (applyFilter(filter, operation.getTags(), operation.getOperationId(), context)) {
            openAPISummary.addOperation(httpMethod, operation);
        }
    }

    /**
     * Extract the details to be validated from the resource.
     * @param functions         documented functions
     * @return List of ResourcePathSummary
     */
    public static Map<String, ResourcePathSummary> summarizeResources(List<FunctionDefinitionNode> functions,
                                                                      SyntaxNodeAnalysisContext context) {
        // Iterate resources available in a service and extract details to be validated.
        Map<String, ResourcePathSummary> resourceSummaryList = new HashMap<>();
        for (FunctionDefinitionNode resourceNode: functions) {
            NodeList<Node> relativeResourcePathNodes = resourceNode.relativeResourcePath();
            Map<String, Node> parameterNodeMap = new HashMap<>();
            String path = generatePath(relativeResourcePathNodes, parameterNodeMap);
            if (resourceSummaryList.containsKey(path)) {
                extractResourceMethodDetails(resourceNode, path, resourceSummaryList.get(path), parameterNodeMap,
                        context);
            } else {
                ResourcePathSummary resourcePathSummary = new ResourcePathSummary(path);
                extractResourceMethodDetails(resourceNode, path, resourcePathSummary, parameterNodeMap, context);
                resourceSummaryList.put(path, resourcePathSummary);
            }
        }
        return resourceSummaryList;
    }

    /**
     * This function generates resource function path that align to OAS format.
     */
    private static String generatePath(NodeList<Node> nodes, Map<String, Node> parameterNodeMap) {
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(SLASH);
        for (Node next : nodes) {
            String node = next.toString().trim();
            if (next instanceof ResourcePathParameterNode) {
                ResourcePathParameterNode pathParameterNode = (ResourcePathParameterNode) next;
                String paramName = unescapeIdentifier(pathParameterNode.paramName().text().trim());
                node = "{" + paramName + "}";
                parameterNodeMap.put(paramName, next);
            }
            pathBuilder.append(node);
        }
        String path = pathBuilder.toString();
        if (!path.endsWith(FULL_STOP)) {
            return path;
        }
        return path.substring(0, path.length() - 1);
    }

    /**
     * This util function is to extract the function node and summarised relevant details in resource model.
     *
     * @param resourceNode  FunctionNode for resource
     * @param path          Resource path
     * @param resourcePath {@link ResourcePathSummary} that collection of resource method for specific path.
     */
    private static void extractResourceMethodDetails(FunctionDefinitionNode resourceNode, String path,
                                                     ResourcePathSummary resourcePath,
                                                     Map<String, Node> parameterNodes,
                                                     SyntaxNodeAnalysisContext context) {

        FunctionSignatureNode signatureNode = resourceNode.functionSignature();
        String httpMethod = resourceNode.functionName().text().trim();
        SeparatedNodeList<ParameterNode> parameters = signatureNode.parameters();
        // Create resource builder
        ResourceMethod.ResourceMethodBuilder resourceMethodBuilder = new ResourceMethod.ResourceMethodBuilder();
        resourceMethodBuilder.path(path);
        resourceMethodBuilder.method(httpMethod);
        resourceMethodBuilder.location(resourceNode.location());
        Map<String, Node> headers = new HashMap<>();

        for (ParameterNode param : parameters) {
            // To avoid the http:Caller validation and filter the headers and payload.
            if (param instanceof RequiredParameterNode) {
                RequiredParameterNode requiredParamNode = (RequiredParameterNode) param;
                NodeList<AnnotationNode> annotations = requiredParamNode.annotations();
                if (!requiredParamNode.typeName().toString().trim().equals(HTTP_CALLER) &&
                        !requiredParamNode.typeName().toString().trim().equals(HTTP_REQUEST)) {
                    // Set payloads and headers
                    for (AnnotationNode annotation : annotations) {
                        if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_HEADER)) {
                            List<String> annotationHeaders = extractAnnotationFieldDetails(HTTP_HEADER, HEADER_NAME,
                                    annotations, context.semanticModel());
                            String headerName = unescapeIdentifier(requiredParamNode.paramName().orElseThrow().text());
                            if (!annotationHeaders.isEmpty()) {
                                headerName = annotationHeaders.get(0);
                            }
                            headers.put(headerName, requiredParamNode);
                        } else if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_PAYLOAD)) {
                            resourceMethodBuilder.body(requiredParamNode);
                        }
                    }
                    if (annotations.isEmpty()) {
                        parameterNodes.put(requiredParamNode.paramName().orElseThrow().text(), requiredParamNode);
                    }
                }

            } else if (param instanceof DefaultableParameterNode) {
                DefaultableParameterNode defaultParam = (DefaultableParameterNode) param;
                NodeList<AnnotationNode> annotations = defaultParam.annotations();
                if (!defaultParam.typeName().toString().equals(HTTP_CALLER) &&
                        !defaultParam.typeName().toString().equals(HTTP_REQUEST)) {
                    // Set payloads and headers
                    for (AnnotationNode annotation : annotations) {
                        if ((annotation.annotReference().toString()).trim().equals(Constants.HTTP_HEADER)) {
                            headers.put(defaultParam.paramName().orElseThrow().text(), defaultParam);
                        }
                    }
                    if (annotations.isEmpty()) {
                        parameterNodes.put(defaultParam.paramName().orElseThrow().text(), defaultParam);
                    }
                }
            }
        }
        resourceMethodBuilder.headers(headers);
        resourceMethodBuilder.parameters(parameterNodes);
        resourceMethodBuilder.returnNode(signatureNode.returnTypeDesc().orElse(null));
        resourcePath.addMethod(httpMethod, resourceMethodBuilder.build());
    }



    public static String unescapeIdentifier(String parameterName) {
        return parameterName.replaceAll("\\\\", "");
    }

    /**
     * This util functions is used to extract the details of annotation field.
     *
     * @param annotationReference   Annotation reference name that need to extract
     * @param annotationField       Annotation field name that need to extract details.
     * @param annotations            Annotation nodes
     * @return List of string
     */

    public static List<String> extractAnnotationFieldDetails(String annotationReference, String annotationField,
                                                             NodeList<AnnotationNode> annotations,
                                                             SemanticModel semanticModel) {

        List<String> fieldValues = new ArrayList<>();
        Iterator<AnnotationNode> iterator = annotations.stream().iterator();
        while (iterator.hasNext()) {
            AnnotationNode annotation = iterator.next();
            Node annotReference = annotation.annotReference();
            if (annotReference.toString().trim().equals(annotationReference) && annotation.annotValue().isPresent()) {
                MappingConstructorExpressionNode listOfAnnotValue = annotation.annotValue().get();
                for (MappingFieldNode field : listOfAnnotValue.fields()) {
                    SpecificFieldNode fieldNode = (SpecificFieldNode) field;
                    if (!((fieldNode).fieldName().toString().trim().equals(annotationField)) &&
                            fieldNode.valueExpr().isEmpty()) {
                        continue;
                    }
                    ExpressionNode expressionNode = fieldNode.valueExpr().get();
                    if (expressionNode instanceof ListConstructorExpressionNode) {
                        SeparatedNodeList mimeList = ((ListConstructorExpressionNode) expressionNode).expressions();
                        for (Object mime : mimeList) {
                            if (!(mime instanceof BasicLiteralNode)) {
                                continue;
                            }
                            fieldValues.add(((BasicLiteralNode) mime).literalToken().text().trim().
                                    replaceAll("\"", ""));
                        }
                    } else if (expressionNode instanceof QualifiedNameReferenceNode && semanticModel != null) {
                        QualifiedNameReferenceNode moduleRef = (QualifiedNameReferenceNode) expressionNode;
                        Optional<Symbol> refSymbol = semanticModel.symbol(moduleRef);
                        if (refSymbol.isPresent() && (refSymbol.get().kind() == SymbolKind.CONSTANT)
                                && ((ConstantSymbol) refSymbol.get()).resolvedValue().isPresent()) {
                            String mediaType = ((ConstantSymbol) refSymbol.get()).resolvedValue().get();
                            fieldValues.add(mediaType.replaceAll("\"", ""));
                        }
                    } else {
                        fieldValues.add(expressionNode.toString().trim().replaceAll("\"", ""));
                    }
                }
            }
        }
        return fieldValues;
    }

    public static String getMediaType(SyntaxKind kind) {
        String mediaType;
        // Return mediaType
        switch (kind) {
            case STRING_TYPE_DESC:
                mediaType = "text/plain";
                break;
            case XML_TYPE_DESC:
                mediaType = "application/xml";
                break;
            case MAP_TYPE_DESC:
            case SIMPLE_NAME_REFERENCE:
            case RECORD_TYPE_DESC:
            case INT_TYPE_DESC:
            case BOOLEAN_TYPE_DESC:
            case ARRAY_TYPE_DESC:
            case TABLE_TYPE_DESC:
            case DECIMAL_TYPE_DESC:
            case BYTE_TYPE_DESC:
            case FLOAT_KEYWORD:
            case JSON_TYPE_DESC:
            default:
                mediaType = "application/json";
                break;
        }
        return mediaType;
    }
    public static String getMediaType(TypeDescKind kind) {
        String mediaType;
        // Return mediaType
        switch (kind) {
            case STRING:
                mediaType = "text/plain";
                break;
            case XML:
                mediaType = "application/xml";
                break;
            case MAP:
            case OBJECT:
            case RECORD:
            case TYPE_REFERENCE:
            case JSON:
            default:
                mediaType = "application/json";
                break;
        }
        return mediaType;
    }
    /**
     * This method will extract reference type by splitting the reference string.
     *
     * @param referenceVariable - Reference String
     * @return Reference variable name
     */
    public static Optional<String> extractReferenceType(String referenceVariable)  {
        if (referenceVariable.startsWith("#") && referenceVariable.contains("/")) {
            String[] refArray = referenceVariable.split("/");
            return Optional.of(refArray[refArray.length - 1]);
        } else {
            // Note : Current implementation will not support external links a references.
            // TODO: "Invalid reference value : " + referenceVariable + "\nBallerina only supports local reference
            // values.");
            return Optional.empty();
        }
    }
}
