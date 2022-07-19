import ballerina/http;

# Provides API key configurations needed when communicating with a remote HTTP endpoint.
public type ApiKeysConfig record {|
    # API key to authorize requests.
    string appid1;
    # API key to authorize requests.
    string appid2;
|};

public isolated client class Client {
    final http:Client clientEp;
    final readonly & ApiKeysConfig apiKeyConfig;
    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API keys for authorization
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://petstore.swagger.io:443/v2") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeyConfig = apiKeyConfig.cloneReadOnly();
        return;
    }
    # List all pets
    #
    # + 'limit - How many items to return at one time (max 100)
    # + return - An paged array of pets
    resource isolated function get pets(int? 'limit = ()) returns Pets|error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {"limit": 'limit, "appid1": self.apiKeyConfig.appid1, "appid2": self.apiKeyConfig.appid2};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Pets response = check self.clientEp->get(resourcePath);
        return response;
    }
    # Update a pet
    #
    # + return - Null response
    resource isolated function put pets() returns http:Response|error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {"appid1": self.apiKeyConfig.appid1, "appid2": self.apiKeyConfig.appid2};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response response = check self.clientEp-> put(resourcePath, request);
        return response;
    }
    # Create a pet
    #
    # + return - Null response
    resource isolated function post pets() returns http:Response|error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {"appid1": self.apiKeyConfig.appid1, "appid2": self.apiKeyConfig.appid2};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response response = check self.clientEp-> post(resourcePath, request);
        return response;
    }
    # Delete a pet
    #
    # + return - Ok response
    resource isolated function delete pets() returns http:Response|error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {"appid1": self.apiKeyConfig.appid1, "appid2": self.apiKeyConfig.appid2};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        http:Response response = check self.clientEp->delete(resourcePath);
        return response;
    }
}
