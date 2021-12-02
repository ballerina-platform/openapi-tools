import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://petstore.swagger.io:443/v2") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    # Get a pet
    #
    # + return - The status information is returned for the requested file upload.
    remote isolated function getPet() returns PetDetails|error? {
        string path = string `/pets`;
        PetDetails? response = check self.clientEp->get(path);
        return response;
    }
}
