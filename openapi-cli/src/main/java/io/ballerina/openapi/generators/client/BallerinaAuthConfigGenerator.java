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

import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.*;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.*;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.*;
import static io.ballerina.openapi.generators.GeneratorConstants.*;
import static io.ballerina.openapi.generators.GeneratorUtils.escapeIdentifier;

/**
 * This class is used to generate authentication related nodes of the ballerina connector client syntax tree.
 */
public class BallerinaAuthConfigGenerator {
    private final List<String> headerApiKeyNameList = new ArrayList<>();
    private final List<String> queryApiKeyNameList = new ArrayList<>();
    private boolean isAPIKey = false;
    private boolean isHttpOROAuth = false;
    private final Set<String> authTypes = new LinkedHashSet<>();
    private String apiKeyDescription;

    public BallerinaAuthConfigGenerator(boolean isAPIKey, boolean isHttpOROAuth) {

        this.isAPIKey = isAPIKey;
        this.isHttpOROAuth = isHttpOROAuth;
    }

    public BallerinaAuthConfigGenerator() {

    }
    public boolean isHttpOROAuth() {

        return isHttpOROAuth;
    }

    public boolean isAPIKey() {
        return isAPIKey;
    }

    /**
     * Generate the Config record for the relevant authentication type.
     * -- ex: Config record for Http and OAuth 2.0 Authentication mechanisms.
     * <pre>
     *     public type ClientConfig record {
     *          http:CredentialsConfig authConfig;
     *          http:ClientSecureSocket secureSocketConfig?;
     *     };
     * </pre>
     * -- ex: Config record for API Key Authentication mechanism.
     * <pre>
     *     public type ApiKeysConfig record {
     *          map<string|string[]> apiKeys;
     *     };
     * </pre>
     *
     * @param openAPI                       OpenApi object received from swagger open-api parser
     * @return {@link TypeDefinitionNode}   Syntax tree node of config record
     */
    public TypeDefinitionNode getConfigRecord(OpenAPI openAPI) {
        if (openAPI.getComponents() != null && openAPI.getComponents().getSecuritySchemes() != null) {
            List<Node> recordFieldList = addItemstoRecordFieldList(openAPI);
            if (!recordFieldList.isEmpty()) {
                Token typeName;
                if (isAPIKey) {
                    typeName = AbstractNodeFactory.createIdentifierToken(API_KEY_CONFIG);
                } else {
                    typeName = AbstractNodeFactory.createIdentifierToken(CONFIG_RECORD_NAME);

                }
                Token visibilityQualifierNode = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.PUBLIC);
                Token typeKeyWord = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.TYPE);
                Token recordKeyWord = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.RECORD);
                Token bodyStartDelimiter = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.OPEN_BRACE);
                NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFieldList);
                Token bodyEndDelimiter = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.CLOSE_BRACE);
                RecordTypeDescriptorNode recordTypeDescriptorNode =
                        NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter,
                                fieldNodes, null, bodyEndDelimiter);
                Token semicolon = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.SEMICOLON);
                return NodeFactory.createTypeDefinitionNode(null,
                        visibilityQualifierNode, typeKeyWord, typeName, recordTypeDescriptorNode, semicolon);
            }
        }
        return null;
    }

    /**
     * Generate Class variable for api key map {@code map<string|string[]> apiKeys; }.
     *
     * @return {@link List<ObjectFieldNode>}    syntax tree object field node list
     */
    public ObjectFieldNode getApiKeyMapClassVariable() { // return ObjectFieldNode
        if (isAPIKey) {
            NodeList<Token> qualifierList = createNodeList(createToken(FINAL_KEYWORD));
            TypeDescriptorNode readOnlyNode = createTypeReferenceTypeDescNode(createSimpleNameReferenceNode
                    (createToken(READONLY_KEYWORD)));
            TypeDescriptorNode apiKeyMapNode = createMapTypeDescriptorNode(createToken(MAP_KEYWORD),
                    createTypeParameterNode(createToken(LT_TOKEN),
                            createTypeReferenceTypeDescNode(
                                    createSimpleNameReferenceNode(createToken(STRING_KEYWORD))),
                            createToken(GT_TOKEN)));
            TypeDescriptorNode intersectionTypeDescriptorNode = createIntersectionTypeDescriptorNode(readOnlyNode,
                    createToken(BITWISE_AND_TOKEN), apiKeyMapNode);
            IdentifierToken fieldName = createIdentifierToken(API_KEY_CONFIG_RECORD_FIELD);
            MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
            return createObjectFieldNode(metadataNode, null,
                    qualifierList, intersectionTypeDescriptorNode, fieldName, null, null,
                    createToken(SEMICOLON_TOKEN));
        }
        return null;
    }

    /**
     * Generate the config parameters of the client class init method.
     * -- ex: Config param for Http and OAuth 2.0 Authentication mechanisms.
     *        {@code ClientConfig clientConfig }
     * -- ex: Config param for API Key Authentication mechanism.
     *        {@code ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig = {} }
     * -- ex: Config param for API Key Authentication mechanism.
     *        {@code http:ClientConfiguration clientConfig = {} }
     *
     * @return  {@link List<Node>}  syntax tree node list of config parameters
     */
    public List<Node> getConfigParamForClassInit() {
        List<Node> parameters  = new ArrayList<>();
        IdentifierToken equalToken = createIdentifierToken(GeneratorConstants.EQUAL);
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        if (isHttpOROAuth) {
            BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(CONFIG_RECORD_NAME));
            IdentifierToken paramName = createIdentifierToken(CONFIG_RECORD_ARG);
            RequiredParameterNode authConfig = createRequiredParameterNode(annotationNodes, typeName, paramName);
            parameters.add(authConfig);
        } else {
            if (isAPIKey) {
                BuiltinSimpleNameReferenceNode apiKeyConfigTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(API_KEY_CONFIG));
                IdentifierToken apiKeyConfigParamName = createIdentifierToken(API_KEY_CONFIG_PARAM);
                RequiredParameterNode apiKeyConfigParamNode = createRequiredParameterNode(annotationNodes,
                        apiKeyConfigTypeName, apiKeyConfigParamName);
                parameters.add(apiKeyConfigParamNode);
                parameters.add(createToken(COMMA_TOKEN));
            }
            BuiltinSimpleNameReferenceNode httpClientConfigTypeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken("http:ClientConfiguration"));
            IdentifierToken httpClientConfig = createIdentifierToken(CONFIG_RECORD_ARG);
            BasicLiteralNode emptyexpression = createBasicLiteralNode(null, createIdentifierToken(" {}"));
            DefaultableParameterNode defaultHTTPConfig = createDefaultableParameterNode(annotationNodes,
                    httpClientConfigTypeName,
                    httpClientConfig, equalToken, emptyexpression);
            parameters.add(defaultHTTPConfig);
        }
        return parameters;
    }

    /**
     * Generate {@code http:ClientSecureSocket secureSocketConfig } local variable.
     *
     * @return  {@link VariableDeclarationNode} syntax tree variable declaration node.
     */
    public VariableDeclarationNode getSecureSocketInitNode () {
        if (isHttpOROAuth) {
            NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
            TypeDescriptorNode typeName = createOptionalTypeDescriptorNode(
                    createBuiltinSimpleNameReferenceNode(null,
                            createIdentifierToken("http:ClientSecureSocket")), createToken(QUESTION_MARK_TOKEN));
            CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                    createIdentifierToken(SSL_FIELD_NAME));
            TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeName,
                    bindingPattern);
            ExpressionNode configRecordSSLField = createOptionalFieldAccessExpressionNode(
                    createSimpleNameReferenceNode(createIdentifierToken(CONFIG_RECORD_ARG)),
                    createIdentifierToken("?."),
                    createSimpleNameReferenceNode(createIdentifierToken(SSL_FIELD_NAME)));
            return createVariableDeclarationNode(annotationNodes,
                    null, typedBindingPatternNode, createToken(EQUAL_TOKEN), configRecordSSLField,
                    createToken(SEMICOLON_TOKEN));
        }
        return null;
    }

    /**
     * Generate http:client initialization node
     * -- ex: Config record for Http and OAuth 2.0 Authentication mechanisms.
     * <pre>
     *     http:Client httpEp = check new (serviceUrl, {
     *          auth: clientConfig.authConfig,
     *          secureSocket: secureSocketConfig
     *     });
     * </pre>
     * -- ex: Config record for API Key Authentication mechanism.
     * <pre>
     *     http:Client httpEp = check new (serviceUrl, clientConfig);
     * </pre>
     * * -- ex: Config record when no Security Schema is given
     * <pre>
     *     http:Client httpEp = check new (serviceUrl, clientConfig);
     * </pre>
     *
     * @return {@link VariableDeclarationNode}   Synatx tree node of client initialization
     */
    public VariableDeclarationNode getClientInitializationNode () {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("http:Client"));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken("httpEp"));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        //Expression node
        Token newKeyWord = createIdentifierToken("new");
        Token openParenArg = createIdentifierToken("(");
        List<Node> argumentsList = new ArrayList<>();
        PositionalArgumentNode positionalArgumentNode01 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.SERVICE_URL)));
        argumentsList.add(positionalArgumentNode01);
        Token comma1 = createIdentifierToken(",");
        PositionalArgumentNode positionalArgumentNode02;
        if (isHttpOROAuth) {
            // try to create specific node
            positionalArgumentNode02 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                    createIdentifierToken(String.format("{ auth: %s.%s, secureSocket: %s }", CONFIG_RECORD_ARG,
                            AUTH_CONFIG_FILED_NAME, SSL_FIELD_NAME))));
            argumentsList.add(comma1);
            argumentsList.add(positionalArgumentNode02);
        } else {
            positionalArgumentNode02 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                    createIdentifierToken(CONFIG_RECORD_ARG)));
            argumentsList.add(comma1);
            argumentsList.add(positionalArgumentNode02);
        }
        SeparatedNodeList<FunctionArgumentNode> arguments = createSeparatedNodeList(argumentsList);
        Token closeParenArg = createToken(CLOSE_PAREN_TOKEN);
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(openParenArg, arguments,
                closeParenArg);
        ImplicitNewExpressionNode expressionNode = createImplicitNewExpressionNode(newKeyWord,
                parenthesizedArgList);
        CheckExpressionNode initializer = createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                expressionNode);
        return createVariableDeclarationNode(annotationNodes,
                null, typedBindingPatternNode, createToken(EQUAL_TOKEN), initializer,
                createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate assignment nodes for api key map assignment {@code self.apiKeys=apiKeyConfig.apiKeys.cloneReadOnly();}.
     *
     * @return  {@link AssignmentStatementNode} syntax tree assignment statement node.
     */
    public AssignmentStatementNode getApiKeyAssignmentNode() {
        if (isAPIKey) {
            FieldAccessExpressionNode varRefApiKey = createFieldAccessExpressionNode(
                    createSimpleNameReferenceNode(createIdentifierToken("self")), createToken(DOT_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(API_KEY_CONFIG_RECORD_FIELD)));
            ExpressionNode fieldAccessExpressionNode = createFieldAccessExpressionNode(
                    createRequiredExpressionNode(createIdentifierToken(API_KEY_CONFIG_PARAM)),
                    createToken(DOT_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(API_KEY_CONFIG_RECORD_FIELD)));
            ExpressionNode methodCallExpressionNode = createMethodCallExpressionNode(
                    fieldAccessExpressionNode, createToken(DOT_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken("cloneReadOnly")),
                    createToken(OPEN_PAREN_TOKEN), createSeparatedNodeList(), createToken(CLOSE_PAREN_TOKEN));
            return createAssignmentStatementNode(varRefApiKey,
                    createToken(EQUAL_TOKEN), methodCallExpressionNode, createToken(SEMICOLON_TOKEN));
        }
        return null;
    }

    /**
     * Returns API Key names which need to send in the query string.
     *
     * @return  {@link List<String>}    API key name list
     */
    public List<String> getQueryApiKeyNameList () {
        return queryApiKeyNameList;
    }

    /**
     * Returns API Key names which need to send as request headers.
     *
     * @return  {@link List<String>}    API key name list
     */
    public List<String> getHeaderApiKeyNameList () {
        return headerApiKeyNameList;
    }

    /**
     * Return auth type to generate test file.
     *
     * @return {@link Set<String>}
     */
    public Set<String> getAuthType () {
        return authTypes;
    }

    /**
     * Return fields of config record for the given security schema.
     * -- ex: Record fields for Http and OAuth 2.0 Authentication mechanisms.
     * <pre>
     *      http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig authConfig;
     *      http:ClientSecureSocket secureSocketConfig?;
     * </pre>
     * -- ex: Record fields for API Key Authentication mechanism.
     * <pre>
     *     final readonly & map<string> apiKeys;
     * </pre>
     *
     * @return  {@link List<Node>}  syntax tree node list of record fields
     */
    private List<Node> addItemstoRecordFieldList (OpenAPI openAPI) {
        List<Node> recordFieldNodes = new ArrayList<>();

        Token semicolonToken = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.SEMICOLON);
        Map<String, SecurityScheme> securitySchemeMap = openAPI.getComponents().getSecuritySchemes();
        String httpFieldTypeNames = getConfigRecordFieldTypeNames (securitySchemeMap);
        if (!httpFieldTypeNames.isBlank())  {
            // add auth config field
            Token authFieldType = AbstractNodeFactory.createIdentifierToken(httpFieldTypeNames);
            IdentifierToken authFieldName = AbstractNodeFactory.createIdentifierToken(escapeIdentifier(
                    AUTH_CONFIG_FILED_NAME));
            TypeDescriptorNode fieldTypeNode = createBuiltinSimpleNameReferenceNode(null, authFieldType);
            RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    fieldTypeNode, authFieldName, null, semicolonToken);
            recordFieldNodes.add(recordFieldNode);
            recordFieldNodes.addAll(Arrays.asList(
                    getHttpVersionField(), getHttp1Settings(), getHttp2Settings(),
                    getTimeOutField(), getForwardedField(), getFollowRedirectsField(),
                    getPoolConfigurationField(), getCacheConfigField(), getCompressionField(),
                    getCircuitBreakerConfigField(), getRetryConfigField(), getCookieConfigField(),
                    getResponseLimitConfigsField(), getSSLField()));
        } else if (isAPIKey) {

            Token apiKeyMap = AbstractNodeFactory.createIdentifierToken(API_KEY_MAP);
            IdentifierToken apiKeyMapFieldName = AbstractNodeFactory.createIdentifierToken(API_KEY_CONFIG_RECORD_FIELD);
            TypeDescriptorNode fieldTypeNode = createBuiltinSimpleNameReferenceNode(null, apiKeyMap);
            RecordFieldNode recordFieldNode = NodeFactory.createRecordFieldNode(null, null,
                    fieldTypeNode, apiKeyMapFieldName, null, semicolonToken);
            recordFieldNodes.add(recordFieldNode);
        }
        return recordFieldNodes;
    }
    private RecordFieldWithDefaultValueNode getCacheConfigField() {
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("HTTP caching related configurations")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = createIdentifierToken("cache");
        TypeDescriptorNode fieldTypeNode = createSimpleNameReferenceNode(createIdentifierToken("http:CacheConfig"));
        ExpressionNode emptyExpression = createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(), createToken(CLOSE_BRACE_TOKEN));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), emptyExpression, createToken(SEMICOLON_TOKEN));
    }
    private RecordFieldWithDefaultValueNode getPoolConfigurationField (){
        NilLiteralNode nilLiteralNode =
                createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("Configurations associated with request pooling")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = AbstractNodeFactory.createIdentifierToken("poolConfig");
        TypeDescriptorNode fieldTypeNode = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:PoolConfiguration"), createToken(QUESTION_MARK_TOKEN));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), nilLiteralNode, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getCircuitBreakerConfigField (){
        NilLiteralNode nilLiteralNode =
                createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("Configurations associated with the behaviour of the Circuit Breaker")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = AbstractNodeFactory.createIdentifierToken("circuitBreaker");
        TypeDescriptorNode fieldTypeNode = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:CircuitBreakerConfig"), createToken(QUESTION_MARK_TOKEN));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), nilLiteralNode, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getRetryConfigField (){
        NilLiteralNode nilLiteralNode =
                createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("Configurations associated with retrying")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = AbstractNodeFactory.createIdentifierToken("retryConfig");
        TypeDescriptorNode fieldTypeNode = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:RetryConfig"), createToken(QUESTION_MARK_TOKEN));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), nilLiteralNode, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getCookieConfigField (){
        NilLiteralNode nilLiteralNode =
                createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("Configurations associated with cookies")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = AbstractNodeFactory.createIdentifierToken("cookieConfig");
        TypeDescriptorNode fieldTypeNode = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:CookieConfig"), createToken(QUESTION_MARK_TOKEN));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), nilLiteralNode, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getResponseLimitConfigsField() {
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("Configurations associated with inbound response size limits")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = createIdentifierToken("responseLimits");
        TypeDescriptorNode fieldTypeNode = createSimpleNameReferenceNode(createIdentifierToken("http:ResponseLimitConfigs"));
        ExpressionNode emptyExpression = createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(), createToken(CLOSE_BRACE_TOKEN));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), emptyExpression, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getCompressionField () {
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("Specifies the way of handling compression (`accept-encoding`) header")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = createIdentifierToken("compression");
        TypeDescriptorNode fieldTypeNode = createSimpleNameReferenceNode(createIdentifierToken("http:Compression"));
        ExpressionNode decimalLiteralNode = createRequiredExpressionNode(createIdentifierToken("http:COMPRESSION_AUTO"));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), decimalLiteralNode, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getFollowRedirectsField (){
        NilLiteralNode nilLiteralNode =
                createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("Configurations associated with Redirection")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = AbstractNodeFactory.createIdentifierToken("followRedirects");
        TypeDescriptorNode fieldTypeNode = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:FollowRedirects"), createToken(QUESTION_MARK_TOKEN));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), nilLiteralNode, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getForwardedField () {
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("The choice of setting `forwarded`/`x-forwarded` header")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = createIdentifierToken("forwarded");
        TypeDescriptorNode fieldTypeNode = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        ExpressionNode decimalLiteralNode = createRequiredExpressionNode(createIdentifierToken("\"disable\""));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), decimalLiteralNode, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getTimeOutField () {
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("The maximum time to wait (in seconds) for a response before closing the connection")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = createIdentifierToken("timeout");
        TypeDescriptorNode fieldTypeNode = createSimpleNameReferenceNode(createToken(DECIMAL_KEYWORD));
        ExpressionNode decimalLiteralNode = createRequiredExpressionNode(createIdentifierToken("60"));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), decimalLiteralNode, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getHttp2Settings() {
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("Configurations related to HTTP/2 protocol")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = createIdentifierToken("http2Settings");
        TypeDescriptorNode fieldTypeNode = createSimpleNameReferenceNode(createIdentifierToken("http:ClientHttp2Settings"));
        ExpressionNode emptyExpression = createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(), createToken(CLOSE_BRACE_TOKEN));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), emptyExpression, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getHttp1Settings() {
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("Configurations related to HTTP/1.x protocol")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = createIdentifierToken("http1Settings");
        TypeDescriptorNode fieldTypeNode = createSimpleNameReferenceNode(createIdentifierToken("http:ClientHttp1Settings"));
        ExpressionNode emptyExpression = createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(), createToken(CLOSE_BRACE_TOKEN));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN), emptyExpression, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getSSLField (){
        NilLiteralNode nilLiteralNode =
                createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN), createNodeList(createIdentifierToken("SSL/TLS-related options")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken sslFieldNameNode = AbstractNodeFactory.createIdentifierToken(SSL_FIELD_NAME);
        TypeDescriptorNode sslfieldTypeNode = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:ClientSecureSocket"), createToken(QUESTION_MARK_TOKEN));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, sslfieldTypeNode, sslFieldNameNode, createToken(EQUAL_TOKEN), nilLiteralNode, createToken(SEMICOLON_TOKEN));
    }

    private RecordFieldWithDefaultValueNode getHttpVersionField (){
        MarkdownDocumentationLineNode fieldDescription =
                createMarkdownDocumentationLineNode(null, createToken(SyntaxKind.HASH_TOKEN),
                        createNodeList(createIdentifierToken("The HTTP version understood by the client")));
        MarkdownDocumentationNode documentationNode = createMarkdownDocumentationNode(createNodeList(fieldDescription));
        MetadataNode metadataNode = createMetadataNode(documentationNode, createEmptyNodeList());
        IdentifierToken fieldNameNode = createIdentifierToken("httpVersion");
        TypeDescriptorNode fieldTypeNode = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        RequiredExpressionNode requiredExpressionNode = createRequiredExpressionNode(createIdentifierToken("1.1"));
        return NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, fieldTypeNode, fieldNameNode, createToken(EQUAL_TOKEN),
                requiredExpressionNode, createToken(SEMICOLON_TOKEN));
    }
    /**
     * Travers through the security schemas of the given open api spec.
     * Store api key names which needs to send in the query string and as a request header separately.
     *
     * @param securitySchemeMap     Map of security schemas of the given open api spec
     * @return {@link String}       Type name of the authConfig field in ClientConfig record
     */
    private String getConfigRecordFieldTypeNames(Map<String, SecurityScheme> securitySchemeMap) {
        Set<String> httpFieldTypeNames = new HashSet<>();
        for (Map.Entry<String, SecurityScheme> securitySchemeEntry : securitySchemeMap.entrySet()) {
            SecurityScheme schemaValue = securitySchemeEntry.getValue();
            if (schemaValue != null && schemaValue.getType() != null) {
                String schemaType = schemaValue.getType().name().toLowerCase(Locale.getDefault());
                switch (schemaType) {
                    case HTTP:
                        isHttpOROAuth = true;
                        String scheme = schemaValue.getScheme();
                        if (scheme.equals(BASIC)) {
                            httpFieldTypeNames.add(AuthConfigTypes.BASIC.getValue());
                            authTypes.add(BASIC);
                        } else if (scheme.equals(BEARER)) {
                            httpFieldTypeNames.add(AuthConfigTypes.BEARER.getValue());
                            authTypes.add(BEARER);
                        }
                        break;
                    case OAUTH2:
                        isHttpOROAuth = true;
                        if (schemaValue.getFlows().getClientCredentials() != null) {
                            httpFieldTypeNames.add(AuthConfigTypes.CLIENT_CREDENTIAL.getValue());
                            authTypes.add(CLIENT_CRED);
                        }
                        if (schemaValue.getFlows().getPassword() != null) {
                            httpFieldTypeNames.add(AuthConfigTypes.PASSWORD.getValue());
                            authTypes.add(PASSWORD);
                        }
                        if (schemaValue.getFlows().getAuthorizationCode() != null) {
                            httpFieldTypeNames.add(AuthConfigTypes.BEARER.getValue());
                            httpFieldTypeNames.add(AuthConfigTypes.REFRESH_TOKEN.getValue());
                            authTypes.add(BEARER);
                        }
                        if (schemaValue.getFlows().getImplicit() != null) {
                            httpFieldTypeNames.add(AuthConfigTypes.BEARER.getValue());
                            authTypes.add(BEARER);
                        }
                        break;
                    case API_KEY:
                        isAPIKey = true;
                        String apiKeyType = schemaValue.getIn().name().toLowerCase(Locale.getDefault());
                        authTypes.add(API_KEY);
                        setApiKeyDescription(schemaValue);
                        switch (apiKeyType) {
                            case "query":
                                queryApiKeyNameList.add(schemaValue.getName());
                                break;
                            case "header":
                                headerApiKeyNameList.add(schemaValue.getName());
                                break;
                            default:
                                break;
                        }
                        break;
                }
            }
        }
        return buildConfigRecordFieldTypes(httpFieldTypeNames).toString();
    }

    /**
     * This method is used concat the config record authConfig field type.
     *
     * @param fieldtypes        Type name set from {@link #getConfigRecordFieldTypeNames(Map)} method.
     * @return {@link String}   Pipe concatenated list of type names
     */
    private StringBuilder buildConfigRecordFieldTypes(Set<String> fieldtypes) {
        StringBuilder httpAuthFieldTypes = new StringBuilder();
        if (!fieldtypes.isEmpty()) {
            for (String fieldType: fieldtypes) {
                if (httpAuthFieldTypes.length() != 0) {
                    httpAuthFieldTypes.append("|").append(fieldType);
                } else {
                    httpAuthFieldTypes.append(fieldType);
                }
            }
        }
        return httpAuthFieldTypes;
    }

    private void setApiKeyDescription (SecurityScheme scheme) {
        apiKeyDescription = "API key configuration detail";
        if (scheme.getExtensions() != null) {
            Map<String, Object> extensions = scheme.getExtensions();
            if (!extensions.isEmpty()) {
                for (Map.Entry<String, Object> extension: extensions.entrySet()) {
                    if (extension.getKey().trim().equals("x-apikey-description")) {
                        apiKeyDescription = extension.getValue().toString();
                    }
                }
            }
        }
    }

    public String getApiKeyDescription () {
        return apiKeyDescription;
    }


}
