import  ballerina/http;
import  ballerina/xmldata;

public isolated client class Client {
    public final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return - An error if connector initialization failed
    public isolated function init(http:ClientConfiguration  clientConfig =  {}, string serviceUrl = "http://localhost:9090/petstore/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
        return;
    }
    remote isolated function  pet(Pet payload) returns http:Response | error {
        string resourcePath = string `/pet`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody, "application/xml");
        http:Response  response = check self.clientEp->post(resourcePath, request);
        return response;
    }
    remote isolated function getPetId(string petId, string payload) returns http:Response | error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}`;
        http:Request request = new;
        request.setPayload(payload);
        http:Response  response = check self.clientEp->post(resourcePath, request);
        return response;
    }
    remote isolated function  ImageByimageId(int petId, string imageId) returns http:Response | error {
        string resourcePath = string `/pets/${getEncodedUri(petId)}/Image/${getEncodedUri(imageId)}`;
        http:Response  response = check self.clientEp-> get(resourcePath);
        return response;
    }
}
