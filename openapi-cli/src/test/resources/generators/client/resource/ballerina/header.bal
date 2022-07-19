import ballerina/http;

# Provides API key configurations needed when communicating with a remote HTTP endpoint.
public type ApiKeysConfig record {|
    # API key.
    string appid;
|};

# Get current weather, daily forecast for 16 days, and 3-hourly forecast 5 days for your city.
@display {label: "Open Weather Client"}
public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://api.openweathermap.org/data/2.5/") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
        return;
    }
    # Provide weather forecast for any geographical coordinates
    #
    # + lat - Latitude
    # + lon - Longtitude
    # + exclude - test
    # + units - tests
    # + return - Successful response
    @display {label: "Weather Forecast"}
    resource isolated function get onecall(@display {label: "Latitude"} string lat, @display {label: "Longtitude"} string lon, @display {label: "Exclude"} string exclude = "current", @display {label: "Units"} int units = 12) returns WeatherForecast|error {
        string resourcePath = string `/onecall`;
        map<anydata> queryParam = {"lat": lat, "lon": lon, "appid": self.apiKeyConfig.appid};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<any> headerValues = {"exclude": exclude, "units": units};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        WeatherForecast response = check self.clientEp->get(resourcePath, httpHeaders);
        return response;
    }
    # Info for a specific pet
    #
    # + xRequestId - Tests header 01
    # + xRequestClient - Tests header 02
    # + xRequestPet - Tests header 03
    # + return - Expected response to a valid request
    resource isolated function get weather(string xRequestId, string[] xRequestClient, WeatherForecast[] xRequestPet) returns http:Response|error {
        string resourcePath = string `/weather`;
        map<anydata> queryParam = {"appid": self.apiKeyConfig.appid};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        map<any> headerValues = {"X-Request-ID": xRequestId, "X-Request-Client": xRequestClient, "X-Request-Pet": xRequestPet};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        http:Response response = check self.clientEp->get(resourcePath, httpHeaders);
        return response;
    }
}
