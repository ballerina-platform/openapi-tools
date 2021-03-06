package org.ballerinalang.generators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.ballerinalang.ballerina.Constants;
import org.ballerinalang.openapi.utils.GeneratorConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.openapi.OpenApiMesseges.BAL_KEYWORDS;

public class GeneratorUtils {

    public static ImportDeclarationNode getImportDeclarationNode(String orgName, String moduleName) {
        Token importKeyword = AbstractNodeFactory.createIdentifierToken("import ");
        Token orgNameToken = AbstractNodeFactory.createIdentifierToken(orgName);
        Token slashToken = AbstractNodeFactory.createIdentifierToken("/");
        ImportOrgNameNode importOrgNameNode = NodeFactory.createImportOrgNameNode(orgNameToken, slashToken);
        Token moduleNameToken = AbstractNodeFactory.createIdentifierToken(moduleName);
        SeparatedNodeList<IdentifierToken> moduleNodeList = AbstractNodeFactory.createSeparatedNodeList(moduleNameToken);
        Token semicolon = AbstractNodeFactory.createIdentifierToken(";");

        return NodeFactory.createImportDeclarationNode(importKeyword, importOrgNameNode,
                moduleNodeList, null, semicolon );
    }

    public static QualifiedNameReferenceNode getQualifiedNameReferenceNode(String modulePrefix, String identifier) {

        Token modulePrefixToken = AbstractNodeFactory.createIdentifierToken(modulePrefix);
        Token colon = AbstractNodeFactory.createIdentifierToken(":");
        IdentifierToken identifierToken = AbstractNodeFactory.createIdentifierToken(identifier);
        return NodeFactory.createQualifiedNameReferenceNode(modulePrefixToken, colon, identifierToken);
    }


    public static ListenerDeclarationNode getListenerDeclarationNode(Integer port, String host) {

        // Take first server to Map
        Token listenerKeyword = AbstractNodeFactory.createIdentifierToken("listener");
        // Create type descriptor
        Token modulePrefix = AbstractNodeFactory.createIdentifierToken(" http");
        Token colon = AbstractNodeFactory.createIdentifierToken(":");
        IdentifierToken identifier = AbstractNodeFactory.createIdentifierToken("Listener");
        QualifiedNameReferenceNode typeDescriptor = NodeFactory.createQualifiedNameReferenceNode(modulePrefix,
                colon, identifier);
        // Create variable
        Token variableName = AbstractNodeFactory.createIdentifierToken(" ep0 ");
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
                , String.valueOf(port),leading, trailing);
        BasicLiteralNode expression = NodeFactory.createBasicLiteralNode(SyntaxKind.NUMERIC_LITERAL, literalToken);

        PositionalArgumentNode portNode = NodeFactory.createPositionalArgumentNode(expression);
        // 2. Create comma
        Token comma = AbstractNodeFactory.createIdentifierToken(",");

        // 3. Create host node
        Token name = AbstractNodeFactory.createIdentifierToken("config ");
        SimpleNameReferenceNode argumentName = NodeFactory.createSimpleNameReferenceNode(name);
        // 3.3 create expression
        Token openBrace = AbstractNodeFactory.createIdentifierToken("{");

        Token fieldName = AbstractNodeFactory.createIdentifierToken("host");
        Token literalHostToken = AbstractNodeFactory.createIdentifierToken(host, leading, trailing);
        BasicLiteralNode valueExpr = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                literalHostToken);
        MappingFieldNode HostNode = NodeFactory.createSpecificFieldNode(null, fieldName, colon, valueExpr);
        SeparatedNodeList<MappingFieldNode> fields = NodeFactory.createSeparatedNodeList(HostNode);
        Token closeBrace = AbstractNodeFactory.createIdentifierToken("}");

        MappingConstructorExpressionNode hostExpression =
                NodeFactory.createMappingConstructorExpressionNode(openBrace, fields, closeBrace);

        NamedArgumentNode namedArgumentNode =
                NodeFactory.createNamedArgumentNode(argumentName, equalsToken, hostExpression);

        SeparatedNodeList<FunctionArgumentNode> arguments = NodeFactory.createSeparatedNodeList(portNode,
                comma, namedArgumentNode);

        Token closeParenToken = AbstractNodeFactory.createIdentifierToken(")");

        ParenthesizedArgList parenthesizedArgList =
                NodeFactory.createParenthesizedArgList(openParenToken, arguments, closeParenToken);
        ImplicitNewExpressionNode initializer =
                NodeFactory.createImplicitNewExpressionNode(newKeyword, parenthesizedArgList);

        Token semicolonToken = AbstractNodeFactory.createIdentifierToken(";");
        return NodeFactory.createListenerDeclarationNode(null, null, listenerKeyword,
                typeDescriptor, variableName, equalsToken, initializer, semicolonToken);
    }

    public static List<Node> getRelativeResourcePath(Map.Entry<String, PathItem> path,
                                                     Map.Entry<PathItem.HttpMethod, Operation> operation) {

        List<Node> functionRelativeResourcePath = new ArrayList<>();
        String[] pathNodes = path.getKey().split("/");
        Token slash = AbstractNodeFactory.createIdentifierToken("/");
        if (pathNodes.length > 2) {
            for (String pathNode: pathNodes) {
                if (pathNode.contains("{")) {
                    String pathParam = pathNode.replaceAll("[{}]", "");
                    if (operation.getValue().getParameters() != null) {
                        for (Parameter parameter: operation.getValue().getParameters()) {
                            if (parameter.getIn() == null) {
                                break;
                            }
                            if (pathParam.trim().equals(parameter.getName().trim()) && parameter.getIn().equals("path")) {
                                Token ppOpenB = AbstractNodeFactory.createIdentifierToken("[");
                                NodeList<AnnotationNode> ppAnnotation = NodeFactory.createEmptyNodeList();
                                // TypeDescriptor
                                Token name;
                                if (parameter.getSchema() == null) {
                                    name = AbstractNodeFactory.createIdentifierToken("string");
                                } else {
                                    name = AbstractNodeFactory.createIdentifierToken(parameter.getSchema().getType());
                                }
                                BuiltinSimpleNameReferenceNode builtSNRNode =
                                        NodeFactory.createBuiltinSimpleNameReferenceNode(null, name);
                                IdentifierToken paramName =
                                        AbstractNodeFactory.createIdentifierToken(" " + parameter.getName().trim());
                                Token ppCloseB = AbstractNodeFactory.createIdentifierToken("]");

                                ResourcePathParameterNode resourcePathParameterNode = NodeFactory
                                        .createResourcePathParameterNode(
                                                SyntaxKind.RESOURCE_PATH_SEGMENT_PARAM, ppOpenB,
                                                ppAnnotation, builtSNRNode, null, paramName, ppCloseB);

                                functionRelativeResourcePath.add(resourcePathParameterNode);
                                functionRelativeResourcePath.add(slash);
                                break;
                            }
                        }
                    }
                } else {
                    IdentifierToken idToken =
                            AbstractNodeFactory.createIdentifierToken(pathNode.trim());
                    functionRelativeResourcePath.add(idToken);
                    functionRelativeResourcePath.add(slash);
                }
            }
            functionRelativeResourcePath.remove(functionRelativeResourcePath.size() - 1);
        } else {
            IdentifierToken idToken =
                    AbstractNodeFactory.createIdentifierToken(pathNodes[1]);
            functionRelativeResourcePath.add(idToken);
        }
        return functionRelativeResourcePath;
    }

    /**
     * Method for convert openApi type to ballerina type.
     * @param type  OpenApi parameter types
     * @return ballerina type
     */
    public static String convertOpenAPITypeToBallerina(String type) {
        String convertedType;
        switch (type) {
            case Constants.INTEGER:
                convertedType = "int";
                break;
            case Constants.STRING:
                convertedType = "string";
                break;
            case Constants.BOOLEAN:
                convertedType = "boolean";
                break;
            case Constants.ARRAY:
                convertedType = "[]";
                break;
            case Constants.OBJECT:
                convertedType = "record";
                break;
            case Constants.DECIMAL:
                convertedType = "decimal";
                break;
            default:
                convertedType = "";
        }
        return convertedType;
    }


    /**
     * This method will escape special characters used in method names and identifiers.
     *
     * @param identifier - identifier or method name
     * @return - escaped string
     */
    public static String escapeIdentifier(String identifier) {
        if (!identifier.matches("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b") || BAL_KEYWORDS.stream().anyMatch(identifier::equals)) {
            identifier = identifier.replace("-", "");
            identifier = identifier.replace("$", "");

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
