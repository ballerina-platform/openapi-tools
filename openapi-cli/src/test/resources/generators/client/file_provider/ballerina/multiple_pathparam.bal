import  ballerina/http;

# Title
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    #
    # + 'version - test
    # + name - test
    # + return - Ok
    remote isolated function pathParameter(int 'version, string name) returns string|error {
        string resourcePath = string `/v1/${'version}/v2/${name}`;
        string response = check self.clientEp-> get(resourcePath);
        return response;
    }
}
