import  ballerina/http;

# Provides API key configurations needed when communicating with a remote HTTP endpoint.
public type ApiKeysConfig record {|
    # API keys related to connector authentication
    map<string> apiKeys;
|};

public isolated client class Client {
    final http:Client clientEp;
    final readonly & map<string> apiKeys;

    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API key configuration detail
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://petstore.openapi.io/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeys = apiKeyConfig.apiKeys.cloneReadOnly();
    }
    # Info for a specific pet
    #
    # + xRequestId - Tests header 01
    # + xRequestClient - Tests header 02
    # + return - Expected response to a valid request
    remote isolated function showPetById(string xRequestId, string[] xRequestClient) returns http:Response|error {
        string  path = string `/pets`;
        map<any> headerValues = {"X-Request-ID": xRequestId, "X-Request-Client": xRequestClient, "X-API-KEY": self.apiKeys["X-API-KEY"]};
        map<string|string[]> accHeaders = getMapForHeaders(headerValues);
        http:Response response  = check self.clientEp-> get(path, accHeaders, targetType=http:Response);
        return response;
    }
}


