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
    remote function getPetId(string petId) returns http:Response | error {
        string  path = string `/pets/${petId}`;
        http:Response response = check self.clientEp->get(path, targetType = http:Response);
        return response;
    }
    remote function  ImageByimageId(int petId, string imageId) returns http:Response | error {
        string  path = string `/pets/${petId}/Image/${imageId}`;
        http:Response response = check self.clientEp->get(path, targetType = http:Response);
        return response;
    }
    //if type is String offset should encode url before assign
    remote function  pets(int offset) returns http:Response | error {
        string  path = string `/pets?offset=${offset}`;
        http:Response response = check self.clientEp->get(path, targetType = http:Response);
        return response;
    }
    remote function  users(string[]? offset) returns http:Response | error {
        string  path = string `/users`;
        if (offset is string[]) {
            path = path + `?offset=${offset}`;
        }
        http:Response response = check self.clientEp->get(path, targetType = http:Response);
        return response;
    }
    //Add function to generate path
    remote function getImage(string? tag, int? 'limit) returns http:Response | error {
        string  path = string `/image`;
        if (tag is string) {
            path = path + `?tag=${tag}`;
        }
        if ('limit is int) {
            path = path + `&limit=${'limit}`;
        }
        http:Response response = check self.clientEp->get(path, targetType = http:Response);
        return response;
    }
    remote function  header() returns http:Response | error {
        string  path = string `/header`;
        http:Response response = check self.clientEp->get(path, targetType = http:Response);
        return response;
    }
}
