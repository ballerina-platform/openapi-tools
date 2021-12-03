import ballerina/http;

public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://petstore.{host}.io/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    # Create a pet
    #
    # + return - Null response
    remote isolated function createPet(byte[] payload) returns http:Response|error {
        string resourcePath = string `/pets`;
        http:Request request = new;
        request.setPayload(payload);
        http:Response response = check self.clientEp->post(resourcePath, request);
        return response;
    }
}

