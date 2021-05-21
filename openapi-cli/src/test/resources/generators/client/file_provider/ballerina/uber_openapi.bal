import  ballerina/http;
import  ballerina/url;
import  ballerina/lang.'string;

type ProductArr Product[];

type PriceEstimateArr PriceEstimate[];

public client class Client {
    http:Client clientEp;
    public isolated function init(string serviceUrl = "https://api.uber.com/v1", http:ClientConfiguration  httpClientConfig =  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }
    remote isolated function  products(float latitude, float longitude) returns ProductArr|error {
        string  path = string `/products`;
        map<anydata> queryParam = {latitude: latitude, longitude: longitude};
        path = path + getPathForQueryParam(queryParam);
        ProductArr response = check self.clientEp-> get(path, targetType = ProductArr);
        return response;
    }
    remote isolated function  price(float start_latitude, float start_longitude, float end_latitude, float end_longitude) returns PriceEstimateArr|error {
        string  path = string `/estimates/price`;
        map<anydata> queryParam = {start_latitude: start_latitude, start_longitude: start_longitude, end_latitude: end_latitude, end_longitude: end_longitude};
        path = path + getPathForQueryParam(queryParam);
        PriceEstimateArr response = check self.clientEp-> get(path, targetType = PriceEstimateArr);
        return response;
    }
    remote isolated function  time(float start_latitude, float start_longitude, string? customer_uuid, string? product_id) returns ProductArr|error {
        string  path = string `/estimates/time`;
        map<anydata> queryParam = {start_latitude: start_latitude, start_longitude: start_longitude, customer_uuid: customer_uuid, product_id: product_id};
        path = path + getPathForQueryParam(queryParam);
        ProductArr response = check self.clientEp-> get(path, targetType = ProductArr);
        return response;
    }
    remote isolated function  me() returns Profile|error {
        string  path = string `/me`;
        Profile response = check self.clientEp-> get(path, targetType = Profile);
        return response;
    }
    remote isolated function  history(int? offset, int? 'limit) returns Activities|error {
        string  path = string `/history`;
        map<anydata> queryParam = {offset: offset, 'limit: 'limit};
        path = path + getPathForQueryParam(queryParam);
        Activities response = check self.clientEp-> get(path, targetType = Activities);
        return response;
    }
}

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
