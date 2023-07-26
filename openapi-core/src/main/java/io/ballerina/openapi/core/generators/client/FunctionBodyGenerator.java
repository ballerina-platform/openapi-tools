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
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.core.GeneratorUtils;
import io.ballerina.openapi.core.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.client.mime.MimeType;
import io.ballerina.openapi.core.generators.schema.BallerinaTypesGenerator;
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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTemplateExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BACKTICK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IF_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.openapi.core.GeneratorConstants.API_KEYS_CONFIG;
import static io.ballerina.openapi.core.GeneratorConstants.API_KEY_CONFIG_PARAM;
import static io.ballerina.openapi.core.GeneratorConstants.DELETE;
import static io.ballerina.openapi.core.GeneratorConstants.ENCODING;
import static io.ballerina.openapi.core.GeneratorConstants.EXECUTE;
import static io.ballerina.openapi.core.GeneratorConstants.HEAD;
import static io.ballerina.openapi.core.GeneratorConstants.HEADER;
import static io.ballerina.openapi.core.GeneratorConstants.HEADER_VALUES;
import static io.ballerina.openapi.core.GeneratorConstants.HTTP_HEADERS;
import static io.ballerina.openapi.core.GeneratorConstants.HTTP_REQUEST;
import static io.ballerina.openapi.core.GeneratorConstants.NEW;
import static io.ballerina.openapi.core.GeneratorConstants.NILLABLE;
import static io.ballerina.openapi.core.GeneratorConstants.PATCH;
import static io.ballerina.openapi.core.GeneratorConstants.POST;
import static io.ballerina.openapi.core.GeneratorConstants.PUT;
import static io.ballerina.openapi.core.GeneratorConstants.QUERY;
import static io.ballerina.openapi.core.GeneratorConstants.QUERY_PARAM;
import static io.ballerina.openapi.core.GeneratorConstants.REQUEST;
import static io.ballerina.openapi.core.GeneratorConstants.RESOURCE_PATH;
import static io.ballerina.openapi.core.GeneratorConstants.RESPONSE;
import static io.ballerina.openapi.core.GeneratorConstants.SELF;
import static io.ballerina.openapi.core.GeneratorUtils.generateBodyStatementForComplexUrl;
import static io.ballerina.openapi.core.GeneratorUtils.getOpenAPIType;
import static io.ballerina.openapi.core.GeneratorUtils.getValidName;
import static io.ballerina.openapi.core.GeneratorUtils.isComplexURL;
import static io.ballerina.openapi.core.GeneratorUtils.isaComposedSchema;
import static io.ballerina.openapi.core.generators.service.ServiceGenerationUtils.extractReferenceType;

/**
 * This Util class uses for generating remote function body  {@link FunctionBodyNode}.
 *
 * @since 1.3.0
 */
public class FunctionBodyGenerator {

    private List<ImportDeclarationNode> imports;
    private boolean isHeader;
    private final List<TypeDefinitionNode> typeDefinitionNodeList;
    private final OpenAPI openAPI;
    private final BallerinaTypesGenerator ballerinaSchemaGenerator;
    private final BallerinaUtilGenerator ballerinaUtilGenerator;
    private final BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator;
    private final boolean resourceMode;

    public List<ImportDeclarationNode> getImports() {
        return imports;
    }

    public void setImports(List<ImportDeclarationNode> imports) {
        this.imports = imports;
    }

    public FunctionBodyGenerator(List<ImportDeclarationNode> imports, List<TypeDefinitionNode> typeDefinitionNodeList,
                                 OpenAPI openAPI, BallerinaTypesGenerator ballerinaSchemaGenerator,
                                 BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator,
                                 BallerinaUtilGenerator ballerinaUtilGenerator, boolean resourceMode) {

        this.imports = imports;
        this.isHeader = false;
        this.typeDefinitionNodeList = typeDefinitionNodeList;
        this.openAPI = openAPI;
        this.ballerinaSchemaGenerator = ballerinaSchemaGenerator;
        this.ballerinaUtilGenerator = ballerinaUtilGenerator;
        this.ballerinaAuthConfigGenerator = ballerinaAuthConfigGenerator;
        this.resourceMode = resourceMode;
    }

    /**
     * Generate function body node for the remote function.
     *
     * @param path      - remote function path
     * @param operation - opneapi operation
     * @return - {@link FunctionBodyNode}
     * @throws BallerinaOpenApiException - throws exception if generating FunctionBodyNode fails.
     */
    public FunctionBodyNode getFunctionBodyNode(String path, Map.Entry<PathItem.HttpMethod, Operation> operation)
            throws BallerinaOpenApiException {

        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        FunctionReturnTypeGenerator functionReturnType = new FunctionReturnTypeGenerator(
                openAPI, ballerinaSchemaGenerator, typeDefinitionNodeList);
        isHeader = false;
        // Create statements
        List<StatementNode> statementsList = new ArrayList<>();
        // Check whether given path is complex path , if complex it will handle adding these two statement
        if (resourceMode && isComplexURL(path)) {
            List<StatementNode> bodyStatements = generateBodyStatementForComplexUrl(path);
            statementsList.addAll(bodyStatements);
        }
        //string path - common for every remote functions
        VariableDeclarationNode pathInt = getPathStatement(path, annotationNodes);
        statementsList.add(pathInt);

        //Handel query parameter map
        handleParameterSchemaInOperation(operation, statementsList);

        String method = operation.getKey().name().trim().toLowerCase(Locale.ENGLISH);
        // This return type for target data type binding.
        String rType = functionReturnType.getReturnType(operation.getValue(), true);
        String returnType = returnTypeForTargetTypeField(rType);
        // Statement Generator for requestBody
        if (operation.getValue().getRequestBody() != null) {
            RequestBody requestBody = operation.getValue().getRequestBody();
            handleRequestBodyInOperation(statementsList, method, returnType, requestBody);
        } else {
            createCommonFunctionBodyStatements(statementsList, method, returnType);
        }
        //Create statements
        NodeList<StatementNode> statements = createNodeList(statementsList);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN), null, statements,
                createToken(CLOSE_BRACE_TOKEN), null);
    }

    /**
     * Generate statements for query parameters and headers.
     */
    private void handleParameterSchemaInOperation(Map.Entry<PathItem.HttpMethod, Operation> operation,
                                                  List<StatementNode> statementsList) throws BallerinaOpenApiException {

        List<String> queryApiKeyNameList = new ArrayList<>();
        List<String> headerApiKeyNameList = new ArrayList<>();

        Set<String> securitySchemesAvailable = getSecurityRequirementForOperation(operation.getValue());

        if (securitySchemesAvailable.size() > 0) {
            Map<String, String> queryApiKeyMap = ballerinaAuthConfigGenerator.getQueryApiKeyNameList();
            Map<String, String> headerApiKeyMap = ballerinaAuthConfigGenerator.getHeaderApiKeyNameList();
            for (String schemaName : securitySchemesAvailable) {
                if (queryApiKeyMap.containsKey(schemaName)) {
                    queryApiKeyNameList.add(queryApiKeyMap.get(schemaName));
                } else if (headerApiKeyMap.containsKey(schemaName)) {
                    headerApiKeyNameList.add(headerApiKeyMap.get(schemaName));
                }
            }
        }

        List<Parameter> queryParameters = new ArrayList<>();
        List<Parameter> headerParameters = new ArrayList<>();

        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
            for (Parameter parameter : parameters) {
                if (parameter.get$ref() != null) {
                    String[] splits = parameter.get$ref().split("/");
                    parameter = openAPI.getComponents().getParameters().get(splits[splits.length - 1]);
                }
                if (parameter.getIn().trim().equals(QUERY)) {
                    queryParameters.add(parameter);
                } else if (parameter.getIn().trim().equals(HEADER)) {
                    headerParameters.add(parameter);
                }
            }
        }
        handleQueryParamsAndHeaders(queryParameters, headerParameters, statementsList, queryApiKeyNameList,
                headerApiKeyNameList);
    }

    /**
     * Handle query parameters and headers within a remote function.
     */
    public void handleQueryParamsAndHeaders(List<Parameter> queryParameters, List<Parameter> headerParameters,
                                            List<StatementNode> statementsList, List<String> queryApiKeyNameList,
                                            List<String> headerApiKeyNameList) throws BallerinaOpenApiException {

        boolean combinationOfApiKeyAndHTTPOAuth = ballerinaAuthConfigGenerator.isHttpOROAuth() &&
                ballerinaAuthConfigGenerator.isApiKey();
        if (combinationOfApiKeyAndHTTPOAuth) {
            addUpdatedPathAndHeaders(statementsList, queryApiKeyNameList, queryParameters,
                    headerApiKeyNameList, headerParameters);
        } else {
            if (!queryParameters.isEmpty() || !queryApiKeyNameList.isEmpty()) {
                ballerinaUtilGenerator.setQueryParamsFound(true);
                statementsList.add(getMapForParameters(queryParameters, "map<anydata>",
                        QUERY_PARAM, queryApiKeyNameList));
                getUpdatedPathHandlingQueryParamEncoding(statementsList, queryParameters);
            }
            if (!headerParameters.isEmpty() || !headerApiKeyNameList.isEmpty()) {
                statementsList.add(getMapForParameters(headerParameters, "map<any>",
                        HEADER_VALUES, headerApiKeyNameList));
                statementsList.add(GeneratorUtils.getSimpleExpressionStatementNode(
                        "map<string|string[]> " + HTTP_HEADERS + " = getMapForHeaders(headerValues)"));
                isHeader = true;
                ballerinaUtilGenerator.setHeadersFound(true);
            }
        }
    }

    /**
     * Generate statements for query parameters and headers when a client supports both ApiKey and HTTPOrOAuth
     * authentication.
     */
    private void addUpdatedPathAndHeaders(List<StatementNode> statementsList, List<String> queryApiKeyNameList,
                                          List<Parameter> queryParameters, List<String> headerApiKeyNameList,
                                          List<Parameter> headerParameters) throws BallerinaOpenApiException {

        List<StatementNode> ifBodyStatementsList = new ArrayList<>();

        if (!headerParameters.isEmpty() || !headerApiKeyNameList.isEmpty()) {
            if (!headerParameters.isEmpty()) {
                statementsList.add(getMapForParameters(headerParameters, "map<any>",
                        HEADER_VALUES, new ArrayList<>()));
            } else {
                ExpressionStatementNode headerMapCreation = GeneratorUtils.getSimpleExpressionStatementNode(
                        "map<any> " + HEADER_VALUES + " = {}");
                statementsList.add(headerMapCreation);
            }

            if (!headerApiKeyNameList.isEmpty()) {
                // update headerValues Map within the if block
                // `headerValues["api-key"] = self.apiKeyConfig?.apiKey;`
                addApiKeysToMap(HEADER_VALUES, headerApiKeyNameList, ifBodyStatementsList);
            }
            isHeader = true;
            ballerinaUtilGenerator.setHeadersFound(true);
        }

        if (!queryParameters.isEmpty() || !queryApiKeyNameList.isEmpty()) {
            ballerinaUtilGenerator.setQueryParamsFound(true);
            if (!queryParameters.isEmpty()) {
                statementsList.add(getMapForParameters(queryParameters, "map<anydata>",
                        QUERY_PARAM, new ArrayList<>()));
            } else {
                ExpressionStatementNode queryParamMapCreation = GeneratorUtils.getSimpleExpressionStatementNode(
                        "map<anydata> " + QUERY_PARAM + " = {}");
                statementsList.add(queryParamMapCreation);
            }

            if (!queryApiKeyNameList.isEmpty()) {
                // update queryParam Map within the if block
                // `queryParam["api-key"] = self.apiKeyConfig?.apiKey;`
                addApiKeysToMap(QUERY_PARAM, queryApiKeyNameList, ifBodyStatementsList);
            }
        }

        generateIfBlockToAddApiKeysToMaps(statementsList, ifBodyStatementsList);

        if (!queryParameters.isEmpty() || !queryApiKeyNameList.isEmpty()) {
            getUpdatedPathHandlingQueryParamEncoding(statementsList, queryParameters);
        }
        if (!headerParameters.isEmpty() || !headerApiKeyNameList.isEmpty()) {
            statementsList.add(GeneratorUtils.getSimpleExpressionStatementNode(
                    "map<string|string[]> " + HTTP_HEADERS + " = getMapForHeaders(headerValues)"));
        }
    }

    /**
     * Add apiKeys to a given map (queryParam or headerValues).
     * <p>
     * `queryParam["api-key"] = self.apiKeyConfig?.apiKey;`
     * `headerValues["api-key"] = self.apiKeyConfig?.apiKey;`
     */
    private void addApiKeysToMap(String mapName, List<String> apiKeyNames, List<StatementNode> statementNodeList) {

        if (!apiKeyNames.isEmpty()) {
            for (String apiKey : apiKeyNames) {
                IdentifierToken fieldName = createIdentifierToken(mapName + "[" + '"' + apiKey.trim() + '"' + "]");
                Token equal = createToken(EQUAL_TOKEN);
                FieldAccessExpressionNode fieldExpr = createFieldAccessExpressionNode(
                        createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                        createSimpleNameReferenceNode(createIdentifierToken(API_KEY_CONFIG_PARAM +
                                QUESTION_MARK_TOKEN.stringValue())));
                SimpleNameReferenceNode valueExpr = createSimpleNameReferenceNode(createIdentifierToken(
                        getValidName(getValidName(apiKey, false), false)));
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
            queryParameters) throws BallerinaOpenApiException {

        VariableDeclarationNode queryParamEncodingMap = getQueryParameterEncodingMap(queryParameters);
        if (queryParamEncodingMap != null) {
            statementsList.add(queryParamEncodingMap);
            ExpressionStatementNode updatedPath = GeneratorUtils.getSimpleExpressionStatementNode(
                    RESOURCE_PATH + " = " + RESOURCE_PATH + " + check getPathForQueryParam(queryParam, " +
                            "queryParamEncoding)");
            statementsList.add(updatedPath);
        } else {
            ExpressionStatementNode updatedPath = GeneratorUtils.getSimpleExpressionStatementNode(
                    RESOURCE_PATH + " = " + RESOURCE_PATH + " + check getPathForQueryParam(queryParam)");
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
            if (paramSchema.get$ref() != null) {
                paramSchema = openAPI.getComponents().getSchemas().get(
                        getValidName(extractReferenceType(paramSchema.get$ref()), true));
            }
            if (paramSchema != null && (paramSchema.getProperties() != null ||
                    (getOpenAPIType(paramSchema) != null && getOpenAPIType(paramSchema).equals("array")) ||
                    (isaComposedSchema(paramSchema)))) {
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

        if (securityRequirements.size() > 0) {
            for (SecurityRequirement requirement : securityRequirements) {
                securitySchemasAvailable.addAll(requirement.keySet());
            }
        }
        return securitySchemasAvailable;
    }

    /**
     * Handle request body in operation.
     */
    private void handleRequestBodyInOperation(List<StatementNode> statementsList, String method, String returnType,
                                              RequestBody requestBody)
            throws BallerinaOpenApiException {

        if (requestBody.getContent() != null) {
            Content rbContent = requestBody.getContent();
            Set<Map.Entry<String, MediaType>> entries = rbContent.entrySet();
            Iterator<Map.Entry<String, MediaType>> iterator = entries.iterator();
            //Currently align with first content of the requestBody
            while (iterator.hasNext()) {
                createRequestBodyStatements(isHeader, statementsList, method, returnType, iterator);
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
                createRequestBodyStatements(isHeader, statementsList, method, returnType, iterator);
                break;
            }
        }
    }

    /**
     * Generate common statements in function bosy.
     */
    private void createCommonFunctionBodyStatements(List<StatementNode> statementsList, String method,
                                                    String returnType) {

        String clientCallStatement;

        // This condition for several methods.
        boolean isEntityBodyMethods = method.equals(POST) || method.equals(PUT) || method.equals(PATCH)
                || method.equals(EXECUTE);
        if (isHeader) {
            if (isEntityBodyMethods) {
                ExpressionStatementNode requestStatementNode = GeneratorUtils.getSimpleExpressionStatementNode(
                        "http:Request request = new");
                statementsList.add(requestStatementNode);
                clientCallStatement = "check self.clientEp->" + method + "(" + RESOURCE_PATH +
                        ", request, " + HTTP_HEADERS + ")";
            } else if (method.equals(DELETE)) {
                clientCallStatement = "check self.clientEp->" + method + "(" + RESOURCE_PATH +
                        ", headers = " + HTTP_HEADERS + ")";
            } else if (method.equals(HEAD)) {
                clientCallStatement = "check self.clientEp->" + method + "(" + RESOURCE_PATH + ", " +
                        HTTP_HEADERS + ")";
            } else {
                clientCallStatement = "check self.clientEp->" + method + "(" + RESOURCE_PATH + ", " +
                        HTTP_HEADERS + ")";
            }
        } else if (method.equals(DELETE)) {
            clientCallStatement = "check self.clientEp-> " + method + "(" + RESOURCE_PATH + ")";
        } else if (isEntityBodyMethods) {
            ExpressionStatementNode requestStatementNode = GeneratorUtils.getSimpleExpressionStatementNode(
                    "http:Request request = new");
            statementsList.add(requestStatementNode);
            clientCallStatement = "check self.clientEp-> " + method + "(" + RESOURCE_PATH + ", request)";
        } else {
            clientCallStatement = "check self.clientEp->" + method + "(" + RESOURCE_PATH + ")";
        }
        //Return Variable
        VariableDeclarationNode clientCall = GeneratorUtils.getSimpleStatement(returnType, RESPONSE,
                clientCallStatement);
        statementsList.add(clientCall);
        Token returnKeyWord = createIdentifierToken("return");
        SimpleNameReferenceNode returns = createSimpleNameReferenceNode(createIdentifierToken(RESPONSE));
        ReturnStatementNode returnStatementNode = createReturnStatementNode(returnKeyWord, returns,
                createToken(SEMICOLON_TOKEN));
        statementsList.add(returnStatementNode);
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
                    String replaceVariable = "{getEncodedUri(" + getValidName(d, false) + ")}";
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
     *    json response = check self.clientEp->put(path, request);
     * </pre>
     *
     * @param isHeader       - Boolean value for header availability.
     * @param statementsList - StatementNode list in body node
     * @param method         - Operation method name.
     * @param returnType     - Response type
     * @param iterator       - RequestBody media type
     */
    private void createRequestBodyStatements(boolean isHeader, List<StatementNode> statementsList,
                                             String method, String returnType, Iterator<Map.Entry<String,
            MediaType>> iterator)
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
        VariableDeclarationNode requestStatement =
                GeneratorUtils.getSimpleStatement(returnType, RESPONSE, "check self.clientEp->"
                        + method + "(" + RESOURCE_PATH + ", request)");
        if (isHeader) {
            if (method.equals(POST) || method.equals(PUT) || method.equals(PATCH) || method.equals(DELETE)
                    || method.equals(EXECUTE)) {
                requestStatement = GeneratorUtils.getSimpleStatement(returnType, RESPONSE,
                        "check self.clientEp->" + method + "(" + RESOURCE_PATH + ", request, " +
                                HTTP_HEADERS + ")");
                statementsList.add(requestStatement);
                Token returnKeyWord = createIdentifierToken("return");
                SimpleNameReferenceNode returns = createSimpleNameReferenceNode(createIdentifierToken(RESPONSE));
                ReturnStatementNode returnStatementNode = createReturnStatementNode(returnKeyWord, returns,
                        createToken(SEMICOLON_TOKEN));
                statementsList.add(returnStatementNode);
            }
        } else {
            statementsList.add(requestStatement);
            Token returnKeyWord = createIdentifierToken("return");
            SimpleNameReferenceNode returnVariable = createSimpleNameReferenceNode(createIdentifierToken(RESPONSE));
            ReturnStatementNode returnStatementNode = createReturnStatementNode(returnKeyWord, returnVariable,
                    createToken(SEMICOLON_TOKEN));
            statementsList.add(returnStatementNode);
        }
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

    /**
     * This util function for getting type to the targetType data binding.
     *
     * @param rType - Given Data type
     * @return - return type
     */
    private String returnTypeForTargetTypeField(String rType) {

        String returnType;
        int index = rType.lastIndexOf("|");
        returnType = rType.substring(0, index);
        return (rType.contains(NILLABLE) ? returnType + NILLABLE : returnType);
    }

    /**
     * Generate map variable for query parameters and headers.
     */
    private VariableDeclarationNode getMapForParameters(List<Parameter> parameters, String mapDataType,
                                                        String mapName, List<String> apiKeyNames) {
        List<Node> filedOfMap = new ArrayList<>();
        BuiltinSimpleNameReferenceNode mapType = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken(mapDataType));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(mapName));
        TypedBindingPatternNode bindingPatternNode = createTypedBindingPatternNode(mapType, bindingPattern);

        for (Parameter parameter : parameters) {
            // Initializer
            IdentifierToken fieldName = createIdentifierToken('"' + (parameter.getName().trim()) + '"');
            Token colon = createToken(COLON_TOKEN);
            SimpleNameReferenceNode valueExpr = createSimpleNameReferenceNode(
                    createIdentifierToken(getValidName(parameter.getName().trim(), false)));
            SpecificFieldNode specificFieldNode = createSpecificFieldNode(null,
                    fieldName, colon, valueExpr);
            filedOfMap.add(specificFieldNode);
            filedOfMap.add(createToken(COMMA_TOKEN));
        }

        if (!apiKeyNames.isEmpty()) {
            for (String apiKey : apiKeyNames) {
                IdentifierToken fieldName = createIdentifierToken('"' + apiKey.trim() + '"');
                Token colon = createToken(COLON_TOKEN);
                IdentifierToken apiKeyConfigIdentifierToken = createIdentifierToken(API_KEY_CONFIG_PARAM);
                if (ballerinaAuthConfigGenerator.isHttpOROAuth() && ballerinaAuthConfigGenerator.isApiKey()) {
                    apiKeyConfigIdentifierToken = createIdentifierToken(API_KEY_CONFIG_PARAM +
                            QUESTION_MARK_TOKEN.stringValue());
                }
                SimpleNameReferenceNode apiKeyConfigParamNode = createSimpleNameReferenceNode(
                        apiKeyConfigIdentifierToken);
                FieldAccessExpressionNode fieldExpr = createFieldAccessExpressionNode(
                        createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                        apiKeyConfigParamNode);
                SimpleNameReferenceNode valueExpr = createSimpleNameReferenceNode(createIdentifierToken(
                        getValidName(apiKey, false)));
                SpecificFieldNode specificFieldNode;
                ExpressionNode apiKeyExpr = createFieldAccessExpressionNode(
                        fieldExpr, createToken(DOT_TOKEN), valueExpr);
                specificFieldNode = createSpecificFieldNode(null, fieldName, colon, apiKeyExpr);
                filedOfMap.add(specificFieldNode);
                filedOfMap.add(createToken(COMMA_TOKEN));
            }
        }

        filedOfMap.remove(filedOfMap.size() - 1);
        MappingConstructorExpressionNode initialize = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(filedOfMap),
                createToken(CLOSE_BRACE_TOKEN));
        return createVariableDeclarationNode(createEmptyNodeList(),
                null, bindingPatternNode, createToken(EQUAL_TOKEN), initialize,
                createToken(SEMICOLON_TOKEN));
    }
}
