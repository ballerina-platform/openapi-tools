import ballerina/http;

# Provides API key configurations needed when communicating with a remote HTTP endpoint.
public type ApiKeysConfig record {|
    # Uber API Key
    string serverToken;
|};

# Move your app forward with the Uber API
public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://api.uber.com/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
        return;
    }
    # Product Types
    #
    # + latitude - Latitude component of location.
    # + longitude - Longitude component of location.
    # + return - An array of products
    remote isolated function getProducts(float latitude, float longitude) returns Product[]|error {
        string  path = string `/products`;
        map<anydata> queryParam = {"latitude": latitude, "longitude": longitude, "server_token": self.apiKeyConfig.serverToken};
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
    remote isolated function getPrice(float startLatitude, float startLongitude, float endLatitude, float endLongitude) returns PriceEstimate[]|error {
        string  path = string `/estimates/price`;
        map<anydata> queryParam = {"start_latitude": startLatitude, "start_longitude": startLongitude, "end_latitude": endLatitude, "end_longitude": endLongitude};
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
    remote isolated function getTimeEstimates(float startLatitude, float startLongitude, string? customerUuid = (), string? productId = ()) returns Product[]|error {
        string  path = string `/estimates/time`;
        map<anydata> queryParam = {"start_latitude": startLatitude, "start_longitude": startLongitude, "customer_uuid": customerUuid, "product_id": productId};
        path = path + check getPathForQueryParam(queryParam);
        Product[] response = check self.clientEp-> get(path, targetType = ProductArr);
        return response;
    }
    # User Profile
    #
    # + return - Profile information for a user
    remote isolated function getUserProfile() returns Profile|error {
        string  path = string `/me`;
        Profile response = check self.clientEp-> get(path, targetType = Profile);
        return response;
    }
    # User Activity
    #
    # + offset - Offset the list of returned results by this amount. Default is zero.
    # + 'limit - Number of items to retrieve. Default is 5, maximum is 100.
    # + return - History information for the given user
    remote isolated function getUserActivity(int? offset = (), int? 'limit = ()) returns Activities|error {
        string  path = string `/history`;
        map<anydata> queryParam = {"offset": offset, "limit": 'limit};
        path = path + check getPathForQueryParam(queryParam);
        Activities response = check self.clientEp-> get(path, targetType = Activities);
        return response;
    }
}
