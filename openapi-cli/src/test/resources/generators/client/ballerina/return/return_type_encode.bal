import ballerina/http;

# Move your app forward with the Uber API
public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, ConnectionConfig config =  {}, string serviceUrl = "https://api.uber.com/v1") returns error? {
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
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
        return;
    }

    # Product Types
    #
    # + latitude - Latitude component of location.
    # + longitude - Longitude component of location.
    # + country - Country name.
    # + return - An array of products
    resource isolated function get products/[string country](decimal latitude, decimal longitude) returns Product[]|error {
        string resourcePath = string `/products/${getEncodedUri(country)}`;
        map<anydata> queryParam = {"latitude": latitude, "longitude": longitude};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        return self.clientEp->get(resourcePath);
    }

    # Product Types
    #
    # + latitude - Latitude component of location.
    # + longitude - Longitude component of location.
    # + country - Country name.
    # + return - An array of products
    resource isolated function post products/[string country](decimal latitude, decimal longitude) returns xml|error {
        string resourcePath = string `/products/${getEncodedUri(country)}`;
        map<anydata> queryParam = {"latitude": latitude, "longitude": longitude};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp->post(resourcePath, request);
    }

    # Product Types
    #
    # + latitude - Latitude component of location.
    # + longitude - Longitude component of location.
    # + country - Country name.
    # + return - An array of products
    resource isolated function put products/[string country](decimal latitude, decimal longitude) returns Product[]|error {
        string resourcePath = string `/products/${getEncodedUri(country)}`;
        map<anydata> queryParam = {"latitude": latitude, "longitude": longitude};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        return self.clientEp->put(resourcePath, request);
    }
}
