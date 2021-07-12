import  ballerina/http;
import  ballerina/url;
import  ballerina/lang.'string;

public type ApiKeysConfig record {
    map<string> apiKeys;
};

# Successful response
type CurrentWeatherDataResponse record {
    Coord coord?;
    # (more info Weather condition codes)
    Weather[] weather?;
    # Internal parameter
    string base?;
    Main main?;
    # Visibility, meter
    int visibility?;
    Wind wind?;
    Clouds clouds?;
    Rain rain?;
    Snow snow?;
    # Time of data calculation, unix, UTC
    int dt?;
    Sys sys?;
    # City ID
    int id?;
    string name?;
    # Internal parameter
    int cod?;
};

# Get current weather, daily forecast for 16 days, and 3-hourly forecast 5 days for your city. Helpful stats, graphics, and this day in history charts are available for your reference. Interactive maps show precipitation, clouds, pressure, wind around your location stations. Data is available in JSON, XML, or HTML format. **Note**: This sample Swagger file covers the `current` endpoint only from the OpenWeatherMap API. <br/><br/> **Note**: All parameters are optional, but you must select at least one parameter. Calling the API by city ID (using the `id` parameter) will provide the most precise location results.
#
# + clientEp - Connector http endpoint
@display {label: "Current Weather Details", iconPath: "Path"}
public client class Client {
    http:Client clientEp;
    map<string> apiKeys;

    # Client initialization.
    #
    # + clientConfig - Client Configuration details
    # + serviceUrl - connector server URL
    # + return -  Returns error at failure of client initialization
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://api.openweathermap.org/data/2.5/") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeys = apiKeyConfig.apiKeys;
    }
    # Call current weather data for one location
    #
    # + q - **City name**. *Example: London*. You can call by city name, or by city name and country code. The API responds with a list of results that match a searching word. For the query value, type the city name and optionally the country code divided by comma; use ISO 3166 country codes.
    # + id - **City ID**. *Example: `2172797`*. You can call by city ID. API responds with exact result. The List of city IDs can be downloaded [here](http://bulk.openweathermap.org/sample/). You can include multiple cities in parameter &mdash; just separate them by commas. The limit of locations is 20. *Note: A single ID counts as a one API call. So, if you have city IDs. it's treated as 3 API calls.*
    # + lat - **Latitude**. *Example: 35*. The latitude cordinate of the location of your interest. Must use with `lon`.
    # + lon - **Longitude**. *Example: 139*. Longitude cordinate of the location of your interest. Must use with `lat`.
    # + zip - **Zip code**. Search by zip code. *Example: 95050,us*. Please note if country is not specified then the search works for USA as a default.
    # + units - **Units**. *Example: imperial*. Possible values: `standard`, `metric`, and `imperial`. When you do not use units parameter, format is `standard` by default.
    # + lang - **Language**. *Example: en*. You can use lang parameter to get the output in your language. We support the following languages that you can use with the corresponded lang values: Arabic - `ar`, Bulgarian - `bg`, Catalan - `ca`, Czech - `cz`, German - `de`, Greek - `el`, English - `en`, Persian (Farsi) - `fa`, Finnish - `fi`, French - `fr`, Galician - `gl`, Croatian - `hr`, Hungarian - `hu`, Italian - `it`, Japanese - `ja`, Korean - `kr`, Latvian - `la`, Lithuanian - `lt`, Macedonian - `mk`, Dutch - `nl`, Polish - `pl`, Portuguese - `pt`, Romanian - `ro`, Russian - `ru`, Swedish - `se`, Slovak - `sk`, Slovenian - `sl`, Spanish - `es`, Turkish - `tr`, Ukrainian - `ua`, Vietnamese - `vi`, Chinese Simplified - `zh_cn`, Chinese Traditional - `zh_tw`.
    # + mode - **Mode**. *Example: html*. Determines format of response. Possible values are `xml` and `html`. If mode parameter is empty the format is `json` by default.
    # + return - Successful response
    @display {label: "Current weather"}
    remote isolated function currentWeatherData(@display {label: "City name"} string? q = (), string? id = (), string? lat = (), string? lon = (), string? zip = (), string? units = imperial, string? lang = en, string? mode = json) returns CurrentWeatherDataResponse|error {
        string  path = string `/weather`;
        map<anydata> queryParam = {q: q, id: id, lat: lat, lon: lon, zip: zip, units: units, lang: lang, mode: mode, appid: self.apiKeys["appid"]};
        path = path + check getPathForQueryParam(queryParam);
        CurrentWeatherDataResponse response = check self.clientEp-> get(path, targetType = CurrentWeatherDataResponse);
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
