//import  ballerina/http;
//
//listener http:Listener helloEp = new (9090);
//
//type Pet record {
//    int id;
//    string name;
//    string tag?;
// };
//
// type Dog record {
//     Pet perant;
//     boolean bark;
//  };
// service /payloadV on helloEp {
//     resource function post hi(http:Caller caller, http:Request request, @http:Payload {} Dog payload) {
//     }
// }

import  ballerina/http;
import  ballerina/url;
import  ballerina/lang.'string;
import ballerina/time;

# Description
#
# + apiKeys - Field Description
public type ApiKeysConfig record {
    map<string|string[]> apiKeys;
};

type CurrentWeatherDataResponse record {
    Coord? coord?;
    Weather[]? weather?;
    string? base?;
    Main? main?;
    int? visibility?;
    Wind? wind?;
    Clouds? clouds?;
    Rain? rain?;
    Snow? snow?;
    int? dt?;
    Sys? sys?;
    int? id?;
    string? name?;
    int? cod?;
};

type CurrentWeatherDataResponse02 record {
    Coord coord?;
    Weather[] weather?;
    string base?;
    Main main?;
    int visibility?;
    Wind wind?;
    Clouds clouds?;
    Rain rain?;
    Snow snow?;
    int dt?;
    Sys sys?;
    int id?;
    string name?;
    int cod?;
};

# Google spreadsheet connector client endpoint.
#
# + httpClient - Connector http endpoint
public client class Client {
    http:Client clientEp;
    map<string|string[]> apiKeys;

    # Initializes the Google spreadsheet connector client endpoint.
    #
    # + apiKeyConfig - Parameter Description
    # + clientConfig - Parameter Description
    # + serviceUrl - Parameter Description
    # + return - Return Value Description
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://api.openweathermap.org/data/2.5/") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeys = apiKeyConfig.apiKeys;
    }
    # Description
    #
    # + q - Parameter Description
    # + id - Parameter Description
    # + lat - Parameter Description
    # + lon - Parameter Description
    # + zip - Parameter Description
    # + units - Parameter Description
    # + lang - Parameter Description
    # + mode - Parameter Description
    # + return - Return Value Description
    remote isolated function currentWeatherData(string? q = (), string? id = (), string? lat = (), string? lon = (), string? zip = (), string? units = (), string? lang = (), string? mode = ()) returns CurrentWeatherDataResponse|error {
        string  path = string `/weather`;
        map<anydata> queryParam = {q: q, id: id, lat: lat, lon: lon, zip: zip, units: units, lang: lang, mode: mode, appid: self.apiKeys["appid"]};
        path = path + getPathForQueryParam(queryParam);
        CurrentWeatherDataResponse response = check self.clientEp-> get(path, targetType = CurrentWeatherDataResponse);
        return response;
    }

    # Description
    #
    # + q - Parameter Description
    # + id - Parameter Description
    # + lat - Parameter Description
    # + lon - Parameter Description
    # + zip - Parameter Description
    # + units - Parameter Description
    # + lang - Parameter Description
    # + mode - Parameter Description
    # + return - Return Value Description
    remote isolated function currentWeatherData02(string? q = (), string? id = (), string? lat = (), string? lon = (), string? zip = (), string? units = (), string? lang = (), string? mode = ()) returns json?|error {
        string  path = string `/weather`;
        map<anydata> queryParam = {q: q, id: id, lat: lat, lon: lon, zip: zip, units: units, lang: lang, mode: mode, appid: self.apiKeys["appid"]};
        path = path + getPathForQueryParam(queryParam);
        json? response = check self.clientEp-> get(path);
        return response;
    }
}

# Description
#
# + queryParam - Parameter Description
# + return - Return Value Description
isolated function  getPathForQueryParam(map<anydata>   queryParam)  returns  string {
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
                string updateV =  checkpanic url:encode(value, "UTF-8");
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
