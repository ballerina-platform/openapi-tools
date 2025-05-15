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
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        if config.auth is ApiKeysConfig {
            self.apiKeyConfig = (<ApiKeysConfig>config.auth).cloneReadOnly();
        } else {
            httpClientConfig.auth = <OAuth2ClientCredentialsGrantConfig|http:BearerTokenConfig|OAuth2RefreshTokenGrantConfig>config.auth;
            self.apiKeyConfig = ();
        }
        self.clientEp = check new (serviceUrl, httpClientConfig);
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
}
