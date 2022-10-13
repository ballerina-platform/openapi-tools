import ballerina/http;

# Provides API key configurations needed when communicating with a remote HTTP endpoint.
public type ApiKeysConfig record {|
    # Uber API Key
    string server_token;
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
    remote isolated function getProducts(decimal latitude, decimal longitude) returns Product[]|error {
        string resourcePath = string `/products`;
        map<anydata> queryParam = {"latitude": latitude, "longitude": longitude, "server_token": self.apiKeyConfig.server_token};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Product[] response = check self.clientEp->get(resourcePath);
        return response;
    }
    # Price Estimates
    #
    # + start_latitude - Latitude component of start location.
    # + start_longitude - Longitude component of start location.
    # + end_latitude - Latitude component of end location.
    # + end_longitude - Longitude component of end location.
    # + return - An array of price estimates by product
    remote isolated function getPrice(decimal start_latitude, decimal start_longitude, decimal end_latitude, decimal end_longitude) returns PriceEstimate[]|error {
        string resourcePath = string `/estimates/price`;
        map<anydata> queryParam = {"start_latitude": start_latitude, "start_longitude": start_longitude, "end_latitude": end_latitude, "end_longitude": end_longitude};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        PriceEstimate[] response = check self.clientEp->get(resourcePath);
        return response;
    }
    # Time Estimates
    #
    # + start_latitude - Latitude component of start location.
    # + start_longitude - Longitude component of start location.
    # + customer_uuid - Unique customer identifier to be used for experience customization.
    # + product_id - Unique identifier representing a specific product for a given latitude & longitude.
    # + return - An array of products
    remote isolated function getTimeEstimates(decimal start_latitude, decimal start_longitude, string? customer_uuid = (), string? product_id = ()) returns Product[]|error {
        string resourcePath = string `/estimates/time`;
        map<anydata> queryParam = {"start_latitude": start_latitude, "start_longitude": start_longitude, "customer_uuid": customer_uuid, "product_id": product_id};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Product[] response = check self.clientEp->get(resourcePath);
        return response;
    }
    # User Profile
    #
    # + return - Profile information for a user
    remote isolated function getUserProfile() returns Profile|error {
        string resourcePath = string `/me`;
        Profile response = check self.clientEp->get(resourcePath);
        return response;
    }
    # User Activity
    #
    # + offset - Offset the list of returned results by this amount. Default is zero.
    # + 'limit - Number of items to retrieve. Default is 5, maximum is 100.
    # + return - History information for the given user
    remote isolated function getUserActivity(int? offset = (), int? 'limit = ()) returns Activities|error {
        string resourcePath = string `/history`;
        map<anydata> queryParam = {"offset": offset, "limit": 'limit};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Activities response = check self.clientEp->get(resourcePath);
        return response;
    }
}
