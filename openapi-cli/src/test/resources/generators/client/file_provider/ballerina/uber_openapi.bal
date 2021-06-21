import  ballerina/http;
import  ballerina/url;
import  ballerina/lang.'string;

public type ApiKeysConfig record {
    map<string> apiKeys;
};

public type ProductArr Product[];

public type PriceEstimateArr PriceEstimate[];

# Move your app forward with the Uber API
#
# + clientEp - Connector http endpoint
public client class Client {
    http:Client clientEp;
    map<string> apiKeys;
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://api.uber.com/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeys = apiKeyConfig.apiKeys;
    }
    # Product Types
    #
    # + latitude - Latitude component of location.
    # + longitude - Longitude component of location.
    # + return - An array of products
    remote isolated function  products(float latitude, float longitude) returns ProductArr|error {
        string  path = string `/products`;
        map<anydata> queryParam = {latitude: latitude, longitude: longitude, server_token: self.apiKeys["server_token"]};
        path = path + getPathForQueryParam(queryParam);
        ProductArr response = check self.clientEp-> get(path, targetType = ProductArr);
        return response;
    }
    # Price Estimates
    #
    # + start_latitude - Latitude component of start location.
    # + start_longitude - Longitude component of start location.
    # + end_latitude - Latitude component of end location.
    # + end_longitude - Longitude component of end location.
    # + return - An array of price estimates by product
    remote isolated function  price(float start_latitude, float start_longitude, float end_latitude, float end_longitude) returns PriceEstimateArr|error {
        string  path = string `/estimates/price`;
        map<anydata> queryParam = {start_latitude: start_latitude, start_longitude: start_longitude, end_latitude: end_latitude, end_longitude: end_longitude, server_token: self.apiKeys["server_token"]};
        path = path + getPathForQueryParam(queryParam);
        PriceEstimateArr response = check self.clientEp-> get(path, targetType = PriceEstimateArr);
        return response;
    }
    # Time Estimates
    #
    # + start_latitude - Latitude component of start location.
    # + start_longitude - Longitude component of start location.
    # + customer_uuid - Unique customer identifier to be used for experience customization.
    # + product_id - Unique identifier representing a specific product for a given latitude & longitude.
    # + return - An array of products
    remote isolated function  time(float start_latitude, float start_longitude, string? customer_uuid = (), string? product_id = ()) returns ProductArr|error {
        string  path = string `/estimates/time`;
        map<anydata> queryParam = {start_latitude: start_latitude, start_longitude: start_longitude, customer_uuid: customer_uuid, product_id: product_id, server_token: self.apiKeys["server_token"]};
        path = path + getPathForQueryParam(queryParam);
        ProductArr response = check self.clientEp-> get(path, targetType = ProductArr);
        return response;
    }
    # User Profile
    #
    # + return - Profile information for a user
    remote isolated function  me() returns Profile|error {
        string  path = string `/me`;
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
        map<anydata> queryParam = {offset: offset, 'limit: 'limit, server_token: self.apiKeys["server_token"]};
        path = path + getPathForQueryParam(queryParam);
        Activities response = check self.clientEp-> get(path, targetType = Activities);
        return response;
    }
}

# Generate query path with query parameter.
#
# + queryParam - Query parameter map
# + return - Returns generated Path or error at failure of client initialization
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
