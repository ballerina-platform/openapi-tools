import  ballerina/http;

public isolated client class Client {
    public final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(string serviceUrl = "http://localhost:9090/petstore/v1", http:ClientConfiguration  httpClientConfig =  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        return;
    }
    remote isolated function  pet() returns http:Response | error {
        string resourcePath = string `/pet`;
        http:Response  response = check self.clientEp-> get(resourcePath);
        return response;
    }
    remote isolated function createPet(Pet payload) returns http:Response | error {
        string resourcePath = string `/pet`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody, "application/json");
        http:Response  response = check self.clientEp->post(resourcePath, request);
        return response;
    }
    remote isolated function getpetsBypetId(string petId) returns Pet|error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}`;
        Pet response = check self.clientEp-> get(resourcePath);
        return response;
    }
    remote isolated function deletepetsBypetId(int petId) returns http:Response | error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}`;
        http:Request request = new;
        //TODO: Update the request as needed;
        http:Response  response = check self.clientEp-> delete(resourcePath, request);
        return response;
    }
    remote isolated function  Image(int petId) returns http:Response | error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}/Image`;
        http:Response  response = check self.clientEp-> get(resourcePath);
        return response;
    }
}
