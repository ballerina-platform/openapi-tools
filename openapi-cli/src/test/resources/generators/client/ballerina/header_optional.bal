import ballerina/http;

# Provides API key configurations needed when communicating with a remote HTTP endpoint.
public type ApiKeysConfig record {|
    # API keys related to connector authentication
    map<string> apiKeys;
|};

# Get current weather, daily forecast for 16 days, and 3-hourly forecast 5 days for your city.
@display {label: "Open Weather Client"}
public isolated client class Client {
    final http:Client clientEp;
    final readonly & map<string> apiKeys;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API key configuration detail
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://api.openweathermap.org/data/2.5/") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeys = apiKeyConfig.apiKeys.cloneReadOnly();
    }
    # Provide weather forecast for any geographical coordinates
    #
    # + lat - Latitude
    # + lon - Longtitude
    # + exclude - test
    # + units - tests
    # + return - Successful response
    @display {label: "Weather Forecast"}
    remote isolated function getWeatherForecast(@display {label: "Latitude"} string lat, @display {label: "Longtitude"} string lon, @display {label: "Exclude"} string? exclude = (), @display {label: "Units"} int? units = ()) returns WeatherForecast|error {
        string  path = string `/onecall`;
        map<anydata> queryParam = {"lat": lat, "lon": lon, "appid": self.apiKeys["appid"]};
        path = path + check getPathForQueryParam(queryParam);
        map<any> headerValues = {"exclude": exclude, "units": units};
        map<string|string[]> accHeaders = getMapForHeaders(headerValues);
        WeatherForecast response = check self.clientEp-> get(path, accHeaders, targetType = WeatherForecast);
        return response;
    }
}

