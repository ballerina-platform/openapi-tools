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
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiType;
import org.ballerinalang.openapi.utils.GeneratorConstants;
import org.ballerinalang.openapi.utils.TypeExtractorUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    public static void generateSyntaxTree(String definitionPath, String serviceName, Filter filter) throws IOException,
            BallerinaOpenApiException, FormatterException {
        // Create imports http and openapi
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode("ballerina", "http");
        ImportDeclarationNode importForOpenapi = GeneratorUtils.getImportDeclarationNode("ballerina", "openapi");
        // Add multiple imports
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createNodeList(importForHttp, importForOpenapi);
        // Summaries OpenAPI details
//        final BallerinaOpenApiType openApi = getBallerinaOpenApiType(definitionPath, serviceName, filter);
        OpenAPI openApi = getBallerinaOpenApiType(definitionPath, serviceName, filter);

        final BallerinaOpenApiType openApi2 = TypeExtractorUtil.extractOpenApiObject(openApi, filter);
        openApi2.setBalServiceName(serviceName);
        openApi2.setServers(openApi);
        openApi2.setTags(openApi.getTags());

        // Generate listeners
        if (!openApi.getServers().isEmpty()) {
            for (Server server:openApi.getServers()) {
            }
            // Generate listener
            ListenerDeclarationNode listener = getListenerDeclarationNode(openApi2);

            // Generate ServiceDeclaration
            NodeList<Token> qualifiers = AbstractNodeFactory.createEmptyNodeList();
            Token serviceKeyWord = AbstractNodeFactory.createIdentifierToken(" service ");
            String[] basePathNode = openApi2.getServers().get(0).getBasePath().split("/");
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
//            System.out.println(generatedSyntaxTree.toSourceCode());
            System.out.println(Formatter.format(generatedSyntaxTree.toSourceCode()));

        } else {
            //handel if servers empty
        }

        // Generate Service - done
        // -- Generate resource function - done
        // -- Generate multiple resource - done
        // -- Generate multiple query parameters -done
        // -- Generate multiple path parameters -done
        // -- fix formatter issue
        // -- fix request Payload - partial done (for single request)
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
        Token returnKeyWord = AbstractNodeFactory.createIdentifierToken(" return ");
        NodeList<AnnotationNode> returnAnnotation = NodeFactory.createEmptyNodeList();
        // create Type
        // --errortypeDescriptor
        Token errorKeyWordToken = AbstractNodeFactory.createIdentifierToken("error");
        // errorTypeParamsNode can be null
        ErrorTypeDescriptorNode errorTypeDescriptorNode =
                NodeFactory.createErrorTypeDescriptorNode(errorKeyWordToken, null);
        Token questionMarkToken = AbstractNodeFactory.createIdentifierToken("?");

        OptionalTypeDescriptorNode type =
                NodeFactory.createOptionalTypeDescriptorNode(errorTypeDescriptorNode, questionMarkToken);
        ReturnTypeDescriptorNode returnNode = NodeFactory.createReturnTypeDescriptorNode(returnKeyWord, annotations,
                type);
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
        Iterator<Map.Entry<String, MediaType>> content = requestBody.getContent().entrySet().iterator();
        List<Node> literals = new ArrayList<>();
        while (content.hasNext()) {
            Map.Entry<String, MediaType> next = content.next();
            String text = next.getKey();
            Token literalToken = AbstractNodeFactory.createLiteralValueToken(null, text,leading, trailing);
            BasicLiteralNode basicLiteralNode = NodeFactory.createBasicLiteralNode(null, literalToken);
            literals.add(basicLiteralNode);
            literals.add(comma);
            MediaType value = next.getValue();
            Schema schema = value.getSchema();
            if (schema.get$ref() != null) {
                identifierToken =
                        AbstractNodeFactory.createIdentifierToken(extractReferenceType(schema.get$ref()));
            } else if (schema.getType() != null) {
                identifierToken = AbstractNodeFactory.createIdentifierToken(schema.getType() + " ");
            }
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

    private static OpenAPI getBallerinaOpenApiType(String definitionPath, String serviceName,
                                                                Filter filter)
            throws IOException, BallerinaOpenApiException {

        String openAPIFileContent = Files.readString(Paths.get(definitionPath));
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
}
