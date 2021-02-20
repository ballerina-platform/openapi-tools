package org.ballerinalang.generators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ErrorTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
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
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
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
import static org.ballerinalang.generators.GeneratorUtils.getRelativeResourcePath;

public class BallerinaServiceGenerator {

    public static void generateSyntaxTree(String definitionPath, String serviceName, Filter filter) throws IOException,
            BallerinaOpenApiException {
        // Create importors http and openapi
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
            System.out.println(generatedSyntaxTree.toSourceCode());
//            System.out.println(Formatter.format(generatedSyntaxTree.toSourceCode()));

        } else {
            //handel if servers empty
        }

        // Generate Service - done

        // -- Generate resource function - done
        // -- Generate multiple resource
        // -- Generate multiple query parameters
        // -- fix formatter issue
        // -- fix request Payload
        // -- fix response Payload

        // Create Module part Node

//        Token eofToken = AbstractNodeFactory.createIdentifierToken("");
//        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, members, eofToken);
//
//        TextDocument textDocument = TextDocuments.from("");
//        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
//        SyntaxTree generatedSyntaxTree = syntaxTree.modifyWith(modulePartNode);
////        SyntaxTree generatedSyntaxTree = syntaxTree.modifyWith(im);
//        System.out.println(generatedSyntaxTree.toSourceCode());
////        System.out.println(Formatter.format(generatedSyntaxTree.toSourceCode()));

    }

    private static void getFunctionDefinitionNode(List<Node> functions, Map.Entry<String, PathItem> path,
                                                  Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                  List<Node> pathNodes) {

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
        Token modulePrefix = AbstractNodeFactory.createIdentifierToken("http");
        Token colon = AbstractNodeFactory.createIdentifierToken(":");
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken("Request ");
        QualifiedNameReferenceNode typeName =
                NodeFactory.createQualifiedNameReferenceNode(modulePrefix, colon, identifier);
        // --paramName
        Token paramName = AbstractNodeFactory.createIdentifierToken("request");
        RequiredParameterNode httpNode =
                NodeFactory.createRequiredParameterNode(annotations, typeName, paramName);
        params.add(httpNode);

        Token comma = AbstractNodeFactory.createIdentifierToken(",");

        // Handle query parameter
        //type​ ​ BasicType​ ​ boolean|int|float|decimal|string​ ;
        //public​ ​ type​ ​ QueryParamType​ ()​ |BasicType|BasicType​ [];
        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
            for (Parameter parameter: parameters) {
                if (parameter.getIn().trim().equals("query")) {
                    Schema schema = parameter.getSchema();
                    if (parameter.getRequired()) {
                        //Required without typeDescriptor
                        NodeList<AnnotationNode> rAnnotations = NodeFactory.createEmptyNodeList();
                        //When it has arrayType
                        if (schema instanceof ArraySchema) {
                            Schema<?> items = ((ArraySchema) schema).getItems();
                            if (!(items instanceof ObjectSchema) || !(items instanceof ArraySchema)) {
                                // create arrayTypeDescriptor
                                //1. memberTypeDescriptor
                                Token name = AbstractNodeFactory.createIdentifierToken(items.getType().trim());
                                BuiltinSimpleNameReferenceNode memberTypeDesc =
                                        NodeFactory.createBuiltinSimpleNameReferenceNode(null, name);
                                //2. openBracket
                                Token openBracket = AbstractNodeFactory.createIdentifierToken("[");
                                //3. arraylength = null
                                //4. closeBracket
                                Token closeBracket = AbstractNodeFactory.createIdentifierToken("]");

                                ArrayTypeDescriptorNode arrayTypeName =
                                        NodeFactory.createArrayTypeDescriptorNode(memberTypeDesc, openBracket, null,
                                                closeBracket);

                                IdentifierToken arrayParamName =
                                        AbstractNodeFactory.createIdentifierToken(" " + escapeIdentifier(parameter.getName().trim()));

                                RequiredParameterNode arrayRparam = NodeFactory
                                        .createRequiredParameterNode(rAnnotations, arrayTypeName, arrayParamName);

                                params.add(comma);
                                params.add(arrayRparam);

                            } else {
                                // handle in case swagger has nested array or record type
                            }
                        } else {
                            Token name =
                                    AbstractNodeFactory.createIdentifierToken(convertOpenAPITypeToBallerina(schema.getType().toLowerCase().trim()));

                            BuiltinSimpleNameReferenceNode rTypeName =
                                    NodeFactory.createBuiltinSimpleNameReferenceNode(null, name);
                            IdentifierToken rParamName =
                                    AbstractNodeFactory.createIdentifierToken(" " + GeneratorUtils.escapeIdentifier(parameter.getName().trim()));
                            RequiredParameterNode param1 =
                                    NodeFactory.createRequiredParameterNode(rAnnotations, rTypeName, rParamName);
                            params.add(comma);
                            params.add(param1);
                        }
                    } else {
                        //Optional TypeDescriptor
                        //Array type
                        if (schema instanceof ArraySchema) {

                        } else {

                        }


                        }
                }
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

    private static OpenAPI getBallerinaOpenApiType(String definitionPath, String serviceName,
                                                                Filter filter)
            throws IOException, BallerinaOpenApiException {

        String openAPIFileContent = Files.readString(Paths.get(definitionPath));
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openAPIFileContent);

        if (parseResult.getMessages().size() > 0) {
            throw new BallerinaOpenApiException("Couldn't read or parse the definition from file: " + definitionPath);
        }
        OpenAPI api = parseResult.getOpenAPI();
        List<Server> servers = api.getServers();
        io.swagger.v3.oas.models.Paths paths = api.getPaths();


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
//        final BallerinaOpenApiType openApi = TypeExtractorUtil.extractOpenApiObject(api, filter);
//        openApi.setBalServiceName(serviceName);
//        openApi.setServers(api);
//        openApi.setTags(api.getTags());
//        return openApi;
    }
}
