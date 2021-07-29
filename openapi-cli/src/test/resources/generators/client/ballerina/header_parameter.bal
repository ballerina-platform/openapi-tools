import  ballerina/http;

public type ApiKeysConfig record {
    map<string> apiKeys;
};

public isolated client class Client {
    final http:Client clientEp;
    final readonly & map<string> apiKeys;

    # Client initialization.
    #
    # + apiKeyConfig - API key configuration detail
    # + clientConfig - Client configuration details
    # + serviceUrl - Connector server URL
    # + return -  An error at the failure of client initialization
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
        map<any> headerValues = {"X-Request-ID": xRequestId, "X-Request-Client": xRequestClient, 'X\-API\-KEY: self.apiKeys["X-API-KEY"]};
        map<string|string[]> accHeaders = getMapForHeaders(headerValues);
        http:Response response  = check self.clientEp-> get(path, accHeaders, targetType=http:Response);
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
