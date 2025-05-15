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
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
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
        map<string|string[]> httpHeaders = http:getHeaderMap(headers);
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
        map<string|string[]> httpHeaders = http:getHeaderMap(headers);
        return self.clientEp->get(resourcePath, httpHeaders);
    }
}
