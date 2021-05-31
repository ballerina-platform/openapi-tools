package io.ballerina.generators.common;

/**
 * Constants for openapi code generator unit test cases.
 */
public class TestConstants {
    public static final String HTTP_BASIC_AUTH_CONFIG_REC = "" +
            "public type ClientConfig record {\n" +
            "    http:CredentialsConfig authConfig;\n" +
            "    http:ClientSecureSocket secureSocketConfig?;\n" +
            "};";
    public static final String HTTP_BEARER_AUTH_CONFIG_REC = "" +
            "public type ClientConfig record {\n" +
            "    http:BearerTokenConfig authConfig;\n" +
            "    http:ClientSecureSocket secureSocketConfig?;\n" +
            "};";
    public static final String HTTP_MULTI_AUTH_CONFIG_REC = "" +
            "public type ClientConfig record {\n" +
            "    http:BearerTokenConfig|http:CredentialsConfig authConfig;\n" +
            "    http:ClientSecureSocket secureSocketConfig?;\n" +
            "};";
    public static final String HTTP_CLIENT_CONFIG_PARAM = "ClientConfig clientConfig";
    public static final String SSL_ASSIGNMENT = "http:ClientSecureSocket? secureSocketConfig = " +
            "clientConfig?.secureSocketConfig;";
    public static final String HTTP_CLIENT_DECLARATION = "" +
            "http:Client httpEp = check new (serviceUrl, " +
            "{ " +
            "   auth: clientConfig.authConfig, " +
            "   secureSocket: secureSocketConfig " +
            "});";
    public static final String OAUTH2_AUTHORIZATION_CODE_CONFIG_REC = "" +
            "public type ClientConfig record {\n" +
            "    http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig authConfig;\n" +
            "    http:ClientSecureSocket secureSocketConfig?;\n" +
            "};";
    public static final String OAUTH2_IMPLICIT_CONFIG_REC = "" +
            "public type ClientConfig record {\n" +
            "    http:BearerTokenConfig authConfig;\n" +
            "    http:ClientSecureSocket secureSocketConfig?;\n" +
            "};";
    public static final String OAUTH2_PASSWORD_CONFIG_REC = "" +
            "public type ClientConfig record {\n" +
            "    http:OAuth2PasswordGrantConfig authConfig;\n" +
            "    http:ClientSecureSocket secureSocketConfig?;\n" +
            "};";
    public static final String OAUTH2_CLIENT_CRED_CONFIG_REC = "" +
            "public type ClientConfig record {\n" +
            "    http:OAuth2ClientCredentialsGrantConfig authConfig;\n" +
            "    http:ClientSecureSocket secureSocketConfig?;\n" +
            "};";
    public static final String OAUTH2_MULTI_FLOWS_CONFIG_REC = "" +
            "public type ClientConfig record {\n" +
            "  http:OAuth2PasswordGrantConfig|http:BearerTokenConfig|http:OAuth2RefreshTokenGrantConfig authConfig;\n" +
            "  http:ClientSecureSocket secureSocketConfig?;\n" +
            "};";
    public static final String API_KEY_CONFIG_REC = "public type ApiKeysConfig record {map<string|string[]> apiKeys;};";
    public static final String API_KEY_MAP_VAR  = "map<string|string[]> apiKeys;";
    public static final String API_KEY_CONFIG_PARAM = "" +
            "ApiKeysConfig apiKeyConfig,http:ClientConfiguration clientConfig =  {}";
    public static final String API_KEY_ASSIGNMENT = "self.apiKeys = apiKeyConfig.apiKeys;";

}
