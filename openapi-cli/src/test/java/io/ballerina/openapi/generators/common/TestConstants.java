package io.ballerina.openapi.generators.common;

/**
 * Constants for openapi code generator unit test cases.
 */
public class TestConstants {
    private static String clientConfigRecordDoc = "" +
            "# Provides a set of configurations for controlling the behaviours when communicating with a remote " +
            "HTTP endpoint.\n" +
            "@display {label: \"Connection Config\"}\n";
    public static String authConfigRecordDoc = "" +
            "# Provides Auth configurations needed when communicating with a remote HTTP endpoint.";
    private static String commonClientConfigurationFields = "# The HTTP version understood by the client\n" +
            "    http:HttpVersion httpVersion = http:HTTP_2_0;\n" +
            "    # Configurations related to HTTP/1.x protocol\n" +
            "    ClientHttp1Settings http1Settings?;\n" +
            "    # Configurations related to HTTP/2 protocol\n" +
            "    http:ClientHttp2Settings http2Settings?;\n" +
            "    # The maximum time to wait (in seconds) for a response before closing the connection\n" +
            "    decimal timeout = 60;\n" +
            "    # The choice of setting `forwarded`/`x-forwarded` header\n" +
            "    string forwarded = \"disable\";\n" +
            "    # Configurations associated with request pooling\n" +
            "    http:PoolConfiguration poolConfig?;\n" +
            "    # HTTP caching related configurations\n" +
            "    http:CacheConfig cache?;\n" +
            "    # Specifies the way of handling compression (`accept-encoding`) header\n" +
            "    http:Compression compression = http:COMPRESSION_AUTO;\n" +
            "    # Configurations associated with the behaviour of the Circuit Breaker\n" +
            "    http:CircuitBreakerConfig circuitBreaker?;\n" +
            "    # Configurations associated with retrying\n" +
            "    http:RetryConfig retryConfig?;\n" +
            "    # Configurations associated with inbound response size limits\n" +
            "    http:ResponseLimitConfigs responseLimits?;\n" +
            "    # SSL/TLS-related options\n" +
            "    http:ClientSecureSocket secureSocket?;\n" +
            "    # Proxy server related options\n" +
            "    http:ProxyConfig proxy?;\n" +
            "    # Enables the inbound payload validation functionality which provided by the constraint package. " +
            "Enabled by default\n" +
            "    boolean validation = true; " +
            "|};";

    public static final String CLIENT_HTTP1_SETTINGS = "# Provides settings related to HTTP/1.x protocol.\n" +
            "public type ClientHttp1Settings record {|\n" +
            "    # Specifies whether to reuse a connection for multiple requests\n" +
            "    http:KeepAlive keepAlive = http:KEEPALIVE_AUTO;\n" +
            "    # The chunking behaviour of the request\n" +
            "    http:Chunking chunking = http:CHUNKING_AUTO;\n" +
            "    # Proxy server related options\n" +
            "    ProxyConfig proxy?;\n" +
            "|};";

    public static final String PROXY_CONFIG = "" +
            "# Proxy server configurations to be used with the HTTP client endpoint.\n" +
            "public type ProxyConfig record {|\n" +
            "    # Host name of the proxy server\n" +
            "    string host = \"\";\n" +
            "    # Proxy server port\n" +
            "    int port = 0;\n" +
            "    # Proxy server username\n" +
            "    string userName = \"\";\n" +
            "    # Proxy server password\n" +
            "    @display {label: \"\", kind: \"password\"}\n" +
            "    string password = \"\";\n" +
            "|};";


    public static final String HTTP_BASIC_AUTH_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ConnectionConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:CredentialsConfig auth;\n" + commonClientConfigurationFields;
    
    public static final String HTTP_BEARER_AUTH_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ConnectionConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:BearerTokenConfig auth;\n" + commonClientConfigurationFields;
    public static final String HTTP_MULTI_AUTH_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ConnectionConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:BearerTokenConfig|http:CredentialsConfig auth;\n" + commonClientConfigurationFields;
    public static final String HTTP_CLIENT_CONFIG_PARAM =
            "ConnectionConfig config, string serviceUrl = \"https:localhost/8080\"";
    public static final String HTTP_CLIENT_CONFIG_PARAM_NO_URL = "ConnectionConfig config, string serviceUrl";
    public static final String HTTP_CLIENT_DECLARATION = "" +
            "http:Client httpEp = check new (serviceUrl, httpClientConfig);";
    public static final String OAUTH2_AUTHORIZATION_CODE_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ConnectionConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_CUSTOM_AUTHORIZATION_CODE_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ConnectionConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:BearerTokenConfig|OAuth2RefreshTokenGrantConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_IMPLICIT_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ConnectionConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:BearerTokenConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_PASSWORD_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ConnectionConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:OAuth2PasswordGrantConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_CUSTOM_PASSWORD_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ConnectionConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   OAuth2PasswordGrantConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_CLIENT_CRED_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ConnectionConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:OAuth2ClientCredentialsGrantConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_CUSTOM_CLIENT_CRED_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ConnectionConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   OAuth2ClientCredentialsGrantConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_MULTI_FLOWS_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ConnectionConfig record {|\n" +
            "  # Configurations related to client authentication\n" +
            " OAuth2PasswordGrantConfig|http:BearerTokenConfig|OAuth2RefreshTokenGrantConfig auth;\n"
            + commonClientConfigurationFields;
    public static final String API_KEY_CONFIG_VAR = "final readonly & ApiKeysConfig apiKeyConfig;";
    public static final String API_KEY_CONFIG_NILLABLE_VAR = "final readonly & ApiKeysConfig? apiKeyConfig;";
    public static final String API_KEY_CONFIG_PARAM = "" +
            "ApiKeysConfig apiKeyConfig, ConnectionConfig config = {}, " +
            "string serviceUrl = \"https:localhost/8080\"";
    public static final String AUTH_CONFIG_PARAM = "" +
            "ConnectionConfig config, " +
            "string serviceUrl = \"https:localhost/8080\"";
    public static final String API_KEY_CONFIG_PARAM_NO_URL = "" +
            "ApiKeysConfig apiKeyConfig, string serviceUrl,ConnectionConfig config =  {}";
    public static final String API_KEYS_CONFIG_RECORD = "# Provides API key configurations needed when communicating " +
            "with a remote HTTP endpoint.\n" +
            "public type ApiKeysConfig record {|\n" +
            "    # API key to authorize requests.\n" +
            "    @display {label: \"\", kind: \"password\"}\n" +
            "    string appid;\n" +
            "    # API key to authorize requests.\n" +
            "    @display {label: \"\", kind: \"password\"}\n" +
            "    string apiXKey;\n" +
            "|};";

    public static final String API_KEY_ASSIGNMENT = "self.apiKeyConfig = apiKeyConfig.cloneReadOnly();";
    public static final String MULTIPLE_API_KEY_RECORD = "# Provides API key configurations needed when " +
            "communicating with a remote HTTP endpoint.\n" +
            "public type ApiKeysConfig record {|\n" +
            "    # API key to authorize GET requests.\n" +
            "    @display {label: \"\", kind: \"password\"}\n" +
            "    string appid;\n" +
            "    # API key to authorize POST requests.\n" +
            "    @display {label: \"\", kind: \"password\"}\n" +
            "    string xApiKey;\n" +
            "|};";
    public static final String MULTI_LINE_API_KEY_DESC = "# Provides API key configurations needed when " +
            "communicating with a remote HTTP endpoint.\n" +
            "public type ApiKeysConfig record {|\n" +
            "    # To use API you have to sign up and get your own API key. Unify API accounts have sandbox mode and " +
            "live mode API keys. To change modes just use the appropriate key to get a live or test object.\n" +
            "    # Authenticate your API requests by including your test or live secret API key in the request " +
            "header.\n" +
            "    # You should use the public keys on the SDKs and the secret keys to authenticate API requests.\n" +
            "    # **Do not share or include your secret API keys on client side code.** Your API keys carry " +
            "significant privileges. Please ensure to keep them 100% secure and be sure to not share your secret " +
            "API keys in areas that are publicly accessible like GitHub.\n" +
            "    # Learn how to set the Authorization header inside Postman " +
            "https://learning.postman.com/docs/postman/sending-api-requests/authorization/#api-key\n" +
            "    # Go to Unify to grab your API KEY https://openweathermap/me/api-keys\n" +
            "    @display {label: \"\", kind: \"password\"}\n" +
            "    string appid;\n" +
            "|};";

    public static final String CONNECTION_CONFIG_NO_AUTH = "" +
            "# Provides a set of configurations for controlling the behaviours when communicating with a " +
            "remote HTTP endpoint.\n" +
            "@display {label: \"Connection Config\"}\n" +
            "public type ConnectionConfig record {|\n" +
            "    # The HTTP version understood by the client\n" +
            "    http:HttpVersion httpVersion = http:HTTP_2_0;\n" +
            "    # Configurations related to HTTP/1.x protocol\n" +
            "    ClientHttp1Settings http1Settings?;\n" +
            "    # Configurations related to HTTP/2 protocol\n" +
            "    http:ClientHttp2Settings http2Settings?;\n" +
            "    # The maximum time to wait (in seconds) for a response before closing the connection\n" +
            "    decimal timeout = 60;\n" +
            "    # The choice of setting `forwarded`/`x-forwarded` header\n" +
            "    string forwarded = \"disable\";\n" +
            "    # Configurations associated with request pooling\n" +
            "    http:PoolConfiguration poolConfig?;\n" +
            "    # HTTP caching related configurations\n" +
            "    http:CacheConfig cache?;\n" +
            "    # Specifies the way of handling compression (`accept-encoding`) header\n" +
            "    http:Compression compression = http:COMPRESSION_AUTO;\n" +
            "    # Configurations associated with the behaviour of the Circuit Breaker\n" +
            "    http:CircuitBreakerConfig circuitBreaker?;\n" +
            "    # Configurations associated with retrying\n" +
            "    http:RetryConfig retryConfig?;\n" +
            "    # Configurations associated with inbound response size limits\n" +
            "    http:ResponseLimitConfigs responseLimits?;\n" +
            "    # SSL/TLS-related options\n" +
            "    http:ClientSecureSocket secureSocket?;\n" +
            "    # Proxy server related options\n" +
            "    http:ProxyConfig proxy?;\n" +
            "    # Enables the inbound payload validation functionality which provided by the constraint package. " +
            "Enabled by default\n" +
            "    boolean validation = true;\n" +
            "|};";
    public static final String CONNECTION_CONFIG_MIXED_AUTH = "" +
            "# Provides a set of configurations for controlling the behaviours when communicating with a " +
            "remote HTTP endpoint.\n" +
            "@display {label: \"Connection Config\"}\n" +
            "public type ConnectionConfig record {|\n" +
            "    # Provides Auth configurations needed when communicating with a remote HTTP endpoint.\n" +
            "    OAuth2ClientCredentialsGrantConfig|http:BearerTokenConfig|" +
            "    OAuth2RefreshTokenGrantConfig|ApiKeysConfig auth;\n" +
            "    # The HTTP version understood by the client\n" +
            "    http:HttpVersion httpVersion = http:HTTP_2_0;\n" +
            "    # Configurations related to HTTP/1.x protocol\n" +
            "    ClientHttp1Settings http1Settings?;\n" +
            "    # Configurations related to HTTP/2 protocol\n" +
            "    http:ClientHttp2Settings http2Settings?;\n" +
            "    # The maximum time to wait (in seconds) for a response before closing the connection\n" +
            "    decimal timeout = 60;\n" +
            "    # The choice of setting `forwarded`/`x-forwarded` header\n" +
            "    string forwarded = \"disable\";\n" +
            "    # Configurations associated with request pooling\n" +
            "    http:PoolConfiguration poolConfig?;\n" +
            "    # HTTP caching related configurations\n" +
            "    http:CacheConfig cache?;\n" +
            "    # Specifies the way of handling compression (`accept-encoding`) header\n" +
            "    http:Compression compression = http:COMPRESSION_AUTO;\n" +
            "    # Configurations associated with the behaviour of the Circuit Breaker\n" +
            "    http:CircuitBreakerConfig circuitBreaker?;\n" +
            "    # Configurations associated with retrying\n" +
            "    http:RetryConfig retryConfig?;\n" +
            "    # Configurations associated with inbound response size limits\n" +
            "    http:ResponseLimitConfigs responseLimits?;\n" +
            "    # SSL/TLS-related options\n" +
            "    http:ClientSecureSocket secureSocket?;\n" +
            "    # Proxy server related options\n" +
            "    http:ProxyConfig proxy?;\n" +
            "    # Enables the inbound payload validation functionality which provided by the constraint package. " +
            "Enabled by default\n" +
            "    boolean validation = true;\n" +
            "|};";
}
