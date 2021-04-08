import  ballerina/http;

public client class Client {
    public http:Client clientEp;
    public function init(string serviceUrl = "http://localhost:9090/petstore/v1", http:ClientConfiguration httpClientConfig= {})
    returns error?{
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }
    remote function  pet() returns http:Response | error {
        string  path = string `/pet`;
        http:Response response = check self.clientEp->get(path, targetType = http:Response);
        return response;
    }
    remote function createPet(Pet payload) returns http:Response | error {
        string  path = string `/pet`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        http:Response response = check self.clientEp->post(path, request);
        return response;
    }
    remote function getpetsBypetId(string petId) returns http:Response | error {
        string  path = string `/pets/${petId}`;
        http:Response response = check self.clientEp->get(path, targetType = http:Response);
        return response;
    }
    remote function deletepetsBypetId(int petId) returns http:Response | error {
        string  path = string `/pets/${petId}`;
        http:Response response = check self.clientEp->delete(path, targetType = http:Response);
        return response;
    }
    remote function  Image(int petId) returns http:Response | error {
        string  path = string `/pets/${petId}/Image`;
        http:Response response = check self.clientEp->get(path, targetType = http:Response);
        return response;
    }
}
