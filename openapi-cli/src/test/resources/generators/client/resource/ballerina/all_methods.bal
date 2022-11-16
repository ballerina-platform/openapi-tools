import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
        public isolated function init(ApiKeysConfig apiKeyConfig, ConnectionConfig config = {}, string serviceUrl = "https://petstore.swagger.io:443/v2") returns error? {
        http:ClientConfiguration httpClientConfig = {
            httpVersion: config.httpVersion,
            timeout: config.timeout,
            forwarded: config.forwarded,
            poolConfig: config.poolConfig,
            compression: config.compression,
            circuitBreaker: config.circuitBreaker,
            retryConfig: config.retryConfig,
            validation: config.validation
        };
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
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
        return;
    }
    # List all pets
    #
    # + 'limit - How many items to return at one time (max 100)
    # + return - An paged array of pets
    resource isolated function get pets(int? 'limit = ()) returns Pets|error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {"limit": 'limit, "appid1": self.apiKeyConfig.appid1, "appid2": self.apiKeyConfig.appid2};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Pets response = check self.clientEp->get(resourcePath);
        return response;
    }
    # Update a pet
    #
    # + return - Null response
    resource isolated function put pets() returns http:Response|error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {"appid1": self.apiKeyConfig.appid1, "appid2": self.apiKeyConfig.appid2};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response response = check self.clientEp-> put(resourcePath, request);
        return response;
    }
    # Create a pet
    #
    # + return - Null response
    resource isolated function post pets() returns http:Response|error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {"appid1": self.apiKeyConfig.appid1, "appid2": self.apiKeyConfig.appid2};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response response = check self.clientEp-> post(resourcePath, request);
        return response;
    }
    # Delete a pet
    #
    # + return - Ok response
    resource isolated function delete pets() returns http:Response|error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {"appid1": self.apiKeyConfig.appid1, "appid2": self.apiKeyConfig.appid2};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Response response = check self.clientEp->delete(resourcePath);
        return response;
    }
}
