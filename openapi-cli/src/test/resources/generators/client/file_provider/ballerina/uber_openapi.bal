import  ballerina/http;
import  ballerina/url;
import  ballerina/lang.'string;

public type ApiKeysConfig record {
    map<string> apiKeys;
};

# Move your app forward with the Uber API
#
# + clientEp - Connector http endpoint
# + apiKeys - API key-value pairs
public isolated client class Client {
    final http:Client clientEp;
    final readonly & map<string> apiKeys;
    # Client initialization.
    #
    # + apiKeyConfig - API key configuration detail
    # + clientConfig - Client configuration details
    # + serviceUrl - Connector server URL
    # + return -  An error at the failure of client initialization
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://api.uber.com/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeys = apiKeyConfig.apiKeys.cloneReadOnly();
    }
    # Product Types
    #
    # + latitude - Latitude component of location.
    # + longitude - Longitude component of location.
    # + return - An array of products
    remote isolated function  products(float latitude, float longitude) returns Product[]|error {
        string  path = string `/products`;
        map<anydata> queryParam = {"latitude": latitude, "longitude": longitude, server_token: self.apiKeys["server_token"]};
        path = path + check getPathForQueryParam(queryParam);
        Product[] response = check self.clientEp-> get(path, targetType = ProductArr);
        return response;
    }
    # Price Estimates
    #
    # + startLatitude - Latitude component of start location.
    # + startLongitude - Longitude component of start location.
    # + endLatitude - Latitude component of end location.
    # + endLongitude - Longitude component of end location.
    # + return - An array of price estimates by product
    remote isolated function  price(float startLatitude, float startLongitude, float endLatitude, float endLongitude) returns PriceEstimate[]|error {
        string  path = string `/estimates/price`;
        map<anydata> queryParam = {"start_latitude": startLatitude, "start_longitude": startLongitude, "end_latitude": endLatitude, "end_longitude": endLongitude, server_token: self.apiKeys["server_token"]};
        path = path + check getPathForQueryParam(queryParam);
        PriceEstimate[] response = check self.clientEp-> get(path, targetType = PriceEstimateArr);
        return response;
    }
    # Time Estimates
    #
    # + startLatitude - Latitude component of start location.
    # + startLongitude - Longitude component of start location.
    # + customerUuid - Unique customer identifier to be used for experience customization.
    # + productId - Unique identifier representing a specific product for a given latitude & longitude.
    # + return - An array of products
    remote isolated function  time(float startLatitude, float startLongitude, string? customerUuid = (), string? productId = ()) returns Product[]|error {
        string  path = string `/estimates/time`;
        map<anydata> queryParam = {"start_latitude": startLatitude, "start_longitude": startLongitude, "customer_uuid": customerUuid, "product_id": productId, server_token: self.apiKeys["server_token"]};
        path = path + check getPathForQueryParam(queryParam);
        Product[] response = check self.clientEp-> get(path, targetType = ProductArr);
        return response;
    }
    # User Profile
    #
    # + return - Profile information for a user
    remote isolated function  me() returns Profile|error {
        string  path = string `/me`;
        map<anydata> queryParam = {server_token: self.apiKeys["server_token"]};
        path = path + check getPathForQueryParam(queryParam);
        Profile response = check self.clientEp-> get(path, targetType = Profile);
        return response;
    }
    # User Activity
    #
    # + offset - Offset the list of returned results by this amount. Default is zero.
    # + 'limit - Number of items to retrieve. Default is 5, maximum is 100.
    # + return - History information for the given user
    remote isolated function  history(int? offset = (), int? 'limit = ()) returns Activities|error {
        string  path = string `/history`;
        map<anydata> queryParam = {"offset": offset, "limit": 'limit, server_token: self.apiKeys["server_token"]};
        path = path + check getPathForQueryParam(queryParam);
        Activities response = check self.clientEp-> get(path, targetType = Activities);
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
