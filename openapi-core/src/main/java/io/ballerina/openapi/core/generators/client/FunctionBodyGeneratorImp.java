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

package io.ballerina.openapi.core.generators.client;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.core.generators.client.AuthConfigGeneratorImp.ApiKeyNamePair;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.mime.MimeType;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAssignmentStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBinaryExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBlockStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIfElseStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createReturnStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTemplateExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BACKTICK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IF_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.API_KEYS_CONFIG;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.API_KEY_CONFIG_PARAM;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DELETE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.ENCODING;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.EXECUTE;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HEADER_VALUES;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HTTP_HEADERS;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HTTP_REQUEST;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.NEW;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.PATCH;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.POST;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.PUT;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.QUERIES;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.QUERY;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.QUERY_PARAM;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.REQUEST;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.RESOURCE_PATH;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.RETURN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.SELF;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.isComposedSchema;

/**
 * This Util class uses for generating remote function body  {@link FunctionBodyNode}.
 *
 * @since 1.3.0
 */
public class FunctionBodyGeneratorImp implements FunctionBodyGenerator {

    public static final String MAP_ANYDATA = "map<anydata> ";
    public static final String MAP_STRING_STRING_ARRAY = "map<string|string[]> ";
    public static final String GET_MAP_FOR_HEADERS = " = http:getHeaderMap(";
    private final List<ImportDeclarationNode> imports;
    private final String path;
    protected final Map.Entry<PathItem.HttpMethod, Operation> operation;
    private final OpenAPI openAPI;
    private final BallerinaUtilGenerator ballerinaUtilGenerator;
    private final AuthConfigGeneratorImp ballerinaAuthConfigGeneratorImp;
    private String headersParamName;

    private final boolean hasHeaders;
    private final boolean hasQueries;
    private boolean hasDefaultHeaders;

    public List<ImportDeclarationNode> getImports() {
        return imports;
    }


    public FunctionBodyGeneratorImp(String path, Map.Entry<PathItem.HttpMethod, Operation> operation,
                                    OpenAPI openAPI, AuthConfigGeneratorImp ballerinaAuthConfigGeneratorImp,
                                    BallerinaUtilGenerator ballerinaUtilGenerator,
                                    List<ImportDeclarationNode> imports, boolean hasHeaders,
                                    boolean hasDefaultHeaders, boolean hasQueries, String headersParamName) {
        this.path = path;
        this.operation = operation;
        this.openAPI = openAPI;
        this.ballerinaUtilGenerator = ballerinaUtilGenerator;
        this.ballerinaAuthConfigGeneratorImp = ballerinaAuthConfigGeneratorImp;
        this.imports = imports;
        this.hasHeaders = hasHeaders;
        this.hasQueries = hasQueries;
        this.hasDefaultHeaders = hasDefaultHeaders;
        this.headersParamName = headersParamName;
    }

    /**
     * Generate function body node for the remote function.
     *
     * @return - {@link FunctionBodyNode}
     */
    @Override
    public Optional<FunctionBodyNode> getFunctionBodyNode() {

        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        // Create statements
        List<StatementNode> statementsList = new ArrayList<>();
        //string path - common for every remote functions
        VariableDeclarationNode pathInt = getPathStatement(path, annotationNodes);
        statementsList.add(pathInt);
        try {
            //Handle query parameter map
            handleParameterSchemaInOperation(operation, statementsList);

            String method = operation.getKey().name().trim().toLowerCase(Locale.ENGLISH);
            // Statement Generator for requestBody
            if (operation.getValue().getRequestBody() != null) {
                RequestBody requestBody = operation.getValue().getRequestBody();
                handleRequestBodyInOperation(statementsList, method, requestBody);
            } else {
                createCommonFunctionBodyStatements(statementsList, method);
            }

            //Create statements
            NodeList<StatementNode> statements = createNodeList(statementsList);
            return Optional.of(createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN), null,
                    statements,
                    createToken(CLOSE_BRACE_TOKEN), null));
        } catch (BallerinaOpenApiException e) {
            //todo diagnostic message
            return Optional.empty();
        }
    }

    /**
     * Generate statements for query parameters and headers.
     */
    private void handleParameterSchemaInOperation(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                  List<StatementNode> statementsList) throws
            BallerinaOpenApiException {

        List<ApiKeyNamePair> queryApiKeyNameList = new ArrayList<>();
        List<ApiKeyNamePair> headerApiKeyNameList = new ArrayList<>();

        Set<String> securitySchemesAvailable = getSecurityRequirementForOperation(operation.getValue());

        if (!securitySchemesAvailable.isEmpty()) {
            Map<String, ApiKeyNamePair> queryApiKeyMap = ballerinaAuthConfigGeneratorImp.getQueryApiKeyNameMap();
            Map<String, ApiKeyNamePair> headerApiKeyMap = ballerinaAuthConfigGeneratorImp.getHeaderApiKeyNameMap();
            for (String schemaName : securitySchemesAvailable) {
                if (queryApiKeyMap.containsKey(schemaName)) {
                    queryApiKeyNameList.add(queryApiKeyMap.get(schemaName));
                } else if (headerApiKeyMap.containsKey(schemaName)) {
                    headerApiKeyNameList.add(headerApiKeyMap.get(schemaName));
                }
            }
        }

        List<Parameter> queryParameters = new ArrayList<>();

        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
            for (Parameter parameter : parameters) {
                if (parameter.get$ref() != null) {
                    String[] splits = parameter.get$ref().split("/");
                    parameter = openAPI.getComponents().getParameters().get(splits[splits.length - 1]);
                }
                if (parameter.getIn().trim().equals(QUERY)) {
                    queryParameters.add(parameter);
                }
            }
        }

        handleQueryParamsAndHeaders(queryParameters, statementsList, queryApiKeyNameList, headerApiKeyNameList);
    }

    /**
     * Handle query parameters and headers within a remote function.
     */
    public void handleQueryParamsAndHeaders(List<Parameter> queryParameters, List<StatementNode> statementsList,
                                            List<ApiKeyNamePair> queryApiKeyNameList,
                                            List<ApiKeyNamePair> headerApiKeyNameList)
            throws BallerinaOpenApiException {

        boolean combinationOfApiKeyAndHTTPOAuth = ballerinaAuthConfigGeneratorImp.isHttpOROAuth() &&
                ballerinaAuthConfigGeneratorImp.isApiKey();
        if (combinationOfApiKeyAndHTTPOAuth) {
            addUpdatedPathAndHeaders(statementsList, queryApiKeyNameList, queryParameters,
                    headerApiKeyNameList);
        } else {
            if (hasQueries || !queryApiKeyNameList.isEmpty()) {
                if (!queryApiKeyNameList.isEmpty()) {
                    String defaultValue = "{}";
                    if (hasQueries) {
                        defaultValue = "{..." + QUERIES + "}";
                    }

                    ExpressionStatementNode queryMapCreation = GeneratorUtils.getSimpleExpressionStatementNode(
                            MAP_ANYDATA + QUERY_PARAM + " = " + defaultValue);
                    statementsList.add(queryMapCreation);
                    addApiKeysToMap(QUERY_PARAM, queryApiKeyNameList, statementsList);
                }
                getUpdatedPathHandlingQueryParamEncoding(statementsList, queryParameters,
                        queryApiKeyNameList.isEmpty() ? QUERIES : QUERY_PARAM);
                ballerinaUtilGenerator.setQueryParamsFound(true);
            }
            if (hasHeaders || !headerApiKeyNameList.isEmpty()) {
                if (!headerApiKeyNameList.isEmpty()) {
                    String defaultValue = "{}";
                    if (hasHeaders) {
                        defaultValue = "{..." + headersParamName + "}";
                    }
                    hasDefaultHeaders = false;

                    ExpressionStatementNode headerMapCreation = GeneratorUtils.getSimpleExpressionStatementNode(
                            MAP_ANYDATA + HEADER_VALUES + " = " + defaultValue);
                    statementsList.add(headerMapCreation);
                    addApiKeysToMap(HEADER_VALUES, headerApiKeyNameList, statementsList);
                    statementsList.add(GeneratorUtils.getSimpleExpressionStatementNode(
                            MAP_STRING_STRING_ARRAY + HTTP_HEADERS + GET_MAP_FOR_HEADERS + HEADER_VALUES + ")"));
                } else if (!hasDefaultHeaders) {
                    statementsList.add(GeneratorUtils.getSimpleExpressionStatementNode(
                            MAP_STRING_STRING_ARRAY + HTTP_HEADERS + GET_MAP_FOR_HEADERS + headersParamName + ")"));
                }
            }
        }
    }

    /**
     * Generate statements for query parameters and headers when a client supports both ApiKey and HTTPOrOAuth
     * authentication.
     */
    private void addUpdatedPathAndHeaders(List<StatementNode> statementsList, List<ApiKeyNamePair> queryApiKeyNameList,
                                          List<Parameter> queryParameters, List<ApiKeyNamePair> headerApiKeyNameList)
            throws BallerinaOpenApiException {

        List<StatementNode> ifBodyStatementsList = new ArrayList<>();
        String headerVarName = HEADER_VALUES;

        if (hasHeaders || !headerApiKeyNameList.isEmpty()) {
            if (headerApiKeyNameList.isEmpty()) {
                if (!hasDefaultHeaders) {
                    headerVarName = headersParamName;
                }
            } else {
                hasDefaultHeaders = false;
                String defaultValue = "{}";
                if (hasHeaders) {
                    defaultValue = "{..." + headersParamName + "}";
                }
                ExpressionStatementNode headerMapCreation = GeneratorUtils.getSimpleExpressionStatementNode(
                        MAP_ANYDATA + HEADER_VALUES + " = " + defaultValue);
                statementsList.add(headerMapCreation);
                // update headerValues Map within the if block
                // `headerValues["api-key"] = self.apiKeyConfig?.apiKey;`
                addApiKeysToMap(HEADER_VALUES, headerApiKeyNameList, ifBodyStatementsList);
            }
        }

        if (!queryApiKeyNameList.isEmpty()) {
                String defaultValue = "{}";
                if (hasQueries) {
                    defaultValue = "{..." + QUERIES + "}";
                }

                ExpressionStatementNode queryParamMapCreation = GeneratorUtils.getSimpleExpressionStatementNode(
                        MAP_ANYDATA + QUERY_PARAM + " = " + defaultValue);
                statementsList.add(queryParamMapCreation);

                if (!queryApiKeyNameList.isEmpty()) {
                    // update queryParam Map within the if block
                    // `queryParam["api-key"] = self.apiKeyConfig?.apiKey;`
                    addApiKeysToMap(QUERY_PARAM, queryApiKeyNameList, ifBodyStatementsList);
                }

        }

        generateIfBlockToAddApiKeysToMaps(statementsList, ifBodyStatementsList);

        if (hasQueries || !queryApiKeyNameList.isEmpty()) {
            getUpdatedPathHandlingQueryParamEncoding(statementsList, queryParameters,
                    queryApiKeyNameList.isEmpty() ? QUERIES : QUERY_PARAM);
            ballerinaUtilGenerator.setQueryParamsFound(true);
        }
        if ((hasHeaders || !headerApiKeyNameList.isEmpty()) && !hasDefaultHeaders) {
            statementsList.add(GeneratorUtils.getSimpleExpressionStatementNode(
                    MAP_STRING_STRING_ARRAY + HTTP_HEADERS + GET_MAP_FOR_HEADERS + headerVarName + ")"));
        }
    }

    /**
     * Add apiKeys to a given map (queryParam or headerValues).
     * <p>
     * `queryParam["api-key"] = self.apiKeyConfig?.apiKey;`
     * `headerValues["api-key"] = self.apiKeyConfig?.apiKey;`
     */
    private void addApiKeysToMap(String mapName, List<ApiKeyNamePair> apiKeyNames,
                                 List<StatementNode> statementNodeList) {
        if (!apiKeyNames.isEmpty()) {
            for (ApiKeyNamePair apiKeyNamePair : apiKeyNames) {
                IdentifierToken fieldName = createIdentifierToken(mapName + "[" + '"' +
                        apiKeyNamePair.actualName().trim() + '"' + "]");
                Token equal = createToken(EQUAL_TOKEN);
                String apiKeyConfigToken = API_KEY_CONFIG_PARAM;
                if (ballerinaAuthConfigGeneratorImp.isHttpOROAuth() && ballerinaAuthConfigGeneratorImp.isApiKey()) {
                    apiKeyConfigToken += QUESTION_MARK_TOKEN.stringValue();
                }
                FieldAccessExpressionNode fieldExpr = createFieldAccessExpressionNode(
                        createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                        createSimpleNameReferenceNode(createIdentifierToken(apiKeyConfigToken)));
                SimpleNameReferenceNode valueExpr = createSimpleNameReferenceNode(createIdentifierToken(
                        escapeIdentifier(apiKeyNamePair.displayName())));
                ExpressionNode apiKeyExpr = createFieldAccessExpressionNode(
                        fieldExpr, createToken(DOT_TOKEN), valueExpr);
                statementNodeList.add(createAssignmentStatementNode(fieldName, equal, apiKeyExpr, createToken(
                        SEMICOLON_TOKEN)));
            }
        }
    }

    /**
     * Get updated path considering queryParamEncodingMap.
     */
    private void getUpdatedPathHandlingQueryParamEncoding(List<StatementNode> statementsList, List<Parameter>
            queryParameters, String queryVarName) throws BallerinaOpenApiException {
        VariableDeclarationNode queryParamEncodingMap = getQueryParameterEncodingMap(queryParameters);
        if (queryParamEncodingMap != null) {
            statementsList.add(queryParamEncodingMap);
            ExpressionStatementNode updatedPath = GeneratorUtils.getSimpleExpressionStatementNode(
                    RESOURCE_PATH + " = " + RESOURCE_PATH + " + check getPathForQueryParam(" + queryVarName + ", " +
                            "queryParamEncoding)");
            statementsList.add(updatedPath);
        } else {
            ExpressionStatementNode updatedPath = GeneratorUtils.getSimpleExpressionStatementNode(
                    RESOURCE_PATH + " = " + RESOURCE_PATH + " + check getPathForQueryParam(" + queryVarName + ")");
            statementsList.add(updatedPath);
        }
    }

    /**
     * Generate if block when a client supports both ApiKey and HTTPOrOAuth authentication.
     *
     * <pre>
     * if self.apiKeyConfig is ApiKeysConfig {
     *      --- given statements ---
     * }
     * </pre>
     */
    private void generateIfBlockToAddApiKeysToMaps(List<StatementNode> statementsList,
                                                   List<StatementNode> ifBodyStatementsList) {

        if (!ifBodyStatementsList.isEmpty()) {
            NodeList<StatementNode> ifBodyStatementsNodeList = createNodeList(ifBodyStatementsList);
            BlockStatementNode ifBody = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN),
                    ifBodyStatementsNodeList, createToken(CLOSE_BRACE_TOKEN));

            // Create expression `self.apiKeyConfig is ApiKeysConfig`
            ExpressionNode condition = createBinaryExpressionNode(null, createIdentifierToken(SELF +
                            DOT_TOKEN.stringValue() + API_KEY_CONFIG_PARAM),
                    createToken(IS_KEYWORD),
                    createIdentifierToken(API_KEYS_CONFIG));
            IfElseStatementNode ifBlock = createIfElseStatementNode(createToken(IF_KEYWORD), condition, ifBody, null);
            statementsList.add(ifBlock);
        }
    }

    /**
     * Generate VariableDeclarationNode for query parameter encoding map which includes the data related serialization
     * mechanism that needs to be used with object or array type parameters. Parameters in primitive types will not be
     * included to the map even when the serialization mechanisms are specified. These data is given in the `style` and
     * `explode` sections of the OpenAPI definition. Style defines how multiple values are delimited and explode
     * specifies whether arrays and objects should generate separate parameters
     * <p>
     * --ex: {@code map<Encoding> queryParamEncoding = {"expand": ["deepObject", true]};}
     *
     * @param queryParameters List of query parameters defined in a particular function
     * @return {@link VariableDeclarationNode}
     * @throws BallerinaOpenApiException When invalid referenced schema is given.
     */
    private VariableDeclarationNode getQueryParameterEncodingMap(List<Parameter> queryParameters)
            throws BallerinaOpenApiException {

        List<Node> filedOfMap = new ArrayList<>();
        BuiltinSimpleNameReferenceNode mapType = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("map<" + ENCODING + ">"));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken("queryParamEncoding"));
        TypedBindingPatternNode bindingPatternNode = createTypedBindingPatternNode(mapType, bindingPattern);

        for (Parameter parameter : queryParameters) {
            Schema paramSchema = parameter.getSchema();
            if (paramSchema != null && paramSchema.get$ref() != null) {
                paramSchema = openAPI.getComponents().getSchemas().get(
                        escapeIdentifier(extractReferenceType(paramSchema.get$ref())));
            }
            if (paramSchema != null && (paramSchema.getProperties() != null ||
                    (getOpenAPIType(paramSchema) != null && getOpenAPIType(paramSchema).equals("array")) ||
                    (isComposedSchema(paramSchema)))) {
                if (parameter.getStyle() != null || parameter.getExplode() != null) {
                    GeneratorUtils.createEncodingMap(filedOfMap, parameter.getStyle().toString(),
                            parameter.getExplode(), parameter.getName().trim());
                }
            }
        }
        if (!filedOfMap.isEmpty()) {
            filedOfMap.remove(filedOfMap.size() - 1);
            MappingConstructorExpressionNode initialize = createMappingConstructorExpressionNode(
                    createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(filedOfMap),
                    createToken(CLOSE_BRACE_TOKEN));
            return createVariableDeclarationNode(createEmptyNodeList(),
                    null, bindingPatternNode, createToken(EQUAL_TOKEN), initialize,
                    createToken(SEMICOLON_TOKEN));
        }
        return null;

    }

    /**
     * Provides the list of security schemes available for the given operation.
     *
     * @param operation Current operation
     * @return Security schemes that can be used to authorize the given operation
     */
    private Set<String> getSecurityRequirementForOperation(Operation operation) {

        Set<String> securitySchemasAvailable = new LinkedHashSet<>();
        List<SecurityRequirement> securityRequirements = new ArrayList<>();
        if (operation.getSecurity() != null) {
            securityRequirements = operation.getSecurity();
        } else if (openAPI.getSecurity() != null) {
            securityRequirements = openAPI.getSecurity();
        }

        if (!securityRequirements.isEmpty()) {
            for (SecurityRequirement requirement : securityRequirements) {
                securitySchemasAvailable.addAll(requirement.keySet());
            }
        }
        return securitySchemasAvailable;
    }

    /**
     * Handle request body in operation.
     */
    private void handleRequestBodyInOperation(List<StatementNode> statementsList, String method,
                                              RequestBody requestBody)
            throws BallerinaOpenApiException {

        if (requestBody.getContent() != null) {
            Content rbContent = requestBody.getContent();
            Set<Map.Entry<String, MediaType>> entries = rbContent.entrySet();
            Iterator<Map.Entry<String, MediaType>> iterator = entries.iterator();
            //Currently align with first content of the requestBody
            while (iterator.hasNext()) {
                createRequestBodyStatements(statementsList, method, iterator);
                break;
            }
        } else if (requestBody.get$ref() != null) {
            RequestBody requestBodySchema =
                    openAPI.getComponents().getRequestBodies().get(extractReferenceType(requestBody.get$ref()));
            Content rbContent = requestBodySchema.getContent();
            Set<Map.Entry<String, MediaType>> entries = rbContent.entrySet();
            Iterator<Map.Entry<String, MediaType>> iterator = entries.iterator();
            //Currently align with first content of the requestBody
            while (iterator.hasNext()) {
                createRequestBodyStatements(statementsList, method, iterator);
                break;
            }
        }
    }

    /**
     * Generate common statements in function body.
     */
    private void createCommonFunctionBodyStatements(List<StatementNode> statementsList, String method) {

        String clientCallStatement;

        // This condition for several methods.
        boolean isEntityBodyMethods = method.equals(POST) || method.equals(PUT) || method.equals(PATCH)
                || method.equals(EXECUTE);
        if (hasHeaders) {
            String paramName = hasDefaultHeaders ? headersParamName : HTTP_HEADERS;
            if (isEntityBodyMethods) {
                ExpressionStatementNode requestStatementNode = GeneratorUtils.getSimpleExpressionStatementNode(
                        "http:Request request = new");
                statementsList.add(requestStatementNode);
                clientCallStatement = getClientCallWithRequestAndHeaders().formatted(method, RESOURCE_PATH, paramName);

            } else if (method.equals(DELETE)) {
                clientCallStatement = getClientCallWithHeadersParam().formatted(method, RESOURCE_PATH, paramName);
            } else {
                clientCallStatement = getClientCallWithHeaders().formatted(method, RESOURCE_PATH, paramName);
            }
        } else if (method.equals(DELETE)) {
            clientCallStatement = getSimpleClientCall().formatted(method, RESOURCE_PATH);
        } else if (isEntityBodyMethods) {
            ExpressionStatementNode requestStatementNode = GeneratorUtils.getSimpleExpressionStatementNode(
                    "http:Request request = new");
            statementsList.add(requestStatementNode);
            clientCallStatement = getClientCallWithRequest().formatted(method, RESOURCE_PATH);
        } else {
            clientCallStatement =  getSimpleClientCall().formatted(method, RESOURCE_PATH);
        }
        //Return Variable
        generateReturnStatement(statementsList, clientCallStatement);
    }

    protected String getClientCallWithHeadersParam() {
        return "self.clientEp->%s(%s, headers = %s)";
    }

    protected String getClientCallWithRequestAndHeaders() {
        return "self.clientEp->%s(%s, request, %s)";
    }

    protected String getClientCallWithHeaders() {
        return "self.clientEp->%s(%s, %s)";
    }

    protected String getClientCallWithRequest() {
        return "self.clientEp->%s(%s, request)";
    }

    protected String getSimpleClientCall() {
        return "self.clientEp->%s(%s)";
    }

    /**
     * This method use to generate Path statement inside the function body node.
     * <p>
     * ex:
     * <pre> string  path = string `/weather`; </pre>
     *
     * @param path            - Given path
     * @param annotationNodes - Node list for path implementation
     * @return - VariableDeclarationNode for path statement.
     */
    private VariableDeclarationNode getPathStatement(String path, NodeList<AnnotationNode> annotationNodes) {

        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(createSimpleNameReferenceNode(
                createToken(STRING_KEYWORD)), createCaptureBindingPatternNode(
                createIdentifierToken(RESOURCE_PATH)));
        // Create initializer
        // Content  should decide with /pet and /pet/{pet}
        path = generatePathWithPathParameter(path);
        //String path generator
        NodeList<Node> content = createNodeList(createLiteralValueToken(null, path, createEmptyMinutiaeList(),
                createEmptyMinutiaeList()));
        TemplateExpressionNode initializer = createTemplateExpressionNode(null, createToken(STRING_KEYWORD),
                createToken(BACKTICK_TOKEN), content, createToken(BACKTICK_TOKEN));
        return createVariableDeclarationNode(annotationNodes, null,
                typedBindingPatternNode, createToken(EQUAL_TOKEN), initializer, createToken(SEMICOLON_TOKEN));
    }

    /**
     * This method is to used for generating path when it has path parameters.
     *
     * @param path - yaml contract path
     * @return string of path
     */
    public String generatePathWithPathParameter(String path) {

        if (path.contains("{")) {
            String refinedPath = path;
            Pattern p = Pattern.compile("\\{[^}]*}");
            Matcher m = p.matcher(path);
            while (m.find()) {
                String pathVariable = path.substring(m.start(), m.end());
                if (pathVariable.startsWith("{") && pathVariable.endsWith("}")) {
                    String d = pathVariable.replace("{", "").replace("}", "");
                    String replaceVariable = "{getEncodedUri(" + escapeIdentifier(d) + ")}";
                    refinedPath = refinedPath.replace(pathVariable, replaceVariable);
                }
            }
            path = refinedPath.replaceAll("[{]", "\\${");
        }
        ballerinaUtilGenerator.setPathParametersFound(true);
        return path;
    }

    /**
     * This function for creating requestBody statements.
     * -- ex: Request body with json payload.
     * <pre>
     *    http:Request request = new;
     *    json jsonBody = payload.toJson();
     *    request.setPayload(jsonBody, "application/json");
     *    self.clientEp->put(path, request);
     * </pre>
     *
     * @param statementsList - StatementNode list in body node
     * @param method         - Operation method name.
     * @param iterator       - RequestBody media type
     */
    private void createRequestBodyStatements(List<StatementNode> statementsList, String method,
                                             Iterator<Map.Entry<String, MediaType>> iterator)
            throws BallerinaOpenApiException {

        //Create Request statement
        Map.Entry<String, MediaType> mediaTypeEntry = iterator.next();
        if (GeneratorUtils.isSupportedMediaType(mediaTypeEntry)) {
            VariableDeclarationNode requestVariable = GeneratorUtils.getSimpleStatement(HTTP_REQUEST,
                    REQUEST, NEW);
            statementsList.add(requestVariable);
        }
        if (mediaTypeEntry.getValue() != null && GeneratorUtils.isSupportedMediaType(mediaTypeEntry)) {
            genStatementsForRequestMediaType(statementsList, mediaTypeEntry);
            // TODO:Fill with other mime type
        } else {
            // Add default value comment
            ExpressionStatementNode expressionStatementNode = GeneratorUtils.getSimpleExpressionStatementNode(
                    "// TODO: Update the request as needed");
            statementsList.add(expressionStatementNode);
        }
        // POST, PUT, PATCH, DELETE, EXECUTE
        String requestStatement = getClientCallWithRequest().formatted(method, RESOURCE_PATH);
        if (hasHeaders) {
            if (method.equals(POST) || method.equals(PUT) || method.equals(PATCH) || method.equals(DELETE)
                    || method.equals(EXECUTE)) {
                requestStatement = getClientCallWithRequestAndHeaders().formatted(method, RESOURCE_PATH,
                        hasDefaultHeaders ? headersParamName : HTTP_HEADERS);
                generateReturnStatement(statementsList, requestStatement);
            }
        } else {
            generateReturnStatement(statementsList, requestStatement);
        }
    }

    /**
     * This function is used for generating return statement.
     *
     * @param statementsList  - Previous statements list
     * @param returnStatement - Request statement
     */
    protected void generateReturnStatement(List<StatementNode> statementsList, String returnStatement) {
        Token returnKeyWord = createIdentifierToken(RETURN);
        SimpleNameReferenceNode returns;
        returns = createSimpleNameReferenceNode(createIdentifierToken(returnStatement));
        ReturnStatementNode returnStatementNode = createReturnStatementNode(returnKeyWord, returns,
                createToken(SEMICOLON_TOKEN));
        statementsList.add(returnStatementNode);
    }

    /**
     * This function is used for generating function body statements according to the given request body media type.
     *
     * @param statementsList - Previous statements list
     * @param mediaTypeEntry - Media type entry
     */
    private void genStatementsForRequestMediaType(List<StatementNode> statementsList,
                                                  Map.Entry<String, MediaType> mediaTypeEntry)
            throws BallerinaOpenApiException {
        MimeFactory factory = new MimeFactory();
        MimeType mimeType = factory.getMimeType(mediaTypeEntry, ballerinaUtilGenerator, imports);
        mimeType.setPayload(statementsList, mediaTypeEntry);
    }

    @Override
    public List<ClientDiagnostic> getDiagnostics() {
        return new ArrayList<>();
    }
}
