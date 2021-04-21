import  ballerina/http;
import  ballerina/xmldata;

public client class Client {
    public http:Client clientEp;
    public function init(string serviceUrl = "http://localhost:9090/petstore/v1", http:ClientConfiguration httpClientConfig= {})
    returns error?{
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }
    remote function  pet(Pet payload) returns http:Response | error {
        string  path = string `/pet`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody);
        http:Response response = check self.clientEp->post(path, request);
        return response;
    }
    remote function getPetId(string petId, string payload) returns http:Response | error {
        string  path = string `/pets/${petId}`;
        http:Request request = new;
        request.setPayload(payload);
        http:Response response = check self.clientEp->post(path, request);
        return response;
    }
    remote function  ImageByimageId(int petId, string imageId) returns http:Response | error {
        string  path = string `/pets/${petId}/Image/${imageId}`;
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
}
