import  ballerina/http;

public type ApiKeysConfig record {
    map<string> apiKeys;
};

public isolated client class Client {
    final http:Client clientEp;
    final readonly & map<string> apiKeys;

    # Gets invoked to initialize the `connector`.
    #
    # + apiKeyConfig - API key configuration detail
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error at the failure of client initialization
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://petstore.openapi.io/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeys = apiKeyConfig.apiKeys.cloneReadOnly();
    }
    # Info for a specific pet
    #
    # + return - Expected response to a valid request
    remote isolated function showPetById() returns http:Response|error {
        string  path = string `/pets`;
        map<any> headerValues = {"X-API-KEY": self.apiKeys["X-API-KEY"]};
        map<string|string[]> accHeaders = getMapForHeaders(headerValues);
        http:Response response = check self.clientEp-> get(path, accHeaders, targetType=http:Response);
        return response;
    }
}

# Generate header map for given header values.
#
# + headerParam - Headers  map
# + return - Returns generated map or error at failure of client initialization
isolated function  getMapForHeaders(map<any>   headerParam)  returns  map<string|string[]> {
    map<string|string[]> headerMap = {};
    foreach  var [key, value] in  headerParam.entries() {
        if  value  is  string ||  value  is  string[] {
            headerMap[key] = value;
        }
    }
    return headerMap;
}
