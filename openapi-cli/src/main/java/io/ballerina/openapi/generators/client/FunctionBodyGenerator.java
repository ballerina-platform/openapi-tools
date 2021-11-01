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

package io.ballerina.openapi.generators.client;

import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
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
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.GeneratorUtils;
import io.ballerina.openapi.generators.schema.BallerinaSchemaGenerator;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Encoding;
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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionBodyBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
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
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.openapi.generators.GeneratorConstants.API_KEY_CONFIG_PARAM;
import static io.ballerina.openapi.generators.GeneratorConstants.BALLERINA;
import static io.ballerina.openapi.generators.GeneratorConstants.DELETE;
import static io.ballerina.openapi.generators.GeneratorConstants.ENCODING;
import static io.ballerina.openapi.generators.GeneratorConstants.EXECUTE;
import static io.ballerina.openapi.generators.GeneratorConstants.EXPLODE;
import static io.ballerina.openapi.generators.GeneratorConstants.HEAD;
import static io.ballerina.openapi.generators.GeneratorConstants.PATCH;
import static io.ballerina.openapi.generators.GeneratorConstants.POST;
import static io.ballerina.openapi.generators.GeneratorConstants.PUT;
import static io.ballerina.openapi.generators.GeneratorConstants.RESPONSE;
import static io.ballerina.openapi.generators.GeneratorConstants.STRING;
import static io.ballerina.openapi.generators.GeneratorConstants.STYLE;
import static io.ballerina.openapi.generators.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.generators.GeneratorUtils.extractReferenceType;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;

/**
 * This Util class uses for generating remote function body  {@link io.ballerina.compiler.syntax.tree.FunctionBodyNode}.
 */
public class FunctionBodyGenerator {
    private List<ImportDeclarationNode> imports;
    private boolean isHeader;
    private final List<TypeDefinitionNode> typeDefinitionNodeList;
    private final OpenAPI openAPI;
    private final BallerinaSchemaGenerator ballerinaSchemaGenerator;
    private final BallerinaUtilGenerator ballerinaUtilGenerator;
    private final GeneratorUtils generatorUtils;
    private final BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator;

    public List<ImportDeclarationNode> getImports() {
        return imports;
    }

    public void setImports(List<ImportDeclarationNode> imports) {
        this.imports = imports;
    }

    public FunctionBodyGenerator(List<ImportDeclarationNode> imports, List<TypeDefinitionNode> typeDefinitionNodeList,
                                 OpenAPI openAPI, BallerinaSchemaGenerator ballerinaSchemaGenerator,
                                 BallerinaAuthConfigGenerator ballerinaAuthConfigGenerator,
                                 BallerinaUtilGenerator ballerinaUtilGenerator) {

        this.imports = imports;
        this.isHeader = false;
        this.typeDefinitionNodeList = typeDefinitionNodeList;
        this.openAPI = openAPI;
        this.ballerinaSchemaGenerator = ballerinaSchemaGenerator;
        this.ballerinaUtilGenerator = ballerinaUtilGenerator;
        this.generatorUtils = new GeneratorUtils();
        this.ballerinaAuthConfigGenerator = ballerinaAuthConfigGenerator;
    }

    /**
     * Generate function body node for the remote function.
     *
     * @param path      - remote function path
     * @param operation - opneapi operation
     * @return - {@link io.ballerina.compiler.syntax.tree.FunctionBodyNode}
     * @throws BallerinaOpenApiException - throws exception if generating FunctionBodyNode fails.
     */
    public FunctionBodyNode getFunctionBodyNode(String path, Map.Entry<PathItem.HttpMethod, Operation> operation)
            throws BallerinaOpenApiException {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        FunctionReturnType functionReturnType = new FunctionReturnType(
                openAPI, ballerinaSchemaGenerator, typeDefinitionNodeList);
        isHeader = false;
        // Create statements
        List<StatementNode> statementsList =  new ArrayList<>();
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
            createCommonFunctionBodyStatements(statementsList, method, rType, returnType);
        }
        //Create statements
        NodeList<StatementNode> statements = createNodeList(statementsList);
        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN), null, statements,
                createToken(CLOSE_BRACE_TOKEN));
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

        if (operation.getValue().getParameters() != null) {
            List<Parameter> parameters = operation.getValue().getParameters();
            List<Parameter> queryParameters = new ArrayList<>();
            List<Parameter> headerParameters = new ArrayList<>();
            for (Parameter parameter: parameters) {
                if (parameter.getIn().trim().equals("query")) {
                    queryParameters.add(parameter);
                } else if (parameter.getIn().trim().equals("header")) {
                    headerParameters.add(parameter);
                }
            }
            if (!queryParameters.isEmpty() || !queryApiKeyNameList.isEmpty()) {
                ballerinaUtilGenerator.setQueryParamsFound(true);
                statementsList.add(getMapForParameters(queryParameters, "map<anydata>",
                        "queryParam", queryApiKeyNameList));
                VariableDeclarationNode queryParamEncodingMap = getQueryParameterEncodingMap(queryParameters);
                if (queryParamEncodingMap != null) {
                    statementsList.add(queryParamEncodingMap);
                    ExpressionStatementNode updatedPath = generatorUtils.getSimpleExpressionStatementNode(
                            "path = path + check getPathForQueryParam(queryParam, queryParamEncoding)");
                    statementsList.add(updatedPath);
                } else {
                    ExpressionStatementNode updatedPath = generatorUtils.getSimpleExpressionStatementNode(
                            "path = path + check getPathForQueryParam(queryParam)");
                    statementsList.add(updatedPath);
                }
            }
            if (!headerParameters.isEmpty() || !headerApiKeyNameList.isEmpty()) {
                statementsList.add(getMapForParameters(headerParameters, "map<any>",
                        "headerValues", headerApiKeyNameList));
                statementsList.add(generatorUtils.getSimpleExpressionStatementNode(
                        "map<string|string[]> accHeaders = getMapForHeaders(headerValues)"));
                isHeader = true;
                ballerinaUtilGenerator.setHeadersFound(true);
            }
        } else {

            if (!queryApiKeyNameList.isEmpty()) {
                ballerinaUtilGenerator.setQueryParamsFound(true);
                statementsList.add(getMapForParameters(new ArrayList<>(), "map<anydata>",
                        "queryParam", queryApiKeyNameList));
                // Add updated path
                ExpressionStatementNode updatedPath = generatorUtils.getSimpleExpressionStatementNode(
                        "path = path + check getPathForQueryParam(queryParam)");
                statementsList.add(updatedPath);
            }
            if (!headerApiKeyNameList.isEmpty()) {
                statementsList.add(getMapForParameters(new ArrayList<>(), "map<any>",
                        "headerValues", headerApiKeyNameList));
                statementsList.add(generatorUtils.getSimpleExpressionStatementNode(
                        "map<string|string[]> accHeaders = getMapForHeaders(headerValues)"));
                isHeader = true;
                ballerinaUtilGenerator.setHeadersFound(true);
            }

        }
    }

    /**
     * Generate VariableDeclarationNode for query parameter encoding map which includes the data related serialization
     * mechanism that needs to be used with object or array type parameters. Parameters in primitive types will not be
     * included to the map even when the serialization mechanisms are specified. These data is given in the `style` and
     * `explode` sections of the OpenAPI definition. Style defines how multiple values are delimited and explode
     * specifies whether arrays and objects should generate separate parameters
     *
     * --ex: {@code map<Encoding> queryParamEncoding = {"expand": ["deepObject", true]};}
     *
     * @param queryParameters               List of query parameters defined in a particular function
     * @return                              {@link VariableDeclarationNode}
     * @throws BallerinaOpenApiException    When invalid referenced schema is given.
     */
    public VariableDeclarationNode getQueryParameterEncodingMap(List<Parameter> queryParameters)
            throws BallerinaOpenApiException {
        List<Node> filedOfMap = new ArrayList<>();
        BuiltinSimpleNameReferenceNode mapType = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("map<" + ENCODING + ">"));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken("queryParamEncoding"));
        TypedBindingPatternNode bindingPatternNode = createTypedBindingPatternNode(mapType, bindingPattern);

        for (Parameter parameter: queryParameters) {
            Schema paramSchema = parameter.getSchema();
            if (paramSchema.get$ref() != null) {
                paramSchema  = openAPI.getComponents().getSchemas().get(extractReferenceType(paramSchema.get$ref()));
            }
            if (paramSchema != null && (paramSchema.getProperties() != null ||
                    (paramSchema.getType() != null && paramSchema.getType().equals("array")))) {
                if (parameter.getStyle() != null || parameter.getExplode() != null) {
                    createEncodingMap(filedOfMap, parameter.getStyle().toString(),
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
     * Create each item of the encoding map.
     *
     * @param filedOfMap    Includes all the items in the encoding map
     * @param style         Defines how multiple values are delimited and explode
     * @param explode       Specifies whether arrays and objects should generate separate parameters
     * @param key           Key of the item in the map
     */
    private void createEncodingMap(List<Node> filedOfMap, String style, Boolean explode, String key) {
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


    /**
     * Provides the list of security schemes available for the given operation.
     * @param operation     Current operation
     * @return              Security schemes that can be used to authorize the given operation
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
            for (SecurityRequirement requirement: securityRequirements) {
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
    private void createCommonFunctionBodyStatements(List<StatementNode> statementsList, String method, String rType,
                                                    String returnType) {

        String clientCallStatement;

        // This condition for several methods.
        boolean isMethod = method.equals(POST) || method.equals(PUT) || method.equals(PATCH) || method.equals(EXECUTE);
        if (isHeader) {
            if (isMethod) {
                ExpressionStatementNode requestStatementNode = generatorUtils.getSimpleExpressionStatementNode(
                        "http:Request request = new");
                statementsList.add(requestStatementNode);
                ExpressionStatementNode expressionStatementNode = generatorUtils.getSimpleExpressionStatementNode(
                        "//TODO: Update the request as needed");
                statementsList.add(expressionStatementNode);
                clientCallStatement = "check self.clientEp->" + method + "(path, request, headers = " +
                            "accHeaders)";

            } else {
                if (method.equals(HEAD)) {
                    clientCallStatement = "check self.clientEp->" + method + "(path, accHeaders)";
                } else {
                    clientCallStatement = "check self.clientEp->" + method + "(path, accHeaders)";
                }
            }
        } else if (isMethod) {
            ExpressionStatementNode requestStatementNode = generatorUtils.getSimpleExpressionStatementNode(
                    "http:Request request = new");
            statementsList.add(requestStatementNode);
            ExpressionStatementNode expressionStatementNode = generatorUtils.getSimpleExpressionStatementNode(
                    "//TODO: Update the request as needed");
            statementsList.add(expressionStatementNode);
            clientCallStatement =
                        "check self.clientEp-> " + method + "(path, request)";
        } else {
            clientCallStatement = "check self.clientEp->" + method + "(path)";
        }
        //Return Variable
        VariableDeclarationNode clientCall = generatorUtils.getSimpleStatement(returnType, RESPONSE,
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
     *
     * ex:
     * <pre> string  path = string `/weather`; </pre>
     *
     * @param path              - Given path
     * @param annotationNodes   - Node list for path implementation
     * @return                  - VariableDeclarationNode for path statement.
     */
    private VariableDeclarationNode getPathStatement(String path, NodeList<AnnotationNode> annotationNodes) {

        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(createSimpleNameReferenceNode(
                createIdentifierToken("string")), createCaptureBindingPatternNode(
                createIdentifierToken("path")));
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
                    String replaceVariable = "{" + escapeIdentifier(getValidName(d, false)) + "}";
                    refinedPath = refinedPath.replace(pathVariable, replaceVariable);
                }
            }
            path = refinedPath.replaceAll("[{]", "\\${");
        }
        return path;
    }

    /**
     * This function for creating requestBody statements.
     * -- ex: Request body with json payload.
     * <pre>
     *    http:Request request = new;
     *    json jsonBody = check payload.cloneWithType(json);
     *    request.setPayload(jsonBody);
     *    json response = check self.clientEp->put(path, request);
     * </pre>
     *
     * @param isHeader       - Boolean value for header availability.
     * @param statementsList - StatementNode list in body node
     * @param method         - Operation method name.
     * @param returnType     - Response type
     * @param iterator       - RequestBody media type
     */
    private  void createRequestBodyStatements(boolean isHeader, List<StatementNode> statementsList,
                                              String method, String returnType, Iterator<Map.Entry<String,
                                              MediaType>> iterator)
            throws BallerinaOpenApiException {

        //Create Request statement
        Map.Entry<String, MediaType> next = iterator.next();
        VariableDeclarationNode requestVariable = generatorUtils.getSimpleStatement("http:Request",
                "request", "new");
        statementsList.add(requestVariable);
        if (next.getValue().getSchema() != null) {
            genStatementsForRequestMediaType(statementsList, next);
            // TODO:Fill with other mime type
        } else {
            // Add default value comment
            ExpressionStatementNode expressionStatementNode = generatorUtils.getSimpleExpressionStatementNode(
                    "TODO: Update the request as needed");
            statementsList.add(expressionStatementNode);
        }
        // POST, PUT, PATCH, DELETE, EXECUTE
        VariableDeclarationNode requestStatement =
                generatorUtils.getSimpleStatement(returnType, RESPONSE, "check self.clientEp->"
                        + method + "(path," + " request)");
        if (isHeader) {
            if (method.equals(POST) || method.equals(PUT) || method.equals(PATCH) || method.equals(DELETE)
                    || method.equals(EXECUTE)) {
                requestStatement = generatorUtils.getSimpleStatement(returnType, RESPONSE,
                        "check self.clientEp->" + method + "(path, request, headers = accHeaders)");
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
     * This function use for generating function body statements according to the given request body media type.
     *
     * @param statementsList    - previous statements list
     * @param mediaTypeEntry    - Media type entry
     */
    private void genStatementsForRequestMediaType(List<StatementNode> statementsList,
                                                  Map.Entry<String, MediaType> mediaTypeEntry)
            throws BallerinaOpenApiException {

        Schema requestBodySchema = mediaTypeEntry.getValue().getSchema();
        if (mediaTypeEntry.getKey().contains("json")) {
            if (requestBodySchema.get$ref() != null || requestBodySchema.getType() != null
                    || requestBodySchema.getProperties() != null) {
                VariableDeclarationNode jsonVariable = generatorUtils.getSimpleStatement("json",
                        "jsonBody", "check payload.cloneWithType(json)");
                statementsList.add(jsonVariable);
                ExpressionStatementNode expressionStatementNode = generatorUtils.getSimpleExpressionStatementNode(
                        "request.setPayload(jsonBody)");
                statementsList.add(expressionStatementNode);
            } else {
                ExpressionStatementNode expressionStatementNode = generatorUtils.getSimpleExpressionStatementNode(
                        "request.setPayload(payload)");
                statementsList.add(expressionStatementNode);
            }
        } else if (mediaTypeEntry.getKey().contains("xml")) {

            if (requestBodySchema.get$ref() != null || requestBodySchema.getType() != null
                    || requestBodySchema.getProperties() != null) {
                ImportDeclarationNode xmlImport = GeneratorUtils.getImportDeclarationNode(
                        BALLERINA, "xmldata");
                if (!checkImportDuplicate(imports, "xmldata")) {
                    imports.add(xmlImport);
                }
                VariableDeclarationNode jsonVariable = generatorUtils.getSimpleStatement("json",
                        "jsonBody", "check payload.cloneWithType(json)");
                statementsList.add(jsonVariable);
                VariableDeclarationNode xmlBody = generatorUtils.getSimpleStatement("xml?", "xmlBody",
                        "check xmldata:fromJson(jsonBody)");
                statementsList.add(xmlBody);
                ExpressionStatementNode expressionStatementNode = generatorUtils.getSimpleExpressionStatementNode(
                        "request.setPayload(xmlBody)");
                statementsList.add(expressionStatementNode);
            } else {
                ExpressionStatementNode expressionStatementNode = generatorUtils.getSimpleExpressionStatementNode(
                        "request.setPayload(payload)");
                statementsList.add(expressionStatementNode);
            }
        } else if (mediaTypeEntry.getKey().contains("plain")) {
            ExpressionStatementNode expressionStatementNode = generatorUtils.getSimpleExpressionStatementNode(
                    "request.setPayload(payload)");
            statementsList.add(expressionStatementNode);
        } else if (mediaTypeEntry.getKey().contains("x-www-form-urlencoded")) {
            ballerinaUtilGenerator.setRequestBodyEncodingFound(true);
            ExpressionStatementNode setContentTypeExpression = generatorUtils.getSimpleExpressionStatementNode(
                    "check request.setContentType(\"application/x-www-form-urlencoded\")");
            statementsList.add(setContentTypeExpression);
            VariableDeclarationNode requestBodyEncodingMap = getRequestBodyEncodingMap(
                    mediaTypeEntry.getValue().getEncoding());
            if (requestBodyEncodingMap != null) {
                statementsList.add(requestBodyEncodingMap);
                VariableDeclarationNode requestBodyVariable = generatorUtils.getSimpleStatement(STRING,
                        "encodedRequestBody",
                        "createFormURLEncodedRequestBody(payload, requestBodyEncoding)");
                statementsList.add(requestBodyVariable);
            } else {
                VariableDeclarationNode requestBodyVariable = generatorUtils.getSimpleStatement(STRING,
                        "encodedRequestBody", "createFormURLEncodedRequestBody(payload)");
                statementsList.add(requestBodyVariable);
            }
            ExpressionStatementNode setPayloadExpression = generatorUtils.getSimpleExpressionStatementNode(
                    "request.setPayload(encodedRequestBody)");
            statementsList.add(setPayloadExpression);
        } else {
            throw new BallerinaOpenApiException("Unsupported media type '" + mediaTypeEntry.getKey() +
                    "' is given in the request body");
        }
    }

    /**
     * Generate VariableDeclarationNode for request body encoding map which includes the data related serialization
     * mechanism that needs to be used in each array or object type property. These data is given in the `style` and
     * `explode` sections under the requestBody encoding section of the OpenAPI definition. Style defines how multiple
     * values are delimited and explode specifies whether arrays and objects should generate separate parameters.
     *
     * --ex: {@code map<[string, boolean]> requestBodyEncoding =
     *                                      {"address": ["deepObject", true], "bank_account": ["deepObject", true]}}
     *
     * @param encodingMap   Encoding details of the `application/x-www-form-urlencoded` type request body
     * @return              {@link VariableDeclarationNode}
     */
    public VariableDeclarationNode getRequestBodyEncodingMap(Map<String, Encoding> encodingMap) {
        List<Node> filedOfMap = new ArrayList<>();
        BuiltinSimpleNameReferenceNode mapType = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("map<" + ENCODING + ">"));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken("requestBodyEncoding"));
        TypedBindingPatternNode bindingPatternNode = createTypedBindingPatternNode(mapType, bindingPattern);
        if (encodingMap != null && encodingMap.size() > 0) {
            for (Map.Entry<String, Encoding> encoding : encodingMap.entrySet()) {
                if (encoding.getValue().getStyle() != null || encoding.getValue().getExplode() != null) {
                    createEncodingMap(filedOfMap, encoding.getValue().getStyle().toString(),
                            encoding.getValue().getExplode(), encoding.getKey());
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
        }
        return null;
    }

    /**
     * This util function for getting type to the targetType data binding.
     *
     * @param rType - Given Data type
     * @return      - return type
     */
    private String returnTypeForTargetTypeField(String rType) {

        String returnType;
        int index = rType.lastIndexOf("|");
        returnType = rType.substring(0, index);
        if (returnType.contains("|")) {
            returnType = returnType.replaceAll("\\|", "");
        }
        return returnType;
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

        for (Parameter parameter: parameters) {
            // Initializer
            IdentifierToken fieldName = createIdentifierToken('"' + (parameter.getName().trim()) + '"');
            Token colon = createToken(COLON_TOKEN);
            SimpleNameReferenceNode valueExpr = createSimpleNameReferenceNode(
                    createIdentifierToken(escapeIdentifier(getValidName(parameter.getName().trim(), false))));
            SpecificFieldNode specificFieldNode = createSpecificFieldNode(null,
                    fieldName, colon, valueExpr);
            filedOfMap.add(specificFieldNode);
            filedOfMap.add(createToken(COMMA_TOKEN));
        }

        if (!apiKeyNames.isEmpty()) {
            for (String apiKey : apiKeyNames) {
                IdentifierToken fieldName = createIdentifierToken('"' + apiKey.trim() + '"');
                Token colon = createToken(COLON_TOKEN);
                FieldAccessExpressionNode fieldExpr = createFieldAccessExpressionNode(
                        createSimpleNameReferenceNode(createIdentifierToken("self")), createToken(DOT_TOKEN),
                        createSimpleNameReferenceNode(createIdentifierToken(API_KEY_CONFIG_PARAM)));
                SimpleNameReferenceNode valueExpr = createSimpleNameReferenceNode(createIdentifierToken(
                        getValidName(getValidName(apiKey, false), false)));
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

    private boolean checkImportDuplicate(List<ImportDeclarationNode> imports, String module) {
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
}
