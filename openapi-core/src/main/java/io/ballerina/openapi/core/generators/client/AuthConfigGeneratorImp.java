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
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingFieldNode;
import io.ballerina.compiler.syntax.tree.MarkdownDocumentationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.ParameterNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RecordFieldNode;
import io.ballerina.compiler.syntax.tree.RecordFieldWithDefaultValueNode;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.RequiredExpressionNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SpecificFieldNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.openapi.core.generators.client.diagnostic.ClientDiagnostic;
import io.ballerina.openapi.core.generators.client.exception.ClientException;
import io.ballerina.openapi.core.generators.common.GeneratorConstants;
import io.ballerina.openapi.core.generators.common.GeneratorUtils;
import io.ballerina.openapi.core.generators.common.exception.BallerinaOpenApiException;
import io.ballerina.openapi.core.generators.document.DocCommentsGeneratorUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyMinutiaeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createLiteralValueToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createAnnotationNode;
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
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSpecificFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceTypeDescNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypedBindingPatternNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createUnionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createVariableDeclarationNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ASTERISK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BITWISE_AND_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CHECK_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_PAREN_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
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
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PIPE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.QUESTION_MARK_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.READONLY_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.RECORD_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.STRING_LITERAL;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TRUE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.TYPE_KEYWORD;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.API_KEY;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.API_KEYS_CONFIG;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.API_KEY_CONFIG_PARAM;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.AUTH;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.BASIC;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.BEARER;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.BOOLEAN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.CLIENT_CRED;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.CONFIG;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.CONNECTION_CONFIG;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.DEFAULT_HTTP_VERSION;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HTTP;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HTTP_CLIENT_CONFIG;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HTTP_VERIONS_EXT;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.HTTP_VERSION;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.OAUTH2;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.PASSWORD;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.REFRESH_TOKEN;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.SELF;
import static io.ballerina.openapi.core.generators.common.GeneratorConstants.X_BALLERINA_HTTP_CONFIGURATIONS;
import static io.ballerina.openapi.core.generators.common.GeneratorUtils.escapeIdentifier;
import static io.ballerina.openapi.core.generators.type.GeneratorConstants.HTTP_VERSION_MAP;

/**
 * This class is used to generate authentication related nodes of the ballerina connector client syntax tree.
 *
 * @since 1.3.0
 */
public class AuthConfigGeneratorImp {

    private final Map<String, ApiKeyNamePair> headerApiKeyNameMap = new HashMap<>();
    private final Map<String, ApiKeyNamePair> queryApiKeyNameMap = new HashMap<>();
    private final List<Node> apiKeysConfigRecordFields = new ArrayList<>();
    private boolean apiKey;
    private boolean httpOROAuth;
    private String clientCredGrantTokenUrl;
    private String passwordGrantTokenUrl;
    private String refreshTokenUrl;
    private String httpVersion = HTTP_VERSION_MAP.get(DEFAULT_HTTP_VERSION);
    private final Set<String> authTypes = new LinkedHashSet<>();

    private List<TypeDefinitionNode> authRelatedTypeDefinitionNodes = new ArrayList<>();
    private List<ClientDiagnostic> diagnostics = new ArrayList<>();

    public record ApiKeyNamePair(String actualName, String displayName) {
    }

    public AuthConfigGeneratorImp(boolean isAPIKey, boolean isHttpOROAuth) {
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
     * @return API key name list
     */
    public Map<String, ApiKeyNamePair> getQueryApiKeyNameMap() {
        return queryApiKeyNameMap;
    }

    /**
     * Returns API key names which need to send as request headers.
     *
     * @return API key name list
     */
    public Map<String, ApiKeyNamePair> getHeaderApiKeyNameMap() {
        return headerApiKeyNameMap;
    }

    /**
     * Returns auth type to generate test file.
     *
     * @return {@link Set<String>}
     */
    public Set<String> getAuthType() {

        return authTypes;
    }

    public List<ClientDiagnostic> getDiagnostics() {
        return diagnostics;
    }
    public List<TypeDefinitionNode> getAuthRelatedTypeDefinitionNodes() {
        return authRelatedTypeDefinitionNodes;
    }

    /**
     * Add authentication related records.
     *
     * @param openAPI OpenAPI object received from swagger open-api parser
     * @throws BallerinaOpenApiException When function fails
     */
    public void addAuthRelatedRecords(OpenAPI openAPI) throws ClientException {
        List<TypeDefinitionNode> nodes = new ArrayList<>();
        if (openAPI.getComponents() != null) {
            // set auth types
            if (openAPI.getComponents().getSecuritySchemes() != null) {
                Map<String, SecurityScheme> securitySchemeMap = openAPI.getComponents().getSecuritySchemes();
                setAuthTypes(securitySchemeMap, openAPI);
            }

            // TODO: Handle the scenario when invalid http version is given.
            //  No diagnostics available at this level to send a warning.
            //  Currently ignore the values and gen with default.
            if (openAPI.getExtensions() != null &&
                    openAPI.getExtensions().containsKey(X_BALLERINA_HTTP_CONFIGURATIONS)) {
                LinkedHashMap<String, String> extFields =
                        (LinkedHashMap<String, String>) openAPI.getExtensions().get(X_BALLERINA_HTTP_CONFIGURATIONS);
                if (extFields.containsKey(HTTP_VERIONS_EXT)) {
                    String httpVersion = extFields.get(HTTP_VERIONS_EXT);
                    if (httpVersion != null && HTTP_VERSION_MAP.containsKey(httpVersion)) {
                        this.httpVersion = HTTP_VERSION_MAP.get(httpVersion);
                    }
                }
            }

            // generate related records
            TypeDefinitionNode connectionConfigRecord = generateConnectionConfigRecord();
            nodes.addAll(Arrays.asList(connectionConfigRecord));

            if (isApiKey()) {
                nodes.add(generateApiKeysConfig());
            }

            // Add custom `OAuth2ClientCredentialsGrantConfig` record with default tokenUrl if `tokenUrl` is available
            if (clientCredGrantTokenUrl != null) {
                nodes.add(getOAuth2ClientCredsGrantConfigRecord());
            }

            // Add custom `OAuth2PasswordGrantConfig` record with default tokenUrl if `tokenUrl` is available
            if (passwordGrantTokenUrl != null) {
                nodes.add(getOAuth2PasswordGrantConfigRecord());
            }

            // Add custom `OAuth2RefreshTokenGrantConfig` record with default refreshUrl if `refreshUrl` is available
            if (refreshTokenUrl != null) {
                nodes.add(getOAuth2RefreshTokenGrantConfigRecord());
            }
        }
        this.authRelatedTypeDefinitionNodes = nodes;
    }

    /**
     * Generate the Config record for the relevant authentication type.
     * -- ex: Config record for Http and OAuth 2.0 Authentication mechanisms.
     * <pre>
     * # Provides a set of configurations for controlling the behaviours when communicating with a remote HTTP endpoint.
     * @display {label: "Connection Config"}
     * public type ConnectionConfig record {|
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
     *          # Proxy server related options
     *          ProxyConfig? proxy = ();
     *          # Enables the inbound payload validation functionality which provided by the constraint package.
     *          # Enabled by default
     *          boolean validation = true;
     *          # Enables relaxed data binding on the client side. When enabled, `nil` values are treated as optional,
     *          # and absent fields are handled as `nilable` types. Enabled by default.
     *          boolean laxDataBinding = true;
     * |};
     * </pre>
     * Scenario 1 : For openapi contracts with no authentication mechanism given, auth field will not be generated
     * Scenario 2 : For openapi contracts with authentication mechanism, auth field in relevant types will be generated
     * Scenario 3 : For openapi contracts with only apikey authentication mechanism, auth field will not be generated
     * Scenario 4 : For openapi contracts with both http and apikey authentication mechanisms given,
     *              auth field in relevant types will be generated
     * @return {@link TypeDefinitionNode}   Syntax tree node of config record
     */
    public TypeDefinitionNode generateConnectionConfigRecord() {

        AnnotationNode annotationNode = getDisplayAnnotationForRecord("Connection Config");
        MetadataNode configRecordMetadataNode = getMetadataNode(
                "Provides a set of configurations for controlling the behaviours when communicating " +
                        "with a remote HTTP endpoint.", Collections.singletonList(annotationNode));
        Token typeName = AbstractNodeFactory.createIdentifierToken(CONNECTION_CONFIG);
        NodeList<Node> recordFieldList = createNodeList(getClientConfigRecordFields());

        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                        createToken(OPEN_BRACE_PIPE_TOKEN), recordFieldList, null,
                        createToken(CLOSE_BRACE_PIPE_TOKEN));
        TypeDefinitionNode node = NodeFactory.createTypeDefinitionNode(configRecordMetadataNode,
                createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD), typeName,
                recordTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
        return node;

    }

    /**
     * Generate the ApiKeysConfig record when the api-key auth type is given.
     * <pre>
     *  # Provides API key configurations needed when communicating with a remote HTTP endpoint.
     *  public type ApiKeysConfig record {|
     *     # Represents API Key `Authorization`
     *     @display {label: "", kind: "password"}
     *     string authorization;
     *     # Represents API Key `apikey`
     *     @display {label: "", kind: "password"}
     *     string apikey;
     *  |};
     * </pre>
     *
     * @return {@link TypeDefinitionNode}   Syntax tree node of config record
     */
    public TypeDefinitionNode generateApiKeysConfig() {
        MetadataNode configRecordMetadataNode = getMetadataNode(
                "Provides API key configurations needed when communicating " +
                        "with a remote HTTP endpoint.");
        Token typeName = AbstractNodeFactory.createIdentifierToken(API_KEYS_CONFIG);
        NodeList<Node> recordFieldList = createNodeList(apiKeysConfigRecordFields);
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
     *      # OAuth2 Client Credentials Grant Configs
     *      public type OAuth2ClientCredentialsGrantConfig record {|
     *          *http:OAuth2ClientCredentialsGrantConfig;
     *          # Token URL
     *          string tokenUrl = "https://zoom.us/oauth/token";
     *      |};
     * </pre>
     *
     * @return {@link TypeDefinitionNode}   Custom `OAuth2ClientCredentialsGrantConfig` record with default tokenUrl
     */
    private TypeDefinitionNode getOAuth2ClientCredsGrantConfigRecord() {
        Token typeName = AbstractNodeFactory.createIdentifierToken(GeneratorConstants
                .AuthConfigTypes.CUSTOM_CLIENT_CREDENTIAL.getValue());
        NodeList<Node> recordFieldList = createNodeList(getClientCredsGrantConfigFields());
        MetadataNode configRecordMetadataNode = getMetadataNode("OAuth2 Client Credentials Grant Configs");
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
                createToken(ASTERISK_TOKEN),
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
     * Create `OAuth2PasswordGrantConfig` record with default tokenUrl.
     *
     * <pre>
     *      # OAuth2 Password Grant Configs
     *      public type OAuth2PasswordGrantConfig record {|
     *          *http:OAuth2PasswordGrantConfig;
     *          # Token URL
     *          string tokenUrl = "https://zoom.us/oauth/token";
     *      |};
     * </pre>
     *
     * @return {@link TypeDefinitionNode}   Custom `OAuth2PasswordGrantConfig` record with default tokenUrl
     */
    private TypeDefinitionNode getOAuth2PasswordGrantConfigRecord() {
        Token typeName = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.AuthConfigTypes
                .CUSTOM_PASSWORD.getValue());
        NodeList<Node> recordFieldList = createNodeList(getPasswordGrantConfigFields());
        MetadataNode configRecordMetadataNode = getMetadataNode("OAuth2 Password Grant Configs");
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                        createToken(OPEN_BRACE_PIPE_TOKEN), recordFieldList, null,
                        createToken(CLOSE_BRACE_PIPE_TOKEN));
        return NodeFactory.createTypeDefinitionNode(configRecordMetadataNode,
                createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD), typeName,
                recordTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates fields of `OAuth2PasswordGrantConfig` record with default tokenUrl.
     *
     * <pre>
     *      *http:OAuth2PasswordGrantConfig;
     *      # Token URL
     *      string tokenUrl = "https://zoom.us/oauth/token";
     * </pre>
     *
     * @return {@link List<Node>}
     */
    private List<Node> getPasswordGrantConfigFields() {

        List<Node> recordFieldNodes = new ArrayList<>();
        Token semicolonToken = createToken(SEMICOLON_TOKEN);
        Token equalToken = createToken(EQUAL_TOKEN);

        recordFieldNodes.add(createIncludedRecordParameterNode(createEmptyNodeList(),
                createToken(ASTERISK_TOKEN),
                createIdentifierToken("http:OAuth2PasswordGrantConfig;"), null));

        MetadataNode metadataNode = getMetadataNode("Token URL");
        TypeDescriptorNode stringType = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        IdentifierToken fieldNameTokenUrl = createIdentifierToken("tokenUrl");
        ExpressionNode defaultValue = createRequiredExpressionNode(createIdentifierToken("\"" +
                passwordGrantTokenUrl + "\""));
        RecordFieldWithDefaultValueNode fieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, stringType, fieldNameTokenUrl, equalToken, defaultValue,
                semicolonToken);
        recordFieldNodes.add(fieldNode);
        return recordFieldNodes;
    }

    /**
     * Create `OAuth2RefreshTokenGrantConfig` record with default refreshUrl.
     *
     * <pre>
     *      # OAuth2 Refresh Token Grant Configs
     *      public type OAuth2RefreshTokenGrantConfig record {|
     *          *http:OAuth2RefreshTokenGrantConfig;
     *          # Refresh URL
     *          string refreshUrl = "https://zoom.us/oauth/token";
     *      |};
     * </pre>
     *
     * @return {@link TypeDefinitionNode}   Custom `OAuth2RefreshTokenGrantConfig` record with default refreshUrl
     */
    private TypeDefinitionNode getOAuth2RefreshTokenGrantConfigRecord() {
        Token typeName = AbstractNodeFactory.createIdentifierToken(GeneratorConstants.AuthConfigTypes
                .CUSTOM_REFRESH_TOKEN.getValue());
        NodeList<Node> recordFieldList = createNodeList(getRefreshTokenGrantConfigFields());
        MetadataNode configRecordMetadataNode = getMetadataNode("OAuth2 Refresh Token Grant Configs");
        RecordTypeDescriptorNode recordTypeDescriptorNode =
                NodeFactory.createRecordTypeDescriptorNode(createToken(RECORD_KEYWORD),
                        createToken(OPEN_BRACE_PIPE_TOKEN), recordFieldList, null,
                        createToken(CLOSE_BRACE_PIPE_TOKEN));
        return NodeFactory.createTypeDefinitionNode(configRecordMetadataNode,
                createToken(PUBLIC_KEYWORD), createToken(TYPE_KEYWORD), typeName,
                recordTypeDescriptorNode, createToken(SEMICOLON_TOKEN));
    }

    /**
     * Generates fields of `OAuth2RefreshTokenGrantConfig` record with default refreshUrl.
     *
     * <pre>
     *      *http:OAuth2RefreshTokenGrantConfig;
     *      # Refresh URL
     *      string refreshUrl = "https://zoom.us/oauth/token";
     * </pre>
     *
     * @return {@link List<Node>}
     */
    private List<Node> getRefreshTokenGrantConfigFields() {

        List<Node> recordFieldNodes = new ArrayList<>();
        Token semicolonToken = createToken(SEMICOLON_TOKEN);
        Token equalToken = createToken(EQUAL_TOKEN);

        recordFieldNodes.add(createIncludedRecordParameterNode(createEmptyNodeList(),
                createToken(ASTERISK_TOKEN),
                createIdentifierToken("http:OAuth2RefreshTokenGrantConfig;"), null));

        MetadataNode metadataNode = getMetadataNode("Refresh URL");
        TypeDescriptorNode stringType = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        IdentifierToken fieldNameTokenUrl = createIdentifierToken("refreshUrl");
        ExpressionNode defaultValue = createRequiredExpressionNode(createIdentifierToken("\"" +
                refreshTokenUrl + "\""));
        RecordFieldWithDefaultValueNode fieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                metadataNode, null, stringType, fieldNameTokenUrl, equalToken, defaultValue,
                semicolonToken);
        recordFieldNodes.add(fieldNode);
        return recordFieldNodes;
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
     * {@code ClientConfig clientConfig, string serviceUrl = "https://petstore.swagger.io:443/v2" }
     * -- ex: Config param for API Key Authentication mechanism.
     * {@code ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig = {},
     * string serviceUrl = "https://petstore.swagger.io:443/v2" }
     * Config param for API Key Authentication mechanism with no server URL given
     * {@code ApiKeysConfig apiKeyConfig, string serviceUrl; http:ClientConfiguration clientConfig = {}}
     * -- ex: Config param when no authentication mechanism given.
     * {@code http:ClientConfiguration clientConfig = {},
     * string serviceUrl = "https://petstore.swagger.io:443/v2" }
     * Config param when no authentication mechanism given with no server URL
     * {@code string serviceUrl, http:ClientConfiguration clientConfig = {}}
     * -- ex: Config param for combination of API Key and Http or OAuth 2.0 Authentication mechanisms.
     * {@code AuthConfig authConfig,ConnectionConfig config =  {},
     * string serviceUrl = "https://petstore.swagger.io:443/v2" }
     *
     * @return {@link List<Node>}  syntax tree node list of config parameters
     */
    public List<ParameterNode> getConfigParamForClassInit() {

        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        List<ParameterNode> parameters = new ArrayList<>();
        IdentifierToken equalToken = createIdentifierToken(GeneratorConstants.EQUAL);
        if (httpOROAuth) {
            BuiltinSimpleNameReferenceNode typeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(CONNECTION_CONFIG));
            IdentifierToken paramName = createIdentifierToken(CONFIG);
            RequiredParameterNode authConfig = createRequiredParameterNode(annotationNodes, typeName, paramName);
            parameters.add(authConfig);
//            parameters.add(createToken(COMMA_TOKEN));
//            parameters.add(serviceURLNode);
        } else {
            if (apiKey) {
                BuiltinSimpleNameReferenceNode apiKeyConfigTypeName = createBuiltinSimpleNameReferenceNode(null,
                        createIdentifierToken(API_KEYS_CONFIG));
                IdentifierToken apiKeyConfigParamName = createIdentifierToken(API_KEY_CONFIG_PARAM);
                RequiredParameterNode apiKeyConfigParamNode = createRequiredParameterNode(annotationNodes,
                        apiKeyConfigTypeName, apiKeyConfigParamName);
                parameters.add(apiKeyConfigParamNode);
//                parameters.add(createToken(COMMA_TOKEN));
            }

            BuiltinSimpleNameReferenceNode httpClientConfigTypeName = createBuiltinSimpleNameReferenceNode(null,
                    createIdentifierToken(CONNECTION_CONFIG));
            IdentifierToken httpClientConfig = createIdentifierToken(CONFIG);
            BasicLiteralNode emptyexpression = createBasicLiteralNode(null, createIdentifierToken(" {}"));
            DefaultableParameterNode defaultConnectionConfig = createDefaultableParameterNode(annotationNodes,
                    httpClientConfigTypeName,
                    httpClientConfig, equalToken, emptyexpression);
            parameters.add(defaultConnectionConfig);
//            if (serviceURLNode instanceof RequiredParameterNode) {
//                parameters.add(serviceURLNode);
//                parameters.add(createToken(COMMA_TOKEN));
//                parameters.add(defaultConnectionConfig);
//            } else {
//                parameters.add(defaultConnectionConfig);
//                parameters.add(createToken(COMMA_TOKEN));
//                parameters.add(serviceURLNode);
//            }
        }
        return parameters;
    }

    /**
     * Generate `httpClientConfig` variable.
     * <pre>
     * http:ClientConfiguration httpClientConfig = {
     *     httpVersion: config.httpVersion,
     *     http1Settings: config.http1Settings,
     *     http2Settings: config.http2Settings,
     *     timeout: config.timeout,
     *     forwarded: config.forwarded,
     *     followRedirects: config.followRedirects,
     *     poolConfig: config.poolConfig,
     *     cache: config.cache,
     *     compression: config.compression,
     *     circuitBreaker: config.circuitBreaker,
     *     retryConfig: config.retryConfig,
     *     cookieConfig: config.cookieConfig,
     *     responseLimits: config.responseLimits,
     *     secureSocket: config.secureSocket,
     *     proxy: config.proxy,
     *     socketConfig: config.socketConfig,
     *     validation: config.validation,
     *     laxDataBinding: config.laxDataBinding
     * };
     * </pre>
     *
     * @return
     */
    public VariableDeclarationNode getHttpClientConfigVariableNode() {
        Token comma = createToken(COMMA_TOKEN);
        NodeList<AnnotationNode> annotationNodes = createEmptyNodeList();
        // http:ClientConfiguration variable declaration
        SimpleNameReferenceNode typeBindingPattern = createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.HTTP_CLIENT_CONFIGURATION));
        CaptureBindingPatternNode bindingPattern = createCaptureBindingPatternNode(
                createIdentifierToken(HTTP_CLIENT_CONFIG));
        TypedBindingPatternNode typedBindingPatternNode = createTypedBindingPatternNode(typeBindingPattern,
                bindingPattern);

        List<Node> argumentsList = new ArrayList<>();

        if (isHttpOROAuth() && !isApiKey()) {
            SpecificFieldNode authField = createClientConfigFieldAssignmentNode(AUTH);
            argumentsList.add(authField);
            argumentsList.add(comma);
        }

        GeneratorConstants.CLIENT_CONFIG_FIELD_LIST.forEach(configField -> {
            SpecificFieldNode field = createClientConfigFieldAssignmentNode(configField);
            argumentsList.add(field);
            argumentsList.add(comma);
        });

        // Remove the last comma
        if (!argumentsList.isEmpty()) {
            argumentsList.removeLast();
        }

        SeparatedNodeList<MappingFieldNode> arguments = createSeparatedNodeList(argumentsList);
        MappingConstructorExpressionNode mappingConstructorExpressionNode =
                createMappingConstructorExpressionNode(createToken(OPEN_BRACE_TOKEN),
                        arguments, createToken(CLOSE_BRACE_TOKEN));
        return createVariableDeclarationNode(annotationNodes, null, typedBindingPatternNode,
                createToken(EQUAL_TOKEN), mappingConstructorExpressionNode, createToken(SEMICOLON_TOKEN));
    }

    private static SpecificFieldNode createClientConfigFieldAssignmentNode(String fieldName) {
        ExpressionNode fieldValueExpr = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(CONFIG)),
                createToken(DOT_TOKEN), createSimpleNameReferenceNode(createIdentifierToken(fieldName)));
        return createSpecificFieldNode(null,
                createIdentifierToken(fieldName), createToken(COLON_TOKEN), fieldValueExpr);
    }

    /**
     * Generate http:client initialization node.
     * <pre>
     *     self.clientEp = check new (serviceUrl, clientConfig);
     * </pre>
     *
     * @return {@link AssignmentStatementNode}   Synatx tree node of client initialization
     */
    public AssignmentStatementNode getClientInitializationNode() {

        FieldAccessExpressionNode selfClientExp = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken("clientEp")));

        // Expression node
        List<Node> argumentsList = new ArrayList<>();
        PositionalArgumentNode positionalArgumentNode01 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.SERVICE_URL)));
        argumentsList.add(positionalArgumentNode01);
        Token comma1 = createIdentifierToken(",");

        PositionalArgumentNode positionalArgumentNode02 = createPositionalArgumentNode(createSimpleNameReferenceNode(
                createIdentifierToken(HTTP_CLIENT_CONFIG)));
        argumentsList.add(comma1);
        argumentsList.add(positionalArgumentNode02);

        SeparatedNodeList<FunctionArgumentNode> arguments = createSeparatedNodeList(argumentsList);
        Token closeParenArg = createToken(CLOSE_PAREN_TOKEN);
        ParenthesizedArgList parenthesizedArgList = createParenthesizedArgList(createToken(OPEN_PAREN_TOKEN),
                arguments, closeParenArg);
        ImplicitNewExpressionNode expressionNode = createImplicitNewExpressionNode(createToken(NEW_KEYWORD),
                parenthesizedArgList);
        CheckExpressionNode initializer = createCheckExpressionNode(null, createToken(CHECK_KEYWORD),
                expressionNode);
        return createAssignmentStatementNode(selfClientExp, createToken(EQUAL_TOKEN), initializer,
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
     *     http:HttpVersion httpVersion = http:HTTP_1_1;
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
     *     # Proxy server related options
     *     ProxyConfig? proxy = ();
     *     # Enables the inbound payload validation functionality which provided by the constraint package.
     *     Enabled by default
     *     boolean validation = true;
     *     # Enables relaxed data binding on the client side. When enabled, `nil` values are treated as optional,
     *     # and absent fields are handled as `nilable` types. Enabled by default.
     *     boolean laxDataBinding = true;
     * </pre>
     *
     * @return {@link List<Node>}   ClientConfig record fields' node list
     */
    private List<Node> getClientConfigRecordFields() {

        List<Node> recordFieldNodes = new ArrayList<>();
        Token semicolonToken = createToken(SEMICOLON_TOKEN);
        Token equalToken = createToken(EQUAL_TOKEN);
        Token questionMarkToken = createToken(QUESTION_MARK_TOKEN);

        // add auth field
        if (isHttpOROAuth() && !isApiKey()) {
            MetadataNode authMetadataNode = getMetadataNode("Configurations related to client authentication");
            IdentifierToken authFieldName = AbstractNodeFactory.createIdentifierToken(escapeIdentifier(
                    AUTH));
            TypeDescriptorNode authFieldTypeNode =
                    createSimpleNameReferenceNode(createIdentifierToken(getAuthFieldTypeName()));
            RecordFieldNode authFieldNode = NodeFactory.createRecordFieldNode(authMetadataNode, null,
                    authFieldTypeNode, authFieldName, null, semicolonToken);
            recordFieldNodes.add(authFieldNode);
        } else if (isHttpOROAuth() && isApiKey()) {
            MetadataNode authMetadataNode = getMetadataNode(
                    "Provides Auth configurations needed when communicating with a remote HTTP endpoint.");
            IdentifierToken authFieldName = AbstractNodeFactory.createIdentifierToken(escapeIdentifier(
                    AUTH));
            TypeDescriptorNode unionTypeDesctiptor = createUnionTypeDescriptorNode(
                    createSimpleNameReferenceNode(createIdentifierToken(getAuthFieldTypeName())),
                    createToken(PIPE_TOKEN),
                    createSimpleNameReferenceNode(createIdentifierToken(API_KEYS_CONFIG)));
            RecordFieldNode authFieldNode = NodeFactory.createRecordFieldNode(authMetadataNode, null,
                    unionTypeDesctiptor, authFieldName, null, semicolonToken);
            recordFieldNodes.add(authFieldNode);
        }

        // add httpVersion field
        MetadataNode httpVersionMetadata = getMetadataNode("The HTTP version understood by the client");
        TypeDescriptorNode httpVersionFieldType = createSimpleNameReferenceNode(createIdentifierToken(HTTP_VERSION));
        IdentifierToken httpVersionFieldName = createIdentifierToken(GeneratorConstants.HTTP_VERSION_FIELD);
        RequiredExpressionNode httpVersionExpression =
                createRequiredExpressionNode(createIdentifierToken(this.httpVersion));
        RecordFieldWithDefaultValueNode httpVersionFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                httpVersionMetadata, null, httpVersionFieldType, httpVersionFieldName,
                equalToken, httpVersionExpression, semicolonToken);
        recordFieldNodes.add(httpVersionFieldNode);

        // add http1Settings field
        MetadataNode http1SettingsMetadata = getMetadataNode("Configurations related to HTTP/1.x protocol");
        IdentifierToken http1SettingsFieldName = createIdentifierToken(GeneratorConstants.HTTP_1_SETTINGS_FIELD);
        TypeDescriptorNode http1SettingsFieldType =
                createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.HTTP_CLIENT_HTTP_1_SETTINGS));
        RecordFieldWithDefaultValueNode http1SettingsFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                http1SettingsMetadata, null, http1SettingsFieldType, http1SettingsFieldName,
                equalToken, createRequiredExpressionNode(createIdentifierToken(GeneratorConstants.DEFAULT_RECORD)),
                semicolonToken);
        recordFieldNodes.add(http1SettingsFieldNode);

        // add http2Settings fields
        MetadataNode http2SettingsMetadata = getMetadataNode("Configurations related to HTTP/2 protocol");
        TypeDescriptorNode http2SettingsFieldType =
                createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.HTTP_CLIENT_HTTP_2_SETTINGS));
        IdentifierToken http2SettingsFieldName = createIdentifierToken(GeneratorConstants.HTTP_2_SETTINGS_FIELD);
        RecordFieldWithDefaultValueNode http2SettingsFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                http2SettingsMetadata, null, http2SettingsFieldType, http2SettingsFieldName,
                equalToken, createRequiredExpressionNode(createIdentifierToken(GeneratorConstants.DEFAULT_RECORD)),
                semicolonToken);
        recordFieldNodes.add(http2SettingsFieldNode);

        // add timeout field
        MetadataNode timeoutMetadata = getMetadataNode(
                "The maximum time to wait (in seconds) for a response before closing the connection");
        IdentifierToken timeoutFieldName = createIdentifierToken(GeneratorConstants.TIMEOUT_FIELD);
        TypeDescriptorNode timeoutFieldType = createSimpleNameReferenceNode(createToken(DECIMAL_KEYWORD));
        ExpressionNode decimalLiteralNode = createRequiredExpressionNode(createIdentifierToken(
                GeneratorConstants.DEFAULT_TIMEOUT));
        RecordFieldWithDefaultValueNode timeoutFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                timeoutMetadata, null, timeoutFieldType, timeoutFieldName,
                equalToken, decimalLiteralNode, semicolonToken);
        recordFieldNodes.add(timeoutFieldNode);

        // add forwarded field
        MetadataNode forwardedMetadata = getMetadataNode(
                "The choice of setting `forwarded`/`x-forwarded` header");
        IdentifierToken forwardedFieldName = createIdentifierToken(GeneratorConstants.FORWARDED_FIELD);
        TypeDescriptorNode forwardedFieldType = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        ExpressionNode forwardedDefaultValue = createRequiredExpressionNode(createIdentifierToken("\"disable\""));
        RecordFieldWithDefaultValueNode forwardedFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                forwardedMetadata, null, forwardedFieldType, forwardedFieldName,
                equalToken, forwardedDefaultValue, semicolonToken);
        recordFieldNodes.add(forwardedFieldNode);

        // add followRedirects field
        MetadataNode followRedirectsMetadata = getMetadataNode(
                "Configurations associated with Redirection");
        IdentifierToken followRedirectsFieldName = createIdentifierToken(GeneratorConstants.FOLLOW_REDIRECTS_FIELD);
        TypeDescriptorNode followRedirectsFieldType = createSimpleNameReferenceNode(
                createIdentifierToken("http:FollowRedirects"));
        RecordFieldNode followRedirectsFieldNode = NodeFactory.createRecordFieldNode(
                followRedirectsMetadata, null, followRedirectsFieldType, followRedirectsFieldName,
                questionMarkToken, semicolonToken);
        recordFieldNodes.add(followRedirectsFieldNode);

        // add poolConfig field
        MetadataNode poolConfigMetaData = getMetadataNode("Configurations associated with request pooling");
        IdentifierToken poolConfigFieldName = AbstractNodeFactory.createIdentifierToken(
                GeneratorConstants.POOL_CONFIG_FIELD);
        TypeDescriptorNode poolConfigFieldType = createSimpleNameReferenceNode(
                createIdentifierToken("http:PoolConfiguration"));
        RecordFieldNode poolConfigFieldNode = NodeFactory.createRecordFieldNode(
                poolConfigMetaData, null, poolConfigFieldType, poolConfigFieldName,
                questionMarkToken, semicolonToken);
        recordFieldNodes.add(poolConfigFieldNode);

        // add cache field
        MetadataNode cachMetadata = getMetadataNode("HTTP caching related configurations");
        IdentifierToken cacheFieldName = createIdentifierToken(GeneratorConstants.CACHE_FIELD);
        TypeDescriptorNode cacheFieldType =
                createSimpleNameReferenceNode(createIdentifierToken(GeneratorConstants.HTTP_CACHE_CONFIG));
        RecordFieldWithDefaultValueNode cachFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                cachMetadata, null, cacheFieldType, cacheFieldName,
                equalToken, createRequiredExpressionNode(createIdentifierToken(GeneratorConstants.DEFAULT_RECORD)),
                semicolonToken);
        recordFieldNodes.add(cachFieldNode);

        // add compression field
        MetadataNode compressionMetadata = getMetadataNode(
                "Specifies the way of handling compression (`accept-encoding`) header");
        IdentifierToken compressionFieldName = createIdentifierToken(GeneratorConstants.COMPRESSION_FIELD);
        TypeDescriptorNode compressionFieldType = createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.HTTP_COMPRESSION));
        ExpressionNode compressionDefaultValue = createRequiredExpressionNode(
                createIdentifierToken("http:COMPRESSION_AUTO"));
        RecordFieldWithDefaultValueNode compressionFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                compressionMetadata, null, compressionFieldType, compressionFieldName,
                equalToken, compressionDefaultValue, semicolonToken);
        recordFieldNodes.add(compressionFieldNode);

        // add circuitBreaker field
        MetadataNode circuitBreakerMetadata = getMetadataNode(
                "Configurations associated with the behaviour of the Circuit Breaker");
        IdentifierToken circuitBreakerFieldName = AbstractNodeFactory.createIdentifierToken(
                GeneratorConstants.CIRCUIT_BREAKER_FIELD);
        TypeDescriptorNode circuitBreakerFieldType = createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.HTTP_CIRCUIT_BREAKER_CONFIG));
        RecordFieldNode circuitBreakerFieldNode = NodeFactory.createRecordFieldNode(
                circuitBreakerMetadata, null, circuitBreakerFieldType, circuitBreakerFieldName,
                questionMarkToken, semicolonToken);
        recordFieldNodes.add(circuitBreakerFieldNode);

        // add retryConfig field
        MetadataNode retryConfigMetadata = getMetadataNode("Configurations associated with retrying");
        IdentifierToken retryConfigFieldName = AbstractNodeFactory.createIdentifierToken(
                GeneratorConstants.RETRY_CONFIG_FIELD);
        TypeDescriptorNode retryConfigFieldType = createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.HTTP_RETRY_CONFIG));
        RecordFieldNode retryConfigFieldNode = NodeFactory.createRecordFieldNode(
                retryConfigMetadata, null, retryConfigFieldType, retryConfigFieldName,
                questionMarkToken, semicolonToken);
        recordFieldNodes.add(retryConfigFieldNode);

        // add cookieConfig field
        MetadataNode cookieConfigMetadata = getMetadataNode("Configurations associated with cookies");
        IdentifierToken cookieConfigFieldName = AbstractNodeFactory.createIdentifierToken(
                GeneratorConstants.COOKIE_CONFIG_FIELD);
        TypeDescriptorNode cookieConfigFieldType = createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.HTTP_COOKIE_CONFIG));
        RecordFieldNode cookieConfigFieldNode = NodeFactory.createRecordFieldNode(
                cookieConfigMetadata, null, cookieConfigFieldType, cookieConfigFieldName,
                questionMarkToken, semicolonToken);
        recordFieldNodes.add(cookieConfigFieldNode);

        // add responseLimits field
        MetadataNode responseLimitsMetadata = getMetadataNode(
                "Configurations associated with inbound response size limits");
        IdentifierToken responseLimitsFieldName = createIdentifierToken(GeneratorConstants.RESPONSE_LIMITS_FIELD);
        TypeDescriptorNode responseLimitsFieldType = createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.HTTP_RESPONSE_LIMIT_CONFIGS));
        RecordFieldWithDefaultValueNode responseLimitsFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                responseLimitsMetadata, null, responseLimitsFieldType, responseLimitsFieldName,
                equalToken, createRequiredExpressionNode(createIdentifierToken(GeneratorConstants.DEFAULT_RECORD)),
                semicolonToken);
        recordFieldNodes.add(responseLimitsFieldNode);

        // add secureSocket field
        MetadataNode secureSocketMetadata = getMetadataNode("SSL/TLS-related options");
        IdentifierToken secureSocketFieldName = AbstractNodeFactory.createIdentifierToken(
                GeneratorConstants.SECURE_SOCKET_FIELD);
        TypeDescriptorNode secureSocketfieldType = createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.HTTP_CLIENT_SECURE_SOCKET));
        RecordFieldNode secureSocketFieldNode = NodeFactory.createRecordFieldNode(
                secureSocketMetadata, null, secureSocketfieldType, secureSocketFieldName,
                questionMarkToken, semicolonToken);
        recordFieldNodes.add(secureSocketFieldNode);

        // add proxy server field
        MetadataNode proxyConfigMetadata = getMetadataNode("Proxy server related options");
        IdentifierToken proxyConfigFieldName = AbstractNodeFactory.createIdentifierToken(
                GeneratorConstants.PROXY_FIELD);
        TypeDescriptorNode proxyConfigFieldType = createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.HTTP_PROXY_CONFIG));
        RecordFieldNode proxyConfigFieldNode = NodeFactory.createRecordFieldNode(
                proxyConfigMetadata, null, proxyConfigFieldType, proxyConfigFieldName,
                questionMarkToken, semicolonToken);
        recordFieldNodes.add(proxyConfigFieldNode);

        // add socket config
        MetadataNode socketConfigFieldMetadata = getMetadataNode(
                "Provides settings related to client socket configuration");
        IdentifierToken socketConfigFieldName = createIdentifierToken(
                GeneratorConstants.SOCKET_CONFIG_FIELD);
        TypeDescriptorNode socketConfigFieldType = createSimpleNameReferenceNode(
                createIdentifierToken(GeneratorConstants.HTTP_CLIENT_SOCKET_CONFIG));
        RecordFieldWithDefaultValueNode socketConfigFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                socketConfigFieldMetadata, null, socketConfigFieldType, socketConfigFieldName,
                equalToken, createRequiredExpressionNode(createIdentifierToken(GeneratorConstants.DEFAULT_RECORD)),
                semicolonToken);
        recordFieldNodes.add(socketConfigFieldNode);

        // add validation for constraint
        MetadataNode validationMetadata = getMetadataNode("Enables the inbound payload validation " +
                "functionality which provided by the constraint package. Enabled by default");
        IdentifierToken validationFieldName = AbstractNodeFactory.createIdentifierToken(
                GeneratorConstants.VALIDATION_FIELD);
        TypeDescriptorNode validationFieldType = createSimpleNameReferenceNode(createIdentifierToken(BOOLEAN));
        RecordFieldWithDefaultValueNode validateFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                validationMetadata, null, validationFieldType, validationFieldName,
                equalToken, createRequiredExpressionNode(createToken(TRUE_KEYWORD)), semicolonToken);
        recordFieldNodes.add(validateFieldNode);

        // add laxBinding for data binding
        String apiComment = "Enables relaxed data binding on the client side. When enabled, `nil` values are treated " +
                "as optional, \nand absent fields are handled as `nilable` types. Enabled by default.";
        MetadataNode laxDataBindingMetadata = getMetadataNode(apiComment);
        IdentifierToken laxDataBindingFieldName = AbstractNodeFactory.createIdentifierToken(
                GeneratorConstants.LAX_DATA_BINDING_FIELD);
        TypeDescriptorNode laxDataBindingFieldType = createSimpleNameReferenceNode(createIdentifierToken(BOOLEAN));
        RecordFieldWithDefaultValueNode laxDataBindingFieldNode = NodeFactory.createRecordFieldWithDefaultValueNode(
                laxDataBindingMetadata, null, laxDataBindingFieldType, laxDataBindingFieldName,
                equalToken, createRequiredExpressionNode(createToken(TRUE_KEYWORD)), semicolonToken);
        recordFieldNodes.add(laxDataBindingFieldNode);
        return recordFieldNodes;
    }

    /**
     * Generate statements for init function when combination of ApiKeys and HTTP/OAuth authentication is used.
     *
     * <pre>
     *     if config.auth is ApiKeysConfig {
     *         self.apiKeyConfig = (<ApiKeysConfig>config.auth).cloneReadOnly();
     *     } else {
     *         config.auth = <http:BearerTokenConfig>config.auth;
     *         self.apiKeyConfig = ();
     *     }
     * </pre>
     *
     */
    public IfElseStatementNode handleInitForMixOfApiKeyAndHTTPOrOAuth() {
        List<StatementNode> apiKeyConfigAssignmentNodes = new ArrayList<>();

        // `self.apiKeyConfig = (<ApiKeysConfig>config.auth).cloneReadOnly();`
        FieldAccessExpressionNode apiKeyConfigRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(SELF)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(API_KEY_CONFIG_PARAM)));
        SimpleNameReferenceNode apiKeyConfigExpr = createSimpleNameReferenceNode(createIdentifierToken(
                "(<ApiKeysConfig>config.auth).cloneReadOnly()"));
        AssignmentStatementNode apiKeyConfigAssignmentStatementNode = createAssignmentStatementNode(apiKeyConfigRef,
                createToken(EQUAL_TOKEN), apiKeyConfigExpr, createToken(SEMICOLON_TOKEN));
        apiKeyConfigAssignmentNodes.add(apiKeyConfigAssignmentStatementNode);
        NodeList<StatementNode> statementList = createNodeList(apiKeyConfigAssignmentNodes);
        BlockStatementNode ifBody = createBlockStatementNode(createToken(OPEN_BRACE_TOKEN), statementList,
                createToken(CLOSE_BRACE_TOKEN));

        List<StatementNode> clientConfigAssignmentNodes = new ArrayList<>();

        // httpClientConfig.auth = <http:BearerTokenConfig>config.auth;
        FieldAccessExpressionNode clientConfigAuthRef = createFieldAccessExpressionNode(
                createSimpleNameReferenceNode(createIdentifierToken(HTTP_CLIENT_CONFIG)), createToken(DOT_TOKEN),
                createSimpleNameReferenceNode(createIdentifierToken(AUTH)));
        SimpleNameReferenceNode clientConfigExpr = createSimpleNameReferenceNode(
                createIdentifierToken("<" + getAuthFieldTypeName() +
                        ">" + CONFIG + DOT_TOKEN.stringValue() + AUTH));
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
                createIdentifierToken(CONFIG + DOT_TOKEN.stringValue() + AUTH),
                createToken(IS_KEYWORD),
                createIdentifierToken(API_KEYS_CONFIG)
        );
        return createIfElseStatementNode(createToken(IF_KEYWORD), condition,
                ifBody, elseBody);
    }

    private AnnotationNode getDisplayAnnotationForRecord(String label) {
        List<Node> annotFields = new ArrayList<>();
        BasicLiteralNode labelExpr = createBasicLiteralNode(STRING_LITERAL,
                createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                        "\"" + label + "\"",
                        createEmptyMinutiaeList(),
                        createEmptyMinutiaeList()));
        SpecificFieldNode labelField = createSpecificFieldNode(null,
                createIdentifierToken("label"),
                createToken(COLON_TOKEN), labelExpr);
        annotFields.add(labelField);
        MappingConstructorExpressionNode annotValue = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(annotFields),
                createToken(CLOSE_BRACE_TOKEN));

        SimpleNameReferenceNode annotateReference =
                createSimpleNameReferenceNode(createIdentifierToken("display"));

        return createAnnotationNode(createToken(SyntaxKind.AT_TOKEN)
                , annotateReference, annotValue);
    }

    private AnnotationNode getDisplayAnnotationForPasswordField() {
        List<Node> annotFields = new ArrayList<>();
        BasicLiteralNode labelExpr = createBasicLiteralNode(STRING_LITERAL,
                createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                        "\"\"",
                        createEmptyMinutiaeList(),
                        createEmptyMinutiaeList()));
        SpecificFieldNode labelField = createSpecificFieldNode(null,
                createIdentifierToken("label"),
                createToken(COLON_TOKEN), labelExpr);
        annotFields.add(labelField);
        annotFields.add(createToken(COMMA_TOKEN));
        BasicLiteralNode kindExpr = createBasicLiteralNode(STRING_LITERAL,
                createLiteralValueToken(SyntaxKind.STRING_LITERAL_TOKEN,
                        "\"password\"",
                        createEmptyMinutiaeList(),
                        createEmptyMinutiaeList()));
        SpecificFieldNode kindField = createSpecificFieldNode(null,
                createIdentifierToken("kind"),
                createToken(COLON_TOKEN), kindExpr);
        annotFields.add(kindField);

        MappingConstructorExpressionNode annotValue = createMappingConstructorExpressionNode(
                createToken(OPEN_BRACE_TOKEN), createSeparatedNodeList(annotFields),
                createToken(CLOSE_BRACE_TOKEN));

        SimpleNameReferenceNode annotateReference =
                createSimpleNameReferenceNode(createIdentifierToken("display"));

        return createAnnotationNode(createToken(SyntaxKind.AT_TOKEN)
                , annotateReference, annotValue);
    }

    private MetadataNode getMetadataNode(String comment) {

        List<Node> docs = new ArrayList<>(DocCommentsGeneratorUtil.createAPIDescriptionDoc(comment, false));
        MarkdownDocumentationNode authDocumentationNode = createMarkdownDocumentationNode(
                createNodeList(docs));
        return createMetadataNode(authDocumentationNode, createEmptyNodeList());
    }

    private MetadataNode getMetadataNode(String comment, List<AnnotationNode> annotationNodes) {
        List<Node> docs = new ArrayList<>(DocCommentsGeneratorUtil.createAPIDescriptionDoc(comment, false));
        MarkdownDocumentationNode authDocumentationNode = createMarkdownDocumentationNode(
                createNodeList(docs));
        return createMetadataNode(authDocumentationNode, createNodeList(annotationNodes));
    }

    /**
     * Travers through the security schemas of the given open api spec.
     * Store api key names which needs to send in the query string and as a request header separately.
     *
     * @param securitySchemeMap Map of security schemas of the given open api spec
     * @param openAPI
     */
    public void setAuthTypes(Map<String, SecurityScheme> securitySchemeMap, OpenAPI openAPI) throws ClientException {
        List<Server> servers = openAPI.getServers();
        String url = "";
        if (servers.size() > 0) {
            url = GeneratorUtils.buildUrl(servers.get(0).getUrl(), servers.get(0).getVariables());
        }
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
                                if (clientCredGrantTokenUrl.startsWith("/")) {
                                    clientCredGrantTokenUrl = url + clientCredGrantTokenUrl;
                                }
                            }
                            authTypes.add(CLIENT_CRED);
                        }
                        if (schemaValue.getFlows().getPassword() != null) {
                            if (schemaValue.getFlows().getPassword().getTokenUrl() != null) {
                                passwordGrantTokenUrl = schemaValue.getFlows().getPassword().getTokenUrl();
                                if (passwordGrantTokenUrl.startsWith("/")) {
                                    passwordGrantTokenUrl = url + passwordGrantTokenUrl;
                                }
                            }
                            authTypes.add(PASSWORD);
                        }
                        if (schemaValue.getFlows().getAuthorizationCode() != null) {
                            if (schemaValue.getFlows().getAuthorizationCode().getTokenUrl() != null) {
                                refreshTokenUrl = schemaValue.getFlows().getAuthorizationCode().getTokenUrl();
                                if (refreshTokenUrl.startsWith("/")) {
                                    refreshTokenUrl = url + refreshTokenUrl;
                                }
                            }
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
                                String actualName = schemaValue.getName();
                                String name = GeneratorUtils.getBallerinaNameExtension(schemaValue.getExtensions())
                                        .orElse(actualName);
                                String displayName = escapeIdentifier(name);
                                ApiKeyNamePair queryApiKeyNamePair = new ApiKeyNamePair(actualName, displayName);
                                queryApiKeyNameMap.put(securitySchemeEntry.getKey(), queryApiKeyNamePair);
                                break;
                            case "header":
                                String actualHeaderName = schemaValue.getName();
                                String headerName = GeneratorUtils.getBallerinaNameExtension(schemaValue
                                                .getExtensions()).orElse(actualHeaderName);
                                String displayHeaderName = escapeIdentifier(headerName);
                                ApiKeyNamePair headerApiKeyNamePair = new ApiKeyNamePair(actualHeaderName,
                                        displayHeaderName);
                                headerApiKeyNameMap.put(securitySchemeEntry.getKey(), headerApiKeyNamePair);
                                break;
                            default:
                                break;
                        }
                        break;
                }
            }
        }
        if (!(apiKey || httpOROAuth)) {
            throw new ClientException("Unsupported type of security schema");
        }
    }

    /**
     * Returns fields in ApiKeysConfig record.
     * <pre>
     *     # API key related to connector authentication
     *     string apiKey;
     * </pre>
     */
    private void setApiKeysConfigRecordFields(SecurityScheme securityScheme) {

        MetadataNode metadataNode = null;
        if (securityScheme.getDescription() != null) {
            List<AnnotationNode> annotationNodes = Collections.singletonList(getDisplayAnnotationForPasswordField());
            metadataNode = getMetadataNode(securityScheme.getDescription(), annotationNodes);
        }
        TypeDescriptorNode stringTypeDesc = createSimpleNameReferenceNode(createToken(STRING_KEYWORD));
        String name = GeneratorUtils.getBallerinaNameExtension(securityScheme.getExtensions())
                .orElse(securityScheme.getName());
        IdentifierToken apiKeyName = createIdentifierToken(escapeIdentifier(name));
        apiKeysConfigRecordFields.add(createRecordFieldNode(metadataNode, null, stringTypeDesc,
                apiKeyName, null, createToken(SEMICOLON_TOKEN)));
    }

    /**
     * Travers through the authTypes and generate the field type name of auth field in ClientConfig record.
     *
     * @return {@link String}   Field type name of auth field
     * Ex: {@code http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig}
     */
    private String getAuthFieldTypeName() {
        Set<String> httpFieldTypeNames = new HashSet<>();
        for (String authType : authTypes) {
            switch (authType) {
                case BEARER:
                    httpFieldTypeNames.add(GeneratorConstants.AuthConfigTypes.BEARER.getValue());
                    break;
                case BASIC:
                    httpFieldTypeNames.add(GeneratorConstants.AuthConfigTypes.BASIC.getValue());
                    break;
                case CLIENT_CRED:
                    // Previous version of swagger parser returns null value, when it has an
                    // empty string as a value (ex: tokenURL: ""). Latest version `2.0.30` version 
                    // returns empty string as it is. Therefore, we have to check both null check and empty string
                    // check.
                    if (clientCredGrantTokenUrl != null && !clientCredGrantTokenUrl.isBlank()) {
                        httpFieldTypeNames.add(GeneratorConstants.AuthConfigTypes.CUSTOM_CLIENT_CREDENTIAL.getValue());
                    } else {
                        httpFieldTypeNames.add(GeneratorConstants.AuthConfigTypes.CLIENT_CREDENTIAL.getValue());
                    }
                    break;
                case PASSWORD:
                    if (passwordGrantTokenUrl != null && !passwordGrantTokenUrl.isBlank()) {
                        httpFieldTypeNames.add(GeneratorConstants.AuthConfigTypes.CUSTOM_PASSWORD.getValue());
                    } else {
                        httpFieldTypeNames.add(GeneratorConstants.AuthConfigTypes.PASSWORD.getValue());
                    }
                    break;
                case REFRESH_TOKEN:
                    if (refreshTokenUrl != null && !refreshTokenUrl.isBlank()) {
                        httpFieldTypeNames.add(GeneratorConstants.AuthConfigTypes.CUSTOM_REFRESH_TOKEN.getValue());
                    } else {
                        httpFieldTypeNames.add(GeneratorConstants.AuthConfigTypes.REFRESH_TOKEN.getValue());
                    }
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
     * @param fieldtypes Type name set from {@link #setAuthTypes(Map, OpenAPI)} method.
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
