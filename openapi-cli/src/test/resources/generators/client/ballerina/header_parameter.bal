import  ballerina/http;

# + clientEp - Connector http endpoint
public client class Client {
    http:Client clientEp;
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://petstore.openapi.io/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    # Info for a specific pet
    #
    # + 'X\-Request\-ID - Tests header
    # + 'X\-Request\-Client - Tests header
    # + return - Expected response to a valid request
    remote isolated function showPetById(string 'X\-Request\-ID, string[] 'X\-Request\-Client) returns error? {
        string  path = string `/pets`;
        map<any> headerValues = {'X\-Request\-ID: 'X\-Request\-ID, 'X\-Request\-Client: 'X\-Request\-Client};
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
