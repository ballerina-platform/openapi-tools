/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com). All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.openapi.core;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.MarkdownParameterDocumentationLineNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.ResourcePathParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.BallerinaUtilGenerator;
import io.ballerina.openapi.core.generators.document.DocCommentsGenerator;
import io.ballerina.openapi.core.generators.schema.ballerinatypegenerators.EnumGenerator;
import io.ballerina.openapi.core.generators.schema.model.GeneratorMetaData;
import io.ballerina.openapi.core.model.GenSrcFile;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.tools.diagnostics.Location;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExpressionStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createResourcePathParameterNode;
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
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SLASH_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.openapi.core.GeneratorConstants.ARRAY;
import static io.ballerina.openapi.core.GeneratorConstants.BALLERINA;
import static io.ballerina.openapi.core.GeneratorConstants.BALLERINA_TOML;
import static io.ballerina.openapi.core.GeneratorConstants.BALLERINA_TOML_CONTENT;
import static io.ballerina.openapi.core.GeneratorConstants.BOOLEAN;
import static io.ballerina.openapi.core.GeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.CLOSE_CURLY_BRACE;
import static io.ballerina.openapi.core.GeneratorConstants.CONSTRAINT;
import static io.ballerina.openapi.core.GeneratorConstants.DEFAULT_PARAM_COMMENT;
import static io.ballerina.openapi.core.GeneratorConstants.EXPLODE;
import static io.ballerina.openapi.core.GeneratorConstants.GET;
import static io.ballerina.openapi.core.GeneratorConstants.HEAD;
import static io.ballerina.openapi.core.GeneratorConstants.HTTP_REQUEST;
import static io.ballerina.openapi.core.GeneratorConstants.HTTP_RESPONSE;
import static io.ballerina.openapi.core.GeneratorConstants.IDENTIFIER;
import static io.ballerina.openapi.core.GeneratorConstants.IMAGE_PNG;
import static io.ballerina.openapi.core.GeneratorConstants.INTEGER;
import static io.ballerina.openapi.core.GeneratorConstants.JSON_EXTENSION;
import static io.ballerina.openapi.core.GeneratorConstants.LINE_SEPARATOR;
import static io.ballerina.openapi.core.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.core.GeneratorConstants.NULL;
import static io.ballerina.openapi.core.GeneratorConstants.NUMBER;
import static io.ballerina.openapi.core.GeneratorConstants.OBJECT;
import static io.ballerina.openapi.core.GeneratorConstants.OPEN_CURLY_BRACE;
import static io.ballerina.openapi.core.GeneratorConstants.REGEX_ONLY_NUMBERS_OR_NUMBERS_WITH_SPECIAL_CHARACTERS;
import static io.ballerina.openapi.core.GeneratorConstants.REGEX_WITHOUT_SPECIAL_CHARACTERS;
import static io.ballerina.openapi.core.GeneratorConstants.REGEX_WORDS_STARTING_WITH_NUMBERS;
import static io.ballerina.openapi.core.GeneratorConstants.SERVICE_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.SLASH;
import static io.ballerina.openapi.core.GeneratorConstants.SPECIAL_CHARACTERS_REGEX;
import static io.ballerina.openapi.core.GeneratorConstants.SQUARE_BRACKETS;
import static io.ballerina.openapi.core.GeneratorConstants.STRING;
import static io.ballerina.openapi.core.GeneratorConstants.STYLE;
import static io.ballerina.openapi.core.GeneratorConstants.TYPE_FILE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.OPENAPI_TYPE_TO_FORMAT_MAP;
import static io.ballerina.openapi.core.GeneratorConstants.TYPE_NAME;
import static io.ballerina.openapi.core.GeneratorConstants.UNSUPPORTED_OPENAPI_VERSION_PARSER_MESSAGE;
import static io.ballerina.openapi.core.GeneratorConstants.YAML_EXTENSION;
import static io.ballerina.openapi.core.GeneratorConstants.YML_EXTENSION;

/**
 * This class util for store all the common scenarios.
 *
 * @since 1.3.0
 */
public class GeneratorUtils {

    public static final MinutiaeList SINGLE_WS_MINUTIAE = getSingleWSMinutiae();
    public static final List<String> BAL_KEYWORDS = SyntaxInfo.keywords();
    public static final MinutiaeList SINGLE_END_OF_LINE_MINUTIAE = getEndOfLineMinutiae();
    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaUtilGenerator.class);
    private static final PrintStream OUT_STREAM = System.err;

    private static final List<String> primitiveTypeList =
            new ArrayList<>(Arrays.asList(GeneratorConstants.INTEGER, GeneratorConstants.NUMBER,
                    GeneratorConstants.STRING, GeneratorConstants.BOOLEAN));

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
        IdentifierToken identifierToken = AbstractNodeFactory.createIdentifierToken(identifier);
        return NodeFactory.createQualifiedNameReferenceNode(modulePrefixToken, colon, identifierToken);
    }

    /**
     * Generated resource function relative path node list.
     *
     * @param path      - resource path
     * @param operation - resource operation
     * @return - node lists
     * @throws BallerinaOpenApiException
     */
    public static List<Node> getRelativeResourcePath(String path, Operation operation, List<Node> resourceFunctionDocs)
            throws BallerinaOpenApiException {

        List<Node> functionRelativeResourcePath = new ArrayList<>();
        String[] pathNodes = path.split(SLASH);
        if (pathNodes.length >= 2) {
            for (String pathNode : pathNodes) {
                if (pathNode.contains(OPEN_CURLY_BRACE)) {
                    String pathParam = pathNode;
                    pathParam = pathParam.substring(pathParam.indexOf(OPEN_CURLY_BRACE) + 1);
                    pathParam = pathParam.substring(0, pathParam.indexOf(CLOSE_CURLY_BRACE));
                    pathParam = getValidName(pathParam, false);

                    /**
                     * TODO -> `onCall/[string id]\.json` type of url won't support from syntax
                     * issue https://github.com/ballerina-platform/ballerina-spec/issues/1138
                     * <pre>resource function get onCall/[string id]\.json() returns string {}</>
                     */
                    if (operation.getParameters() != null) {
                        extractPathParameterDetails(operation, functionRelativeResourcePath, pathNode,
                                pathParam, resourceFunctionDocs);
                    }
                } else if (!pathNode.isBlank()) {
                    IdentifierToken idToken = createIdentifierToken(escapeIdentifier(pathNode.trim()));
                    functionRelativeResourcePath.add(idToken);
                    functionRelativeResourcePath.add(createToken(SLASH_TOKEN));
                }
            }
            functionRelativeResourcePath.remove(functionRelativeResourcePath.size() - 1);
        } else if (pathNodes.length == 0) {
            IdentifierToken idToken = createIdentifierToken(".");
            functionRelativeResourcePath.add(idToken);
        } else {
            IdentifierToken idToken = createIdentifierToken(pathNodes[1].trim());
            functionRelativeResourcePath.add(idToken);
        }
        return functionRelativeResourcePath;
    }

    private static void extractPathParameterDetails(Operation operation, List<Node> functionRelativeResourcePath,
                                                    String pathNode, String pathParam, List<Node> resourceFunctionDocs)
            throws BallerinaOpenApiException {
        // check whether path parameter segment has special character
        String[] split = pathNode.split(CLOSE_CURLY_BRACE, 2);
        Pattern pattern = Pattern.compile(SPECIAL_CHARACTERS_REGEX);
        Matcher matcher = pattern.matcher(split[1]);
        boolean hasSpecialCharacter = matcher.find();

        for (Parameter parameter : operation.getParameters()) {
            if (parameter.get$ref() != null) {
                parameter = GeneratorMetaData.getInstance().getOpenAPI().getComponents()
                        .getParameters().get(extractReferenceType(parameter.get$ref()));
            }
            if (parameter.getIn() == null) {
                continue;
            }
            if (pathParam.trim().equals(getValidName(parameter.getName().trim(), false))
                    && parameter.getIn().equals("path")) {
                String paramType;
                if (parameter.getSchema().get$ref() != null) {
                    paramType = getValidName(extractReferenceType(parameter.getSchema().get$ref()), true);
                } else {
                    paramType = convertOpenAPITypeToBallerina(parameter.getSchema());
                    if (paramType.endsWith(NILLABLE)) {
                        throw new BallerinaOpenApiException("Path parameter value cannot be null.");
                    }
                }

                // TypeDescriptor
                BuiltinSimpleNameReferenceNode builtSNRNode = createBuiltinSimpleNameReferenceNode(
                        null,
                        parameter.getSchema() == null || hasSpecialCharacter ?
                                createIdentifierToken(STRING) :
                                createIdentifierToken(paramType));
                IdentifierToken paramName = createIdentifierToken(
                        hasSpecialCharacter ?
                                getValidName(pathNode, false) :
                                pathParam);
                ResourcePathParameterNode resourcePathParameterNode =
                        createResourcePathParameterNode(
                                SyntaxKind.RESOURCE_PATH_SEGMENT_PARAM,
                                createToken(OPEN_BRACKET_TOKEN),
                                NodeFactory.createEmptyNodeList(),
                                builtSNRNode,
                                null,
                                paramName,
                                createToken(CLOSE_BRACKET_TOKEN));
                functionRelativeResourcePath.add(resourcePathParameterNode);
                functionRelativeResourcePath.add(createToken(SLASH_TOKEN));

                // Add documentation
                if (resourceFunctionDocs != null) {
                    String parameterName = paramName.text();
                    String paramComment = parameter.getDescription() != null && !parameter.getDescription().isBlank() ?
                            parameter.getDescription() : DEFAULT_PARAM_COMMENT;
                    MarkdownParameterDocumentationLineNode paramAPIDoc =
                            DocCommentsGenerator.createAPIParamDoc(parameterName, paramComment);
                    resourceFunctionDocs.add(paramAPIDoc);
                }
                break;
            }
        }
    }

    /**
     * Method for convert openApi type of format to ballerina type.
     *
     * @param schema OpenApi schema
     * @return ballerina type
     */
    public static String convertOpenAPITypeToBallerina(Schema<?> schema) throws BallerinaOpenApiException {
        String type = getOpenAPIType(schema);
        if (schema.getEnum() != null && !schema.getEnum().isEmpty() && primitiveTypeList.contains(type)) {
            EnumGenerator enumGenerator = new EnumGenerator(schema, null);
            return enumGenerator.generateTypeDescriptorNode().toString();
        } else if ((INTEGER.equals(type) || NUMBER.equals(type) || STRING.equals(type)) && schema.getFormat() != null) {
            return convertOpenAPITypeFormatToBallerina(type, schema);
        } else {
            if (GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.containsKey(type)) {
                return GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.get(type);
            } else {
                throw new BallerinaOpenApiException("Unsupported OAS data type `" + type + "`");
            }
        }
    }

    /**
     * This utility is used to select the Ballerina data type for a given OpenAPI type format.
     *
     * @param dataType name of the data type. ex: number, integer, string
     * @param schema uses to generate the type descriptor name ex: int32, int64
     * @return data type for invalid numeric data formats
     */
    private static String convertOpenAPITypeFormatToBallerina(final String dataType, final Schema<?> schema)
            throws BallerinaOpenApiException {
        if (OPENAPI_TYPE_TO_FORMAT_MAP.containsKey(dataType) &&
                OPENAPI_TYPE_TO_FORMAT_MAP.get(dataType).contains(schema.getFormat())) {
            if (GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.containsKey(schema.getFormat())) {
                return GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.get(schema.getFormat());
            } else {
                OUT_STREAM.printf("WARNING: unsupported format `%s` will be skipped when generating the counterpart " +
                        "Ballerina type for openAPI schema type: `%s`%n", schema.getFormat(), schema.getType());
                if (GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.containsKey(dataType)) {
                    return GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.get(dataType);
                } else {
                    throw new BallerinaOpenApiException("Unsupported OAS data type `" + dataType + "`");
                }
            }
        } else {
            if (GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.containsKey(dataType)) {
                return GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.get(dataType);
            } else {
                throw new BallerinaOpenApiException("Unsupported OAS data type `" + dataType + "`");
            }
        }
    }

    /**
     * This method will escape special characters used in method names and identifiers.
     *
     * @param identifier - identifier or method name
     * @return - escaped string
     */
    public static String escapeIdentifier(String identifier) {

        if (identifier.matches(REGEX_ONLY_NUMBERS_OR_NUMBERS_WITH_SPECIAL_CHARACTERS)
                || identifier.matches(REGEX_WORDS_STARTING_WITH_NUMBERS)) {
            // this is to handle scenarios 220 => '220, 2023-06-28 => '2023\-06\-28, 3h => '3h
            return "'" + identifier.replaceAll(GeneratorConstants.ESCAPE_PATTERN, "\\\\$1");
        } else if (!identifier.matches(REGEX_WITHOUT_SPECIAL_CHARACTERS)) {
            return identifier.replaceAll(GeneratorConstants.ESCAPE_PATTERN, "\\\\$1");
        } else if (BAL_KEYWORDS.contains(identifier)) {
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
            for (String part : split) {
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
     * This util function is for updating the list of nodes {@link TypeDefinitionNode}.
     * It updates the list while checking the duplicates.
     *
     * @param typeName               - Given node name
     * @param typeDefNode            - Generated node
     * @param typeDefinitionNodeList - Current node list
     */
    public static void updateTypeDefNodeList(String typeName, TypeDefinitionNode typeDefNode,
                                             List<TypeDefinitionNode> typeDefinitionNodeList) {
        boolean anyMatch = typeDefinitionNodeList.stream().anyMatch(node ->
                (node.typeName().text().trim().equals(typeName)));
        if (!anyMatch) {
            typeDefinitionNodeList.add(typeDefNode);
        }
    }

    /**
     * Check the given recordName is valid name.
     *
     * @param recordName - String record name
     * @return - boolean value
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
     *                                   Note : Current implementation will not support external links a references.
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
        if (!(definitionPath.toString().endsWith(YAML_EXTENSION) ||
                definitionPath.toString().endsWith(JSON_EXTENSION) ||
                definitionPath.toString().endsWith(YML_EXTENSION))) {
            throw new BallerinaOpenApiException(ErrorMessages.invalidFileType());
        }
        String openAPIFileContent = Files.readString(definitionPath);
        ParseOptions parseOptions = new ParseOptions();
        parseOptions.setResolve(true);
        parseOptions.setFlatten(true);
        SwaggerParseResult parseResult = new OpenAPIParser().readContents(openAPIFileContent, null, parseOptions);
        if (!parseResult.getMessages().isEmpty()) {
            if (parseResult.getMessages().contains(UNSUPPORTED_OPENAPI_VERSION_PARSER_MESSAGE)) {
                throw new BallerinaOpenApiException(ErrorMessages.unsupportedOpenAPIVersion());
            }

            StringBuilder errorMessage = new StringBuilder("OpenAPI definition has errors: \n");
            for (String message : parseResult.getMessages()) {
                errorMessage.append(message).append(LINE_SEPARATOR);
            }

            throw new BallerinaOpenApiException(errorMessage.toString());
        }
        return parseResult.getOpenAPI();
    }

    /**
     * Check whether the given media type is currently supported in the tool.
     *
     * @param mediaTypeEntry
     * @return
     */
    public static boolean isSupportedMediaType(Map.Entry<String,
            io.swagger.v3.oas.models.media.MediaType> mediaTypeEntry) {
        String mediaType = mediaTypeEntry.getKey();
        String defaultBallerinaType = getBallerinaMediaType(mediaType, true);
        Schema<?> schema = mediaTypeEntry.getValue().getSchema();

        boolean isValidMultipartFormData = mediaType.equals(MediaType.MULTIPART_FORM_DATA) && schema != null &&
                (schema.get$ref() != null || schema.getProperties() != null || getOpenAPIType(schema).equals(OBJECT));

        if (defaultBallerinaType.equals(HTTP_REQUEST) && !isValidMultipartFormData) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Generate BallerinaMediaType for all the return mediaTypes.
     */
    public static String getBallerinaMediaType(String mediaType, boolean isRequest) {
        if (mediaType.matches(".*/json") || mediaType.matches("application/.*\\+json")) {
            return SyntaxKind.JSON_KEYWORD.stringValue();
        } else if (mediaType.matches(".*/xml") || mediaType.matches("application/.*\\+xml")) {
            return SyntaxKind.XML_KEYWORD.stringValue();
        } else if (mediaType.equals(MediaType.APPLICATION_FORM_URLENCODED) || mediaType.matches("text/.*")) {
            return STRING_KEYWORD.stringValue();
        } else if (mediaType.equals(MediaType.APPLICATION_OCTET_STREAM) ||
                mediaType.equals(IMAGE_PNG) || mediaType.matches("application/.*\\+octet-stream")) {
            return SyntaxKind.BYTE_KEYWORD.stringValue() + SQUARE_BRACKETS;
        } else {
            return isRequest ? HTTP_REQUEST : HTTP_RESPONSE;
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
     * @param absUrl    abstract url with template values
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
     * @param filedOfMap Includes all the items in the encoding map
     * @param style      Defines how multiple values are delimited and explode
     * @param explode    Specifies whether arrays and objects should generate separate parameters
     * @param key        Key of the item in the map
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

        for (ImportDeclarationNode importModule : imports) {
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
     * TODO: address the other /{id}.json.{name}, /report.{format}
     */
    public static boolean isComplexURL(String path) {

        String[] subPathSegment = path.split(SLASH);
        Pattern pattern = Pattern.compile(SPECIAL_CHARACTERS_REGEX);
        for (String subPath : subPathSegment) {
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
        for (String subPath : subPathSegment) {
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

        if (value.getProperties() != null) {
            boolean constraintExists = value.getProperties().values().stream()
                    .anyMatch(GeneratorUtils::hasConstraints);
            if (constraintExists) {
                return true;
            }
        } else if (isComposedSchema(value)) {
            List<Schema> allOf = value.getAllOf();
            List<Schema> oneOf = value.getOneOf();
            List<Schema> anyOf = value.getAnyOf();
            boolean constraintExists = false;
            if (allOf != null) {
                constraintExists = allOf.stream().anyMatch(GeneratorUtils::hasConstraints);
            } else if (oneOf != null) {
                constraintExists = oneOf.stream().anyMatch(GeneratorUtils::hasConstraints);
            } else if (anyOf != null) {
                constraintExists = anyOf.stream().anyMatch(GeneratorUtils::hasConstraints);
            }
            if (constraintExists) {
                return true;
            }

        } else if (isArraySchema(value)) {
            if (!isConstraintExists(value)) {
                return isConstraintExists(value.getItems());
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
                propertyValue.getExclusiveMaximum() != null ||
                propertyValue.getPattern() != null;
    }

    /**
     * Normalized OpenAPI specification with adding proper naming to schema.
     *
     * @param openAPIPath - openAPI file path
     * @return - openAPI specification
     * @throws IOException
     * @throws BallerinaOpenApiException
     */
    public static OpenAPI normalizeOpenAPI(Path openAPIPath, boolean isClient) throws IOException,
            BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPIFromOpenAPIV3Parser(openAPIPath);
        io.swagger.v3.oas.models.Paths openAPIPaths = openAPI.getPaths();
        if (isClient) {
            validateOperationIds(openAPIPaths.entrySet());
        }
        validateRequestBody(openAPIPaths.entrySet());

        if (openAPI.getComponents() != null) {
            // Refactor schema name with valid name
            Components components = openAPI.getComponents();
            Map<String, Schema> componentsSchemas = components.getSchemas();
            if (componentsSchemas != null) {
                Map<String, Schema> refacSchema = new HashMap<>();
                for (Map.Entry<String, Schema> schemaEntry : componentsSchemas.entrySet()) {
                    String name = getValidName(schemaEntry.getKey(), true);
                    refacSchema.put(name, schemaEntry.getValue());
                }
                openAPI.getComponents().setSchemas(refacSchema);
            }
        }
        return openAPI;
    }

    /**
     * Check whether an operationId has been defined in each path. If given rename the operationId to accepted format.
     * -- ex: GetPetName -> getPetName
     *
     * @param paths List of paths given in the OpenAPI definition
     * @throws BallerinaOpenApiException When operationId is missing in any path
     */
    public static void validateOperationIds(Set<Map.Entry<String, PathItem>> paths) throws BallerinaOpenApiException {
        List<String> errorList = new ArrayList<>();
        for (Map.Entry<String, PathItem> entry : paths) {
            for (Map.Entry<PathItem.HttpMethod, Operation> operation :
                    entry.getValue().readOperationsMap().entrySet()) {
                if (operation.getValue().getOperationId() != null) {
                    String operationId = getValidName(operation.getValue().getOperationId(), false);
                    operation.getValue().setOperationId(operationId);
                } else {
                    errorList.add(String.format("OperationId is missing in the resource path: %s(%s)", entry.getKey(),
                            operation.getKey()));
                }
            }
        }
        if (!errorList.isEmpty()) {
            throw new BallerinaOpenApiException(
                    "OpenAPI definition has errors: " + LINE_SEPARATOR + String.join(LINE_SEPARATOR, errorList));
        }
    }

    /**
     * Validate if requestBody found in GET/DELETE/HEAD operation.
     *
     * @param paths - List of paths given in the OpenAPI definition
     * @throws BallerinaOpenApiException - If requestBody found in GET/DELETE/HEAD operation
     */
    public static void validateRequestBody(Set<Map.Entry<String, PathItem>> paths) throws BallerinaOpenApiException {
        List<String> errorList = new ArrayList<>();
        for (Map.Entry<String, PathItem> entry : paths) {
            if (!entry.getValue().readOperationsMap().isEmpty()) {
                for (Map.Entry<PathItem.HttpMethod, Operation> operation : entry.getValue().readOperationsMap()
                        .entrySet()) {
                    String method = operation.getKey().name().trim().toLowerCase(Locale.ENGLISH);
                    boolean isRequestBodyInvalid = method.equals(GET) || method.equals(HEAD);
                    if (isRequestBodyInvalid && operation.getValue().getRequestBody() != null) {
                        errorList.add(method.toUpperCase(Locale.ENGLISH) + " operation cannot have a requestBody. "
                                + "Error at operationId: " + operation.getValue().getOperationId());
                    }
                }
            }
        }

        if (!errorList.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("OpenAPI definition has errors: " + LINE_SEPARATOR);
            for (String message : errorList) {
                errorMessage.append(message).append(LINE_SEPARATOR);
            }
            throw new BallerinaOpenApiException(errorMessage.toString());
        }
    }

    public static String removeUnusedEntities(SyntaxTree schemaSyntaxTree, String clientContent, String schemaContent,
                                              String serviceContent) throws IOException, FormatterException {
        Map<String, String> tempSourceFiles = new HashMap<>();
        tempSourceFiles.put(CLIENT_FILE_NAME, clientContent);
        tempSourceFiles.put(TYPE_FILE_NAME, schemaContent);
        if (serviceContent != null) {
            tempSourceFiles.put(SERVICE_FILE_NAME, schemaContent);
        }
        List<String> unusedTypeDefinitionNameList = getUnusedTypeDefinitionNameList(tempSourceFiles);
        while (unusedTypeDefinitionNameList.size() > 0) {
            ModulePartNode modulePartNode = schemaSyntaxTree.rootNode();
            NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
            List<ModuleMemberDeclarationNode> unusedTypeDefinitionNodeList = new ArrayList<>();
            for (ModuleMemberDeclarationNode node : members) {
                if (node.kind().equals(SyntaxKind.TYPE_DEFINITION)) {
                    for (ChildNodeEntry childNodeEntry : node.childEntries()) {
                        if (childNodeEntry.name().equals(TYPE_NAME)) {
                            if (unusedTypeDefinitionNameList.contains(childNodeEntry.node().get().toString())) {
                                unusedTypeDefinitionNodeList.add(node);
                            }
                        }
                    }
                } else if (node.kind().equals(SyntaxKind.ENUM_DECLARATION)) {
                    for (ChildNodeEntry childNodeEntry : node.childEntries()) {
                        if (childNodeEntry.name().equals(IDENTIFIER)) {
                            if (unusedTypeDefinitionNameList.contains(childNodeEntry.node().get().toString())) {
                                unusedTypeDefinitionNodeList.add(node);
                            }
                        }
                    }
                }
            }
            NodeList<ModuleMemberDeclarationNode> modifiedMembers = members.removeAll
                    (unusedTypeDefinitionNodeList);
            ModulePartNode modiedModulePartNode = modulePartNode.modify(modulePartNode.imports(),
                    modifiedMembers, modulePartNode.eofToken());
            schemaSyntaxTree = schemaSyntaxTree.modifyWith(modiedModulePartNode);
            schemaContent = Formatter.format(schemaSyntaxTree).toSourceCode();
            tempSourceFiles.put(TYPE_FILE_NAME, schemaContent);
            unusedTypeDefinitionNameList = getUnusedTypeDefinitionNameList(tempSourceFiles);
        }
        ModulePartNode rootNode = schemaSyntaxTree.rootNode();
        NodeList<ImportDeclarationNode> imports = rootNode.imports();
        imports = removeUnusedImports(rootNode, imports);

        ModulePartNode modiedModulePartNode = rootNode.modify(imports, rootNode.members(), rootNode.eofToken());
        schemaSyntaxTree = schemaSyntaxTree.modifyWith(modiedModulePartNode);
        schemaContent = Formatter.format(schemaSyntaxTree).toSourceCode();
        return schemaContent;
    }

    private static NodeList<ImportDeclarationNode> removeUnusedImports(ModulePartNode rootNode,
                                                                       NodeList<ImportDeclarationNode> imports) {
        //TODO: This function can be extended to check all the unused imports, for this time only handle constraint
        // imports
        boolean hasConstraint = false;
        NodeList<ModuleMemberDeclarationNode> members = rootNode.members();
        for (ModuleMemberDeclarationNode member:members) {
            if (member.kind().equals(SyntaxKind.TYPE_DEFINITION)) {
                TypeDefinitionNode typeDefNode = (TypeDefinitionNode) member;
                if (typeDefNode.typeDescriptor().kind().equals(SyntaxKind.RECORD_TYPE_DESC)) {
                    RecordTypeDescriptorNode record = (RecordTypeDescriptorNode) typeDefNode.typeDescriptor();
                    NodeList<Node> fields = record.fields();
                    //Traverse record fields to check for constraints
                    for (Node node: fields) {
                        if (node instanceof RecordFieldNode) {
                            RecordFieldNode recField = (RecordFieldNode) node;
                            if (recField.metadata().isPresent()) {
                                hasConstraint = traverseAnnotationNode(recField.metadata(), hasConstraint);
                            }
                        }
                        if (hasConstraint) {
                            break;
                        }
                    }
                }

                if (typeDefNode.metadata().isPresent()) {
                    hasConstraint = traverseAnnotationNode(typeDefNode.metadata(), hasConstraint);
                }
            }
            if (hasConstraint) {
                break;
            }
        }
        if (!hasConstraint) {
            for (ImportDeclarationNode importNode: imports) {
                if (importNode.orgName().isPresent()) {
                    if (importNode.orgName().get().toString().equals("ballerina/") &&
                            importNode.moduleName().get(0).text().equals(CONSTRAINT)) {
                        imports = imports.remove(importNode);
                    }
                }
            }
        }
        return imports;
    }

    private static boolean traverseAnnotationNode(Optional<MetadataNode> recField, boolean hasConstraint) {
        MetadataNode metadata = recField.get();
        for (AnnotationNode annotation : metadata.annotations()) {
            String annotationRef = annotation.annotReference().toString();
            if (annotationRef.startsWith(CONSTRAINT)) {
                hasConstraint = true;
                break;
            }
        }
        return hasConstraint;
    }

    private static List<String> getUnusedTypeDefinitionNameList(Map<String, String> srcFiles) throws IOException {
        List<String> unusedTypeDefinitionNameList = new ArrayList<>();
        Path tmpDir = Files.createTempDirectory(".openapi-tmp" + System.nanoTime());
        writeFilesTemp(srcFiles, tmpDir);
        if (Files.exists(tmpDir.resolve(CLIENT_FILE_NAME)) && Files.exists(tmpDir.resolve(TYPE_FILE_NAME)) &&
                Files.exists(tmpDir.resolve(BALLERINA_TOML))) {
            SemanticModel semanticModel = getSemanticModel(tmpDir.resolve(CLIENT_FILE_NAME));
            List<Symbol> symbols = semanticModel.moduleSymbols();
            for (Symbol symbol : symbols) {
                if (symbol.kind().equals(SymbolKind.TYPE_DEFINITION) || symbol.kind().equals(SymbolKind.ENUM)) {
                    List<Location> references = semanticModel.references(symbol);
                    if (references.size() == 1) {
                        unusedTypeDefinitionNameList.add(symbol.getName().get());
                    }
                }
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                FileUtils.deleteDirectory(tmpDir.toFile());
            } catch (IOException ex) {
                LOGGER.error("Unable to delete the temporary directory : " + tmpDir, ex);
            }
        }));
        return unusedTypeDefinitionNameList;
    }

    private static void writeFilesTemp(Map<String, String> srcFiles, Path tmpDir) throws IOException {
        srcFiles.put(BALLERINA_TOML, BALLERINA_TOML_CONTENT);
        PrintWriter writer = null;
        for (Map.Entry<String, String> entry : srcFiles.entrySet()) {
            String key = entry.getKey();
            Path filePath = tmpDir.resolve(key);
            try {
                writer = new PrintWriter(filePath.toString(), "UTF-8");
                writer.print(entry.getValue());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }

    private static SemanticModel getSemanticModel(Path clientPath) throws ProjectException {
        // Load project instance for single ballerina file
        try {
            Project project = ProjectLoader.loadProject(clientPath);
            Package packageName = project.currentPackage();
            DocumentId docId;

            if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
                docId = project.documentId(clientPath);
            } else {
                // Take module instance for traversing the syntax tree
                Module currentModule = packageName.getDefaultModule();
                Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();
                docId = documentIterator.next();
            }
            return project.currentPackage().getCompilation().getSemanticModel(docId.moduleId());
        } catch (ProjectException e) {
            throw new ProjectException(e.getMessage());
        }
    }

    public static String getOpenAPIType(Schema<?> schema) {
        if (schema.getTypes() != null && !schema.getTypes().isEmpty()) {
            for (String type : schema.getTypes()) {
                // TODO: https://github.com/ballerina-platform/openapi-tools/issues/1497
                // this returns the first non-null type in the list
                if (!type.equals(NULL)) {
                    return type;
                }
            }
        } else if (schema.getType() != null) {
            return schema.getType();
        }
        return null;
    }

    public static boolean isArraySchema(Schema schema) {
        return getOpenAPIType(schema) != null && Objects.equals(getOpenAPIType(schema), ARRAY);
    }

    public static boolean isMapSchema(Schema schema) {
        return schema.getAdditionalProperties() != null;
    }

    public static boolean isObjectSchema(Schema schema) {
        return getOpenAPIType(schema) != null && Objects.equals(getOpenAPIType(schema), OBJECT);
    }

    public static boolean isComposedSchema(Schema schema) {
        return schema.getAnyOf() != null || schema.getOneOf() != null ||
                schema.getAllOf() != null;
    }

    public static boolean isStringSchema(Schema<?> schema) {
        return getOpenAPIType(schema) != null && Objects.equals(getOpenAPIType(schema), STRING);
    }

    public static boolean isBooleanSchema(Schema<?> schema) {
        return getOpenAPIType(schema) != null && Objects.equals(getOpenAPIType(schema), BOOLEAN);
    }

    public static boolean isIntegerSchema(Schema<?> fieldSchema) {
        return Objects.equals(GeneratorUtils.getOpenAPIType(fieldSchema), INTEGER);
    }

    public static boolean isNumberSchema(Schema<?> fieldSchema) {
        return Objects.equals(GeneratorUtils.getOpenAPIType(fieldSchema), NUMBER);
    }
}
