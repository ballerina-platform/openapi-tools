import ballerina/http;

# Get current weather, daily forecast for 16 days, and 3-hourly forecast 5 days for your city.
@display {label: "Open Weather Client"}
public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, ConnectionConfig config =  {}, string serviceUrl = "http://api.openweathermap.org/data/2.5/") returns error? {
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

    # Provide weather forecast for any geographical coordinates
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - Successful response
    @display {label: "Weather Forecast"}
    resource isolated function get onecall(GetWeatherForecastHeaders headers = {}, *GetWeatherForecastQueries queries) returns WeatherForecast|error {
        string resourcePath = string `/onecall`;
        map<anydata> queryParam = {...queries};
        queryParam["appid"] = self.apiKeyConfig.appid;
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = getMapForHeaders(headers);
        return self.clientEp->get(resourcePath, httpHeaders);
    }

    # Info for a specific pet
    #
    # + headers - Headers to be sent with the request
    # + return - Expected response to a valid request
    resource isolated function get weather(ShowPetByIdHeaders headers) returns error? {
        string resourcePath = string `/weather`;
        map<anydata> queryParam = {};
        queryParam["appid"] = self.apiKeyConfig.appid;
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<string|string[]> httpHeaders = getMapForHeaders(headers);
        return self.clientEp->get(resourcePath, httpHeaders);
    }
}
