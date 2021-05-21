import  ballerina/http;

public client class Client {
    http:Client clientEp;
    public isolated function init(string serviceUrl = "https", http:ClientConfiguration  httpClientConfig =  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }
    remote isolated function pathParameter(int 'version, string name) returns string|error {
        string  path = string `/v1/${'version}/v2/${name}`;
        string response = check self.clientEp->get(path, targetType = string);
        return response;
    }
}

