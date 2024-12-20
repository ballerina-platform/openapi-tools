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
            httpClientConfig.auth = <OAuth2ClientCredentialsGrantConfig|http:BearerTokenConfig|OAuth2RefreshTokenGrantConfig>config.auth;
            self.apiKeyConfig = ();
        }
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        return;
    }

    # Delete a pet
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - Expected response to a valid request
    remote isolated function deletePetInfo(map<string|string[]> headers = {}, *DeletePetInfoQueries queries) returns Pet|error {
        string resourcePath = string `/pets/management`;
        map<anydata> headerValues = {...headers};
        map<anydata> queryParam = {...queries};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.api\-key;
            queryParam["api-key-2"] = self.apiKeyConfig?.api\-key\-2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        return self.clientEp->delete(resourcePath, headers = httpHeaders);
    }

    # Delete a pet 2
    #
    # + headers - Headers to be sent with the request
    # + return - Expected response to a valid request
    remote isolated function deletePetInfo2(DeletePetInfo2Headers headers) returns Pet|error {
        string resourcePath = string `/pets/management2`;
        map<anydata> headerValues = {...headers};
        map<anydata> queryParam = {};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.api\-key;
            queryParam["api-key-2"] = self.apiKeyConfig?.api\-key\-2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        return self.clientEp->delete(resourcePath, headers = httpHeaders);
    }

    # Info for a specific pet
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - Expected response to a valid request
    remote isolated function getPetInfo(GetPetInfoHeaders headers, *GetPetInfoQueries queries) returns Pet|error {
        string resourcePath = string `/pets/management`;
        map<anydata> headerValues = {...headers};
        map<anydata> queryParam = {...queries};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.api\-key;
            queryParam["api-key-2"] = self.apiKeyConfig?.api\-key\-2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        return self.clientEp->get(resourcePath, httpHeaders);
    }

    # Vote for a pet
    #
    # + headers - Headers to be sent with the request
    # + return - Expected response to a valid request
    remote isolated function votePet(map<string|string[]> headers = {}) returns Pet|error {
        string resourcePath = string `/pets/management`;
        map<anydata> headerValues = {...headers};
        map<anydata> queryParam = {};
        if self.apiKeyConfig is ApiKeysConfig {
            headerValues["api-key"] = self.apiKeyConfig?.api\-key;
            queryParam["api-key-2"] = self.apiKeyConfig?.api\-key\-2;
        }
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = http:getHeaderMap(headerValues);
        http:Request request = new;
        return self.clientEp->post(resourcePath, request, httpHeaders);
    }
}
