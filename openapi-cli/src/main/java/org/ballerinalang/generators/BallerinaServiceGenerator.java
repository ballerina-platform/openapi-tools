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

package org.ballerinalang.generators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ErrorTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.OptionalTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.ReturnTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypeReferenceNode;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.utils.GeneratorConstants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import static org.ballerinalang.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static org.ballerinalang.generators.GeneratorUtils.getListenerDeclarationNode;
import static org.ballerinalang.generators.GeneratorUtils.getQualifiedNameReferenceNode;
import static org.ballerinalang.generators.GeneratorUtils.getRelativeResourcePath;
import static org.ballerinalang.openapi.OpenApiMesseges.BAL_KEYWORDS;

/**
 * This Util class use for generating ballerina service file according to given yaml file.
 */
public class BallerinaServiceGenerator {
    private static final int httpPort = 80;
    private static final int httpsPort = 443;
    private static final Token openBraceToken = AbstractNodeFactory.createIdentifierToken("{");
    private static final Token closeBraceToken = AbstractNodeFactory.createIdentifierToken("}");
    private static final Token openSBracketToken = AbstractNodeFactory.createIdentifierToken("[");
    private static final Token closeSBracketToken = AbstractNodeFactory.createIdentifierToken("]");
    private static final Token colon = AbstractNodeFactory.createIdentifierToken(":");
    private static final Token semicolonToken = AbstractNodeFactory.createIdentifierToken(";");
    // Add basicLiteralNode
    private static final MinutiaeList leading = AbstractNodeFactory.createEmptyMinutiaeList();
    private static final Minutiae whitespace = AbstractNodeFactory.createWhitespaceMinutiae(" ");
    private static final MinutiaeList trailing = AbstractNodeFactory.createMinutiaeList(whitespace);
    private static final Token questionMark = NodeFactory.createIdentifierToken("?");

    @Nonnull
    public static SyntaxTree generateSyntaxTree(Path definitionPath, String serviceName, Filter filter) throws
            IOException, BallerinaOpenApiException, FormatterException {
        // Create imports http and openapi
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode("ballerina"
                , "http");
        //Disable openapi import adding due to validator is disable
        //ImportDeclarationNode importForOpenapi = GeneratorUtils.getImportDeclarationNode("ballerina", "openapi");
        // Add multiple imports
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createNodeList(importForHttp);
        // Summaries OpenAPI details
        OpenAPI openApi = getBallerinaOpenApiType(definitionPath, serviceName);
        // Assign host port value to listeners
        String host;
        int port;
        String basePath;
        if (openApi.getServers() != null) {
            Server server = openApi.getServers().get(0);
            if (server.getVariables() != null) {
                ServerVariables variables = server.getVariables();
                URL url;
                try {
                    String resolvedUrl = buildUrl(server.getUrl(), variables);
                    url = new URL(resolvedUrl);
                    host = url.getHost();
                    basePath = url.getPath();
                    port = url.getPort();
                    boolean isHttps = "https".equalsIgnoreCase(url.getProtocol());

                    if (isHttps) {
                        port = port == -1 ? httpsPort : port;
                    } else {
                        port = port == -1 ? httpPort : port;
                    }
                } catch (MalformedURLException e) {
                    throw new BallerinaOpenApiException("Failed to read endpoint details of the server: " +
                            server.getUrl(), e);
                }
            } else if (!server.getUrl().isBlank()) {
                String[] split = server.getUrl().trim().split("/");
                basePath =  "/" + split[split.length - 1];
                host = "localhost";
                port = 9090;
            } else {
                basePath = "/";
                host = "localhost";
                port = 9090;
            }
        } else {
            basePath = "/";
            host = "localhost";
            port = 9090;
        }
        // Generate listener
        ListenerDeclarationNode listener = getListenerDeclarationNode(port, host);

        // Generate ServiceDeclaration
        NodeList<Token> qualifiers = AbstractNodeFactory.createEmptyNodeList();
        Token serviceKeyWord = AbstractNodeFactory.createIdentifierToken(" service ");
        String[] basePathNode = basePath.split("/");
        List<Node> nodeList = new ArrayList<>();
        for (String node: basePathNode) {
            if (!node.isBlank()) {
                Token pathNode = AbstractNodeFactory.createIdentifierToken("/" + node);
                nodeList.add(pathNode);
            }
        }
        NodeList<Node> absoluteResourcePath = AbstractNodeFactory.createNodeList(nodeList);
        Token onKeyWord = AbstractNodeFactory.createIdentifierToken(" on");

        SimpleNameReferenceNode listenerName =
                NodeFactory.createSimpleNameReferenceNode(listener.variableName());
        SeparatedNodeList<ExpressionNode> expressions = NodeFactory.createSeparatedNodeList(listenerName);

        // Fill the members with function
        List<Node> functions =  new ArrayList<>();
        if (!openApi.getPaths().isEmpty()) {
            io.swagger.v3.oas.models.Paths paths = openApi.getPaths();
            Set<Map.Entry<String, PathItem>> pathsItems = paths.entrySet();
            Iterator<Map.Entry<String, PathItem>> pathItr = pathsItems.iterator();
            while (pathItr.hasNext()) {
                Map.Entry<String, PathItem> path = pathItr.next();
                if (!path.getValue().readOperationsMap().isEmpty()) {
                    Map<PathItem.HttpMethod, Operation> operationMap = path.getValue().readOperationsMap();
                    for (Map.Entry<PathItem.HttpMethod, Operation> operation : operationMap.entrySet()) {
                        //Add filter availability
                        //1.Tag filter
                        //2.Operation filter
                        //3. Both tag and operation filter
                        List<String> filterTags = filter.getTags();
                        List<String> operationTags = operation.getValue().getTags();
                        List<String> filterOperations  = filter.getOperations();
                        if (!filterTags.isEmpty() || !filterOperations.isEmpty()) {
                            if (operationTags != null || ((!filterOperations.isEmpty())
                                    && (operation.getValue().getOperationId() != null))) {
                                if (GeneratorUtils.hasTags(operationTags, filterTags) ||
                                        ((operation.getValue().getOperationId() != null) &&
                                        filterOperations.contains(operation.getValue().getOperationId().trim()))) {
                                    // getRelative resource path
                                    List<Node> functionRelativeResourcePath = getRelativeResourcePath(path, operation);
                                    // function call
                                    getFunctionDefinitionNode(functions, operation, functionRelativeResourcePath);
                                } else {
//                                    skip
                                }
                            } else {
//                                skip
                            }
                        } else {
                            // getRelative resource path
                            List<Node> functionRelativeResourcePath = getRelativeResourcePath(path, operation);
                            // function call
                            getFunctionDefinitionNode(functions, operation, functionRelativeResourcePath);
                        }
                    }
                }
            }
        }

        NodeList<Node> members = NodeFactory.createNodeList(functions);

        ServiceDeclarationNode serviceDeclarationNode = NodeFactory
                .createServiceDeclarationNode(null, qualifiers, serviceKeyWord, null,
                        absoluteResourcePath, onKeyWord, expressions, openBraceToken, members, closeBraceToken);

        // Create module member declaration
        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(listener,
                serviceDeclarationNode);

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private static void getFunctionDefinitionNode(List<Node> functions,
                                                  Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                  List<Node> pathNodes) throws BallerinaOpenApiException {

        Token resource = AbstractNodeFactory.createIdentifierToken("    resource");
        NodeList<Token> qualifiersList = NodeFactory.createNodeList(resource);

        Token functionKeyWord = AbstractNodeFactory.createIdentifierToken(" function ");

        IdentifierToken functionName =
                AbstractNodeFactory.createIdentifierToken(operation.getKey().name().toLowerCase(
                        Locale.ENGLISH) + " ");

        NodeList<Node> relativeResourcePath = NodeFactory.createNodeList(pathNodes);

        // Create FunctionSignature
        Token openParenToken = AbstractNodeFactory.createIdentifierToken("(");
        List<Node> params = new ArrayList<>();
        // Create http:Request node RequiredParam
        // --annotation
        NodeList<AnnotationNode> annotations = AbstractNodeFactory.createEmptyNodeList();

        Token comma = AbstractNodeFactory.createIdentifierToken(",");
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

        SeparatedNodeList<ParameterNode> parameters = AbstractNodeFactory.createSeparatedNodeList(params);
        //return Type descriptors
        Token returnKeyWord = AbstractNodeFactory.createIdentifierToken(" returns ");
        // Generate return type is node
        ReturnTypeDescriptorNode returnNode = getReturnTypeDescriptorNode(operation, annotations,
                returnKeyWord);

        Token closeParenToken = AbstractNodeFactory.createIdentifierToken(")");

        FunctionSignatureNode functionSignatureNode = NodeFactory
                .createFunctionSignatureNode(openParenToken, parameters, closeParenToken, returnNode);

        // Function Body Node
        NodeList<StatementNode> statements = AbstractNodeFactory.createEmptyNodeList();
        FunctionBodyBlockNode functionBodyBlockNode = NodeFactory
                .createFunctionBodyBlockNode(openBraceToken, null, statements, closeBraceToken);
        FunctionDefinitionNode functionDefinitionNode = NodeFactory
                .createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, null,
                        qualifiersList, functionKeyWord, functionName, relativeResourcePath,
                        functionSignatureNode, functionBodyBlockNode);
        functions.add(functionDefinitionNode);
    }

    private static void createNodeForHeaderParameter(List<Node> params, Token comma, Parameter parameter) {

        Schema schema = parameter.getSchema();
        String type = "string";
        TypeDescriptorNode headerTypeName;
        IdentifierToken parameterName =
                AbstractNodeFactory.createIdentifierToken(" " + escapeIdentifier(parameter.getName().toLowerCase(
                        Locale.ENGLISH)));

        if (schema == null || parameter.getContent() != null) {
            RequiredParameterNode param =
                    NodeFactory.createRequiredParameterNode(NodeFactory.createEmptyNodeList(),
                            AbstractNodeFactory.createIdentifierToken("string"), parameterName);
            params.add(param);
            //Diagnostic for null schema
        } else {
            if (!schema.getType().equals(type) && !(schema instanceof ArraySchema)) {
                //TO-DO: Generate diagnostic about to error type
                headerTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                        AbstractNodeFactory.createIdentifierToken(type));
            } else if (schema instanceof ArraySchema) {
                String arrayType = ((ArraySchema) schema).getItems().getType();
                BuiltinSimpleNameReferenceNode headerArrayItemTypeName =
                        NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                                AbstractNodeFactory.createIdentifierToken(arrayType));
                headerTypeName = NodeFactory
                        .createArrayTypeDescriptorNode(headerArrayItemTypeName, openSBracketToken, null,
                                closeSBracketToken);
            } else {
                headerTypeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                        AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(
                                schema.getType().trim())));
            }

            // Create annotation
            MappingConstructorExpressionNode annotValue =
                    NodeFactory.createMappingConstructorExpressionNode(openBraceToken,
                            NodeFactory.createSeparatedNodeList(), closeBraceToken);
            AnnotationNode headerNode = getAnnotationNode("Header", annotValue);
            NodeList<AnnotationNode> headerAnnotations = NodeFactory.createNodeList(headerNode);

            RequiredParameterNode param =  NodeFactory.createRequiredParameterNode(headerAnnotations,
                    headerTypeName, parameterName);
            params.add(param);
            params.add(comma);
        }
    }

    /**
     * This function used to generate return function node in the function signature.
     * Payload media type will not be added to the annotation.
     * @param operation     OpenApi operation
     * @param annotations   Annotation node list
     * @param returnKeyWord    Keyword for return
     * @return return returnNode
     * @throws BallerinaOpenApiException
     */
    private static ReturnTypeDescriptorNode getReturnTypeDescriptorNode(
            @Nonnull Map.Entry<PathItem.HttpMethod, Operation> operation, @Nonnull NodeList<AnnotationNode> annotations,
            @Nonnull Token returnKeyWord) throws BallerinaOpenApiException {

        ReturnTypeDescriptorNode returnNode = null;
        if (operation.getValue().getResponses() != null) {
            ApiResponses responses = operation.getValue().getResponses();
            Iterator<Map.Entry<String, ApiResponse>> responseIter = responses.entrySet().iterator();
            if (responses.size() > 1) {
                UnionTypeDescriptorNode type = getUnionNode(responseIter);
                returnNode = NodeFactory.createReturnTypeDescriptorNode(returnKeyWord, annotations, type);
            } else {
                while (responseIter.hasNext()) {
                    Map.Entry<String, ApiResponse> response = responseIter.next();

                    if (response.getValue().getContent() == null && response.getValue().get$ref() == null) {
                        String code = getHttpStatusCode(response.getKey().trim());
                        // Scenario 01
                        QualifiedNameReferenceNode statues = getQualifiedNameReferenceNode("http", code);
                        returnNode = NodeFactory.createReturnTypeDescriptorNode(returnKeyWord, annotations, statues);
                    } else if (response.getValue().getContent() != null) {
                        
                        if (response.getKey().trim().equals("200")) {
                            Iterator<Map.Entry<String, MediaType>> iterator =
                                    response.getValue().getContent().entrySet().iterator();

                            if (response.getValue().getContent().entrySet().size() > 1) {
                                returnNode = NodeFactory.createReturnTypeDescriptorNode(returnKeyWord,
                                        NodeFactory.createEmptyNodeList(), getUnionNodeForContent(iterator));
                            } else {
                                while (iterator.hasNext()) {
                                    Map.Entry<String, MediaType> next = iterator.next();
                                    returnNode = NodeFactory.createReturnTypeDescriptorNode(returnKeyWord,
                                            NodeFactory.createEmptyNodeList(), getIdentifierToken(next));
                                }
                            }

                        } else {
                            String code = getHttpStatusCode(response.getKey().trim());
                            Content content = response.getValue().getContent();
                            Iterator<Map.Entry<String, MediaType>> contentItr = content.entrySet().iterator();

                            TypeDescriptorNode type = null;
                            if (content.entrySet().size() > 1) {
                                type = getUnionNodeForContent(contentItr);
                            } else {
                                // Handle for only first content type
                                String dataType;
                                while (contentItr.hasNext()) {
                                    Map.Entry<String, MediaType> mediaTypeEntry = contentItr.next();
                                    if (mediaTypeEntry.getValue().getSchema() != null) {
                                        Schema schema = mediaTypeEntry.getValue().getSchema();
                                        if (schema.get$ref() != null) {
                                            dataType = extractReferenceType(schema.get$ref().trim());
                                            type = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                                                    NodeFactory.createIdentifierToken(dataType));
                                        } else if (schema instanceof ObjectSchema) {
                                            type = getRecordTypeDescriptorNode(schema);
                                        } else if (schema instanceof ComposedSchema) {
                                            Iterator<Schema> iterator = ((ComposedSchema) schema).getOneOf().iterator();
                                            type = getUnionNodeForOneOf(iterator);
                                        } else {
                                            type =  getIdentifierToken(mediaTypeEntry);
                                        }
                                    }
                                }
                            }

                            RecordTypeDescriptorNode recordType = createRecordTypeDescriptorNode(code, type);
                            NodeList<AnnotationNode> ann  = NodeFactory.createEmptyNodeList();
                            returnNode = NodeFactory.createReturnTypeDescriptorNode(returnKeyWord, ann, recordType);
                        }
                    }
                }
            }
        } else {
            // --errorTypeDescriptor
            Token errorKeyWordToken = AbstractNodeFactory.createIdentifierToken("error");
            // errorTypeParamsNode can be null
            ErrorTypeDescriptorNode errorTypeDescriptorNode =
                    NodeFactory.createErrorTypeDescriptorNode(errorKeyWordToken, null);
            Token questionMarkToken = AbstractNodeFactory.createIdentifierToken("?");

            OptionalTypeDescriptorNode type =
                    NodeFactory.createOptionalTypeDescriptorNode(errorTypeDescriptorNode, questionMarkToken);
            returnNode = NodeFactory.createReturnTypeDescriptorNode(returnKeyWord, annotations,
                    type);
        }
        return returnNode;
    }

    /**
     * This for generate record node for object schema.
     */
    private static TypeDescriptorNode getRecordTypeDescriptorNode(Schema schema) throws BallerinaOpenApiException {

        TypeDescriptorNode type;
        Token recordKeyWord = AbstractNodeFactory.createIdentifierToken("record ");
        Token bodyStartDelimiter = AbstractNodeFactory.createIdentifierToken("{|");
        // Create record fields
        List<Node> recordfields = new ArrayList<>();
        if (schema.getProperties() != null) {
            Map<String, Schema> properties =
                    (Map<String, Schema>) schema.getProperties();
            for (Map.Entry<String, Schema> field: properties.entrySet()) {
                Token fieldName = AbstractNodeFactory.createIdentifierToken(field.getKey().trim());
                String typeF;
                if (field.getValue().get$ref() != null) {
                    typeF = extractReferenceType(field.getValue().get$ref());
                } else {
                    typeF = convertOpenAPITypeToBallerina(field.getValue().getType());
                }
                Token typeR = AbstractNodeFactory.createIdentifierToken(typeF + " ");
                RecordFieldNode recordFieldNode =
                        NodeFactory.createRecordFieldNode(null, null, typeR, fieldName,
                                null, semicolonToken);
                recordfields.add(recordFieldNode);
            }
        }

        NodeList<Node> fieldsList = NodeFactory.createSeparatedNodeList(recordfields);
        Token bodyEndDelimiter = AbstractNodeFactory.createIdentifierToken("|}");
        type = NodeFactory.createRecordTypeDescriptorNode(recordKeyWord,
                bodyStartDelimiter, fieldsList, null,
                bodyEndDelimiter);
        return type;
    }

    /**
     * Create recordType TypeDescriptor.
     */
    private static RecordTypeDescriptorNode createRecordTypeDescriptorNode(String code, TypeDescriptorNode type) {

        // Create Type
        Token recordKeyWord = AbstractNodeFactory.createIdentifierToken("record ");
        Token bodyStartDelimiter = AbstractNodeFactory.createIdentifierToken("{|");
        // Create record fields
        List<Node> recordfields = new ArrayList<>();
        // Type reference node
        Token asteriskToken = AbstractNodeFactory.createIdentifierToken("*");
        QualifiedNameReferenceNode typeNameField = getQualifiedNameReferenceNode("http", code);
        TypeReferenceNode typeReferenceNode =
                NodeFactory.createTypeReferenceNode(asteriskToken, typeNameField, semicolonToken);
        recordfields.add(typeReferenceNode);
        // Record field name
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(" body");
        RecordFieldNode recordFieldNode =
                NodeFactory.createRecordFieldNode(null, null, type, fieldName,
                        null, semicolonToken);
        recordfields.add(recordFieldNode);

        NodeList<Node> fieldsList = NodeFactory.createSeparatedNodeList(recordfields);
        Token bodyEndDelimiter = AbstractNodeFactory.createIdentifierToken("|}");

        return NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                fieldsList, null, bodyEndDelimiter);
    }

    /**
     * This for creating request Body for given request object.
     */
    private static void createNodeForRequestBody(List<Node> params, Token comma, RequestBody requestBody)
            throws BallerinaOpenApiException {

        List<Node> literals = new ArrayList<>();
        MappingConstructorExpressionNode annotValue;
        TypeDescriptorNode typeName;

        if (requestBody.getContent().entrySet().size() > 1) {
            IdentifierToken mediaType = AbstractNodeFactory.createIdentifierToken("mediaType");
            // --create value expression
            // ---create expression
            // Filter same data type
            HashSet<Map.Entry<String, MediaType>> equalDataType = new HashSet();
            Set<Map.Entry<String, MediaType>> entries = requestBody.getContent().entrySet();
            Iterator<Map.Entry<String, MediaType>> iterator = entries.iterator();
            List<Map.Entry<String, MediaType>> updatedEntries = new ArrayList<>(entries);
            while (iterator.hasNext()) {
//             remove element from updateEntries
                Map.Entry<String, MediaType> mediaTypeEntry = iterator.next();
                updatedEntries.remove(mediaTypeEntry);
                if (!updatedEntries.isEmpty()) {
                    Iterator<Map.Entry<String, MediaType>> updateIter = updatedEntries.iterator();
                    while (updateIter.hasNext()) {
                        Map.Entry<String, MediaType> updateNext = updateIter.next();
                        MediaType parentValue = mediaTypeEntry.getValue();
                        MediaType childValue = updateNext.getValue();
                        if (parentValue.getSchema().get$ref() != null && childValue.getSchema().get$ref() != null) {
                            String parentRef = parentValue.getSchema().get$ref().trim();
                            String childRef = childValue.getSchema().get$ref().trim();
                            if (extractReferenceType(parentRef).equals(extractReferenceType(childRef))) {
                                equalDataType.add(updateNext);
                            }
                        }
                    }
                    if (!equalDataType.isEmpty()) {
                        equalDataType.add(mediaTypeEntry);
                        break;
                    }
                }
            }
            if (!equalDataType.isEmpty()) {

                typeName = getIdentifierTokenForJsonSchema(equalDataType.iterator().next().getValue().getSchema());
                Iterator<Map.Entry<String, MediaType>> iter = equalDataType.iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, MediaType> next = iter.next();
                    literals.add(AbstractNodeFactory.createIdentifierToken('"' + next.getKey().trim() + '"'));
                    literals.add(comma);
                }
                literals.remove(literals.size() - 1);
                SeparatedNodeList<Node> expression = NodeFactory.createSeparatedNodeList(literals);
                ListConstructorExpressionNode valueExpr = NodeFactory.createListConstructorExpressionNode(
                        openSBracketToken, expression, closeSBracketToken);
                SpecificFieldNode specificFieldNode = NodeFactory.createSpecificFieldNode(
                        null, mediaType, colon, valueExpr);
                SeparatedNodeList<MappingFieldNode> fields = NodeFactory.createSeparatedNodeList(specificFieldNode);
                annotValue = NodeFactory.createMappingConstructorExpressionNode(openBraceToken,
                        fields, closeBraceToken);
            } else {
                typeName = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                        AbstractNodeFactory.createIdentifierToken("json"));
                annotValue = NodeFactory.createMappingConstructorExpressionNode(openBraceToken,
                        NodeFactory.createSeparatedNodeList(), closeBraceToken);
            }
        } else {
            Iterator<Map.Entry<String, MediaType>> content = requestBody.getContent().entrySet().iterator();
            Map.Entry<String, MediaType> next = createBasicLiteralNodeList(comma, leading, trailing, literals, content);
            typeName = getIdentifierToken(next);
            annotValue = NodeFactory.createMappingConstructorExpressionNode(openBraceToken,
                    NodeFactory.createSeparatedNodeList(), closeBraceToken);
        }

        AnnotationNode annotationNode = getAnnotationNode("Payload ", annotValue);
        NodeList<AnnotationNode> annotation =  NodeFactory.createNodeList(annotationNode);
        Token paramName = AbstractNodeFactory.createIdentifierToken(" payload");

        RequiredParameterNode payload =
                NodeFactory.createRequiredParameterNode(annotation, typeName, paramName);

        params.add(payload);
        params.add(comma);
    }


    private static Map.Entry<String, MediaType> createBasicLiteralNodeList(Token comma, MinutiaeList leading,
                                                                           MinutiaeList trailing, List<Node> literals,
                                                                           Iterator<Map.Entry<String, MediaType>> con) {
        Map.Entry<String, MediaType> next = con.next();
        String text = next.getKey();
        Token literalToken = AbstractNodeFactory.createLiteralValueToken(null, text, leading, trailing);
        BasicLiteralNode basicLiteralNode = NodeFactory.createBasicLiteralNode(null, literalToken);
        literals.add(basicLiteralNode);
        literals.add(comma);
        return next;
    }

    /**
     * Generate typeDescriptor for application/json type.
     */
    private static TypeDescriptorNode getIdentifierTokenForJsonSchema(Schema schema) throws BallerinaOpenApiException {
        IdentifierToken identifierToken;
        if (schema != null) {
            if (schema.get$ref() != null) {
                identifierToken = AbstractNodeFactory.createIdentifierToken(extractReferenceType(schema.get$ref()));
            } else if (schema.getType() != null) {
                if (schema instanceof ObjectSchema) {
                    return getRecordTypeDescriptorNode(schema);
                } else if (schema instanceof ArraySchema) {
                    TypeDescriptorNode member;
                    if (((ArraySchema) schema).getItems().get$ref() != null) {
                        member = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                                AbstractNodeFactory.createIdentifierToken(extractReferenceType(((ArraySchema) schema).
                                        getItems().get$ref())));
                    } else if (!(((ArraySchema) schema).getItems() instanceof ArraySchema)) {
                        member = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                                AbstractNodeFactory.createIdentifierToken("string"));
                    } else {
                        member = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                                AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(
                                        ((ArraySchema) schema).getItems().getType())));
                    }
                    return  NodeFactory.createArrayTypeDescriptorNode(member, openSBracketToken, null,
                            closeSBracketToken);
                } else {
                    identifierToken =  AbstractNodeFactory.createIdentifierToken(schema.getType() + " ");
                }
            } else if (schema instanceof ComposedSchema) {
                if (((ComposedSchema) schema).getOneOf() != null) {
                    Iterator<Schema> iterator = ((ComposedSchema) schema).getOneOf().iterator();
                    return getUnionNodeForOneOf(iterator);
                } else {
                    identifierToken =  AbstractNodeFactory.createIdentifierToken("json ");
                }
            } else {
                identifierToken =  AbstractNodeFactory.createIdentifierToken("json ");
            }
        } else {
            identifierToken =  AbstractNodeFactory.createIdentifierToken("json ");
        }
        return NodeFactory.createSimpleNameReferenceNode(identifierToken);
    }

    /**
     * Generate TypeDescriptor for all the mediaTypes.
     */
    private static TypeDescriptorNode getIdentifierToken(Map.Entry<String, MediaType> mediaType)
            throws BallerinaOpenApiException {
        String mediaTypeContent = mediaType.getKey().trim();
        MediaType value = mediaType.getValue();
        Schema schema = value.getSchema();
        IdentifierToken identifierToken;
        switch (mediaTypeContent) {
            case "application/json":
                return getIdentifierTokenForJsonSchema(schema);
            case "application/xml":
                identifierToken = AbstractNodeFactory.createIdentifierToken("xml");
                return NodeFactory.createSimpleNameReferenceNode(identifierToken);
            case "text/plain":
                identifierToken = AbstractNodeFactory.createIdentifierToken("string");
                return NodeFactory.createSimpleNameReferenceNode(identifierToken);
            case "application/octet-stream":
                return NodeFactory.createArrayTypeDescriptorNode(NodeFactory.createBuiltinSimpleNameReferenceNode(
                        null, AbstractNodeFactory.createIdentifierToken("byte")), openSBracketToken,
                        null, closeSBracketToken);
            default:
                identifierToken = AbstractNodeFactory.createIdentifierToken("json");
                return NodeFactory.createSimpleNameReferenceNode(identifierToken);
        }
    }

    /**
     * This for generate query parameter nodes.
     */
    private static void createNodeForQueryParam(List<Node> params, Token comma, Parameter parameter) {
        if (parameter.getIn().trim().equals("query")) {
            Schema schema = parameter.getSchema();
            NodeList<AnnotationNode> annotations = NodeFactory.createEmptyNodeList();
            IdentifierToken parameterName =
                    AbstractNodeFactory.createIdentifierToken(" " + escapeIdentifier(parameter.getName().trim()));
            if (schema == null || parameter.getContent() != null) {
                RequiredParameterNode param =
                        NodeFactory.createRequiredParameterNode(NodeFactory.createEmptyNodeList(),
                                AbstractNodeFactory.createIdentifierToken("string"), parameterName);
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
                            RequiredParameterNode arrayRparam = NodeFactory
                                    .createRequiredParameterNode(annotations, arrayTypeName, parameterName);
                            params.add(arrayRparam);

                        } else {
                            // handle in case swagger has nested array or record type
                            //create optional query parameter
                            Token arrayName = AbstractNodeFactory.createIdentifierToken("string");
                            BuiltinSimpleNameReferenceNode memberTypeDesc =
                                    NodeFactory.createBuiltinSimpleNameReferenceNode(null, arrayName);
                            ArrayTypeDescriptorNode arrayTypeName = NodeFactory.createArrayTypeDescriptorNode(
                                    memberTypeDesc, openSBracketToken, null, closeSBracketToken);
                            // create Optional type descriptor
                                    OptionalTypeDescriptorNode optionalTypeDescriptorNode =
                                    NodeFactory.createOptionalTypeDescriptorNode(arrayTypeName, questionMark);
                            RequiredParameterNode arrayRparam = NodeFactory
                                    .createRequiredParameterNode(annotations, optionalTypeDescriptorNode,
                                            parameterName);
                            params.add(arrayRparam);
                        }
                    } else {
                        Token name =
                                AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(
                                        schema.getType().toLowerCase(Locale.ENGLISH).trim()));
                        BuiltinSimpleNameReferenceNode rTypeName =
                                NodeFactory.createBuiltinSimpleNameReferenceNode(null, name);
                        RequiredParameterNode param1 =
                                NodeFactory.createRequiredParameterNode(annotations, rTypeName, parameterName);
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
                                    NodeFactory.createOptionalTypeDescriptorNode(arrayTypeName, questionMark);
                            RequiredParameterNode arrayRparam = NodeFactory
                                    .createRequiredParameterNode(annotations, optionalTypeDescriptorNode,
                                            parameterName);
                            params.add(arrayRparam);
                            params.add(comma);

                        } else {
                            // handle in case swagger has nested array or record type
                            // create diagnostic after checking with team.
                        }
                    } else {
                        Token name =
                                AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(
                                        schema.getType().toLowerCase(Locale.ENGLISH).trim()));
                        BuiltinSimpleNameReferenceNode rTypeName =
                                NodeFactory.createBuiltinSimpleNameReferenceNode(null, name);
                        OptionalTypeDescriptorNode optionalTypeDescriptorNode =
                                NodeFactory.createOptionalTypeDescriptorNode(rTypeName, questionMark);
                        RequiredParameterNode param1 =
                                NodeFactory.createRequiredParameterNode(annotations, optionalTypeDescriptorNode,
                                        parameterName);
                        params.add(param1);
                        params.add(comma);
                    }
                }
            }
        }
    }

    // Create ArrayTypeDescriptorNode using Schema
    private static ArrayTypeDescriptorNode getArrayTypeDescriptorNode(Schema<?> items) {

        Token arrayName = AbstractNodeFactory.createIdentifierToken(items.getType().trim());
        BuiltinSimpleNameReferenceNode memberTypeDesc =
                NodeFactory.createBuiltinSimpleNameReferenceNode(null, arrayName);
        return NodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openSBracketToken, null,
                closeSBracketToken);
    }

    /**
     * Util for take OpenApi spec from given yaml file.
     */
    private static OpenAPI getBallerinaOpenApiType(Path definitionPath, String serviceName)
            throws IOException, BallerinaOpenApiException {

        String openAPIFileContent = Files.readString(definitionPath);
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openAPIFileContent);

        if (parseResult.getMessages().size() > 0) {
            throw new BallerinaOpenApiException("Couldn't read or parse the definition from file: " + definitionPath);
        }
        OpenAPI api = parseResult.getOpenAPI();
        if (api.getInfo() == null) {
            throw new BallerinaOpenApiException("Info section of the definition file cannot be empty/null: " +
                    definitionPath);
        }

        if (api.getInfo().getTitle().isBlank() && (serviceName == null || serviceName.isBlank())) {
            api.getInfo().setTitle(GeneratorConstants.UNTITLED_SERVICE);
        } else {
            api.getInfo().setTitle(serviceName);
        }
        return api;
    }

    /**
     * This method will extract reference type by splitting the reference string.
     *
     * @param referenceVariable - Reference String
     * @return Reference variable name
     * @throws BallerinaOpenApiException - Throws an exception if the reference string is incompatible.
     *                                     Note : Current implementation will not support external links a references.
     */
    public static String extractReferenceType(String referenceVariable) throws BallerinaOpenApiException {
        if (referenceVariable.startsWith("#") && referenceVariable.contains("/")) {
            String[] refArray = referenceVariable.split("/");
            return escapeIdentifier(refArray[refArray.length - 1]);
        } else {
            throw new BallerinaOpenApiException("Invalid reference value : " + referenceVariable
                    + "\nBallerina only supports local reference values.");
        }
    }

    /**
     * If there are template values in the {@code absUrl} derive resolved url using {@code variables}.
     *
     * @param absUrl abstract url with template values
     * @param variables variable values to populate the url template
     * @return resolved url
     */
    private static String buildUrl(String absUrl, ServerVariables variables) {
        String url = absUrl;
        if (variables != null) {
            for (Map.Entry<String, ServerVariable> entry : variables.entrySet()) {
                // According to the oas spec, default value must be specified
                String replaceKey = "\\{" + entry.getKey() + '}';
                url = url.replaceAll(replaceKey, entry.getValue().getDefault());
            }
        }
        return url;
    }

    /**
     * Util for select http key words with http codes.
     * @param code http code.
     * @return Http identification word.
     */
    private static String getHttpStatusCode(String code) {
        switch (code) {
            case "100":
                return "Continue";
            case "200":
                return "Ok";
            case "201":
                return "Created";
            case "202":
                return "Accepted";
            case "400":
                return "BadRequest";
            case "401":
                return "Unauthorized";
            case "402":
                return "PaymentRequired";
            case "403":
                return "Forbidden";
            case "404":
                return "NotFound";
            case "405":
                return "MethodNotAllowed";
            case "500":
                return "InternalServerError";
            case "501":
                return "NotImplemented";
            case "502":
                return "BadGateway";
            case "503":
                return "ServiceUnavailable";
            default:
                return "Ok";
        }
    }

    /**
     * Generate union type node when operation has multiple responses.
     */
    private static UnionTypeDescriptorNode getUnionNode(Iterator<Map.Entry<String, ApiResponse>> responseIter)
            throws BallerinaOpenApiException {
        List<TypeDescriptorNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = AbstractNodeFactory.createIdentifierToken("|");

        while (responseIter.hasNext()) {
            Map.Entry<String, ApiResponse> response = responseIter.next();
            String code = getHttpStatusCode(response.getKey().trim());
            if (response.getValue().getContent() == null && response.getValue().get$ref() == null) {
                //key and value
                QualifiedNameReferenceNode node = getQualifiedNameReferenceNode("http", code);
                qualifiedNodes.add(node);
            } else if (response.getValue().getContent() != null) {
                TypeDescriptorNode record =
                        getIdentifierToken(response.getValue().getContent().entrySet().iterator().next());
                if (response.getKey().trim().equals("200") || response.getKey().trim().equals("default")) {
                    qualifiedNodes.add(record);
                } else {
                    RecordTypeDescriptorNode node = createRecordTypeDescriptorNode(code, record);
                    qualifiedNodes.add(node);
                }
            }
        }
        TypeDescriptorNode right = qualifiedNodes.get(qualifiedNodes.size() - 1);
        TypeDescriptorNode traversRight = qualifiedNodes.get(qualifiedNodes.size() - 2);
        UnionTypeDescriptorNode traversUnion = NodeFactory.createUnionTypeDescriptorNode(traversRight, pipeToken,
                right);
        if (qualifiedNodes.size() >= 3) {
            for (int i = qualifiedNodes.size() - 3; i >= 0; i--) {
                traversUnion = NodeFactory.createUnionTypeDescriptorNode(qualifiedNodes.get(i), pipeToken,
                        traversUnion);
            }
        }
        return traversUnion;
    }

    /**
     * Generate union type node when response has multiple content types.
     */
    private static UnionTypeDescriptorNode getUnionNodeForContent (Iterator<Map.Entry<String, MediaType>> iterator)
            throws BallerinaOpenApiException {
        List<SimpleNameReferenceNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = AbstractNodeFactory.createIdentifierToken("|");

        while (iterator.hasNext()) {
            Map.Entry<String, MediaType> contentType = iterator.next();
            TypeDescriptorNode node = getIdentifierToken(contentType);
            qualifiedNodes.add((SimpleNameReferenceNode) node);

        }
        SimpleNameReferenceNode right = qualifiedNodes.get(qualifiedNodes.size() - 1);
        SimpleNameReferenceNode traversRight = qualifiedNodes.get(qualifiedNodes.size() - 2);
        UnionTypeDescriptorNode traversUnion = NodeFactory.createUnionTypeDescriptorNode(traversRight, pipeToken,
                right);
        if (qualifiedNodes.size() >= 3) {
            for (int i = qualifiedNodes.size() - 3; i >= 0; i--) {
                traversUnion = NodeFactory.createUnionTypeDescriptorNode(qualifiedNodes.get(i), pipeToken,
                        traversUnion);
            }
        }
        return traversUnion;
    }

    private static UnionTypeDescriptorNode getUnionNodeForOneOf(Iterator<Schema> iterator)
            throws BallerinaOpenApiException {

        List<SimpleNameReferenceNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = AbstractNodeFactory.createIdentifierToken("|");
        while (iterator.hasNext()) {
            Schema contentType = iterator.next();
            TypeDescriptorNode node = getIdentifierTokenForJsonSchema(contentType);
            qualifiedNodes.add((SimpleNameReferenceNode) node);

        }
        SimpleNameReferenceNode right = qualifiedNodes.get(qualifiedNodes.size() - 1);
        SimpleNameReferenceNode traversRight = qualifiedNodes.get(qualifiedNodes.size() - 2);
        UnionTypeDescriptorNode traversUnion = NodeFactory.createUnionTypeDescriptorNode(traversRight, pipeToken,
                right);
        if (qualifiedNodes.size() >= 3) {
            for (int i = qualifiedNodes.size() - 3; i >= 0; i--) {
                traversUnion = NodeFactory.createUnionTypeDescriptorNode(qualifiedNodes.get(i), pipeToken,
                        traversUnion);
            }
        }
        return traversUnion;
    }

    private static AnnotationNode getAnnotationNode(String identifier, MappingConstructorExpressionNode annotValue) {
        // Create annotation
        Token atToken = AbstractNodeFactory.createIdentifierToken("@");
        QualifiedNameReferenceNode annotReference = getQualifiedNameReferenceNode("http", identifier);
        return NodeFactory.createAnnotationNode(atToken, annotReference, annotValue);
    }

    /**
     * This method will escape special characters used in method names and identifiers.
     *
     * @param identifier - identifier or method name
     * @return - escaped string
     */
    public static String escapeIdentifier(String identifier) {
        if (!identifier.matches("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b") || BAL_KEYWORDS.stream().anyMatch(identifier::equals)) {

            // TODO: Remove this `if`. Refer - https://github.com/ballerina-platform/ballerina-lang/issues/23045
            if (identifier.equals("error")) {
                identifier = "_error";
            } else {
                identifier = identifier.replaceAll(GeneratorConstants.ESCAPE_PATTERN, "\\\\$1");
                if (identifier.endsWith("?")) {
                    if (identifier.charAt(identifier.length() - 2) == '\\') {
                        StringBuilder stringBuilder = new StringBuilder(identifier);
                        stringBuilder.deleteCharAt(identifier.length() - 2);
                        identifier = stringBuilder.toString();
                    }
                    if (BAL_KEYWORDS.stream().anyMatch(Optional.ofNullable(identifier)
                            .filter(sStr -> sStr.length() != 0)
                            .map(sStr -> sStr.substring(0, sStr.length() - 1))
                            .orElse(identifier)::equals)) {
                        identifier = "'" + identifier;
                    } else {
                        return identifier;
                    }
                } else {
                    identifier = "'" + identifier;
                }
            }
        }
        return identifier;
    }
}
