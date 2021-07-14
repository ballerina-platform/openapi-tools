import  ballerina/http;

# refComponent
#
# + clientEp - Connector http endpoint
public client class Client {
    http:Client clientEp;
    # Client initialization.
    #
    # + clientConfig - Client Configuration details
    # + serviceUrl - connector server URL
    # + return -  Returns error at failure of client initialization
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    # Creates a new user.
    #
    # + return - OK
    remote isolated function  requestBody(User payload) returns http:Response|error {
        string  path = string `/requestBody`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        http:Response response = check self.clientEp-> post(path, request, targetType=http:Response);
        return response;
    }
}
