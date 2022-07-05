import ballerina/http;

# API description in Markdown.
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://api.example.com") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    # Returns a list of users.
    #
    # + return - OK
    resource isolated function get users() returns http:Response|error {
        string resourcePath = string `/users`;
        http:Response response = check self.clientEp->get(resourcePath);
        return response;
    }
}
