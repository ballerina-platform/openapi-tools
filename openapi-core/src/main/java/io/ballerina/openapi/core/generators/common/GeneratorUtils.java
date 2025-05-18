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

package io.ballerina.openapi.core.generators.common;

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.ArrayDimensionNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
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
import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.core.generators.common.diagnostic.CommonDiagnostic;
import io.ballerina.openapi.core.generators.common.diagnostic.CommonDiagnosticMessages;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.common.exception.InvalidReferenceException;
import io.ballerina.openapi.core.generators.common.exception.NullPathParameterException;
import io.ballerina.openapi.core.generators.common.exception.UnsupportedOASDataTypeException;
import io.ballerina.openapi.core.generators.common.model.GenSrcFile;
import io.ballerina.openapi.core.generators.type.exception.OASTypeGenException;
import io.ballerina.openapi.core.generators.type.generators.EnumGenerator;
import io.ballerina.openapi.core.generators.type.model.GeneratorMetaData;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createArrayTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createExpressionStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createResourcePathParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.AT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACKET_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SLASH_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.APPLICATION_FORM_URLENCODED;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.APPLICATION_OCTET_STREAM;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.ARRAY;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.BALLERINA;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.BOOLEAN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.CATCH_ALL_PATH;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.CLOSE_CURLY_BRACE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.EXPLODE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NAME_ANNOTATION;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.GET;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HEAD;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HEADER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HEADER_ANNOTATION;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HTTP_REQUEST;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HTTP_RESPONSE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.IMAGE_PNG;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.INTEGER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.JSON_EXTENSION;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.LINE_SEPARATOR;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.MULTIPART_FORM_DATA;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NULL;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NUMBER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.OBJECT;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.OPENAPI_TYPE_TO_FORMAT_MAP;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.OPEN_CURLY_BRACE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.QUERY;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.QUERY_ANNOTATION;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.REGEX_ONLY_NUMBERS_OR_NUMBERS_WITH_SPECIAL_CHARACTERS;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.REGEX_WITHOUT_SPECIAL_CHARACTERS;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.REGEX_WORDS_STARTING_WITH_NUMBERS;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.RESPONSE_RECORD_NAME;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.SLASH;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.SPECIAL_CHARACTERS_REGEX;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.SQUARE_BRACKETS;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.STRING;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.STYLE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.TEXT_EVENT_STREAM;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.UNSUPPORTED_OPENAPI_VERSION_PARSER_MESSAGE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.X_BALLERINA_NAME;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.X_PARAM_TYPE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.YAML_EXTENSION;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.YML_EXTENSION;
import static io.ballerina.openapi.core.generators.common.diagnostic.CommonDiagnosticMessages.OAS_COMMON_101;

/**
 * This class util for store all the common scenarios.
 *
 * @since 1.3.0
 */
public class GeneratorUtils {

    public static final MinutiaeList SINGLE_WS_MINUTIAE = getSingleWSMinutiae();
    public static final List<String> BAL_KEYWORDS = SyntaxInfo.keywords();
    public static final MinutiaeList SINGLE_END_OF_LINE_MINUTIAE = getEndOfLineMinutiae();
    private static final PrintStream OUT_STREAM = System.err;
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final char CHAR = '"';
    public static final String JSONDATA_IMPORT = "import ballerina/data.jsondata;";
    public static final String HTTP_IMPORT = "import ballerina/http;";
    private static HashMap<String, Integer> recordCountMap;

    private static final List<String> primitiveTypeList =
            new ArrayList<>(Arrays.asList(GeneratorConstants.INTEGER, GeneratorConstants.NUMBER,
                    GeneratorConstants.STRING, GeneratorConstants.BOOLEAN));

    // This is needs to be initialized at every CLI run. Otherwise, the record type details are persisted
    // for a second invocation as well. Todo: Need to update this with a different appraoch.
    public static void initializeRecordCountMap() {
        recordCountMap = new HashMap<>();
    }

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
    public static NodeList<Node> getRelativeResourcePath(String path, Operation operation, Components components,
                                                         boolean isWithoutDataBinding, List<Diagnostic> diagnostics)
            throws BallerinaOpenApiException {

        List<Node> functionRelativeResourcePath = new ArrayList<>();
        String[] pathNodes = path.split(SLASH);
        if (pathNodes.length >= 2) {
            for (String pathNode : pathNodes) {
                if (pathNode.contains(OPEN_CURLY_BRACE)) {
                    String pathParam = pathNode;
                    pathParam = pathParam.substring(pathParam.indexOf(OPEN_CURLY_BRACE) + 1);
                    pathParam = pathParam.substring(0, pathParam.indexOf(CLOSE_CURLY_BRACE));
                    pathParam = escapeIdentifier(pathParam);

                    /**
                     * TODO -> `onCall/[string id]\.json` type of url won't support from syntax
                     * issue https://github.com/ballerina-platform/ballerina-spec/issues/1138
                     * <pre>resource function get onCall/[string id]\.json() returns string {}</>
                     */
                    if (operation.getParameters() != null) {
                        extractPathParameterDetails(operation, functionRelativeResourcePath, pathNode,
                                pathParam, components, isWithoutDataBinding, diagnostics);
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
        return createNodeList(functionRelativeResourcePath);
    }

    private static void extractPathParameterDetails(Operation operation, List<Node> functionRelativeResourcePath,
                                                    String pathNode, String pathParam, Components components,
                                                    boolean isWithoutDataBinding, List<Diagnostic> diagnostics)
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
            if (pathParam.trim().equals(escapeIdentifier(parameter.getName().trim()))
                    && parameter.getIn().equals("path")) {
                String paramType;
                if (parameter.getSchema().get$ref() != null) {
                    paramType = resolveReferenceType(parameter.getSchema(), components, isWithoutDataBinding,
                            pathParam, diagnostics);
                } else {
                    paramType = getPathParameterType(parameter.getSchema(), pathParam, diagnostics);
                    if (paramType.endsWith(NILLABLE)) {
                        throw new NullPathParameterException();
                    }
                }
                BuiltinSimpleNameReferenceNode builtSNRNode = createBuiltinSimpleNameReferenceNode(
                        null,
                        parameter.getSchema() == null || hasSpecialCharacter ?
                                createIdentifierToken(STRING) :
                                createIdentifierToken(paramType));
                IdentifierToken paramName = createIdentifierToken(
                        hasSpecialCharacter ?
                                escapeIdentifier(pathNode) :
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
    public static String convertOpenAPITypeToBallerina(Schema<?> schema, boolean ignoreNullableFlag)
            throws UnsupportedOASDataTypeException {
        String type = getOpenAPIType(schema);
        if (schema.getEnum() != null && !schema.getEnum().isEmpty() && primitiveTypeList.contains(type)) {
            EnumGenerator enumGenerator = new EnumGenerator(schema, null, ignoreNullableFlag,
                    new HashMap<>(), new HashMap<>());
            try {
                return enumGenerator.generateTypeDescriptorNode().toString();
            } catch (OASTypeGenException exp) {
                return "";
            }
        } else if ((INTEGER.equals(type) || NUMBER.equals(type) || STRING.equals(type)) && schema.getFormat() != null) {
            return convertOpenAPITypeFormatToBallerina(type, schema);
        } else {
            if (GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.containsKey(type)) {
                return GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.get(type);
            } else {
                throw new UnsupportedOASDataTypeException(type);
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
            throws UnsupportedOASDataTypeException {
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
                    throw new UnsupportedOASDataTypeException(dataType);
                }
            }
        } else {
            if (GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.containsKey(dataType)) {
                return GeneratorConstants.OPENAPI_TYPE_TO_BAL_TYPE_MAP.get(dataType);
            } else {
                throw new UnsupportedOASDataTypeException(dataType);
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
        if (identifier.isBlank()) {
            return "\\" + identifier;
        }
        //For the flatten enable we need to remove first Part of valid name check
        // this - > !identifier.matches("\\b[a-zA-Z][a-zA-Z0-9]*\\b") &&
        if (!identifier.matches("\\b[0-9]*\\b")) {
            String[] split = identifier.split(GeneratorConstants.ESCAPE_PATTERN_FOR_MODIFIER);
            StringBuilder validName = new StringBuilder();
            for (String part : split) {
                if (!part.isBlank()) {
                    if (split.length > 1) {
                        part = part.substring(0, 1).toUpperCase(Locale.ENGLISH) + part.substring(1);
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
    public static String extractReferenceType(String referenceVariable) throws InvalidReferenceException {
        if (referenceVariable.startsWith("#") && referenceVariable.contains("/")) {
            String[] refArray = referenceVariable.split("/");
            return refArray[refArray.length - 1];
        } else {
            throw new InvalidReferenceException(referenceVariable);
        }
    }

    public static String extractReferenceSection(String referenceVariable) throws InvalidReferenceException {
        if (referenceVariable.startsWith("#") && referenceVariable.contains("/")) {
            String[] refArray = referenceVariable.split("/");
            return refArray[refArray.length - 2];
        } else {
            throw new InvalidReferenceException(referenceVariable);
        }
    }

    public static Optional<String> getBallerinaNameExtension(Parameter parameter) {
        if (Objects.isNull(parameter) || Objects.isNull(parameter.getExtensions())) {
            return Optional.empty();
        }
        return getBallerinaNameExtension(parameter.getExtensions());
    }

    public static Optional<String> getBallerinaNameExtension(Schema schema) {
        return Optional.ofNullable(schema)
                .map(Schema::getExtensions)
                .flatMap(GeneratorUtils::getBallerinaNameExtension);
    }

    public static Optional<String> getBallerinaNameExtension(Map<String, Object> extensions) {
        return Optional.ofNullable(extensions)
                .map(ext -> ext.get(X_BALLERINA_NAME))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .map(String::trim);
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
    public static boolean isSupportedMediaType(Map.Entry<String, MediaType> mediaTypeEntry) {
        String mediaType = mediaTypeEntry.getKey();
        String[] contentTypes = mediaType.split(";");
        if (mediaType.length() > 1) {
            mediaType = contentTypes[0];
        }
        String defaultBallerinaType = getBallerinaMediaType(mediaType, true);
        Schema<?> schema = mediaTypeEntry.getValue().getSchema();

        boolean isValidMultipartFormData = mediaType.equals(MULTIPART_FORM_DATA) && schema != null &&
                (schema.get$ref() != null || schema.getProperties() != null || getOpenAPIType(schema).equals(OBJECT));

        if (defaultBallerinaType.equals(HTTP_REQUEST) && !isValidMultipartFormData) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean hasRequestBinding(String mediaType) {
        return getBallerinaMediaType(mediaType, true).equals(HTTP_REQUEST);
    }

    /**
     * Generate BallerinaMediaType for all the return mediaTypes.
     */
    public static String getBallerinaMediaType(String mediaType, boolean isRequest) {
        String[] contentTypes = mediaType.split(";");
        if (mediaType.length() > 1) {
            mediaType = contentTypes[0];
        }
        if (mediaType.matches(".*/json") || mediaType.matches("application/.*\\+json")) {
            return SyntaxKind.JSON_KEYWORD.stringValue();
        } else if (mediaType.matches(".*/xml") || mediaType.matches("application/.*\\+xml")) {
            return SyntaxKind.XML_KEYWORD.stringValue();
        } else if (mediaType.equals(TEXT_EVENT_STREAM) && !isRequest) {
            return "stream<http:SseEvent, error?>";
        } else if (mediaType.equals(APPLICATION_FORM_URLENCODED) || mediaType.matches("text/.*")) {
            return STRING_KEYWORD.stringValue();
        } else if (mediaType.equals(APPLICATION_OCTET_STREAM) ||
                mediaType.equals(IMAGE_PNG) || mediaType.matches("application/.*\\+octet-stream")) {
            return SyntaxKind.BYTE_KEYWORD.stringValue() + SQUARE_BRACKETS;
        } else if (mediaType.equals("application/x-www-form-urlencoded")) {
            return "map<string>";
        } else if (mediaType.equals(MULTIPART_FORM_DATA) && isRequest) {
            return "record{}";
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

        IdentifierToken fieldName = createIdentifierToken(CHAR + key + CHAR);
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
        if (imports == null) {
            imports = new ArrayList<>();
        }
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
     * @param validateOpIds - validate  operation ids
     * @param isSanitized - sanitize schema names
     * @return - openAPI specification
     * @throws IOException - IOException
     * @throws BallerinaOpenApiException - Ballerina OpenAPI Exception
     */
    public static OpenAPI normalizeOpenAPI(Path openAPIPath, boolean validateOpIds, boolean isSanitized) throws
            IOException, BallerinaOpenApiException {
        return normalizeOpenAPI(openAPIPath, validateOpIds, isSanitized, false);
    }

    /**
     * Normalized OpenAPI specification with adding proper naming to schema.
     *
     * @param openAPIPath - openAPI file path
     * @param validateOpIds - validate  operation ids
     * @param isSanitized - sanitize schema names
     * @param isFlatten - flatten  OpenAPI specification
     * @return - openAPI specification
     * @throws IOException - IOException
     * @throws BallerinaOpenApiException - Ballerina OpenAPI Exception
     */
    public static OpenAPI normalizeOpenAPI(Path openAPIPath, boolean validateOpIds, boolean isSanitized,
                                           boolean isFlatten) throws IOException, BallerinaOpenApiException {
        OpenAPI openAPI = getOpenAPIFromOpenAPIV3Parser(openAPIPath);
        return normalizeOpenAPI(openAPI, validateOpIds, isSanitized, isFlatten);
    }

    /**
     * Normalized OpenAPI specification.
     *
     * @param openAPI - openAPI specification
     * @param validateOpIds - validate operation ids
     * @param isSanitized - sanitise OpenAPI specification
     * @return - openAPI specification
     * @throws BallerinaOpenApiException - Ballerina OpenAPI Exception
     */
    public static OpenAPI normalizeOpenAPI(OpenAPI openAPI, boolean validateOpIds, boolean isSanitized)
            throws BallerinaOpenApiException {
        return normalizeOpenAPI(openAPI, validateOpIds, isSanitized, false);
    }

    /**
     * Normalized OpenAPI specification.
     *
     * @param openAPI - openAPI specification
     * @param validateOpIds - validate operation ids
     * @param isSanitized - sanitise OpenAPI specification
     * @param flatten - flatten OpenAPI specification
     * @return - openAPI specification
     * @throws BallerinaOpenApiException - Ballerina OpenAPI Exception
     */
    public static OpenAPI normalizeOpenAPI(OpenAPI openAPI, boolean validateOpIds, boolean isSanitized,
                                           boolean flatten) throws BallerinaOpenApiException {
        io.swagger.v3.oas.models.Paths openAPIPaths = openAPI.getPaths();
        if (validateOpIds) {
            validateOperationIds(openAPIPaths.entrySet());
        }
        validateRequestBody(openAPIPaths.entrySet());
        if (flatten) {
            new InlineModelResolver(true, false).flatten(openAPI);
        }
        if (isSanitized) {
            openAPI = new OASModifier().modify(openAPI);
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
                    errorList.add(String.format("OperationId is missing in the resource path: '%s(%s)'", entry.getKey(),
                            operation.getKey()));
                }
            }
        }
        if (!errorList.isEmpty()) {
            throw new BallerinaOpenApiException("the configured generation mode requires operation ids for all " +
                    "operations: " + LINE_SEPARATOR + String.join(LINE_SEPARATOR, errorList));
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

    public static String resolveReferenceType(Schema<?> schema, Components components, boolean isWithoutDataBinding,
                                              String pathParam, List<Diagnostic> diagnostics)
            throws InvalidReferenceException, UnsupportedOASDataTypeException {
        String type = GeneratorUtils.extractReferenceType(schema.get$ref());

        if (isWithoutDataBinding) {
            Schema<?> referencedSchema = components.getSchemas().get(escapeIdentifier(type));
            if (referencedSchema != null) {
                if (referencedSchema.get$ref() != null) {
                    type = resolveReferenceType(referencedSchema, components, isWithoutDataBinding, pathParam,
                            diagnostics);
                } else {
                    type = getPathParameterType(referencedSchema, pathParam, diagnostics);
                }
            }
        } else {
            type = escapeIdentifier(type
            );
            TypeHandler.getInstance().getTypeNodeFromOASSchema(schema);
        }
        return type;
    }

    private static String getPathParameterType(Schema<?> typeSchema, String pathParam, List<Diagnostic> diagnostics)
            throws UnsupportedOASDataTypeException {
        String type = null;
        if (!(isStringSchema(typeSchema) || isNumberSchema(typeSchema) || isBooleanSchema(typeSchema)
                || isIntegerSchema(typeSchema))) {
            type = STRING;
            CommonDiagnosticMessages unsupportedPath = CommonDiagnosticMessages.OAS_COMMON_204;
            CommonDiagnostic diagnostic = new CommonDiagnostic(unsupportedPath, pathParam);
            diagnostics.add(diagnostic);
        } else {
            type = GeneratorUtils.convertOpenAPITypeToBallerina(typeSchema, true);
        }
        return type;
    }

    /**
     *  Collect the complex paths in the OAS definition.
     *
     * @param openAPI - openAPI definition.
     * @return List of complex paths
     */
    public static List<String> getComplexPaths(OpenAPI openAPI) {
        //Check given openapi has complex path
        List<String> complexPathList = new ArrayList<>();
        if (openAPI.getPaths() != null) {
            for (Map.Entry<String, PathItem> path : openAPI.getPaths().entrySet()) {
                if (isComplexURL(path.getKey())) {
                    complexPathList.add(path.getKey());
                }
            }
        }
        return complexPathList;
    }

    public static String selectMediaType(String mediaTypeContent) {
        String[] contentTypes = mediaTypeContent.split(";");
        if (mediaTypeContent.length() > 1) {
            mediaTypeContent = contentTypes[0];
        }
        if (mediaTypeContent.matches("application/.*\\+json") || mediaTypeContent.matches(".*/json")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_JSON;
        } else if (mediaTypeContent.matches("application/.*\\+xml") || mediaTypeContent.matches(".*/xml")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_XML;
        } else if (mediaTypeContent.matches("text/.*")) {
            mediaTypeContent = GeneratorConstants.TEXT;
        }  else if (mediaTypeContent.matches("application/.*\\+octet-stream")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_OCTET_STREAM;
        } else if (mediaTypeContent.matches("application/.*\\+x-www-form-urlencoded")) {
            mediaTypeContent = GeneratorConstants.APPLICATION_URL_ENCODE;
        }
        return mediaTypeContent;
    }

    public static Schema getHeadersTypeSchema(ApiResponse apiResponse) {
        Map<String, Header> headers = apiResponse.getHeaders();
        if (Objects.isNull(headers)) {
            return null;
        }

        Map<String, Schema> properties = new HashMap<>();
        List<String> requiredFields = new ArrayList<>();
        for (Map.Entry<String, Header> headerEntry : headers.entrySet()) {
            String headerName = headerEntry.getKey();
            Header header = headerEntry.getValue();
            Schema headerTypeSchema = getValidatedHeaderSchema(header.getSchema());
            headerTypeSchema.setDescription(header.getDescription());
            headerTypeSchema.deprecated(header.getDeprecated());
            headerTypeSchema.extensions(header.getExtensions());
            properties.put(headerName, headerTypeSchema);
            if (header.getRequired() != null && header.getRequired()) {
                requiredFields.add(headerName);
            }
        }

        if (properties.isEmpty()) {
            return null;
        }

        ObjectSchema headersSchema = new ObjectSchema();
        headersSchema.setProperties(properties);
        if (!requiredFields.isEmpty()) {
            headersSchema.setRequired(requiredFields);
        }
        headersSchema.setAdditionalProperties(false);
        return headersSchema;
    }

    public static  Schema getValidatedHeaderSchema(Schema headerSchema) {
        return getValidatedHeaderSchema(headerSchema, headerSchema);
    }

    private static Schema getValidatedHeaderSchema(Schema targetSchema, Schema originalSchema) {
        // Only supporting string, integer, boolean and the array types of the above types
        if (targetSchema instanceof StringSchema || targetSchema instanceof IntegerSchema ||
                targetSchema instanceof BooleanSchema) {
            return originalSchema;
        }
        if (targetSchema instanceof ArraySchema arraySchema) {
            Schema items = arraySchema.getItems();
            return getValidatedHeaderSchema(items, originalSchema);
        }
        // Returning a string schema as the default type
        // TODO: Add support for reference, oneof schemas
        return new StringSchema();
    }

    public static String replaceContentWithinBrackets(String input, String replacement) {
        Pattern pattern = Pattern.compile("[\\{\\[].*?[\\}\\]]");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        input = sb.toString();
        if (!input.startsWith(".")) {
            //Pattern to ignore especial characters
            Pattern pattern2 = Pattern.compile("/(?=[^/]*[^a-zA-Z0-9_/])(.*?)(?=/)");
            Matcher matcher2 = pattern2.matcher(input);
            StringBuffer sb2 = new StringBuffer();
            while (matcher2.find()) {
                matcher2.appendReplacement(sb2, "/ZZZ");
            }
            matcher2.appendTail(sb2);
            //todo: need to improve the regex pattern to capture  abc.json
//            input = sb2.toString();
//            Pattern pattern3 = Pattern.compile("\\..*?[^/]*");
//            Matcher matcher3 = pattern3.matcher(input);
//            StringBuffer sb3 = new StringBuffer();
//            while (matcher3.find()) {
//                matcher3.appendReplacement(sb3, "/ZZZ");
//            }
//            matcher3.appendTail(sb3);
            return sb2.toString();
        } else {
            return input;
        }
    }

    public static TypeDescriptorNode generateStatusCodeTypeInclusionRecord(String code, ApiResponse response,
                                                                           String method, OpenAPI openAPI, String path,
                                                                           List<Diagnostic> diagnosticList)
            throws InvalidReferenceException {
        Content responseContent = response.getContent();
        Set<Map.Entry<String, MediaType>> bodyTypeSchema;
        if (Objects.nonNull(responseContent)) {
            bodyTypeSchema = responseContent.entrySet();
        } else if (response.get$ref() != null) {
            String referenceType = GeneratorUtils.extractReferenceType(response.get$ref());
            ApiResponse apiResponse = openAPI.getComponents().getResponses().get(referenceType);
            bodyTypeSchema = apiResponse.getContent().entrySet();
        } else {
            bodyTypeSchema = new LinkedHashSet<>();
        }
        Schema headersTypeSchema = getHeadersTypeSchema(response);

        HashMap<String, TypeDescriptorNode> generatedTypes = new LinkedHashMap<>();
        if (!code.equals("NoContent")) {
            for (Map.Entry<String, MediaType> mediaTypeEntry : bodyTypeSchema) {
                TypeDescriptorNode mediaTypeToken = generateTypeDescForMediaType(openAPI, path,
                        false, mediaTypeEntry);
                generatedTypes.put(mediaTypeToken.toString(), mediaTypeToken);
            }
        } else {
            if (!bodyTypeSchema.isEmpty()) {
                diagnosticList.add(new CommonDiagnostic(OAS_COMMON_101));
            }
            return TypeHandler.getInstance().createTypeInclusionRecord(code, null,
                    TypeHandler.getInstance().generateHeaderType(headersTypeSchema), method);
        }
        TypeDescriptorNode typeDescriptorNode = getUnionTypeDescriptorNodeFromTypeDescNodes(generatedTypes);
        return TypeHandler.getInstance().createTypeInclusionRecord(code, typeDescriptorNode,
                TypeHandler.getInstance().generateHeaderType(headersTypeSchema), method);
    }

    public static TypeDescriptorNode generateTypeDescForMediaType(OpenAPI openAPI, String path, boolean isRequest,
                                                                  Map.Entry<String, MediaType> mediaTypeEntry) {
        String mediaType = selectMediaType(mediaTypeEntry.getKey().trim());
        TypeDescriptorNode mediaTypeToken = switch (mediaType) {
            case GeneratorConstants.APPLICATION_JSON -> {
                if (mediaTypeEntry.getValue().getSchema() != null) {
                    TypeDescriptorNode typeDescriptorNode =
                            generateTypeDescriptorForGenericMediaType(mediaTypeEntry, path, isRequest);
                    if (typeDescriptorNode != null) {
                        yield typeDescriptorNode;
                    }
                }
                yield createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.JSON));
            }
            case GeneratorConstants.APPLICATION_XML -> generateTypeDescriptorForXMLContent();
            case GeneratorConstants.APPLICATION_URL_ENCODE -> generateTypeDescriptorForMapStringContent();
            // Commented due to the data binding issue in the ballerina http module
            // TODO: Related issue:https://github.com/ballerina-platform/ballerina-standard-library/issues/4090
            case GeneratorConstants.TEXT -> {
                if (mediaTypeEntry.getValue().getSchema() != null) {
                    yield generateTypeDescriptorForGenericMediaType(mediaTypeEntry, path, isRequest);
                }
                yield getSimpleNameReferenceNode(GeneratorConstants.STRING);
            }
            case GeneratorConstants.APPLICATION_OCTET_STREAM -> generateTypeDescriptorForOctetStreamContent();
            default -> null;
        };
        if (mediaTypeToken == null) {
            if (isRequest) {
                mediaTypeToken = createSimpleNameReferenceNode(createIdentifierToken(HTTP_REQUEST));
            } else {
                mediaTypeToken = createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.ANYDATA));
            }
        }
        return mediaTypeToken;
    }

    public static TypeDescriptorNode generateTypeDescriptorForXMLContent() {
        return getSimpleNameReferenceNode(GeneratorConstants.XML);
    }

    public static TypeDescriptorNode generateTypeDescriptorForMapStringContent() {
        return getSimpleNameReferenceNode(GeneratorConstants.MAP_STRING);
    }

    private static TypeDescriptorNode generateTypeDescriptorForGenericMediaType(
            Map.Entry<String, MediaType> mediaTypeEntry, String path, boolean isRequest) {
        TypeDescriptorNode typeDescNode;
        Schema<?> schema = mediaTypeEntry.getValue().getSchema();
        typeDescNode = TypeHandler.getInstance().getTypeNodeFromOASSchema(schema).orElse(null);
        if (GeneratorUtils.isMapSchema(mediaTypeEntry.getValue().getSchema())) {
            String recordName = getNewStatusCodeRecordName(path, isRequest);
            TypeHandler.getInstance().addTypeDefinitionNode(recordName,
                    NodeFactory.createTypeDefinitionNode(null, createToken(PUBLIC_KEYWORD),
                            createToken(TYPE_KEYWORD), createIdentifierToken(recordName),
                            typeDescNode, createToken(SEMICOLON_TOKEN)));
            typeDescNode = createSimpleNameReferenceNode(createIdentifierToken(recordName));
        }
        return typeDescNode;
    }

    public static TypeDescriptorNode generateTypeDescriptorForOctetStreamContent() {
        ArrayDimensionNode dimensionNode = NodeFactory.createArrayDimensionNode(
                createToken(SyntaxKind.OPEN_BRACKET_TOKEN), null, createToken(SyntaxKind.CLOSE_BRACKET_TOKEN));
        return createArrayTypeDescriptorNode(createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(GeneratorConstants.BYTE)), NodeFactory.createNodeList(dimensionNode));
    }

    private static TypeDescriptorNode getSimpleNameReferenceNode(String typeName) {
        return createSimpleNameReferenceNode(createIdentifierToken(typeName));
    }

    private static String getNewStatusCodeRecordName(String path, boolean isRequest) {
        String pathRecord = Objects.equals(path, SLASH) || Objects.equals(path, CATCH_ALL_PATH) ? "" :
                GeneratorUtils.getValidName(path, true);
        String typeSuffix = isRequest ? GeneratorConstants.REQUEST_RECORD_NAME : RESPONSE_RECORD_NAME;
        String recordName = pathRecord + typeSuffix;
        if (recordCountMap.containsKey(recordName)) {
            recordCountMap.put(recordName, recordCountMap.get(recordName) + 1);
            return recordName + "_" + recordCountMap.get(recordName);
        } else {
            recordCountMap.put(recordName, 0);
            return recordName;
        }
    }

    public static TypeDescriptorNode getUnionTypeDescriptorNodeFromTypeDescNodes(HashMap<String, TypeDescriptorNode>
                                                                                         typeDescNodes) {
        if (typeDescNodes.isEmpty()) {
            return null;
        }
        List<TypeDescriptorNode> qualifiedNodeList = typeDescNodes.values().stream().toList();
        TypeDescriptorNode unionTypeDescriptorNode = qualifiedNodeList.get(0);
        for (int i = 1; i < qualifiedNodeList.size(); i++) {
            TypeDescriptorNode rightTypeDesc = qualifiedNodeList.get(i);
            unionTypeDescriptorNode = createUnionTypeDescriptorNode(unionTypeDescriptorNode, createToken(PIPE_TOKEN),
                    rightTypeDesc);
        }
        return unionTypeDescriptorNode;
    }

    public static void addCommonParamsToOperationParams(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                        OpenAPI openAPI, String path) {
        List<Parameter> parameters = operation.getValue().getParameters();
        List<Parameter> commonParameters = openAPI.getPaths().get(path).getParameters();
        if (parameters == null) {
            operation.getValue().setParameters(commonParameters);
        } else if (commonParameters != null) {
            parameters.forEach(parameter -> {
                if (commonParameters.contains(parameter)) {
                    commonParameters.remove(parameter);
                }
            });
            parameters.addAll(commonParameters);
        }
    }

    public static String generateOperationUniqueId(Operation operation, String path, String method) {
        return Objects.nonNull(operation.getOperationId()) ?
                operation.getOperationId() : method + getValidName(path, true);
    }

    public static MetadataNode getNameAnnotationMetadataNode(String fieldName, Schema fieldSchema) {
        String annotationType = getAnnotationType(fieldSchema);
        AnnotationNode annotationNode = getNameAnnotationNode(fieldName, annotationType);
        return createMetadataNode(null, createNodeList(annotationNode));
    }

    public static AnnotationNode getNameAnnotationNode(String fieldName, String annotationType) {
        TypeHandler.getInstance().addImport(annotationType.equals(NAME_ANNOTATION) ? JSONDATA_IMPORT : HTTP_IMPORT);
        SimpleNameReferenceNode annotType = createSimpleNameReferenceNode(createIdentifierToken(annotationType));
        SpecificFieldNode nameField = createSpecificFieldNode(null,
                createIdentifierToken(annotationType.equals(NAME_ANNOTATION)  ? VALUE : NAME),
                createToken(COLON_TOKEN), createBasicLiteralNode(STRING_LITERAL,
                        createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                                CHAR + fieldName + CHAR, createEmptyMinutiaeList(),
                                createEmptyMinutiaeList())));
        MappingConstructorExpressionNode recExp = createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN),
                createSeparatedNodeList(nameField), createToken(CLOSE_BRACE_TOKEN));
        return createAnnotationNode(createToken(AT_TOKEN), annotType, recExp);
    }

    private static String getAnnotationType(Schema schema) {
        Map<String, Object> extensions = schema.getExtensions();
        if (Objects.isNull(extensions) || Objects.isNull(extensions.get(X_PARAM_TYPE)) ||
                !(extensions.get(X_PARAM_TYPE) instanceof String type)) {
            return NAME_ANNOTATION;
        }

        return switch (type) {
            case QUERY -> QUERY_ANNOTATION;
            case HEADER -> HEADER_ANNOTATION;
            default -> NAME_ANNOTATION;
        };
    }
}
