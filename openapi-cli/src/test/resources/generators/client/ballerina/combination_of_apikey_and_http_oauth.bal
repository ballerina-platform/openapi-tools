import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig? apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ConnectionConfig config, string serviceUrl = "https://petstore.swagger.io:443/v2") returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, timeout: config.timeout, forwarded: config.forwarded, poolConfig: config.poolConfig, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, validation: config.validation};
        do {
            if config.http1Settings is ClientHttp1Settings {
                ClientHttp1Settings settings = check config.http1Settings.ensureType(ClientHttp1Settings);
                httpClientConfig.http1Settings = {...settings};
            }
            if config.http2Settings is http:ClientHttp2Settings {
                httpClientConfig.http2Settings = check config.http2Settings.ensureType(http:ClientHttp2Settings);
            }
            if config.cache is http:CacheConfig {
                httpClientConfig.cache = check config.cache.ensureType(http:CacheConfig);
            }
            if config.responseLimits is http:ResponseLimitConfigs {
                httpClientConfig.responseLimits = check config.responseLimits.ensureType(http:ResponseLimitConfigs);
            }
            if config.secureSocket is http:ClientSecureSocket {
                httpClientConfig.secureSocket = check config.secureSocket.ensureType(http:ClientSecureSocket);
            }
            if config.proxy is http:ProxyConfig {
                httpClientConfig.proxy = check config.proxy.ensureType(http:ProxyConfig);
            }
        }
        if config.auth is ApiKeysConfig {
            self.apiKeyConfig = (<ApiKeysConfig>config.auth).cloneReadOnly();
        } else {
            config.auth = <OAuth2ClientCredentialsGrantConfig|http:BearerTokenConfig|OAuth2RefreshTokenGrantConfig>config.auth;
            self.apiKeyConfig = ();
        }
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        return;
    }
    # Info for a specific pet
    #
    # + petId - The id of the pet to retrieve
    # + headerX - Header X
    # + return - Expected response to a valid request
    remote isolated function getPetInfo(string petId, string headerX) returns Pet|error {
        string resourcePath = string `/pets/management`;
        map<any> headerValues = {"headerX": headerX};
        map<anydata> queryParam = {"petId": petId};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.apiKey;
            queryParam["api-key-2"] = self.apiKeyConfig?.apiKey2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        Pet response = check self.clientEp->get(resourcePath, httpHeaders);
        return response;
    }
    # Vote for a pet
    #
    # + return - Expected response to a valid request
    remote isolated function votePet() returns Pet|error {
        string resourcePath = string `/pets/management`;
        map<any> headerValues = {};
        map<anydata> queryParam = {};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.apiKey;
            queryParam["api-key-2"] = self.apiKeyConfig?.apiKey2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Request request = new;
        //TODO: Update the request as needed;
        Pet response = check self.clientEp->post(resourcePath, request, httpHeaders);
        return response;
    }
    # Delete a pet
    #
    # + petId - The id of the pet to delete
    # + return - Expected response to a valid request
    remote isolated function deletePetInfo(string petId) returns Pet|error {
        string resourcePath = string `/pets/management`;
        map<any> headerValues = {};
        map<anydata> queryParam = {"petId": petId};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.apiKey;
            queryParam["api-key-2"] = self.apiKeyConfig?.apiKey2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        Pet response = check self.clientEp->delete(resourcePath, headers = httpHeaders);
        return response;
    }
    # Delete a pet 2
    #
    # + petId - The id of the pet to delete
    # + return - Expected response to a valid request
    remote isolated function deletePetInfo2(string petId) returns Pet|error {
        string resourcePath = string `/pets/management2`;
        map<any> headerValues = {"petId": petId};
        map<anydata> queryParam = {};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.apiKey;
            queryParam["api-key-2"] = self.apiKeyConfig?.apiKey2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        Pet response = check self.clientEp->delete(resourcePath, headers = httpHeaders);
        return response;
    }
}
