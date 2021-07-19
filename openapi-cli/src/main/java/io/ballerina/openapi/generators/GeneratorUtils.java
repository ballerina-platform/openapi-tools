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

package io.ballerina.openapi.generators;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
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
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.error.ErrorMessages;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.openapi.Constants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExpressionStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownParameterDocumentationLineNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.openapi.cmd.OpenApiMesseges.BAL_KEYWORDS;
import static io.ballerina.openapi.cmd.OpenApiMesseges.BAL_TYPES;
import static io.ballerina.openapi.generators.GeneratorConstants.DELETE;
import static io.ballerina.openapi.generators.GeneratorConstants.GET;
import static io.ballerina.openapi.generators.GeneratorConstants.HEAD;
import static io.ballerina.openapi.generators.GeneratorConstants.OPTIONS;
import static io.ballerina.openapi.generators.GeneratorConstants.PATCH;
import static io.ballerina.openapi.generators.GeneratorConstants.PUT;
import static io.ballerina.openapi.generators.GeneratorConstants.TRACE;

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
        } else if (!identifier.matches("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b")
                || BAL_KEYWORDS.stream().anyMatch(identifier::equals)) {

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
     * Generate operationId by removing special characters.
     *
     * @param identifier input function name, record name or operation Id
     * @return string with new generated name
     */
    public static String getValidName(String identifier, boolean isSchema) {
        //For the flatten enable we need to remove first Part of valid name check
        // this - > !identifier.matches("\\b[a-zA-Z][a-zA-Z0-9]*\\b") &&
        if (!identifier.matches("\\b[0-9]*\\b")) {
            String[] split = identifier.split(GeneratorConstants.ESCAPE_PATTERN);
            StringBuilder validName = new StringBuilder();
            for (String part: split) {
                if (!part.isBlank()) {
                    if (split.length > 1) {
                        part = part.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                                part.substring(1).toLowerCase(Locale.ENGLISH);
                    }
                    validName.append(part);
                }
            }
            identifier = validName.toString();
        }
        if (isSchema) {
            return identifier.substring(0, 1).toUpperCase(Locale.ENGLISH) + identifier.substring(1);
        } else {
            return identifier.substring(0, 1).toLowerCase(Locale.ENGLISH) + identifier.substring(1);
        }
    }

    /**
     * Check the given recordName is valid name.
     *
     * @param recordName - String record name
     * @return           - boolean value
     */
    public static boolean isValidSchemaName(String recordName) {
        return !recordName.matches("\\b[0-9]*\\b");
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
            return refArray[refArray.length - 1];
        } else {
            throw new BallerinaOpenApiException("Invalid reference value : " + referenceVariable
                    + "\nBallerina only supports local reference values.");
        }
    }

    public boolean hasTags(List<String> tags, List<String> filterTags) {
        return !Collections.disjoint(filterTags, tags);
    }

    /**
     * Util for take OpenApi spec from given yaml file.
     */
    public OpenAPI getOpenAPIFromOpenAPIV3Parser(Path definitionPath) throws IOException, BallerinaOpenApiException {

        Path contractPath = java.nio.file.Paths.get(definitionPath.toString());
        if (!Files.exists(contractPath)) {
            throw new BallerinaOpenApiException(ErrorMessages.invalidFilePath(definitionPath.toString()));
        }
        if (!(definitionPath.toString().endsWith(".yaml") || definitionPath.toString().endsWith(".json") ||
                definitionPath.toString().endsWith(".yml"))) {
            throw new BallerinaOpenApiException(ErrorMessages.invalidFileType());
        }
        String openAPIFileContent = Files.readString(definitionPath);
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setFlatten(true);
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
    public String buildUrl(String absUrl, ServerVariables variables) {
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
    public String escapeType(String type) {
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
    public  String getBallerinaMediaType(String mediaType) {
        switch (mediaType) {
            case "*/*":
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
    public String getOneOfUnionType(List<Schema> oneOf) throws BallerinaOpenApiException {

        StringBuilder unionType = new StringBuilder();
        for (Schema oneOfSchema: oneOf) {
            if (oneOfSchema.getType() != null) {
                String oneOfSchemaType = oneOfSchema.getType();
                String type = convertOpenAPITypeToBallerina(oneOfSchemaType);
                if (oneOfSchema instanceof ArraySchema) {
                    ArraySchema oneOfArraySchema = (ArraySchema) oneOfSchema;
                    Schema<?> items = oneOfArraySchema.getItems();
                    if (items.get$ref() != null) {
                        String reference = items.get$ref();
                        type = getValidName(extractReferenceType(reference), true) + "[]";
                    } else if (items.getType() != null) {
                        type = convertOpenAPITypeToBallerina(items.getType().trim()) + "[]";
                    }
                }
                if (!type.equals("record")) {
                    unionType.append("|");
                    unionType.append(type);
                }
            } else if (oneOfSchema.get$ref() != null) {
                String type = extractReferenceType(oneOfSchema.get$ref());
                unionType.append("|");
                unionType.append(type);
            }
//            else if (!oneOfSchema.getProperties().isEmpty()) {
                // TODO: generate warning to move inline schema to schema
//            }
        }
        String unionTypeCont = unionType.toString();
        if (!unionTypeCont.isBlank() && unionTypeCont.startsWith("|")) {
            unionTypeCont = unionTypeCont.replaceFirst("\\|", "");
        }
        return unionTypeCont;
    }

    public  MarkdownParameterDocumentationLineNode createParamAPIDoc(String paramName, String description) {

        return createMarkdownParameterDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                createToken(SyntaxKind.PLUS_TOKEN), createIdentifierToken(paramName),
                createToken(SyntaxKind.MINUS_TOKEN), createNodeList(createLiteralValueToken(null
                        , description,  createEmptyMinutiaeList(), createEmptyMinutiaeList())));
    }

    /**
     * Generate remote function method name , when operation ID is not available for given operation.
     *
     * @param paths - swagger paths object
     * @return {@link io.swagger.v3.oas.models.Paths }
     */
    public  Paths setOperationId(Paths paths) {
        Set<Map.Entry<String, PathItem>> entries = paths.entrySet();
        for (Map.Entry<String, PathItem> entry: entries) {
            PathItem pathItem = entry.getValue();
            int countMissId = 0;
            for (Operation operation : entry.getValue().readOperations()) {
                if (operation.getOperationId() == null) {
                    //simplify here with 1++
                    countMissId = countMissId + 1;
                } else {
                    String operationId = getValidName(operation.getOperationId(), false);
                    operation.setOperationId(operationId);
                }
            }

            if (pathItem.getGet() != null) {
                Operation getOp = pathItem.getGet();
                if (getOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, GET);
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    getOp.setOperationId(operationId);
                }
            }
            if (pathItem.getPut() != null) {
                Operation putOp = pathItem.getPut();
                if (putOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, PUT);
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    putOp.setOperationId(operationId);
                }
            }
            if (pathItem.getPost() != null) {
                Operation postOp = pathItem.getPost();
                if (postOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, "post");
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    postOp.setOperationId(operationId);
                }
            }
            if (pathItem.getDelete() != null) {
                Operation deleteOp = pathItem.getDelete();
                if (deleteOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, DELETE);
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    deleteOp.setOperationId(operationId);
                }
            }
            if (pathItem.getOptions() != null) {
                Operation optionOp = pathItem.getOptions();
                if (optionOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, OPTIONS);
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    optionOp.setOperationId(operationId);
                }
            }
            if (pathItem.getHead() != null) {
                Operation headOp = pathItem.getHead();
                if (headOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, HEAD);
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    headOp.setOperationId(operationId);
                }
            }
            if (pathItem.getPatch() != null) {
                Operation patchOp = pathItem.getPatch();
                if (patchOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, PATCH);
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    patchOp.setOperationId(operationId);
                }
            }
            if (pathItem.getTrace() != null) {
                Operation traceOp = pathItem.getTrace();
                if (traceOp.getOperationId() == null) {
                    String operationId;
                    String[] split = entry.getKey().trim().split("/");
                    if (countMissId > 1) {
                        operationId = getOperationId(split, TRACE);
                    } else {
                        operationId = getOperationId(split, " ");
                    }
                    traceOp.setOperationId(operationId);
                }
            }
        }
        return paths;
    }

    private  String getOperationId(String[] split, String method) {
        String operationId;
        String regEx = "\\{([^}]*)\\}";
        Matcher matcher = Pattern.compile(regEx).matcher(split[split.length - 1]);
        if (matcher.matches()) {
            operationId = method + split[split.length - 2] + "By" + matcher.group(1);
        } else {
            operationId = method + split[split.length - 1];
        }
        return Character.toLowerCase(operationId.charAt(0)) + operationId.substring(1);
    }

    /*
     * Generate variableDeclarationNode.
     */
    public  VariableDeclarationNode getSimpleStatement(String responseType, String variable,
                                                             String initializer) {
        SimpleNameReferenceNode resTypeBind = createSimpleNameReferenceNode(createIdentifierToken(responseType));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(createIdentifierToken(variable));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(resTypeBind, bindingPattern);
        SimpleNameReferenceNode init = createSimpleNameReferenceNode(createIdentifierToken(initializer));

        return createVariableDeclarationNode(createEmptyNodeList(), null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), init, createToken(SEMICOLON_TOKEN));
    }

    /*
     * Generate expressionStatementNode.
     */
    public  ExpressionStatementNode getSimpleExpressionStatementNode(String expression) {
        SimpleNameReferenceNode expressionNode = createSimpleNameReferenceNode(
                createIdentifierToken(expression));
        return createExpressionStatementNode(null, expressionNode, createToken(SEMICOLON_TOKEN));
    }
}
