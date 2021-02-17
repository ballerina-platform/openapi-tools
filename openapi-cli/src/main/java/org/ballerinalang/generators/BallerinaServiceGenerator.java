package org.ballerinalang.generators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.model.BallerinaServer;
import org.ballerinalang.openapi.typemodel.BallerinaOpenApiType;
import org.ballerinalang.openapi.utils.GeneratorConstants;
import org.ballerinalang.openapi.utils.TypeExtractorUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BallerinaServiceGenerator {

    public static void genetrateSyntaxTree(String definitionPath, String serviceName, Filter filter) throws IOException,
            BallerinaOpenApiException {
        // Create importors http and openapi
        ImportDeclarationNode importForHttp = GeneratorUtils.getImportDeclarationNode("ballerina", "http");
        ImportDeclarationNode importForOpenapi = GeneratorUtils.getImportDeclarationNode("ballerina", "http");
        // Add multiple imports
        NodeList<ImportDeclarationNode> imports = AbstractNodeFactory.createNodeList(importForHttp);
        imports.add(importForOpenapi);
        // Summaries OpenAPI details
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
        final BallerinaOpenApiType openApi = TypeExtractorUtil.extractOpenApiObject(api, filter);
        openApi.setBalServiceName(serviceName);
        openApi.setServers(api);
        openApi.setTags(api.getTags());


        // Generate listeners
        if (!openApi.getServers().isEmpty()) {
            for (BallerinaServer server:openApi.getServers()) {

            }
            // Take first server to Map
            Token listenerKeyword = AbstractNodeFactory.createIdentifierToken("listner");
            // Create type descriptor
            Token modulePrefix = AbstractNodeFactory.createIdentifierToken("http");
            Token colon = AbstractNodeFactory.createIdentifierToken(":");
            IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken("Listener");
            QualifiedNameReferenceNode typeDescriptor = NodeFactory.createQualifiedNameReferenceNode(modulePrefix,
                    colon, identifier);
            // Create variable
            Token variableName = AbstractNodeFactory.createIdentifierToken("ep0");
            MinutiaeList leading = AbstractNodeFactory.createEmptyMinutiaeList();
            Minutiae whitespace = AbstractNodeFactory.createWhitespaceMinutiae(" ");
            MinutiaeList trailing = AbstractNodeFactory.createMinutiaeList(whitespace);
            variableName.modify(leading, trailing);

            Token equalsToken = AbstractNodeFactory.createIdentifierToken("=");

            // Create initializer
            Token newKeyword = AbstractNodeFactory.createIdentifierToken("new");

            // Create parenthesizedArgList
            Token openParenToken = AbstractNodeFactory.createIdentifierToken("(");
            // Create arguments
            // 1. Create port Node
            Token literalToken = AbstractNodeFactory.createLiteralValueToken(SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN
                    , String.valueOf(openApi.getServers().get(0).getPort()),leading, trailing);
            BasicLiteralNode expression = NodeFactory.createBasicLiteralNode(SyntaxKind.NUMERIC_LITERAL, literalToken);

            PositionalArgumentNode portNode = NodeFactory.createPositionalArgumentNode(expression);
            // 2. Create comma
            Token comma = AbstractNodeFactory.createIdentifierToken(",");


            // 3. Create host node
            Token name = AbstractNodeFactory.createIdentifierToken("config ");
            SimpleNameReferenceNode argumentName = NodeFactory.createSimpleNameReferenceNode(name);
            // 3.3 create expresion
            Token openBrace = AbstractNodeFactory.createIdentifierToken("{");

            Token fieldName = AbstractNodeFactory.createIdentifierToken("host");
            Token literalHostToken = AbstractNodeFactory.createIdentifierToken(openApi.getServers().get(0).getHost(),
                    leading, trailing);
            BasicLiteralNode valueExpr = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                    literalHostToken);
            MappingFieldNode HostNode = NodeFactory.createSpecificFieldNode(null, fieldName, colon, valueExpr);
            SeparatedNodeList<MappingFieldNode> fields = NodeFactory.createSeparatedNodeList(HostNode);
            Token closeBrace = AbstractNodeFactory.createIdentifierToken("}");

            MappingConstructorExpressionNode hostExpression =
                    NodeFactory.createMappingConstructorExpressionNode(openBrace, fields, closeBrace);

            NamedArgumentNode namedArgumentNode =
                    NodeFactory.createNamedArgumentNode(argumentName, equalsToken, hostExpression);

            SeparatedNodeList<FunctionArgumentNode> arguments = NodeFactory.createSeparatedNodeList();
            arguments.add(portNode);
            //For testing
//            arguments.add(comma);
            arguments.add(namedArgumentNode);

            Token closeParenToken = AbstractNodeFactory.createIdentifierToken(")");

            ParenthesizedArgList parenthesizedArgList =
                    NodeFactory.createParenthesizedArgList(openParenToken, arguments, closeParenToken);
            ImplicitNewExpressionNode initializer =
                    NodeFactory.createImplicitNewExpressionNode(newKeyword, parenthesizedArgList);

            Token semicolonToken = AbstractNodeFactory.createIdentifierToken(":");
            ListenerDeclarationNode listener =
                    NodeFactory.createListenerDeclarationNode(null, null, listenerKeyword, typeDescriptor, variableName,
                            equalsToken, initializer, semicolonToken);
            NodeList<ModuleMemberDeclarationNode> members = AbstractNodeFactory.createEmptyNodeList();
            members.add(listener);

            Token eofToken = AbstractNodeFactory.createIdentifierToken("");
            ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, members, eofToken);
            TextDocument textDocument = TextDocuments.from("");
            SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
            SyntaxTree generatedSyntaxTree = syntaxTree.modifyWith(modulePartNode);
//        SyntaxTree generatedSyntaxTree = syntaxTree.modifyWith(im);
            System.out.println(generatedSyntaxTree.toSourceCode());
//        System.out.println(Formatter.format(generatedSyntaxTree.toSourceCode()));

//            NodeFactory.createModulePartNode(imports, )

        } else {
            //handel if servers empty
        }

        // Generate Service

        // -- Generate resource function
//        ModulePartNode modulePartNode = NodeFactory.createModulePartNode(imports, null, null);

//        TextDocument textDocument = TextDocuments.from("");
//        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
//        SyntaxTree generatedSyntaxTree = syntaxTree.modifyWith(modulePartNode);
////        SyntaxTree generatedSyntaxTree = syntaxTree.modifyWith(im);
//        System.out.println(generatedSyntaxTree.toSourceCode());
////        System.out.println(Formatter.format(generatedSyntaxTree.toSourceCode()));





    }
}
