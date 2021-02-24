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
import org.ballerinalang.formatter.core.Formatter;
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
import java.util.Set;

import static org.ballerinalang.generators.GeneratorUtils.convertOpenAPITypeToBallerina;
import static org.ballerinalang.generators.GeneratorUtils.escapeIdentifier;
import static org.ballerinalang.generators.GeneratorUtils.getListenerDeclarationNode;
import static org.ballerinalang.generators.GeneratorUtils.getQualifiedNameReferenceNode;
import static org.ballerinalang.generators.GeneratorUtils.getRelativeResourcePath;

public class BallerinaServiceGenerator {
    private static int HTTP_PORT = 80;
    private static int HTTPS_PORT = 443;

    public static SyntaxTree generateSyntaxTree(Path definitionPath, String serviceName, Filter filter) throws IOException,
            BallerinaOpenApiException, FormatterException {
        // Create imports http and openapi
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode("ballerina", "http");
        ImportDeclarationNode importForOpenapi = GeneratorUtils.getImportDeclarationNode("ballerina", "openapi");
        // Add multiple imports
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createNodeList(importForHttp, importForOpenapi);
        // Summaries OpenAPI details
        OpenAPI openApi = getBallerinaOpenApiType(definitionPath, serviceName);
        // Assign host port value to listeners
        String host;
        int port;
        String basePath;
        if (openApi.getServers() != null) {
            Server server = openApi.getServers().get(0);
            if(server.getVariables() != null) {
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
                        port = port == -1 ? HTTPS_PORT : port;
                    } else {
                        port = port == -1 ? HTTP_PORT : port;
                    }
                } catch (MalformedURLException e) {
                    throw new BallerinaOpenApiException("Failed to read endpoint details of the server: " + server.getUrl(), e);
                }
            } else {
                basePath = "/";
                host = null;
                port = 9090;
            }
        } else {
            basePath = "/";
            host = null;
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
        Token openBraceToken = AbstractNodeFactory.createIdentifierToken("{");

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
                        // getRelative resource path
                        List<Node> functionRelativeResourcePath = getRelativeResourcePath(path, operation);
                        // function call
                        getFunctionDefinitionNode(functions, path, operation, functionRelativeResourcePath);
                    }
                }
            }
        }

        NodeList<Node> members = NodeFactory.createNodeList(functions);
        Token closeBraceToken = AbstractNodeFactory.createIdentifierToken("}");

        ServiceDeclarationNode serviceDeclarationNode = NodeFactory
                .createServiceDeclarationNode(null, qualifiers, serviceKeyWord, null, absoluteResourcePath,
                        onKeyWord, expressions, openBraceToken, members, closeBraceToken);

        // Create module member declaration
        NodeList<ModuleMemberDeclarationNode> moduleMembers = AbstractNodeFactory.createNodeList(listener, serviceDeclarationNode);

        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, moduleMembers, eofToken);

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        SyntaxTree generatedSyntaxTree = syntaxTree.modifyWith(modulePartNode);
        System.out.println(Formatter.format(generatedSyntaxTree.toSourceCode()));
        return syntaxTree.modifyWith(modulePartNode);
//            System.out.println(generatedSyntaxTree.toSourceCode());

        // Generate Service - done
        // -- Generate resource function - done
        // -- Generate multiple resource - done
        // -- Generate multiple query parameters -done
        // -- Generate multiple path parameters -done
        // -- fix formatter issue - done
        // -- fix request Payload - partial done
        // -- fix response Payload
    }

    private static void getFunctionDefinitionNode(List<Node> functions, Map.Entry<String, PathItem> path,
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
        // --typeName
        QualifiedNameReferenceNode typeName = getQualifiedNameReferenceNode("http", "Request ");
        // --paramName
        Token paramName = AbstractNodeFactory.createIdentifierToken("request");
        RequiredParameterNode httpNode =
                NodeFactory.createRequiredParameterNode(annotations, typeName, paramName);
        params.add(httpNode);

        Token comma = AbstractNodeFactory.createIdentifierToken(",");

        // Handle query parameter
        // type​ ​ BasicType​ ​ boolean|int|float|decimal|string​ ;
        //public​ ​ type​ ​ QueryParamType​ ()​ |BasicType|BasicType​ [];
        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
            createNodeForQueryParam(params, comma, parameters);
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

        SeparatedNodeList<ParameterNode> parameters = AbstractNodeFactory.createSeparatedNodeList(params);
        //return Type descriptors
        Token returnKeyWord = AbstractNodeFactory.createIdentifierToken(" returns ");
        // Check return type is available
        ReturnTypeDescriptorNode returnNode = null;
        returnNode = getReturnTypeDescriptorNode(operation, annotations, returnKeyWord, returnNode);
        // create Type
        
        Token closeParenToken = AbstractNodeFactory.createIdentifierToken(")");

        FunctionSignatureNode functionSignatureNode = NodeFactory
                .createFunctionSignatureNode(openParenToken, parameters, closeParenToken, returnNode);

        // Function Body Node
        Token openBrace = AbstractNodeFactory.createIdentifierToken("{");
        NodeList<StatementNode> statements = AbstractNodeFactory.createEmptyNodeList();
        Token closeBrace = AbstractNodeFactory.createIdentifierToken("}");

        FunctionBodyBlockNode functionBodyBlockNode = NodeFactory
                .createFunctionBodyBlockNode(openBrace, null, statements, closeBrace);
        FunctionDefinitionNode functionDefinitionNode = NodeFactory
                .createFunctionDefinitionNode(SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, null,
                        qualifiersList, functionKeyWord, functionName, relativeResourcePath,
                        functionSignatureNode, functionBodyBlockNode);
        functions.add(functionDefinitionNode);
    }

    private static ReturnTypeDescriptorNode getReturnTypeDescriptorNode(
            Map.Entry<PathItem.HttpMethod, Operation> operation, NodeList<AnnotationNode> annotations,
            Token returnKeyWord, ReturnTypeDescriptorNode returnNode) throws BallerinaOpenApiException {

        List<AnnotationNode> annotationsReturn = new ArrayList<>();
        if (operation.getValue().getResponses() != null) {
            ApiResponses responses = operation.getValue().getResponses();
            Iterator<Map.Entry<String, ApiResponse>> responseIter = responses.entrySet().iterator();
            if (responses.size() > 1) {
                // here call recursion --- Scenario 04
                UnionTypeDescriptorNode type = getUnionNode(responseIter);
                returnNode = NodeFactory.createReturnTypeDescriptorNode(returnKeyWord, annotations, type);
            } else {
                while (responseIter.hasNext()) {
                    Map.Entry<String, ApiResponse> response = responseIter.next();

                    // Create annotations -- common details
                    Token atToken = AbstractNodeFactory.createIdentifierToken("@");
                    QualifiedNameReferenceNode annotationReference = getQualifiedNameReferenceNode("http",
                            "Payload");
                    Token openBrace = AbstractNodeFactory.createIdentifierToken("{");
                    // Fields
                    // --readonlyKey word can be null
                    IdentifierToken media = AbstractNodeFactory.createIdentifierToken("mediaType");
                    Token colon = AbstractNodeFactory.createIdentifierToken(":");
                    // --create value expression
                    Token openBracket = AbstractNodeFactory.createIdentifierToken("[");
                    // add basicLiteralNode
                    MinutiaeList leading = AbstractNodeFactory.createEmptyMinutiaeList();
                    Minutiae whitespace = AbstractNodeFactory.createWhitespaceMinutiae(" ");
                    MinutiaeList trailing = AbstractNodeFactory.createMinutiaeList(whitespace);
                    List<Node> literals = new ArrayList<>();
                    List<Node> fields = new ArrayList<>();

                    if (response.getValue().getContent() == null && response.getValue().get$ref() == null) {
                        String code = getHttpStatusCode(response.getKey().trim());
                        // Scenario 01
                        QualifiedNameReferenceNode statues = getQualifiedNameReferenceNode("http", code);
                        returnNode = NodeFactory.createReturnTypeDescriptorNode(returnKeyWord, annotations, statues);
                    } else if (response.getValue().getContent() != null) {
                        
                        if (response.getKey().trim().equals("200")) {
                            returnNode = getReturnTypeDescriptorNodeFor200(returnKeyWord, annotationsReturn, response,
                                    atToken, annotationReference, openBrace,
                                    media, colon, openBracket, leading, trailing, literals, fields);
                        } else {
                            String code = getHttpStatusCode(response.getKey().trim());
                            Content content = response.getValue().getContent();
                            Iterator<Map.Entry<String, MediaType>> contentItr = content.entrySet().iterator();
                            // Handle for only first content type
                            String dataType = null;
                            String mediaType;
                            TypeDescriptorNode type = null;
                            while (contentItr.hasNext()) {
                                Map.Entry<String, MediaType> mediaTypeEntry = contentItr.next();
                                mediaType = '"' + mediaTypeEntry.getKey().trim() + '"';

                                if (mediaTypeEntry.getValue().getSchema() != null) {
                                    Schema schema = mediaTypeEntry.getValue().getSchema();
                                    if (schema.get$ref() != null) {
                                        dataType = extractReferenceType(schema.get$ref().trim());
                                        type = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                                                NodeFactory.createIdentifierToken(dataType));
                                    } else if( schema instanceof ObjectSchema ) {
                                        // Create Type
                                        Token recordKeyWord = AbstractNodeFactory.createIdentifierToken("record ");
                                        Token bodyStartDelimiter = AbstractNodeFactory.createIdentifierToken("{|");
                                        // Create record fields
                                        List<Node> recordfields = new ArrayList<>();
                                        if (schema.getProperties() != null) {
                                            Map<String, Schema> properties =
                                                    (Map<String, Schema>) schema.getProperties();
                                            for (Map.Entry<String, Schema> field: properties.entrySet()) {
                                                Token semicolonToken = AbstractNodeFactory.createIdentifierToken(";");
                                                Token fieldName = AbstractNodeFactory.createIdentifierToken(field.getKey().trim());
                                                String typeF;
                                                if (field.getValue().get$ref() != null) {
                                                    typeF = extractReferenceType(field.getValue().get$ref());
                                                } else {
                                                    typeF = convertOpenAPITypeToBallerina(field.getValue().getType());
                                                }
                                                Token typeR = AbstractNodeFactory.createIdentifierToken(typeF + " ");
                                                RecordFieldNode recordFieldNode =
                                                        NodeFactory.createRecordFieldNode(null, null, typeR, fieldName, null, semicolonToken);
                                                recordfields.add(recordFieldNode);
                                            }
                                        }
                                        
                                        NodeList<Node> fieldsList = NodeFactory.createSeparatedNodeList(recordfields);
                                        Token bodyEndDelimiter = AbstractNodeFactory.createIdentifierToken("|}");
                                        type = NodeFactory.createRecordTypeDescriptorNode(recordKeyWord,
                                                bodyStartDelimiter, fieldsList, null,
                                                bodyEndDelimiter);
                                    }else {
                                        dataType = convertOpenAPITypeToBallerina(schema.getType());
                                        type = NodeFactory.createBuiltinSimpleNameReferenceNode(null,
                                                NodeFactory.createIdentifierToken(dataType));
                                    }
                                }
                                // ---create expression
                                Token literalToken = AbstractNodeFactory.createLiteralValueToken(null, mediaType, leading, trailing);
                                BasicLiteralNode basicLiteralNode = NodeFactory.createBasicLiteralNode(null, literalToken);
                                literals.add(basicLiteralNode);
                                SeparatedNodeList<Node> expression = NodeFactory.createSeparatedNodeList(literals);
                                Token closeBracket = AbstractNodeFactory.createIdentifierToken("]");
                                ListConstructorExpressionNode valueExpr = NodeFactory.createListConstructorExpressionNode(openBracket,
                                        expression, closeBracket);
                                SpecificFieldNode specificFieldNode = NodeFactory.createSpecificFieldNode(null, media, colon,
                                        valueExpr);
                                fields.add(specificFieldNode);
                                break;
                            }
                            RecordTypeDescriptorNode recordType = getRecordTypeDescriptorNode(code, type);

                            SeparatedNodeList<MappingFieldNode> field = NodeFactory.createSeparatedNodeList(fields);
                            Token closeBrace = AbstractNodeFactory.createIdentifierToken("}");
                            MappingConstructorExpressionNode annotValue = NodeFactory.createMappingConstructorExpressionNode(openBrace,
                                    field, closeBrace);
                            AnnotationNode annotationNode = NodeFactory.createAnnotationNode(atToken, annotationReference, annotValue);
                            annotationsReturn.add(annotationNode);
                            NodeList<AnnotationNode> ann  = NodeFactory.createNodeList(annotationsReturn);
                            returnNode = NodeFactory.createReturnTypeDescriptorNode(returnKeyWord,ann, recordType);
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

    private static RecordTypeDescriptorNode getRecordTypeDescriptorNode(String code,
                                                                        TypeDescriptorNode type) {

        // Create Type
        Token recordKeyWord = AbstractNodeFactory.createIdentifierToken("record ");
        Token bodyStartDelimiter = AbstractNodeFactory.createIdentifierToken("{|");
        // Create record fields
        List<Node> recordfields = new ArrayList<>();
        // Type reference node
        Token asteriskToken = AbstractNodeFactory.createIdentifierToken("*");
        QualifiedNameReferenceNode typeNameField = getQualifiedNameReferenceNode("http", code);
        Token semicolonToken = AbstractNodeFactory.createIdentifierToken(";");
        TypeReferenceNode typeReferenceNode =
                NodeFactory.createTypeReferenceNode(asteriskToken, typeNameField, semicolonToken);
        recordfields.add(typeReferenceNode);
        // Record field name
        IdentifierToken fieldName = AbstractNodeFactory.createIdentifierToken(" body");
        RecordFieldNode recordFieldNode =
                NodeFactory.createRecordFieldNode(null, null, type, fieldName, null, semicolonToken);
        recordfields.add(recordFieldNode);

        NodeList<Node> fieldsList = NodeFactory.createSeparatedNodeList(recordfields);
        Token bodyEndDelimiter = AbstractNodeFactory.createIdentifierToken("|}");

        return NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                fieldsList, null, bodyEndDelimiter);
    }

    private static ReturnTypeDescriptorNode getReturnTypeDescriptorNodeFor200(Token returnKeyWord,
                                                                              List<AnnotationNode> annotationsReturn,
                                                                              Map.Entry<String, ApiResponse> response,
                                                                              Token atToken,
                                                                              QualifiedNameReferenceNode annotationReference,
                                                                              Token openBrace, IdentifierToken media,
                                                                              Token colon, Token openBracket,
                                                                              MinutiaeList leading,
                                                                              MinutiaeList trailing,
                                                                              List<Node> literals, List<Node> fields)
            throws BallerinaOpenApiException {

        ReturnTypeDescriptorNode returnNode;
        Content content = response.getValue().getContent();
        Iterator<Map.Entry<String, MediaType>> contentItr = content.entrySet().iterator();

        // Handle for only first content type
        String dataType = null;
        String mediaType;
        while (contentItr.hasNext()) {
            Map.Entry<String, MediaType> mediaTypeEntry = contentItr.next();
            mediaType = '"' + mediaTypeEntry.getKey().trim() + '"';

            if (mediaTypeEntry.getValue().getSchema() != null) {
                Schema schema = mediaTypeEntry.getValue().getSchema();
                if (schema.get$ref() != null) {
                    dataType = extractReferenceType(schema.get$ref().trim());
                } else {
                    dataType = convertOpenAPITypeToBallerina(schema.getType());
                }
            }
            // ---create expression
            Token literalToken = AbstractNodeFactory.createLiteralValueToken(null, mediaType, leading, trailing);
            BasicLiteralNode
                    basicLiteralNode = NodeFactory.createBasicLiteralNode(null, literalToken);
            literals.add(basicLiteralNode);
            SeparatedNodeList<Node> expression = NodeFactory.createSeparatedNodeList(literals);
            Token closeBracket = AbstractNodeFactory.createIdentifierToken("]");
            ListConstructorExpressionNode
                    valueExpr = NodeFactory.createListConstructorExpressionNode(openBracket,
                    expression, closeBracket);
            SpecificFieldNode
                    specificFieldNode = NodeFactory.createSpecificFieldNode(null, media, colon,
                    valueExpr);
            fields.add(specificFieldNode);
            break;
        }
        SeparatedNodeList<MappingFieldNode> field = NodeFactory.createSeparatedNodeList(fields);
        Token closeBrace = AbstractNodeFactory.createIdentifierToken("}");
        MappingConstructorExpressionNode
                annotValue = NodeFactory.createMappingConstructorExpressionNode(openBrace,
                field, closeBrace);
        AnnotationNode annotationNode = NodeFactory.createAnnotationNode(atToken, annotationReference, annotValue);
        annotationsReturn.add(annotationNode);
        NodeList<AnnotationNode> ann  = NodeFactory.createNodeList(annotationsReturn);
        Token type = AbstractNodeFactory.createIdentifierToken(dataType);
        returnNode = NodeFactory.createReturnTypeDescriptorNode(returnKeyWord,ann, type);
        return returnNode;
    }

    private static void createNodeForRequestBody(List<Node> params, Token comma, RequestBody requestBody)
            throws BallerinaOpenApiException {

        IdentifierToken identifierToken = AbstractNodeFactory.createIdentifierToken("json");
        // Create annotation
        Token atToken = AbstractNodeFactory.createIdentifierToken("@");
        QualifiedNameReferenceNode annotReference = getQualifiedNameReferenceNode("http", "Payload ");
        // Fill annotation value
        Token openBrace = AbstractNodeFactory.createIdentifierToken("{");
        // Fields
        // --readonlyKey word can be null
        IdentifierToken mediaType = AbstractNodeFactory.createIdentifierToken("mediaType");
        Token colon = AbstractNodeFactory.createIdentifierToken(":");
        // --create value expression
        Token openBracket = AbstractNodeFactory.createIdentifierToken("[");
        // ---create expression
        // add basicLiteralNode
        MinutiaeList leading = AbstractNodeFactory.createEmptyMinutiaeList();
        Minutiae whitespace = AbstractNodeFactory.createWhitespaceMinutiae(" ");
        MinutiaeList trailing = AbstractNodeFactory.createMinutiaeList(whitespace);
        List<Node> literals = new ArrayList<>();

        // Filter same data type
        // Generate two hashMaps 1 for parent travers 2 for child travers
        Content bodyContent = requestBody.getContent();
        Set<Map.Entry<String, MediaType>> entries = bodyContent.entrySet();
        Set<Map.Entry<String, MediaType>> updatedEntries = bodyContent.entrySet();
        HashSet<Map.Entry<String, MediaType>> equalDataType =new HashSet();

        Iterator<Map.Entry<String, MediaType>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            // remove element from updateEntries
            Map.Entry<String, MediaType> mediaTypeEntry = iterator.next();
            updatedEntries.remove(mediaTypeEntry.getKey());
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
            Iterator<Map.Entry<String, MediaType>> iter = equalDataType.iterator();
            while (iter.hasNext()) {
                Map.Entry<String, MediaType> next =
                        createBasicLiteralNodeList(comma, leading, trailing, literals, iter);
                MediaType value = next.getValue();
                Schema schema = value.getSchema();
                identifierToken = getIdentifierToken(identifierToken, schema);
            }
        } else {
            Iterator<Map.Entry<String, MediaType>> content = requestBody.getContent().entrySet().iterator();
            Map.Entry<String, MediaType> next = createBasicLiteralNodeList(comma, leading, trailing, literals, content);
            MediaType value = next.getValue();
            Schema schema = value.getSchema();
            identifierToken = getIdentifierToken(identifierToken, schema);
        }

        literals.remove(literals.size() - 1);
        SeparatedNodeList<Node> expression = NodeFactory.createSeparatedNodeList(literals);
        Token closeBracket = AbstractNodeFactory.createIdentifierToken("]");
        ListConstructorExpressionNode valueExpr = NodeFactory.createListConstructorExpressionNode(openBracket,
                expression, closeBracket);
        SpecificFieldNode specificFieldNode = NodeFactory.createSpecificFieldNode(null, mediaType, colon,
                valueExpr);
        SeparatedNodeList<MappingFieldNode> fields = NodeFactory.createSeparatedNodeList(specificFieldNode);
        Token closeBrace = AbstractNodeFactory.createIdentifierToken("}");
        MappingConstructorExpressionNode annotValue = NodeFactory.createMappingConstructorExpressionNode(openBrace,
                fields, closeBrace);
        AnnotationNode annotationNode = NodeFactory.createAnnotationNode(atToken, annotReference, annotValue);

        NodeList<AnnotationNode> annotation =  NodeFactory.createNodeList(annotationNode);
        SimpleNameReferenceNode typeName = NodeFactory.createSimpleNameReferenceNode(identifierToken);
        Token paramName = AbstractNodeFactory.createIdentifierToken(" payload");

        RequiredParameterNode payload =
                NodeFactory.createRequiredParameterNode(annotation, typeName, paramName);

        params.add(comma);
        params.add(payload);
    }

    private static Map.Entry<String, MediaType> createBasicLiteralNodeList(Token comma, MinutiaeList leading,
                                                                           MinutiaeList trailing, List<Node> literals,
                                                                           Iterator<Map.Entry<String, MediaType>> content) {

        Map.Entry<String, MediaType> next = content.next();
        String text = next.getKey();
        Token literalToken = AbstractNodeFactory.createLiteralValueToken(null, text, leading, trailing);
        BasicLiteralNode basicLiteralNode = NodeFactory.createBasicLiteralNode(null, literalToken);
        literals.add(basicLiteralNode);
        literals.add(comma);
        return next;
    }

    private static IdentifierToken getIdentifierToken(IdentifierToken identifierToken, Schema schema)
            throws BallerinaOpenApiException {

        if (schema.get$ref() != null) {
            identifierToken =
                    AbstractNodeFactory.createIdentifierToken(extractReferenceType(schema.get$ref()));
        } else if (schema.getType() != null) {
            identifierToken = AbstractNodeFactory.createIdentifierToken(schema.getType() + " ");
        }
        return identifierToken;
    }

    private static void createNodeForQueryParam(List<Node> params, Token comma, List<Parameter> parameters) {

        for (Parameter parameter: parameters) {
            if (parameter.getIn().trim().equals("query")) {
                params.add(comma);
                Schema schema = parameter.getSchema();
                NodeList<AnnotationNode> annotations = NodeFactory.createEmptyNodeList();
                IdentifierToken parameterName =
                        AbstractNodeFactory.createIdentifierToken(" " + escapeIdentifier(parameter.getName().trim()));
                if (parameter.getRequired()) {
                    //Required without typeDescriptor
                    //When it has arrayType
                    if (schema instanceof ArraySchema) {
                        Schema<?> items = ((ArraySchema) schema).getItems();
                        if (!(items instanceof ObjectSchema) || !(items instanceof ArraySchema)) {
                            // create arrayTypeDescriptor
                            //1. memberTypeDescriptor
                            ArrayTypeDescriptorNode arrayTypeName = getArrayTypeDescriptorNode(items);
                            RequiredParameterNode arrayRparam = NodeFactory
                                    .createRequiredParameterNode(annotations, arrayTypeName, parameterName);
                            params.add(arrayRparam);

                        } else {
                            // handle in case swagger has nested array or record type
                        }
                    } else {
                        Token name =
                                AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(schema.getType().toLowerCase().trim()));
                        BuiltinSimpleNameReferenceNode rTypeName =
                                NodeFactory.createBuiltinSimpleNameReferenceNode(null, name);
                        RequiredParameterNode param1 =
                                NodeFactory.createRequiredParameterNode(annotations, rTypeName, parameterName);
                        params.add(param1);
                    }
                } else {
                    //Optional TypeDescriptor
                    //Array type
                    Token questionMark = NodeFactory.createIdentifierToken("?");
                    //When it has arrayType
                    if (schema instanceof ArraySchema) {
                        Schema<?> items = ((ArraySchema) schema).getItems();
                        if (!(items instanceof ObjectSchema) || !(items instanceof ArraySchema)) {
                            // create arrayTypeDescriptor
                            ArrayTypeDescriptorNode arrayTypeName = getArrayTypeDescriptorNode(items);
                            // create Optional type descriptor
                            OptionalTypeDescriptorNode optionalTypeDescriptorNode =
                                    NodeFactory.createOptionalTypeDescriptorNode(arrayTypeName, questionMark);
                            RequiredParameterNode arrayRparam = NodeFactory
                                    .createRequiredParameterNode(annotations, optionalTypeDescriptorNode,
                                            parameterName);
                            params.add(arrayRparam);

                        } else {
                            // handle in case swagger has nested array or record type
                        }
                    } else {
                        Token name =
                                AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(schema.getType().toLowerCase().trim()));
                        BuiltinSimpleNameReferenceNode rTypeName =
                                NodeFactory.createBuiltinSimpleNameReferenceNode(null, name);
                        OptionalTypeDescriptorNode optionalTypeDescriptorNode =
                                NodeFactory.createOptionalTypeDescriptorNode(rTypeName, questionMark);
                        RequiredParameterNode param1 =
                                NodeFactory.createRequiredParameterNode(annotations, optionalTypeDescriptorNode, parameterName);
                        params.add(param1);
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
        Token openBracket = AbstractNodeFactory.createIdentifierToken("[");
        Token closeBracket = AbstractNodeFactory.createIdentifierToken("]");

        return NodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openBracket, null,
                closeBracket);
    }

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

    private static String getHttpStatusCode(String code) {
        switch (code) {
            case "200":
                return "Ok";
            case "400":
                return "BadRequest";
            case "401":
                return "Unauthorized";
            case "404":
                return "NotFound";
            default:
                return "";
        }
    }

    private static UnionTypeDescriptorNode getUnionNode(Iterator<Map.Entry<String, ApiResponse>> responseIter) {
        List<QualifiedNameReferenceNode> qualifiedNodes = new ArrayList<>();
        Token pipeToken = AbstractNodeFactory.createIdentifierToken("|");

        while (responseIter.hasNext()) {
            Map.Entry<String, ApiResponse> response = responseIter.next();
            String code = getHttpStatusCode(response.getKey().trim());
            if (response.getValue().getContent() == null && response.getValue().get$ref() == null) {
                //key and value
                QualifiedNameReferenceNode node = getQualifiedNameReferenceNode("http", code);
                qualifiedNodes.add(node);
            }
        }
        QualifiedNameReferenceNode right = qualifiedNodes.get(qualifiedNodes.size() - 1);
        QualifiedNameReferenceNode traversRight = qualifiedNodes.get(qualifiedNodes.size() - 2);
        UnionTypeDescriptorNode traversUnion = NodeFactory.createUnionTypeDescriptorNode(traversRight, pipeToken,
                right);
        if (qualifiedNodes.size() >= 3) {
            for (int i = qualifiedNodes.size() - 3; i >= 0 ; i--) {
                traversUnion = NodeFactory.createUnionTypeDescriptorNode(qualifiedNodes.get(i), pipeToken, traversUnion);
                System.out.println(qualifiedNodes.get(i));
            }
        }
        return traversUnion;
    }

}
