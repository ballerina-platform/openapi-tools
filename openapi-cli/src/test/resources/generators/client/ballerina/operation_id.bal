import  ballerina/http;

public isolated client class Client {
    public final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + config - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(string serviceUrl = "http://localhost:9090/petstore/v1", http:ClientConfiguration  httpClientConfig =  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        return;
    }
    remote isolated function  pet() returns http:Response | error {
        string resourcePath = string `/pet`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function createPet(Pet payload) returns http:Response | error {
        string resourcePath = string `/pet`;
        http:Request request = new;
        json jsonBody = payload.toJson();
        request.setPayload(jsonBody, "application/json");
        return self.clientEp->post(resourcePath, request);
    }
    remote isolated function getpetsBypetId(string petId) returns Pet|error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}`;
        return self.clientEp-> get(resourcePath);
    }
    remote isolated function deletepetsBypetId(int petId) returns http:Response | error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}`;
        http:Request request = new;
        return self.clientEp-> delete(resourcePath, request);
    }
    remote isolated function  Image(int petId) returns http:Response | error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}/Image`;
        return self.clientEp-> get(resourcePath);
    }
}
