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
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.ErrorMessages;
import io.ballerina.openapi.cmd.model.GenSrcFile;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.File;
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

import javax.ws.rs.core.MediaType;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExpressionStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.openapi.cmd.OpenApiMesseges.BAL_KEYWORDS;
import static io.ballerina.openapi.generators.GeneratorConstants.ANY_TYPE;
import static io.ballerina.openapi.generators.GeneratorConstants.APPLICATION_PDF;
import static io.ballerina.openapi.generators.GeneratorConstants.BALLERINA;
import static io.ballerina.openapi.generators.GeneratorConstants.DELETE;
import static io.ballerina.openapi.generators.GeneratorConstants.EXPLODE;
import static io.ballerina.openapi.generators.GeneratorConstants.GET;
import static io.ballerina.openapi.generators.GeneratorConstants.HEAD;
import static io.ballerina.openapi.generators.GeneratorConstants.IMAGE_PNG;
import static io.ballerina.openapi.generators.GeneratorConstants.OPTIONS;
import static io.ballerina.openapi.generators.GeneratorConstants.PATCH;
import static io.ballerina.openapi.generators.GeneratorConstants.PUT;
import static io.ballerina.openapi.generators.GeneratorConstants.SQUARE_BRACKETS;
import static io.ballerina.openapi.generators.GeneratorConstants.STYLE;
import static io.ballerina.openapi.generators.GeneratorConstants.TRACE;

/**
 * This class util for store all the common scenarios.
 */
public class GeneratorUtils {

    public static final MinutiaeList SINGLE_WS_MINUTIAE = getSingleWSMinutiae();

    public static ImportDeclarationNode getImportDeclarationNode(String orgName, String moduleName) {

        Token importKeyword = AbstractNodeFactory.createIdentifierToken("import", SINGLE_WS_MINUTIAE,
                SINGLE_WS_MINUTIAE);
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
        IdentifierToken identifierToken = AbstractNodeFactory.createIdentifierToken(identifier, SINGLE_WS_MINUTIAE
                , SINGLE_WS_MINUTIAE);
        return NodeFactory.createQualifiedNameReferenceNode(modulePrefixToken, colon, identifierToken);
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
                    String pathParam = escapeIdentifier(pathNode.replaceAll("[{}]", ""));
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
                            AbstractNodeFactory.createIdentifierToken(escapeIdentifier(pathNode.trim()));
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
        if (GeneratorConstants.TYPE_MAP.containsKey(type)) {
            return GeneratorConstants.TYPE_MAP.get(type);
        } else {
            throw new BallerinaOpenApiException("Unsupported OAS data type `" + type + "`");
        }
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
            return escapeIdentifier(identifier.substring(0, 1).toLowerCase(Locale.ENGLISH) + identifier.substring(1));
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

    public static boolean hasTags(List<String> tags, List<String> filterTags) {
        return !Collections.disjoint(filterTags, tags);
    }

    /**
     * Util for take OpenApi spec from given yaml file.
     */
    public static OpenAPI getOpenAPIFromOpenAPIV3Parser(Path definitionPath) throws
            IOException, BallerinaOpenApiException {

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
            StringBuilder errorMessage = new StringBuilder("OpenAPI file has errors: \n\n");
            for (String message: parseResult.getMessages()) {
                errorMessage.append(message);
            }
            throw new BallerinaOpenApiException(errorMessage.toString());
        }
        return parseResult.getOpenAPI();
    }


    /**
     * Generate BallerinaMediaType for all the mediaTypes.
     */
    public static String getBallerinaMediaType(String mediaType) {
        switch (mediaType) {
            case MediaType.APPLICATION_JSON:
                return SyntaxKind.JSON_KEYWORD.stringValue();
            case MediaType.APPLICATION_XML:
                return SyntaxKind.XML_KEYWORD.stringValue();
            case MediaType.APPLICATION_FORM_URLENCODED:
            case MediaType.TEXT_HTML:
            case MediaType.TEXT_PLAIN:
                return STRING_KEYWORD.stringValue();
            case IMAGE_PNG:
            case MediaType.APPLICATION_OCTET_STREAM:
            case APPLICATION_PDF:
            case ANY_TYPE:
                return SyntaxKind.BYTE_KEYWORD.stringValue() + SQUARE_BRACKETS;
            default:
                return SyntaxKind.JSON_KEYWORD.stringValue();
            // TODO: fill other types
        }
    }

    /**
     * Generate remote function method name , when operation ID is not available for given operation.
     *
     * @param paths - swagger paths object
     * @return {@link io.swagger.v3.oas.models.Paths }
     */
    public static Paths setOperationId(Paths paths) {
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

    private static String getOperationId(String[] split, String method) {
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
    public static VariableDeclarationNode getSimpleStatement(String responseType, String variable,
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
    public static ExpressionStatementNode getSimpleExpressionStatementNode(String expression) {
        SimpleNameReferenceNode expressionNode = createSimpleNameReferenceNode(
                createIdentifierToken(expression));
        return createExpressionStatementNode(null, expressionNode, createToken(SEMICOLON_TOKEN));
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

    private static MinutiaeList getSingleWSMinutiae() {
        Minutiae whitespace = AbstractNodeFactory.createWhitespaceMinutiae(" ");
        MinutiaeList leading = AbstractNodeFactory.createMinutiaeList(whitespace);
        return leading;
    }

    /**
     * This method for setting the file name for generated file.
     *
     * @param listFiles      generated files
     * @param gFile          GenSrcFile object
     * @param duplicateCount add the tag with duplicate number if file already exist
     */
    public static void setGeneratedFileName(List<File> listFiles, GenSrcFile gFile, int duplicateCount) {
        for (File listFile : listFiles) {
            String listFileName = listFile.getName();
            if (listFileName.contains(".") && ((listFileName.split("\\.")).length >= 2) &&
                    (listFileName.split("\\.")[0].equals(gFile.getFileName().split("\\.")[0]))) {
                duplicateCount = 1 + duplicateCount;
            }
        }
        gFile.setFileName(gFile.getFileName().split("\\.")[0] + "." + (duplicateCount) + "." +
                gFile.getFileName().split("\\.")[1]);
    }

    /**
     * Create each item of the encoding map.
     *
     * @param filedOfMap    Includes all the items in the encoding map
     * @param style         Defines how multiple values are delimited and explode
     * @param explode       Specifies whether arrays and objects should generate separate parameters
     * @param key           Key of the item in the map
     */
    public static void createEncodingMap(List<Node> filedOfMap, String style, Boolean explode, String key) {
        IdentifierToken fieldName = createIdentifierToken('"' + key + '"');
        Token colon = createToken(COLON_TOKEN);
        SpecificFieldNode styleField = createSpecificFieldNode(null,
                createIdentifierToken(STYLE), createToken(COLON_TOKEN),
                createRequiredExpressionNode(createIdentifierToken(style.toUpperCase(Locale.ROOT))));
        SpecificFieldNode explodeField = createSpecificFieldNode(null,
                createIdentifierToken(EXPLODE), createToken(COLON_TOKEN),
                createRequiredExpressionNode(createIdentifierToken(explode.toString())));
        ExpressionNode expressionNode = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(styleField, createToken(COMMA_TOKEN),
                        explodeField),
                createToken(CLOSE_BRACE_TOKEN));
        SpecificFieldNode specificFieldNode = createSpecificFieldNode(null,
                fieldName, colon, expressionNode);
        filedOfMap.add(specificFieldNode);
        filedOfMap.add(createToken(COMMA_TOKEN));
    }

    public static boolean checkImportDuplicate(List<ImportDeclarationNode> imports, String module) {
        for (ImportDeclarationNode importModule:imports) {
            StringBuilder moduleBuilder = new StringBuilder();
            for (IdentifierToken identifierToken : importModule.moduleName()) {
                moduleBuilder.append(identifierToken.toString().trim());
            }
            if (BALLERINA.equals((importModule.orgName().get()).orgName().toString().trim()) &&
                    module.equals(moduleBuilder.toString())) {
                return true;
            }
        }
        return false;
    }

    public static void addImport(List<ImportDeclarationNode> imports, String module) {
        if (!checkImportDuplicate(imports, module)) {
            ImportDeclarationNode importModule = GeneratorUtils.getImportDeclarationNode(BALLERINA, module);
            imports.add(importModule);
        }
    }
}
