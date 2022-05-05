import  ballerina/http;


public isolated client class Client {
    public final http:Client clientEp;

    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration clientConfig = {}, string serviceUrl = "https://petstore.swagger.io:443/v2")
    returns error?{
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    remote isolated function listPets(int? 'limit) returns Pets|error {
        string resourcePath = string `/pets`;
        map<anydata> queryParam = {'limit: 'limit};
        resourcePath = resourcePath + check getPathForQueryParam(queryParam);
        Pets response = check self.clientEp->get(resourcePath);
        return response;
    }
    remote isolated function showPetById(string petId) returns Pets|error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}`;
        Pets response = check self.clientEp->get(resourcePath);
        return response;
    }
}

isolated function  getPathForQueryParam(map<anydata>   queryParam)  returns  string|error {
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
                string updateV =  check url:encode(value, "UTF-8");
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
