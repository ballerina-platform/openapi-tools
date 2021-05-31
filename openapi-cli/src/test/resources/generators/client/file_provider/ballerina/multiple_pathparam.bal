import  ballerina/http;

public client class Client {
    http:Client clientEp;
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    remote isolated function pathParameter(int 'version, string name) returns string|error {
        string  path = string `/v1/${'version}/v2/${name}`;
        string response = check self.clientEp-> get(path, targetType = string);
        return response;
    }
}
