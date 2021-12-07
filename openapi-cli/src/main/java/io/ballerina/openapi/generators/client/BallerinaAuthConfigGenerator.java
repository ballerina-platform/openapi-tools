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

import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.BuiltinSimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.DefaultableParameterNode;
import io.ballerina.compiler.syntax.tree.ElseBlockNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.FieldAccessExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.NilLiteralNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredExpressionNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.exception.BallerinaOpenApiException;
import io.ballerina.openapi.generators.DocCommentsGenerator;
import io.ballerina.openapi.generators.GeneratorConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAssignmentStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBasicLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBinaryExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBlockStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createBuiltinSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCaptureBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createCheckExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createDefaultableParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createElseBlockNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFieldAccessExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIfElseStatementNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createImplicitNewExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIncludedRecordParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIntersectionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMappingConstructorExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMarkdownDocumentationNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMethodCallExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNilLiteralNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createOptionalTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createParenthesizedArgList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createPositionalArgumentNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRecordFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredExpressionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createRequiredParameterNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSeparatedNodeList;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceTypeDescNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ASTERISK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BITWISE_AND_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COMMA_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DECIMAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.DOT_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ELSE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EQUAL_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IF_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.IS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.NEW_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.READONLY_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.generators.GeneratorConstants.API_KEY;
import static io.ballerina.openapi.generators.GeneratorConstants.API_KEYS_CONFIG;
import static io.ballerina.openapi.generators.GeneratorConstants.API_KEY_CONFIG_PARAM;
import static io.ballerina.openapi.generators.GeneratorConstants.AUTH;
import static io.ballerina.openapi.generators.GeneratorConstants.AUTH_CONFIG;
import static io.ballerina.openapi.generators.GeneratorConstants.AUTH_CONFIG_RECORD;
import static io.ballerina.openapi.generators.GeneratorConstants.AuthConfigTypes;
import static io.ballerina.openapi.generators.GeneratorConstants.BASIC;
import static io.ballerina.openapi.generators.GeneratorConstants.BEARER;
import static io.ballerina.openapi.generators.GeneratorConstants.CLIENT_CONFIG;
import static io.ballerina.openapi.generators.GeneratorConstants.CLIENT_CRED;
import static io.ballerina.openapi.generators.GeneratorConstants.CONFIG_RECORD_ARG;
import static io.ballerina.openapi.generators.GeneratorConstants.HTTP;
import static io.ballerina.openapi.generators.GeneratorConstants.OAUTH2;
import static io.ballerina.openapi.generators.GeneratorConstants.PASSWORD;
import static io.ballerina.openapi.generators.GeneratorConstants.REFRESH_TOKEN;
import static io.ballerina.openapi.generators.GeneratorConstants.SELF;
import static io.ballerina.openapi.generators.GeneratorConstants.SSL_FIELD_NAME;
import static io.ballerina.openapi.generators.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.generators.GeneratorUtils.getValidName;

/**
 * This class is used to generate authentication related nodes of the ballerina connector client syntax tree.
 */
public class BallerinaAuthConfigGenerator {
    private final Map<String, String> headerApiKeyNameList = new HashMap<>();
    private final Map<String, String> queryApiKeyNameList = new HashMap<>();
    private final List<Node> apiKeysConfigRecordFields = new ArrayList<>();
    private boolean apiKey;
    private boolean httpOROAuth;
    private String clientCredGrantTokenUrl;
    private final Set<String> authTypes = new LinkedHashSet<>();

    public BallerinaAuthConfigGenerator(boolean isAPIKey, boolean isHttpOROAuth) {
        this.apiKey = isAPIKey;
        this.httpOROAuth = isHttpOROAuth;
    }

    /**
     * Returns `true` if authentication mechanism is API key.
     *
     * @return {@link boolean}    values of the flag isAPIKey
     */
    public boolean isApiKey() {
        return apiKey;
    }

    /**
     * Returns `true` if HTTP or OAuth authentication is supported.
     *
     * @return {@link boolean}
     */
    public boolean isHttpOROAuth() {
        return httpOROAuth;
    }

    /**
     * Returns API key names which need to send in the query string.
     *
     * @return {@link List<String>}    API key name list
     */
    public Map<String, String> getQueryApiKeyNameList() {
        return queryApiKeyNameList;
    }

    /**
     * Returns API key names which need to send as request headers.
     *
     * @return {@link List<String>}    API key name list
     */
    public Map<String, String> getHeaderApiKeyNameList() {
        return headerApiKeyNameList;
    }

    /**
     * Returns auth type to generate test file.
     *
     * @return {@link Set<String>}
     */
    public Set<String> getAuthType() {
        return authTypes;
    }

    /**
     * Add authentication related records.
     *
     * @param openAPI   OpenAPI object received from swagger open-api parser
     * @param nodes     List of ModuleMemberDeclarationNode nodes
     * @throws BallerinaOpenApiException    When function fails
     */
    public void addAuthRelatedRecords (OpenAPI openAPI, List<ModuleMemberDeclarationNode> nodes) throws
            BallerinaOpenApiException {
        if (openAPI.getComponents() != null && openAPI.getComponents().getSecuritySchemes() != null) {
            // Add ApiKeysConfig or ClientConfig record
            nodes.add(getConfigRecord(openAPI));

            // Add `AuthConfig` record if combination of ApiKey and HTTP/OAuth authentication is supported
            if (isApiKey() && isHttpOROAuth()) {
                nodes.add(getAuthConfigRecord());
            }

            // Add custom `OAuth2ClientCredentialsGrantConfig` record with default tokenUrl if `tokenUrl` is available
            if (clientCredGrantTokenUrl != null) {
                nodes.add(getOAuth2ClientCredsGrantConfigRecord());
            }
        }
    }

    /**
     * Generate the Config record for the relevant authentication type.
     * -- ex: Config record for Http and OAuth 2.0 Authentication mechanisms.
     * <pre>
     * # Provides a set of configurations for controlling the behaviours when communicating with a remote HTTP endpoint.
     * public type ClientConfig record {|
     *          # Configurations related to client authentication
     *          http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig auth;
     *          # The HTTP version understood by the client
     *          string httpVersion = "1.1";
     *          # Configurations related to HTTP/1.x protocol
     *          http:ClientHttp1Settings http1Settings = {};
     *          # Configurations related to HTTP/2 protocol
     *          http:ClientHttp2Settings http2Settings = {};
     *          # The maximum time to wait (in seconds) for a response before closing the connection
     *          decimal timeout = 60;
     *          # The choice of setting `forwarded`/`x-forwarded` header
     *          string forwarded = "disable";
     *          # Configurations associated with Redirection
     *          http:FollowRedirects? followRedirects = ();
     *          # Configurations associated with request pooling
     *          http:PoolConfiguration? poolConfig = ();
     *          # HTTP caching related configurations
     *          http:CacheConfig cache = {};
     *          # Specifies the way of handling compression (`accept-encoding`) header
     *          http:Compression compression = http:COMPRESSION_AUTO;
     *          # Configurations associated with the behaviour of the Circuit Breaker
     *          http:CircuitBreakerConfig? circuitBreaker = ();
     *          # Configurations associated with retrying
     *          http:RetryConfig? retryConfig = ();
     *          # Configurations associated with cookies
     *          http:CookieConfig? cookieConfig = ();
     *          # Configurations associated with inbound response size limits
     *          http:ResponseLimitConfigs responseLimits = {};
     *          #SSL/TLS-related options
     *          http:ClientSecureSocket? secureSocket = ();
     * |};
     * </pre>
     * -- ex: Config record for API Key Authentication mechanism.
     * <pre>
     *     # Provides API key configurations needed when communicating with a remote HTTP endpoint.
     *     public type ApiKeysConfig record {
     *          # API keys related to connector authentication
     *          map<string|string[]> apiKeys;
     *     };
     * </pre>
     *
     * @param openAPI                       OpenApi object received from swagger open-api parser
     * @return {@link TypeDefinitionNode}   Syntax tree node of config record
     */
    public TypeDefinitionNode getConfigRecord(OpenAPI openAPI) throws BallerinaOpenApiException {
        Map<String, SecurityScheme> securitySchemeMap = openAPI.getComponents().getSecuritySchemes();
        setAuthTypes(securitySchemeMap);
        Token typeName;
        NodeList<Node> recordFieldList;
        MetadataNode configRecordMetadataNode;
        if (apiKey) {
            configRecordMetadataNode = getMetadataNode(
                    "Provides API key configurations needed when communicating " +
                            "with a remote HTTP endpoint.");
            typeName = AbstractNodeFactory.createIdentifierToken(API_KEYS_CONFIG);
            recordFieldList = createNodeList(apiKeysConfigRecordFields);
        } else {
            configRecordMetadataNode = getMetadataNode(
                    "Provides a set of configurations for controlling the behaviours when communicating " +
                            "with a remote HTTP endpoint.");
            typeName = AbstractNodeFactory.createIdentifierToken(CLIENT_CONFIG);
            recordFieldList = createNodeList(getClientConfigRecordFields());
        }
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                        createToken(OPEN_BRACE_PIPE_TOKEN), recordFieldList, null,
                        createToken(CLOSE_BRACE_PIPE_TOKEN));
        return NodeFactory.createTypeDefinitionNode(configRecordMetadataNode,
                createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD), typeName,
                recordTypeDescriptorNode, createToken(SEMICOLON_TOKEN));

    }

    /**
     * Create `OAuth2ClientCredentialsGrantConfig` record with default tokenUrl.
     *
     * <pre>
     *      # OAuth2 Client Credintials Grant Configs
     *      public type OAuth2ClientCredentialsGrantConfig record {|
     *          *http:OAuth2ClientCredentialsGrantConfig;
     *          # Token URL
     *          string tokenUrl = "https://zoom.us/oauth/token";
     *      |};
     * </pre>
     *
     * @return {@link TypeDefinitionNode}   Custom `OAuth2ClientCredentialsGrantConfig` record with default tokenUrl
     */
    public TypeDefinitionNode getOAuth2ClientCredsGrantConfigRecord() {
        Token typeName = AbstractNodeFactory.createIdentifierToken(AuthConfigTypes.CUSTOM_CLIENT_CREDENTIAL.getValue());
        NodeList<Node> recordFieldList = createNodeList(getClientCredsGrantConfigFields());
        MetadataNode configRecordMetadataNode = getMetadataNode("OAuth2 Client Credintials Grant Configs");
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                        createToken(OPEN_BRACE_PIPE_TOKEN), recordFieldList, null,
                        createToken(CLOSE_BRACE_PIPE_TOKEN));
        return NodeFactory.createTypeDefinitionNode(configRecordMetadataNode,
                createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD), typeName,
                recordTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates fields of `OAuth2ClientCredentialsGrantConfig` record with default tokenUrl.
     *
     * <pre>
     *      *http:OAuth2ClientCredentialsGrantConfig;
     *      # Token URL
     *      string tokenUrl = "https://zoom.us/oauth/token";
     * </pre>
     *
     * @return {@link List<Node>}
     */
    private List<Node> getClientCredsGrantConfigFields() {
        List<Node> recordFieldNodes = new ArrayList<>();
        Token semicolonToken = createToken(SEMICOLON_TOKEN);
        Token equalToken = createToken(EQUAL_TOKEN);

        recordFieldNodes.add(createIncludedRecordParameterNode(createEmptyNodeList(),
                createIdentifierToken(ASTERISK_TOKEN.stringValue()),
                createIdentifierToken("http:OAuth2ClientCredentialsGrantConfig;"), null));

        MetadataNode metadataNode = getMetadataNode("Token URL");
        TypeDescriptorNode stringType = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        IdentifierToken fieldNameTokenUrl = createIdentifierToken("tokenUrl");
        ExpressionNode defaultValue = createRequiredExpressionNode(createIdentifierToken("\"" +
                clientCredGrantTokenUrl + "\""));
        RecordFieldWithDefaultValueNode fieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, stringType, fieldNameTokenUrl, equalToken, defaultValue,
                semicolonToken);
        recordFieldNodes.add(fieldNode);
        return recordFieldNodes;
    }

    /**
     * Generate `AuthConfig` record when combination of ApiKey and HTTPOrOAuth authentication is supported.
     *
     * <pre>
     *     # Provides Auth configurations needed when communicating with a remote HTTP endpoint.
     *     public type AuthConfig record {|
     *         # Auth Configuration
     *         http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig|ApiKeysConfig auth;
     *     |};
     * </pre>
     *
     * @return {@link TypeDefinitionNode}   Syntax tree node of config record
     */
    public TypeDefinitionNode getAuthConfigRecord() {
        Token typeName = AbstractNodeFactory.createIdentifierToken(AUTH_CONFIG_RECORD);
        NodeList<Node> recordFieldList = createNodeList(getAuthConfigRecordFields());
        MetadataNode configRecordMetadataNode = getMetadataNode("Provides Auth configurations needed when " +
                "communicating with a remote HTTP endpoint.");
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                        createToken(OPEN_BRACE_PIPE_TOKEN), recordFieldList, null,
                        createToken(CLOSE_BRACE_PIPE_TOKEN));
        return NodeFactory.createTypeDefinitionNode(configRecordMetadataNode,
                createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD), typeName,
                recordTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate Class variable for api key map {@code final readonly & ApiKeysConfig apiKeyConfig;}.
     *
     * @return {@link List<ObjectFieldNode>}    syntax tree object field node list
     */
    public ObjectFieldNode getApiKeyMapClassVariable() { // return ObjectFieldNode
        if (apiKey) {
            NodeList<Token> qualifierList = createNodeList(createToken(FINAL_KEYWORD));
            TypeDescriptorNode readOnlyNode = createTypeReferenceTypeDescNode(createSimpleNameReferenceNode
                    (createToken(READONLY_KEYWORD)));
            TypeDescriptorNode apiKeyMapNode = createSimpleNameReferenceNode(createIdentifierToken(API_KEYS_CONFIG));
            if (httpOROAuth) {
                apiKeyMapNode = createOptionalTypeDescriptorNode(apiKeyMapNode, createToken(QUESTION_MARK_TOKEN));
            }
            TypeDescriptorNode intersectionTypeDescriptorNode = createIntersectionTypeDescriptorNode(readOnlyNode,
                    createToken(BITWISE_AND_TOKEN), apiKeyMapNode);
            IdentifierToken fieldName = createIdentifierToken(API_KEY_CONFIG_PARAM);
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
     *          {@code ClientConfig clientConfig, string serviceUrl = "https://petstore.swagger.io:443/v2" }
     * -- ex: Config param for API Key Authentication mechanism.
     *          {@code ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig = {},
     *          string serviceUrl = "https://petstore.swagger.io:443/v2" }
     *        Config param for API Key Authentication mechanism with no server URL given
     *          {@code ApiKeysConfig apiKeyConfig, string serviceUrl; http:ClientConfiguration clientConfig = {}}
     * -- ex: Config param when no authentication mechanism given.
     *          {@code http:ClientConfiguration clientConfig = {},
     *          string serviceUrl = "https://petstore.swagger.io:443/v2" }
     *        Config param when no authentication mechanism given with no server URL
     *          {@code string serviceUrl, http:ClientConfiguration clientConfig = {}}
     * -- ex: Config param for combination of API Key and Http or OAuth 2.0 Authentication mechanisms.
     *          {@code AuthConfig authConfig, http:ClientConfiguration clientConfig =  {},
     *          string serviceUrl = "https://petstore.swagger.io:443/v2" }
     *
     * @return {@link List<Node>}  syntax tree node list of config parameters
     */
    public List<Node> getConfigParamForClassInit(String serviceUrl) {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        Node serviceURLNode = getServiceURLNode(serviceUrl);
        List<Node> parameters = new ArrayList<>();
        IdentifierToken equalToken = createIdentifierToken(GeneratorConstants.EQUAL);
        if (httpOROAuth && !apiKey) {
            BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(CLIENT_CONFIG));
            IdentifierToken paramName = createIdentifierToken(CONFIG_RECORD_ARG);
            RequiredParameterNode authConfig = createRequiredParameterNode(annotationNodes, typeName, paramName);
            parameters.add(authConfig);
            parameters.add(createToken(COMMA_TOKEN));
            parameters.add(serviceURLNode);
        } else {
            if (apiKey && httpOROAuth) {
                BuiltinSimpleNameReferenceNode authConfigTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(AUTH_CONFIG_RECORD));
                IdentifierToken authConfigParamName = createIdentifierToken(AUTH_CONFIG);
                RequiredParameterNode authConfigParamNode = createRequiredParameterNode(annotationNodes,
                        authConfigTypeName, authConfigParamName);
                parameters.add(authConfigParamNode);
                parameters.add(createToken(COMMA_TOKEN));
            } else if (apiKey) {
                BuiltinSimpleNameReferenceNode apiKeyConfigTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(API_KEYS_CONFIG));
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
            if (serviceURLNode instanceof RequiredParameterNode) {
                parameters.add(serviceURLNode);
                parameters.add(createToken(COMMA_TOKEN));
                parameters.add(defaultHTTPConfig);
            } else {
                parameters.add(defaultHTTPConfig);
                parameters.add(createToken(COMMA_TOKEN));
                parameters.add(serviceURLNode);
            }
        }
        return parameters;
    }

    /**
     * Generate the serviceUrl parameters of the client class init method.
     * @param serviceUrl        service Url given in the OpenAPI file
     * @return  {@link DefaultableParameterNode} when server URl is given in the OpenAPI file
     *          {@link RequiredParameterNode} when server URL is not given in the OpenAPI file
     */
    private Node getServiceURLNode(String serviceUrl) {
        Node serviceURLNode;
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        BuiltinSimpleNameReferenceNode serviceURLType = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("string"));
        IdentifierToken serviceURLVarName = createIdentifierToken(GeneratorConstants.SERVICE_URL);

        if (serviceUrl.equals("/")) {
            serviceURLNode = createRequiredParameterNode(annotationNodes, serviceURLType, serviceURLVarName);
        } else {
            BasicLiteralNode expression = createBasicLiteralNode(STRING_LITERAL,
                    createIdentifierToken('"' + serviceUrl + '"'));
            serviceURLNode = createDefaultableParameterNode(annotationNodes, serviceURLType,
                    serviceURLVarName, createIdentifierToken("="), expression);
        }
        return serviceURLNode;
    }

    /**
     * Generate http:client initialization node.
     * <pre>
     *     http:Client httpEp = check new (serviceUrl, clientConfig);
     * </pre>
     *
     * @return {@link VariableDeclarationNode}   Synatx tree node of client initialization
     */
    public VariableDeclarationNode getClientInitializationNode() {
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        // http:Client variable declaration
        BuiltinSimpleNameReferenceNode typeBindingPattern = createBuiltinSimpleNameReferenceNode(null,
                createIdentifierToken("http:Client"));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken("httpEp"));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        // Expression node
        List<Node> argumentsList = new ArrayList<>();
        PositionalArgumentNode positionalArgumentNode01 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.SERVICE_URL)));
        argumentsList.add(positionalArgumentNode01);
        Token comma1 = createIdentifierToken(",");

        PositionalArgumentNode positionalArgumentNode02 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken(CONFIG_RECORD_ARG)));
        argumentsList.add(comma1);
        argumentsList.add(positionalArgumentNode02);

        SeparatedNodeList<FunctionArgumentNode> arguments = createSeparatedNodeList(argumentsList);
        Token closeParenArg = createToken(CLOSE_PAREN_TOKEN);
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(createToken(OPEN_PAREN_TOKEN), arguments,
                closeParenArg);
        ImplicitNewExpressionNode expressionNode = createImplicitNewExpressionNode(createToken(NEW_KEYWORD),
                parenthesizedArgList);
        CheckExpressionNode initializer = createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                expressionNode);
        return createVariableDeclarationNode(annotationNodes,
                null, typedBindingPatternNode, createToken(EQUAL_TOKEN), initializer,
                createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generate assignment nodes for api key map assignment {@code self.apiKeyConfig=apiKeyConfig.cloneReadOnly();}.
     *
     * @return {@link AssignmentStatementNode} syntax tree assignment statement node.
     */
    public AssignmentStatementNode getApiKeyAssignmentNode() {
        if (apiKey) {
            FieldAccessExpressionNode varRefApiKey = createFieldAccessExpressionNode(
                    createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(API_KEY_CONFIG_PARAM)));
            ExpressionNode fieldAccessExpressionNode = createRequiredExpressionNode(
                    createIdentifierToken(API_KEY_CONFIG_PARAM));
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
     * Returns fields in ClientConfig record.
     * <pre>
     *     # Configurations related to client authentication
     *     http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig auth;
     *     # The HTTP version understood by the client
     *     string httpVersion = "1.1";
     *     # Configurations related to HTTP/1.x protocol
     *     http:ClientHttp1Settings http1Settings = {};
     *     # Configurations related to HTTP/2 protocol
     *     http:ClientHttp2Settings http2Settings = {};
     *     # The maximum time to wait (in seconds) for a response before closing the connection
     *     decimal timeout = 60;
     *     # The choice of setting `forwarded`/`x-forwarded` header
     *     string forwarded = "disable";
     *     # Configurations associated with Redirection
     *     http:FollowRedirects? followRedirects = ();
     *     # Configurations associated with request pooling
     *     http:PoolConfiguration? poolConfig = ();
     *     # HTTP caching related configurations
     *     http:CacheConfig cache = {};
     *     # Specifies the way of handling compression (`accept-encoding`) header
     *     http:Compression compression = http:COMPRESSION_AUTO;
     *     # Configurations associated with the behaviour of the Circuit Breaker
     *     http:CircuitBreakerConfig? circuitBreaker = ();
     *     # Configurations associated with retrying
     *     http:RetryConfig? retryConfig = ();
     *     # Configurations associated with cookies
     *     http:CookieConfig? cookieConfig = ();
     *     # Configurations associated with inbound response size limits
     *     http:ResponseLimitConfigs responseLimits = {};
     *     #SSL/TLS-related options
     *     http:ClientSecureSocket? secureSocket = ();
     * </pre>
     *
     * @return {@link List<Node>}   ClientConfig record fields' node list
     */
    private List<Node> getClientConfigRecordFields() {
        List<Node> recordFieldNodes = new ArrayList<>();
        Token semicolonToken = createToken(SEMICOLON_TOKEN);
        Token equalToken = createToken(EQUAL_TOKEN);
        ExpressionNode emptyExpression = createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN),
                createSeparatedNodeList(), createToken(CLOSE_BRACE_TOKEN));
        NilLiteralNode nilLiteralNode =
                createNilLiteralNode(createToken(OPEN_PAREN_TOKEN), createToken(CLOSE_PAREN_TOKEN));

        // add auth field
        MetadataNode authMetadataNode = getMetadataNode("Configurations related to client authentication");
        IdentifierToken authFieldName = AbstractNodeFactory.createIdentifierToken(escapeIdentifier(
                AUTH));
        TypeDescriptorNode authFieldTypeNode =
                createSimpleNameReferenceNode(createIdentifierToken(getAuthFieldTypeName()));
        RecordFieldNode authFieldNode = NodeFactory.createRecordFieldNode(authMetadataNode, null,
                authFieldTypeNode, authFieldName, null, semicolonToken);
        recordFieldNodes.add(authFieldNode);

        // add httpVersion field
        MetadataNode httpVersionMetadata = getMetadataNode("The HTTP version understood by the client");
        TypeDescriptorNode httpVersionFieldType = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        IdentifierToken httpVersionFieldName = createIdentifierToken("httpVersion");
        RequiredExpressionNode httpVersionExpression =
                createRequiredExpressionNode(createIdentifierToken("\"1.1\""));
        RecordFieldWithDefaultValueNode httpVersionFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                httpVersionMetadata, null, httpVersionFieldType, httpVersionFieldName,
                equalToken, httpVersionExpression, semicolonToken);
        recordFieldNodes.add(httpVersionFieldNode);

        // add http1Settings field
        MetadataNode http1SettingsMetadata = getMetadataNode("Configurations related to HTTP/1.x protocol");
        IdentifierToken http1SettingsFieldName = createIdentifierToken("http1Settings");
        TypeDescriptorNode http1SettingsFieldType =
                createSimpleNameReferenceNode(createIdentifierToken("http:ClientHttp1Settings"));
        RecordFieldWithDefaultValueNode http1SettingsFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                http1SettingsMetadata, null, http1SettingsFieldType, http1SettingsFieldName,
                equalToken, emptyExpression, semicolonToken);
        recordFieldNodes.add(http1SettingsFieldNode);

        // add http2Settings fields
        MetadataNode http2SettingsMetadata = getMetadataNode("Configurations related to HTTP/2 protocol");
        TypeDescriptorNode http2SettingsFieldType =
                createSimpleNameReferenceNode(createIdentifierToken("http:ClientHttp2Settings"));
        IdentifierToken http2SettingsFieldName = createIdentifierToken("http2Settings");
        RecordFieldWithDefaultValueNode http2SettingsFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                http2SettingsMetadata, null, http2SettingsFieldType, http2SettingsFieldName,
                equalToken, emptyExpression, semicolonToken);
        recordFieldNodes.add(http2SettingsFieldNode);

        // add timeout field
        MetadataNode timeoutMetadata = getMetadataNode(
                "The maximum time to wait (in seconds) for a response before closing the connection");
        IdentifierToken timeoutFieldName = createIdentifierToken("timeout");
        TypeDescriptorNode timeoutFieldType = createSimpleNameReferenceNode(createToken(DECIMAL_KEYWORD));
        ExpressionNode decimalLiteralNode = createRequiredExpressionNode(createIdentifierToken("60"));
        RecordFieldWithDefaultValueNode timeoutFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                timeoutMetadata, null, timeoutFieldType, timeoutFieldName,
                equalToken, decimalLiteralNode, semicolonToken);
        recordFieldNodes.add(timeoutFieldNode);

        // add forwarded field
        MetadataNode forwardedMetadata = getMetadataNode(
                "The choice of setting `forwarded`/`x-forwarded` header");
        IdentifierToken forwardedFieldName = createIdentifierToken("forwarded");
        TypeDescriptorNode forwardedFieldType = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        ExpressionNode forwardedDefaultValue = createRequiredExpressionNode(createIdentifierToken("\"disable\""));
        RecordFieldWithDefaultValueNode forwardedFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                forwardedMetadata, null, forwardedFieldType, forwardedFieldName,
                equalToken, forwardedDefaultValue, semicolonToken);
        recordFieldNodes.add(forwardedFieldNode);

        // add followRedirects field
        MetadataNode followRedirectsMetadata = getMetadataNode("Configurations associated with Redirection");
        IdentifierToken followRedirectsFieldName = AbstractNodeFactory.createIdentifierToken("followRedirects");
        TypeDescriptorNode followRedirectsFieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:FollowRedirects"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode followRedirectsFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                followRedirectsMetadata, null, followRedirectsFieldType,
                followRedirectsFieldName, equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(followRedirectsFieldNode);

        // add poolConfig field
        MetadataNode poolConfigMetaData = getMetadataNode("Configurations associated with request pooling");
        IdentifierToken poolConfigFieldName = AbstractNodeFactory.createIdentifierToken("poolConfig");
        TypeDescriptorNode poolConfigFieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:PoolConfiguration"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode poolConfigFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                poolConfigMetaData, null, poolConfigFieldType, poolConfigFieldName,
                equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(poolConfigFieldNode);

        // add cache field
        MetadataNode cachMetadata = getMetadataNode("HTTP caching related configurations");
        IdentifierToken cacheFieldName = createIdentifierToken("cache");
        TypeDescriptorNode cacheFieldType =
                createSimpleNameReferenceNode(createIdentifierToken("http:CacheConfig"));
        RecordFieldWithDefaultValueNode cachFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                cachMetadata, null, cacheFieldType, cacheFieldName,
                equalToken, emptyExpression, semicolonToken);
        recordFieldNodes.add(cachFieldNode);

        // add compression field
        MetadataNode compressionMetadata = getMetadataNode(
                "Specifies the way of handling compression (`accept-encoding`) header");
        IdentifierToken compressionFieldName = createIdentifierToken("compression");
        TypeDescriptorNode compressionFieldType = createSimpleNameReferenceNode(
                createIdentifierToken("http:Compression"));
        ExpressionNode compressionDefaultValue = createRequiredExpressionNode(
                createIdentifierToken("http:COMPRESSION_AUTO"));
        RecordFieldWithDefaultValueNode compressionFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                compressionMetadata, null, compressionFieldType, compressionFieldName,
                equalToken, compressionDefaultValue, semicolonToken);
        recordFieldNodes.add(compressionFieldNode);

        // add circuitBreaker field
        MetadataNode circuitBreakerMetadata = getMetadataNode(
                "Configurations associated with the behaviour of the Circuit Breaker");
        IdentifierToken circuitBreakerFieldName = AbstractNodeFactory.createIdentifierToken("circuitBreaker");
        TypeDescriptorNode circuitBreakerFieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:CircuitBreakerConfig"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode circuitBreakerFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                circuitBreakerMetadata, null, circuitBreakerFieldType, circuitBreakerFieldName,
                equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(circuitBreakerFieldNode);

        // add retryConfig field
        MetadataNode retryConfigMetadata = getMetadataNode("Configurations associated with retrying");
        IdentifierToken retryConfigFieldName = AbstractNodeFactory.createIdentifierToken("retryConfig");
        TypeDescriptorNode returConfigFieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:RetryConfig"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode retryConfigFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                retryConfigMetadata, null, returConfigFieldType, retryConfigFieldName,
                equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(retryConfigFieldNode);

        // add cookieConfig field
        MetadataNode cookieConfigMetadata = getMetadataNode("Configurations associated with cookies");
        IdentifierToken cookieConfigFieldName = AbstractNodeFactory.createIdentifierToken("cookieConfig");
        TypeDescriptorNode cookieConfigFieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:CookieConfig"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode cookieConfigFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                cookieConfigMetadata, null, cookieConfigFieldType, cookieConfigFieldName,
                equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(cookieConfigFieldNode);

        // add responseLimits field
        MetadataNode responseLimitsMetadata = getMetadataNode(
                "Configurations associated with inbound response size limits");
        IdentifierToken responseLimitsFieldName = createIdentifierToken("responseLimits");
        TypeDescriptorNode responseLimitsFieldType = createSimpleNameReferenceNode(
                createIdentifierToken("http:ResponseLimitConfigs"));
        RecordFieldWithDefaultValueNode responseLimitsFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                responseLimitsMetadata, null, responseLimitsFieldType, responseLimitsFieldName,
                equalToken, emptyExpression, semicolonToken);
        recordFieldNodes.add(responseLimitsFieldNode);

        // add secureSocket field
        MetadataNode secureSocketMetadata = getMetadataNode("SSL/TLS-related options");
        IdentifierToken secureSocketFieldName = AbstractNodeFactory.createIdentifierToken(SSL_FIELD_NAME);
        TypeDescriptorNode secureSocketfieldType = createOptionalTypeDescriptorNode(
                createIdentifierToken("http:ClientSecureSocket"), createToken(QUESTION_MARK_TOKEN));
        RecordFieldWithDefaultValueNode secureSocketFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                secureSocketMetadata, null, secureSocketfieldType, secureSocketFieldName,
                equalToken, nilLiteralNode, semicolonToken);
        recordFieldNodes.add(secureSocketFieldNode);

        return recordFieldNodes;
    }

    /**
     * Generate statements for init function when combination of ApiKeys and HTTP/OAuth authentication is used.
     *
     * <pre>
     *     if authConfig.auth is ApiKeysConfig {
     *         self.apiKeyConfig = (<ApiKeysConfig>authConfig.auth).cloneReadOnly();
     *     } else {
     *         clientConfig.auth = http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig>authConfig.auth;
     *         self.apiKeyConfig = ();
     *     }
     * </pre>
     *
     * @param assignmentNodes   List of Assignment Nodes
     */
    public void handleInitForMixOfApiKeyAndHTTPOrOAuth(List<StatementNode> assignmentNodes) {
        List<StatementNode> apiKeyConfigAssignmentNodes = new ArrayList<>();

        // `self.apiKeyConfig = (<ApiKeysConfig>authConfig.auth).cloneReadOnly();`
        FieldAccessExpressionNode apiKeyConfigRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(API_KEY_CONFIG_PARAM)));
        SimpleNameReferenceNode apiKeyConfigExpr = createSimpleNameReferenceNode(createIdentifierToken(
                "(<ApiKeysConfig>authConfig.auth).cloneReadOnly()"));
        AssignmentStatementNode apiKeyConfigAssignmentStatementNode = createAssignmentStatementNode(apiKeyConfigRef,
                createToken(EQUAL_TOKEN), apiKeyConfigExpr, createToken(SEMICOLON_TOKEN));
        apiKeyConfigAssignmentNodes.add(apiKeyConfigAssignmentStatementNode);
        NodeList<StatementNode> statementList = createNodeList(apiKeyConfigAssignmentNodes);
        BlockStatementNode ifBody = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN), statementList,
                createToken(CLOSE_BRACE_TOKEN));

        List<StatementNode> clientConfigAssignmentNodes = new ArrayList<>();

        // `clientConfig.auth = <http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig>authConfig.auth;
        FieldAccessExpressionNode clientConfigAuthRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(CONFIG_RECORD_ARG)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(AUTH)));
        SimpleNameReferenceNode clientConfigExpr = createSimpleNameReferenceNode(
                createIdentifierToken("<" + getAuthFieldTypeName() +
                        ">" + AUTH_CONFIG + DOT_TOKEN.stringValue() + AUTH));
        AssignmentStatementNode httpClientAuthConfigAssignment = createAssignmentStatementNode(clientConfigAuthRef,
                createToken(EQUAL_TOKEN), clientConfigExpr, createToken(SEMICOLON_TOKEN));
        clientConfigAssignmentNodes.add(httpClientAuthConfigAssignment);

        // `self.apiKeyConfig = ();`
        FieldAccessExpressionNode apiKeyConfigNilRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(API_KEY_CONFIG_PARAM)));
        SimpleNameReferenceNode apiKeyConfigNilExpr = createSimpleNameReferenceNode(
                createIdentifierToken("()"));
        AssignmentStatementNode apiKeyConfigNilAssignment = createAssignmentStatementNode(apiKeyConfigNilRef,
                createToken(EQUAL_TOKEN), apiKeyConfigNilExpr, createToken(SEMICOLON_TOKEN));
        clientConfigAssignmentNodes.add(apiKeyConfigNilAssignment);

        NodeList<StatementNode> elseBodyNodeList = createNodeList(clientConfigAssignmentNodes);
        StatementNode elseBodyStatement = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN), elseBodyNodeList,
                createToken(CLOSE_BRACE_TOKEN));
        ElseBlockNode elseBody = createElseBlockNode(createToken(ELSE_KEYWORD), elseBodyStatement);

        ExpressionNode condition = createBinaryExpressionNode(null,
                createIdentifierToken(AUTH_CONFIG + DOT_TOKEN.stringValue() + AUTH),
                createToken(IS_KEYWORD),
                createIdentifierToken(API_KEYS_CONFIG)
        );
        IfElseStatementNode ifElseStatementNode = createIfElseStatementNode(createToken(IF_KEYWORD), condition,
                ifBody, elseBody);
        assignmentNodes.add(ifElseStatementNode);
    }

    /**
     * Generate fields for`AuthConfig` record when combination of ApiKey and HTTPOrOAuth authentication is supported.
     *
     * <pre>
     *     # Auth Configuration
     *     http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig|ApiKeysConfig auth;
     * </pre>
     *
     * @return {@link List<Node>}   fields of AuthConfig record
     */
    private List<Node> getAuthConfigRecordFields() {
        List<Node> recordFieldNodes = new ArrayList<>();
        Token semicolonToken = createToken(SEMICOLON_TOKEN);

        MetadataNode authMetadataNode = getMetadataNode("Auth Configuration");
        IdentifierToken authFieldName = AbstractNodeFactory.createIdentifierToken(escapeIdentifier(AUTH));
        TypeDescriptorNode authFieldTypeNode = createSimpleNameReferenceNode(
                createIdentifierToken(getAuthFieldTypeName() + "|" + API_KEYS_CONFIG));
        RecordFieldNode authFieldNode = NodeFactory.createRecordFieldNode(authMetadataNode, null,
                authFieldTypeNode, authFieldName, null, semicolonToken);
        recordFieldNodes.add(authFieldNode);
        return recordFieldNodes;
    }

    private MetadataNode getMetadataNode(String comment) {
        List<Node> docs = new ArrayList<>(DocCommentsGenerator.createAPIDescriptionDoc(comment, false));
        MarkdownDocumentationNode authDocumentationNode = createMarkdownDocumentationNode(
                createNodeList(docs));
        return createMetadataNode(authDocumentationNode, createEmptyNodeList());
    }

    /**
     * Travers through the security schemas of the given open api spec.
     * Store api key names which needs to send in the query string and as a request header separately.
     *
     * @param securitySchemeMap     Map of security schemas of the given open api spec
     */
    private void setAuthTypes(Map<String, SecurityScheme> securitySchemeMap) throws
            BallerinaOpenApiException {
        for (Map.Entry<String, SecurityScheme> securitySchemeEntry : securitySchemeMap.entrySet()) {
            SecurityScheme schemaValue = securitySchemeEntry.getValue();
            if (schemaValue != null && schemaValue.getType() != null) {
                String schemaType = schemaValue.getType().name().toLowerCase(Locale.getDefault());
                switch (schemaType) {
                    case HTTP:
                        httpOROAuth = true;
                        String scheme = schemaValue.getScheme();
                        if (scheme.equals(BASIC)) {
                            authTypes.add(BASIC);
                        } else if (scheme.equals(BEARER)) {
                            authTypes.add(BEARER);
                        }
                        break;
                    case OAUTH2:
                        httpOROAuth = true;
                        if (schemaValue.getFlows().getClientCredentials() != null) {
                            if (schemaValue.getFlows().getClientCredentials().getTokenUrl() != null) {
                                clientCredGrantTokenUrl = schemaValue.getFlows().getClientCredentials().getTokenUrl();
                            }
                            authTypes.add(CLIENT_CRED);
                        }
                        if (schemaValue.getFlows().getPassword() != null) {
                            authTypes.add(PASSWORD);
                        }
                        if (schemaValue.getFlows().getAuthorizationCode() != null) {
                            authTypes.addAll(Arrays.asList(BEARER, REFRESH_TOKEN));
                        }
                        if (schemaValue.getFlows().getImplicit() != null) {
                            authTypes.add(BEARER);
                        }
                        break;
                    case API_KEY:
                        apiKey = true;
                        String apiKeyType = schemaValue.getIn().name().toLowerCase(Locale.getDefault());
                        authTypes.add(API_KEY);
                        setApiKeysConfigRecordFields(schemaValue);
                        switch (apiKeyType) {
                            case "query":
                                queryApiKeyNameList.put(securitySchemeEntry.getKey(), schemaValue.getName());
                                break;
                            case "header":
                                headerApiKeyNameList.put(securitySchemeEntry.getKey(), schemaValue.getName());
                                break;
                            default:
                                break;
                        }
                        break;
                }
            }
        }
        if (!(apiKey || httpOROAuth)) {
            throw new BallerinaOpenApiException("Unsupported type of security schema");
        }
    }

    /**
     * Returns fields in ApiKeysConfig record.
     * <pre>
     *     # API key related to connector authentication
     *     string apiKey;
     * </pre>
     *
     */
    private void setApiKeysConfigRecordFields(SecurityScheme securityScheme) {
        MetadataNode metadataNode = null;
        if (securityScheme.getDescription() != null) {
            metadataNode = getMetadataNode(securityScheme.getDescription());
        }
        TypeDescriptorNode stringTypeDesc = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        IdentifierToken apiKeyName = createIdentifierToken(getValidName(securityScheme.getName(), false));
        apiKeysConfigRecordFields.add(createRecordFieldNode(metadataNode, null, stringTypeDesc,
                apiKeyName, null, createToken(SEMICOLON_TOKEN)));
    }

    /**
     * Travers through the authTypes and generate the field type name of auth field in ClientConfig record.
     *
     * @return {@link String}   Field type name of auth field
     *                          Ex: {@code http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig}
     */
    public String getAuthFieldTypeName() {
        Set<String> httpFieldTypeNames = new HashSet<>();
        for (String authType : authTypes) {
            switch (authType) {
                case BEARER:
                    httpFieldTypeNames.add(AuthConfigTypes.BEARER.getValue());
                    break;
                case BASIC:
                    httpFieldTypeNames.add(AuthConfigTypes.BASIC.getValue());
                    break;
                case CLIENT_CRED:
                    if (clientCredGrantTokenUrl != null) {
                        httpFieldTypeNames.add(AuthConfigTypes.CUSTOM_CLIENT_CREDENTIAL.getValue());
                    } else {
                        httpFieldTypeNames.add(AuthConfigTypes.CLIENT_CREDENTIAL.getValue());
                    }
                    break;
                case PASSWORD:
                    httpFieldTypeNames.add(AuthConfigTypes.PASSWORD.getValue());
                    break;
                case REFRESH_TOKEN:
                    httpFieldTypeNames.add(AuthConfigTypes.REFRESH_TOKEN.getValue());
                    break;
                default:
                    break;
            }
        }
        return buildConfigRecordFieldTypes(httpFieldTypeNames).toString();
    }

    /**
     * This method is used concat the config record authConfig field type.
     *
     * @param fieldtypes        Type name set from {@link #setAuthTypes(Map)} method.
     * @return {@link String}   Pipe concatenated list of type names
     */
    private StringBuilder buildConfigRecordFieldTypes(Set<String> fieldtypes) {
        StringBuilder httpAuthFieldTypes = new StringBuilder();
        if (!fieldtypes.isEmpty()) {
            for (String fieldType : fieldtypes) {
                if (httpAuthFieldTypes.length() != 0) {
                    httpAuthFieldTypes.append("|").append(fieldType);
                } else {
                    httpAuthFieldTypes.append(fieldType);
                }
            }
        }
        return httpAuthFieldTypes;
    }
}
