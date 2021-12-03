import ballerina/http;
import ballerina/url;
import ballerina/lang.'string;

type ProductArr Product[];

type PriceEstimateArr PriceEstimate[];

public isolated client class Client {
    public final http:Client clientEp;
    public isolated function init(string serviceUrl = "https://api.uber.com/v1", http:ClientConfiguration httpClientConfig =
                                  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        return;
    }
    remote isolated function products(decimal latitude, decimal longitude) returns ProductArr|error {
        string resourcePath = string `/products`;
        map<anydata> queryParam = {
            latitude: latitude,
            longitude: longitude
        };
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        ProductArr response = check self.clientEp->get(resourcePath);
        return response;
    }
    remote isolated function price(decimal start_latitude, decimal start_longitude, decimal end_latitude,
                                   decimal end_longitude) returns PriceEstimateArr|error {
        string resourcePath = string `/estimates/price`;
        map<anydata> queryParam = {
            start_latitude: start_latitude,
            start_longitude: start_longitude,
            end_latitude: end_latitude,
            end_longitude: end_longitude
        };
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        PriceEstimateArr response = check self.clientEp->get(resourcePath);
        return response;
    }
    remote isolated function time(decimal start_latitude, decimal start_longitude, string? customer_uuid,
                                  string? product_id) returns ProductArr|error {
        string resourcePath = string `/estimates/time`;
        map<anydata> queryParam = {
            start_latitude: start_latitude,
            start_longitude: start_longitude,
            customer_uuid: customer_uuid,
            product_id: product_id
        };
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        ProductArr response = check self.clientEp->get(resourcePath);
        return response;
    }
    remote isolated function me() returns Profile|error {
        string resourcePath = string `/me`;
        Profile response = check self.clientEp->get(resourcePath);
        return response;
    }
    remote isolated function history(int? offset, int? 'limit) returns Activities|error {
        string resourcePath = string `/history`;
        map<anydata> queryParam = {offset: offset, 'limit: 'limit};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Activities response = check self.clientEp->get(resourcePath);
        return response;
    }
}

isolated function getPathForQueryParam(map<anydata> queryParam) returns string|error {
    string[] param = [];
    param[param.length()] = "?";
    foreach var [key, value] in queryParam.entries() {
        if value is () {
            _ = queryParam.remove(key);
        } else {
            if string:startsWith(key, "'") {
                param[param.length()] = string:substring(key, 1, key.length());
            } else {
                param[param.length()] = key;
            }
            param[param.length()] = "=";
            if value is string {
                string updateV = check url:encode(value, "UTF-8");
                param[param.length()] = updateV;
            } else {
                param[param.length()] = value.toString();
            }
            param[param.length()] = "&";
        }
    }
    _ = param.remove(param.length() - 1);
    if param.length() == 1 {
        _ = param.remove(0);
    }
    string restOfPath = string:'join("", ...param);
    return restOfPath;
}
