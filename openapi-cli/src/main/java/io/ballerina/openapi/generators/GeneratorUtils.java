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
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
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
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
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
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.openapi.generators.GeneratorConstants.ANY_TYPE;
import static io.ballerina.openapi.generators.GeneratorConstants.APPLICATION_PDF;
import static io.ballerina.openapi.generators.GeneratorConstants.BALLERINA;
import static io.ballerina.openapi.generators.GeneratorConstants.CLOSE_CURLY_BRACE;
import static io.ballerina.openapi.generators.GeneratorConstants.EXPLODE;
import static io.ballerina.openapi.generators.GeneratorConstants.IMAGE_PNG;
import static io.ballerina.openapi.generators.GeneratorConstants.LINE_SEPARATOR;
import static io.ballerina.openapi.generators.GeneratorConstants.OPEN_CURLY_BRACE;
import static io.ballerina.openapi.generators.GeneratorConstants.SLASH;
import static io.ballerina.openapi.generators.GeneratorConstants.SPECIAL_CHARACTERS_REGEX;
import static io.ballerina.openapi.generators.GeneratorConstants.SQUARE_BRACKETS;
import static io.ballerina.openapi.generators.GeneratorConstants.STRING;
import static io.ballerina.openapi.generators.GeneratorConstants.STYLE;

/**
 * This class util for store all the common scenarios.
 */
public class GeneratorUtils {

    public static final MinutiaeList SINGLE_WS_MINUTIAE = getSingleWSMinutiae();
    public static final List<String> BAL_KEYWORDS = SyntaxInfo.keywords();
    public static final MinutiaeList SINGLE_END_OF_LINE_MINUTIAE = getEndOfLineMinutiae();

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




    public static List<Node> getRelativeResourcePath(String path,
                                                     Map.Entry<PathItem.HttpMethod, Operation> operation)
            throws BallerinaOpenApiException {

        List<Node> functionRelativeResourcePath = new ArrayList<>();
        String[] pathNodes = path.split(SLASH);
        Token slash = AbstractNodeFactory.createIdentifierToken(SLASH);
        if (pathNodes.length >= 2) {
            for (String pathNode: pathNodes) {
                if (pathNode.contains(OPEN_CURLY_BRACE)) {
                    String pathParam = pathNode;
                    pathParam = pathParam.substring(pathParam.indexOf(OPEN_CURLY_BRACE) + 1);
                    pathParam = pathParam.substring(0, pathParam.indexOf(CLOSE_CURLY_BRACE));
                    pathParam = getValidName(pathParam, false);
                    // check whether path parameter segment has special character
                    String[] split = pathNode.split(CLOSE_CURLY_BRACE, 2);
                    Pattern pattern = Pattern.compile(SPECIAL_CHARACTERS_REGEX);
                    Matcher matcher = pattern.matcher(split[1]);
                    boolean isPathNameContainsSpecialCharacter = matcher.find();

                    /**
                     * TODO -> `onCall/[string id]\.json` type of url won't support from syntax
                     * issue https://github.com/ballerina-platform/ballerina-spec/issues/1138
                     * <pre>resource function get onCall/[string id]\.json() returns string {}</>
                     */
                    if (operation.getValue().getParameters() != null) {
                        for (Parameter parameter: operation.getValue().getParameters()) {
                            if (parameter.getIn() == null) {
                                break;
                            }
                            if (pathParam.trim().equals(getValidName(parameter.getName().trim(), false))
                                    && parameter.getIn().equals("path")) {

                                Token ppOpenB = AbstractNodeFactory.createToken(OPEN_BRACKET_TOKEN);
                                NodeList<AnnotationNode> ppAnnotation = NodeFactory.createEmptyNodeList();
                                // TypeDescriptor
                                Token name;
                                if (parameter.getSchema() == null) {
                                    name = AbstractNodeFactory.createIdentifierToken(STRING);
                                } else {
                                    name = AbstractNodeFactory.createIdentifierToken(
                                                    convertOpenAPITypeToBallerina(parameter.getSchema().getType()));
                                }
                                BuiltinSimpleNameReferenceNode builtSNRNode =
                                        NodeFactory.createBuiltinSimpleNameReferenceNode(null, name);
                                IdentifierToken paramName = AbstractNodeFactory.createIdentifierToken(
                                                isPathNameContainsSpecialCharacter ?
                                                        getValidName(pathNode, false) :
                                                        pathParam);
                                Token ppCloseB = AbstractNodeFactory.createToken(CLOSE_BRACKET_TOKEN);
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
        } else if (!identifier.matches("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b") || BAL_KEYWORDS.contains(identifier)) {
            identifier = identifier.replaceAll(GeneratorConstants.ESCAPE_PATTERN, "\\\\$1");
            return "'" + identifier;
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
            StringBuilder errorMessage = new StringBuilder("OpenAPI definition has errors: \n\n");
            for (String message: parseResult.getMessages()) {
                errorMessage.append(message).append(LINE_SEPARATOR);
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

    private static MinutiaeList getEndOfLineMinutiae() {
        Minutiae endOfLineMinutiae = AbstractNodeFactory.createEndOfLineMinutiae(LINE_SEPARATOR);
        MinutiaeList leading = AbstractNodeFactory.createMinutiaeList(endOfLineMinutiae);
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

    /**
     * Check the given URL include complex scenarios.
     * ex: /admin/api/2021-10/customers/{customer_id}.json parameterised path parameters
     * TODO: address the other /{id}.json.{name}
     */
    public static boolean isComplexURL(String path) {
        String[] subPathSegment = path.split(SLASH);
        Pattern pattern = Pattern.compile(SPECIAL_CHARACTERS_REGEX);
        for (String subPath: subPathSegment) {
            if (subPath.contains(OPEN_CURLY_BRACE) &&
                    pattern.matcher(subPath.split(CLOSE_CURLY_BRACE, 2)[1]).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add function statements for handle complex URL ex: /admin/api/2021-10/customers/{customer_id}.json.
     *
     * <pre>
     *     if !customerIdDotJson.endsWith(".json") { return error("bad URL"); }
     *     string customerId = customerIdDotJson.substring(0, customerIdDotJson.length() - 4);
     * </pre>
     */
    public static List<StatementNode> generateBodyStatementForComplexUrl(String path) {
        String[] subPathSegment = path.split(SLASH);
        Pattern pattern = Pattern.compile(SPECIAL_CHARACTERS_REGEX);
        List<StatementNode> bodyStatements = new ArrayList<>();
        for (String subPath: subPathSegment) {
            if (subPath.contains(OPEN_CURLY_BRACE) &&
                    pattern.matcher(subPath.split(CLOSE_CURLY_BRACE, 2)[1]).find()) {
                String pathParam = subPath;
                pathParam = pathParam.substring(pathParam.indexOf(OPEN_CURLY_BRACE) + 1);
                pathParam = pathParam.substring(0, pathParam.indexOf(CLOSE_CURLY_BRACE));
                pathParam = getValidName(pathParam, false);

                String[] subPathSplit = subPath.split(CLOSE_CURLY_BRACE, 2);
                String pathParameter = getValidName(subPath, false);
                String restSubPath = subPathSplit[1];
                String resSubPathLength = String.valueOf(restSubPath.length() - 1);

                String ifBlock = "if !" + pathParameter + ".endsWith(\"" + restSubPath + "\") { return error(\"bad " +
                        "URL\"); }";
                StatementNode ifBlockStatement = NodeParser.parseStatement(ifBlock);

                String pathParameterState = "string " + pathParam + " = " + pathParameter + ".substring(0, " +
                        pathParameter + ".length() - " + resSubPathLength + ");";
                StatementNode pathParamStatement = NodeParser.parseStatement(pathParameterState);
                bodyStatements.add(ifBlockStatement);
                bodyStatements.add(pathParamStatement);
            }
        }
        return bodyStatements;
      }
      
    /**
     * This util is to check if the given schema contains any constraints.
     */
    public static boolean hasConstraints(Schema<?> value) {
        if (value instanceof ObjectSchema && value.getProperties() != null) {
            boolean constraintExists = value.getProperties().values().stream()
                    .anyMatch(GeneratorUtils::isConstraintExists);
            if (constraintExists) {
                return true;
            }
        }

        return isConstraintExists(value);
    }

    private static boolean isConstraintExists(Schema<?> propertyValue) {
        return propertyValue.getMaximum() != null ||
                propertyValue.getMinimum() != null ||
                propertyValue.getMaxLength() != null ||
                propertyValue.getMinLength() != null ||
                propertyValue.getMaxItems() != null ||
                propertyValue.getMinItems() != null ||
                propertyValue.getExclusiveMinimum() != null ||
                propertyValue.getExclusiveMaximum() != null;
    }
}
