import  ballerina/http;

public type ApiKeysConfig record {
    map<string> apiKeys;
};

# + clientEp - Connector http endpoint
public client class Client {
    http:Client clientEp;
    map<string> apiKeys;
    public isolated function init(ApiKeysConfig apiKeyConfig, http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://petstore.openapi.io/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        self.apiKeys = apiKeyConfig.apiKeys;
    }
    # Info for a specific pet
    #
    # + xRequestId - Tests header 01
    # + xRequestClient - Tests header 02
    # + return - Expected response to a valid request
    remote isolated function showPetById(string xRequestId, string[] xRequestClient) returns error? {
        string  path = string `/pets`;
        map<any> headerValues = {"X-Request-ID": xRequestId, "X-Request-Client": xRequestClient, 'X\-API\-KEY: self.apiKeys.get("X-API-KEY")};
        map<string|string[]> accHeaders = getMapForHeaders(headerValues);
         _ = check self.clientEp-> get(path, accHeaders, targetType=http:Response);
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
