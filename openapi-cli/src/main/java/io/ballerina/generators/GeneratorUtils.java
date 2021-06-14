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

package io.ballerina.generators;

import io.ballerina.ballerina.Constants;
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
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
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
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownParameterDocumentationLineNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.openapi.OpenApiMesseges.BAL_KEYWORDS;
import static io.ballerina.openapi.OpenApiMesseges.BAL_TYPES;

/**
 * This class util for store all the common scenarios.
 */
public class GeneratorUtils {

    public static ImportDeclarationNode getImportDeclarationNode(String orgName, String moduleName) {
        Token importKeyword = AbstractNodeFactory.createIdentifierToken("import ");
        Token orgNameToken = AbstractNodeFactory.createIdentifierToken(orgName);
        Token slashToken = AbstractNodeFactory.createIdentifierToken("/");
        ImportOrgNameNode importOrgNameNode = NodeFactory.createImportOrgNameNode(orgNameToken, slashToken);
        Token moduleNameToken = AbstractNodeFactory.createIdentifierToken(moduleName);
        SeparatedNodeList<IdentifierToken> moduleNodeList = AbstractNodeFactory.createSeparatedNodeList(
                moduleNameToken);
        Token semicolon = AbstractNodeFactory.createIdentifierToken(";");

        return NodeFactory.createImportDeclarationNode(importKeyword, importOrgNameNode,
                moduleNodeList, null, semicolon);
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
                , String.valueOf(port), leading, trailing);
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
        Token literalHostToken = AbstractNodeFactory.createIdentifierToken('"' + host + '"', leading, trailing);
        BasicLiteralNode valueExpr = NodeFactory.createBasicLiteralNode(SyntaxKind.STRING_LITERAL,
                literalHostToken);
        MappingFieldNode hostNode = NodeFactory.createSpecificFieldNode(null, fieldName, colon, valueExpr);
        SeparatedNodeList<MappingFieldNode> fields = NodeFactory.createSeparatedNodeList(hostNode);
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
                                                     Map.Entry<PathItem.HttpMethod, Operation> operation)
            throws BallerinaOpenApiException {

        List<Node> functionRelativeResourcePath = new ArrayList<>();
        String[] pathNodes = path.getKey().trim().split("/");
        Token slash = AbstractNodeFactory.createIdentifierToken("/");
        if (pathNodes.length >= 2) {
            for (String pathNode: pathNodes) {
                if (pathNode.contains("{")) {
                    String pathParam = pathNode.replaceAll("[{}]", "");
                    if (operation.getValue().getParameters() != null) {
                        for (Parameter parameter: operation.getValue().getParameters()) {
                            if (parameter.getIn() == null) {
                                break;
                            }
                            if (pathParam.trim().equals(parameter.getName().trim())
                                    && parameter.getIn().equals("path")) {

                                Token ppOpenB = AbstractNodeFactory.createIdentifierToken("[");
                                NodeList<AnnotationNode> ppAnnotation = NodeFactory.createEmptyNodeList();
                                // TypeDescriptor
                                Token name;
                                if (parameter.getSchema() == null) {
                                    name = AbstractNodeFactory.createIdentifierToken("string");
                                } else {
                                    name = AbstractNodeFactory.createIdentifierToken(
                                                    convertOpenAPITypeToBallerina(parameter.getSchema().getType()));
                                }
                                BuiltinSimpleNameReferenceNode builtSNRNode =
                                        NodeFactory.createBuiltinSimpleNameReferenceNode(null, name);
                                String parameterName = " " + escapeIdentifier(parameter.getName().trim());
                                IdentifierToken paramName = AbstractNodeFactory.createIdentifierToken(parameterName);
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
                } else if (!pathNode.isBlank()) {
                    IdentifierToken idToken =
                            AbstractNodeFactory.createIdentifierToken(pathNode.trim());
                    functionRelativeResourcePath.add(idToken);
                    functionRelativeResourcePath.add(slash);
                }
            }
            functionRelativeResourcePath.remove(functionRelativeResourcePath.size() - 1);
        } else if (pathNodes.length == 0) {
            IdentifierToken idToken = AbstractNodeFactory.createIdentifierToken(".");
            functionRelativeResourcePath.add(idToken);
        } else {
            IdentifierToken idToken = AbstractNodeFactory.createIdentifierToken(pathNodes[1].trim());
            functionRelativeResourcePath.add(idToken);
        }
        return functionRelativeResourcePath;
    }

    /**
     * Method for convert openApi type to ballerina type.
     *
     * @param type  OpenApi parameter types
     * @return ballerina type
     */
    public static String convertOpenAPITypeToBallerina(String type) throws BallerinaOpenApiException {
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
                convertedType = "record {}";
                break;
            case Constants.DECIMAL:
                convertedType = "decimal";
                break;
            case Constants.NUMBER:
                convertedType = "decimal";
                break;
            case Constants.DOUBLE:
            case Constants.FLOAT:
                convertedType = "float";
                break;
            default:
                throw new BallerinaOpenApiException("Unsupported OAS data type `" + type + "`");
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

        if (identifier.matches("\\b[0-9]*\\b")) {
            return "'" + identifier;
        } else if (!identifier.matches("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b"
            ) || BAL_KEYWORDS.stream().anyMatch(identifier::equals)) {

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

    public static boolean hasTags(List<String> tags, List<String> filterTags) {
        return !Collections.disjoint(filterTags, tags);
    }

    /**
     * Util for take OpenApi spec from given yaml file.
     */
    public static OpenAPI getBallerinaOpenApiType(Path definitionPath)
            throws IOException, BallerinaOpenApiException {
        String openAPIFileContent = Files.readString(definitionPath);
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
//        parseOptions.setFlatten(true);
//        parseOptions.setResolveFully(true);
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(openAPIFileContent, null, parseOptions);
        if (!parseResult.getMessages().isEmpty()) {
            throw new BallerinaOpenApiException("Couldn't read or parse the definition from file: " + definitionPath);
        }
        return parseResult.getOpenAPI();
    }

    /**
     * If there are template values in the {@code absUrl} derive resolved url using {@code variables}.
     *
     * @param absUrl abstract url with template values
     * @param variables variable values to populate the url template
     * @return resolved url
     */
    public static String buildUrl(String absUrl, ServerVariables variables) {
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
     * This method will escape special characters used in method names and identifiers.
     *
     * @param type - type or method name
     * @return - escaped string
     */
    public static String escapeType(String type) {
        if (!type.matches("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b") ||
                (BAL_KEYWORDS.stream().anyMatch(type::equals) && BAL_TYPES.stream().noneMatch(type::equals))) {
            // TODO: Temporary fix(es) as identifier literals only support alphanumerics when writing this.
            //  Refer - https://github.com/ballerina-platform/ballerina-lang/issues/18720
            type = type.replace("-", "");
            type = type.replace("$", "");

            type = type.replaceAll("([\\\\?!<>*\\-=^+()_{}|.$])", "\\\\$1");
            type = "'" + type;
        }
        return type;
    }

    /**
     * Generate BallerinaMediaType for all the mediaTypes.
     */
    public static String getBallerinaMeidaType(String mediaType) {
        switch (mediaType) {
            case "application/json":
                return SyntaxKind.JSON_KEYWORD.stringValue();
            case "application/xml":
                return SyntaxKind.XML_KEYWORD.stringValue();
            case "application/x-www-form-urlencoded":
            case "text/html":
            case "text/plain":
                return STRING_KEYWORD.stringValue();
            case "image/png":
            case "application/octet-stream":
            case "application/pdf":
                return SyntaxKind.BYTE_ARRAY_LITERAL.stringValue() + "[]";
            default:
                return SyntaxKind.JSON_KEYWORD.stringValue();
            // TODO: fill other types
        }
    }

    /**
     * This function for creating the UnionType string for handle oneOf data binding.
     *
     * @param oneOf - OneOf schema
     * @return - UnionString
     * @throws BallerinaOpenApiException
     */
    public static String getOneOfUnionType(List<Schema> oneOf) throws BallerinaOpenApiException {

        StringBuilder unionType = new StringBuilder();
        for (Schema oneOfSchema: oneOf) {
            if (oneOfSchema.getType() != null) {
                String type = convertOpenAPITypeToBallerina(oneOfSchema.getType());
                if (!type.equals("record")) {
                    unionType.append("|");
                    unionType.append(type);
                }
            }
            if (oneOfSchema.get$ref() != null) {
                String type = extractReferenceType(oneOfSchema.get$ref());
                unionType.append("|");
                unionType.append(type);
            }
        }
        String unionTypeCont = unionType.toString();
        if (!unionTypeCont.isBlank() && unionTypeCont.startsWith("|")) {
            unionTypeCont = unionTypeCont.replaceFirst("\\|", "");
        }
        return unionTypeCont;
    }

    public static MarkdownParameterDocumentationLineNode createParamAPIDoc(String paramName, String description) {

        return createMarkdownParameterDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                createToken(SyntaxKind.PLUS_TOKEN), createIdentifierToken(paramName),
                createToken(SyntaxKind.MINUS_TOKEN), createNodeList(createLiteralValueToken(null
                        , description,  createEmptyMinutiaeList(), createEmptyMinutiaeList())));
    }
}
