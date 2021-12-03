package io.ballerina.openapi.generators.common;

/**
 * Constants for openapi code generator unit test cases.
 */
public class TestConstants {
    private static String clientConfigRecordDoc = "" +
            "# Provides a set of configurations for controlling the behaviours when communicating with a remote " +
            "HTTP endpoint.";
    public static String authConfigRecordDoc = "" +
            "#Provides Auth configurations needed when communicating with a remote HTTP endpoint.";
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
    public static final String OAUTH2_CUSTOM_CLIENT_CRED_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ClientConfig record {|\n" +
            "   # Configurations related to client authentication\n" +
            "   OAuth2ClientCredentialsGrantConfig auth;\n" + commonClientConfigurationFields;
    public static final String OAUTH2_MULTI_FLOWS_CONFIG_REC = "" +
            clientConfigRecordDoc +
            "public type ClientConfig record {|\n" +
            "  # Configurations related to client authentication\n" +
            "  http:OAuth2PasswordGrantConfig|http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig auth;\n"
            + commonClientConfigurationFields;
    public static final String API_KEY_CONFIG_VAR = "final readonly & ApiKeysConfig apiKeyConfig;";
    public static final String API_KEY_CONFIG_NILLABLE_VAR = "final readonly & ApiKeysConfig? apiKeyConfig;";
    public static final String API_KEY_CONFIG_PARAM = "" +
            "ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, " +
            "string serviceUrl = \"https:localhost/8080\"";
    public static final String AUTH_CONFIG_PARAM = "" +
            "AuthConfig authConfig, http:ClientConfiguration clientConfig =  {}, " +
            "string serviceUrl = \"https:localhost/8080\"";
    public static final String API_KEY_CONFIG_PARAM_NO_URL = "" +
            "ApiKeysConfig apiKeyConfig, string serviceUrl, http:ClientConfiguration clientConfig =  {}";
    public static final String API_KEYS_CONFIG_RECORD = "# Provides API key configurations needed when communicating " +
            "with a remote HTTP endpoint.\n" +
            "public type ApiKeysConfig record {|\n" +
            "    # API key to authorize requests.\n" +
            "    string appid;\n" +
            "    # API key to authorize requests.\n" +
            "    string apiXKey;\n" +
            "|};";
    public static final String API_KEY_ASSIGNMENT = "self.apiKeyConfig = apiKeyConfig.cloneReadOnly();";
    public static final String MULTIPLE_API_KEY_RECORD = "# Provides API key configurations needed when " +
            "communicating with a remote HTTP endpoint.\n" +
            "public type ApiKeysConfig record {|\n" +
            "    # API key to authorize GET requests.\n" +
            "    string appid;\n" +
            "    # API key to authorize POST requests.\n" +
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
            "    string appid;\n" +
            "|};";
}
