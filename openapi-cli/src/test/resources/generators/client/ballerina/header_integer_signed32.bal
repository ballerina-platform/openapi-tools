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
    public isolated function init(ApiKeysConfig apiKeyConfig, ConnectionConfig config =  {}, string serviceUrl = "http://petstore.openapi.io/v1") returns error? {
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

    # Info for a specific pet
    #
    # + X\-Request\-ID - Tests header 01
    # + X\-Request\-Client - Tests header 02
    # + X\-Request\-Pet - Tests header 03
    # + X\-Request\-Header - Tests header 04
    # + return - Expected response to a valid request
    remote isolated function showPetById(int:Signed32 X\-Request\-ID, int:Signed32[] X\-Request\-Client, Pet[] X\-Request\-Pet, int? X\-Request\-Header = ()) returns http:Response|error {
        string resourcePath = string `/pets`;
        map<any> headerValues = {"X-Request-ID": X\-Request\-ID, "X-Request-Client": X\-Request\-Client, "X-Request-Pet": X\-Request\-Pet, "X-Request-Header": X\-Request\-Header, "X-API-KEY": self.apiKeyConfig.X\-API\-KEY};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        return self.clientEp->get(resourcePath, httpHeaders);
    }
}
