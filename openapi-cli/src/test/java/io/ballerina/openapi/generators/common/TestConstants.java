package io.ballerina.openapi.generators.common;

/**
 * Constants for openapi code generator unit test cases.
 */
public class TestConstants {
    private static String clientConfigRecordDoc = "" +
            "# Provides a set of configurations for controlling the behaviours when communicating with a remote " +
            "HTTP endpoint.";
    private static String commonClientConfigurationFields = "# The HTTP version understood by the client\n" +
            "    string httpVersion = \"1.1\";\n" +
            "    # Configurations related to HTTP/1.x protocol\n" +
            "    http:ClientHttp1Settings http1Settings = {};\n" +
            "    # Configurations related to HTTP/2 protocol\n" +
            "    http:ClientHttp2Settings http2Settings = {};\n" +
            "    # The maximum time to wait (in seconds) for a response before closing the connection\n" +
            "    decimal timeout = 60;\n" +
            "    # The choice of setting `forwarded`/`x-forwarded` header\n" +
            "    string forwarded = \"disable\";\n" +
            "    # Configurations associated with Redirection\n" +
            "    http:FollowRedirects? followRedirects = ();\n" +
            "    # Configurations associated with request pooling\n" +
            "    http:PoolConfiguration? poolConfig = ();\n" +
            "    # HTTP caching related configurations\n" +
            "    http:CacheConfig cache = {};\n" +
            "    # Specifies the way of handling compression (`accept-encoding`) header\n" +
            "    http:Compression compression = http:COMPRESSION_AUTO;\n" +
            "    # Configurations associated with the behaviour of the Circuit Breaker\n" +
            "    http:CircuitBreakerConfig? circuitBreaker = ();\n" +
            "    # Configurations associated with retrying\n" +
            "    http:RetryConfig? retryConfig = ();\n" +
            "    # Configurations associated with cookies\n" +
            "    http:CookieConfig? cookieConfig = ();\n" +
            "    # Configurations associated with inbound response size limits\n" +
            "    http:ResponseLimitConfigs responseLimits = {};\n" +
            "    # SSL/TLS-related options\n" +
            "    http:ClientSecureSocket? secureSocket = ();\n" +
            "|};";
            
    public static final String HTTP_BASIC_AUTH_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ClientConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:CredentialsConfig auth;\n" + commonClientConfigurationFields;
    
    public static final String HTTP_BEARER_AUTH_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ClientConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:BearerTokenConfig auth;\n" + commonClientConfigurationFields;
    public static final String HTTP_MULTI_AUTH_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ClientConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:BearerTokenConfig|http:CredentialsConfig auth;\n" + commonClientConfigurationFields;
    public static final String HTTP_CLIENT_CONFIG_PARAM =
            "ClientConfig clientConfig, string serviceUrl = \"https:localhost/8080\"";
    public static final String HTTP_CLIENT_CONFIG_PARAM_NO_URL = "ClientConfig clientConfig, string serviceUrl";
    public static final String HTTP_CLIENT_DECLARATION = "" +
            "http:Client httpEp = check new (serviceUrl, clientConfig);";
    public static final String OAUTH2_AUTHORIZATION_CODE_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ClientConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_IMPLICIT_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ClientConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:BearerTokenConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_PASSWORD_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ClientConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:OAuth2PasswordGrantConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_CLIENT_CRED_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ClientConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   http:OAuth2ClientCredentialsGrantConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_MULTI_FLOWS_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ClientConfig record {|\n" +
            "  # Configurations related to client authentication\n" +
            "  http:OAuth2PasswordGrantConfig|http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig auth;\n"
            + commonClientConfigurationFields;
    public static final String API_KEY_CONFIG_REC = "" +
            "# Provides API key configurations needed when communicating with a remote HTTP endpoint." +
            "public type ApiKeysConfig record {|" +
            "   # API keys related to connector authentication" +
            "   map<string> apiKeys;" +
            "|};";
    public static final String API_KEY_MAP_VAR  = "final readonly & map<string> apiKeys;";
    public static final String API_KEY_CONFIG_PARAM = "" +
            "ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, " +
            "string serviceUrl = \"https:localhost/8080\"";
    public static final String API_KEY_CONFIG_PARAM_NO_URL = "" +
            "ApiKeysConfig apiKeyConfig, string serviceUrl, http:ClientConfiguration clientConfig =  {}";

    public static final String API_KEY_ASSIGNMENT = "self.apiKeys = apiKeyConfig.apiKeys.cloneReadOnly();";
    public static final String API_KEY_DOC_COMMENT =
            "Provide your API key as `appid` .Eg: `{\"appid\":\"<APIkey>\"}`";

}
