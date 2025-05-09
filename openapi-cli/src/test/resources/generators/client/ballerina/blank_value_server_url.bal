import ballerina/http;

# Get current weather, daily forecast for 16 days, and 3-hourly forecast 5 days for your city.
@display {label: "Open Weather Client"}
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(string serviceUrl, ConnectionConfig config =  {}) returns error? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: config.http1Settings, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, followRedirects: config.followRedirects, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, cookieConfig: config.cookieConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, socketConfig: config.socketConfig, validation: config.validation, laxDataBinding: config.laxDataBinding};
        self.clientEp = check new (serviceUrl, httpClientConfig);
    }

    # Provide weather forecast for any geographical coordinates
    #
    # + headers - Headers to be sent with the request
    # + queries - Queries to be sent with the request
    # + return - Successful response
    @display {label: "Weather Forecast"}
    remote isolated function getWeatherForecast(map<string|string[]> headers = {}, *GetWeatherForecastQueries queries) returns WeatherForecast|error {
        string resourcePath = string `/onecall`;
        resourcePath = resourcePath + check getPathForQueryParam(queries);
        return self.clientEp->get(resourcePath, headers);
    }
}
