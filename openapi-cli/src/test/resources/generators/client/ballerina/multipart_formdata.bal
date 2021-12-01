import ballerina/http;
import ballerina/mime;

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
    # + payload - Pet
    # + return - Null response
    remote isolated function createPet(PetsBody payload) returns http:Response|error {
        string path = string `/pets`;
        http:Request request = new;
        mime:Entity[] bodyParts = check createBodyParts(payload);
        request.setBodyParts(bodyParts);
        http:Response response = check self.clientEp->post(path, request);
        return response;
    }
}
