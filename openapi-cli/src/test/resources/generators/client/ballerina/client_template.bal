import  ballerina/http;

public client class Client {
    public http:Client clientEp;
    public function init(string serviceUrl = "https://petstore.swagger.io:443/v2", http:ClientConfiguration httpClientConfig= {})
    returns error?{
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }
    remote function listPets(int? 'limit) returns http:Response | error {
        string  path = string `/pets`;
        if ('limit is int) {
            path = path + `?limit=${'limit}`;
        }
        http:Response response = check self.clientEp->get(path, targetType = http:Response);
        return response;
    }
    remote function showPetById(string petId) returns http:Response | error {
        string  path = string `/pets/${petId}`;
        http:Response response = check self.clientEp->get(path, targetType = http:Response);
        return response;
    }
}
