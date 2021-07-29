import  ballerina/http;

# + clientEp - Connector http endpoint
public isolated client class Client {
    final http:Client clientEp;
    # Client initialization.
    #
    # + clientConfig - Client configuration details
    # + serviceUrl - Connector server URL
    # + return - An error at the failure of client initialization
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "localhost:9090/payloadV") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    #
    # + id - id value
    # + return - Ok
    remote isolated function operationId03(Id id) returns string|error {
        string  path = string `/v1/${id}`;
        string response = check self.clientEp-> get(path, targetType = string);
        return response;
    }
}
