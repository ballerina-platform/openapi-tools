import  ballerina/http;
import  ballerina/url;
import  ballerina/lang.'string;

public type ApiKeysConfig record {
    map<string|string[]> apiKeys;
};

# Get current weather, daily forecast for 16 days, and 3-hourly forecast 5 days for your city. Helpful stats, graphics, and this day in history charts are available for your reference. Interactive maps show precipitation, clouds, pressure, wind around your location stations. Data is available in JSON, XML, or HTML format. **Note**: All parameters are optional, but you must select at least one parameter. Calling the API by city ID (using the `id` parameter) will provide the most precise location results.
#
# + clientEp - Connector http endpoint
@display {label: "Open Weather Client"}
public client class Client {
    http:Client clientEp;
    map<string|string[]> apiKeys;
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://api.openweathermap.org/data/2.5/") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeys = apiKeyConfig.apiKeys;
    }
    # Provide weather forecast for any geographical coordinates
    #
    # + lat - Latitude
    # + lon - Longtitude
    # + exclude - By using this parameter you can exclude some parts of the weather data from the API response. It should be a comma-delimited list (without spaces).
    # + units - **Units**. *Example: imperial*. Possible values: `standard`, `metric`, and `imperial`. When you do not use units parameter, format is `standard` by default.
    # + lang - **Language**. *Example: en*. You can use lang parameter to get the output in your language. We support the following languages that you can use with the corresponded lang values: Arabic - `ar`, Bulgarian - `bg`, Catalan - `ca`, Czech - `cz`, German - `de`, Greek - `el`, English - `en`, Persian (Farsi) - `fa`, Finnish - `fi`, French - `fr`, Galician - `gl`, Croatian - `hr`, Hungarian - `hu`, Italian - `it`, Japanese - `ja`, Korean - `kr`, Latvian - `la`, Lithuanian - `lt`, Macedonian - `mk`, Dutch - `nl`, Polish - `pl`, Portuguese - `pt`, Romanian - `ro`, Russian - `ru`, Swedish - `se`, Slovak - `sk`, Slovenian - `sl`, Spanish - `es`, Turkish - `tr`, Ukrainian - `ua`, Vietnamese - `vi`, Chinese Simplified - `zh_cn`, Chinese Traditional - `zh_tw`.
    # + return - Successful response
    @display {label: "Weather Forecast"}
    remote isolated function getWeatherForecast(@display {label: "Latitude"} string lat, @display {label: "Longtitude"} string lon, @display {label: "Exclude"} string? exclude = (), @display {label: "Units"} int units = 12, @display {label: "Language"} string? lang = ()) returns WeatherForecast|error {
        string  path = string `/onecall`;
        map<anydata> queryParam = {"lat": lat, "lon": lon, "exclude": exclude, "units": units, "lang": lang, appid: self.apiKeys["appid"]};
        path = path + check getPathForQueryParam(queryParam);
        WeatherForecast response = check self.clientEp-> get(path, targetType = WeatherForecast);
        return response;
    }
}

# Generate query path with query parameter.
#
# + queryParam - Query parameter map
# + return - Returns generated Path or error at failure of client initialization
isolated function  getPathForQueryParam(map<anydata>   queryParam)  returns  string|error {
    string[] param = [];
    param[param.length()] = "?";
    foreach  var [key, value] in  queryParam.entries() {
        if  value  is  () {
            _ = queryParam.remove(key);
        } else {
            if  string:startsWith( key, "'") {
                 param[param.length()] = string:substring(key, 1, key.length());
            } else {
                param[param.length()] = key;
            }
            param[param.length()] = "=";
            if  value  is  string {
                string updateV =  check url:encode(value, "UTF-8");
                param[param.length()] = updateV;
            } else {
                param[param.length()] = value.toString();
            }
            param[param.length()] = "&";
        }
    }
    _ = param.remove(param.length()-1);
    if  param.length() ==  1 {
        _ = param.remove(0);
    }
    string restOfPath = string:'join("", ...param);
    return restOfPath;
}

