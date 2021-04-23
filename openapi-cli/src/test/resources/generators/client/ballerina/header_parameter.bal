import  ballerina/http;

public client class Client {
    public http:Client clientEp;
    public function init(string serviceUrl = "http://petstore.openapi.io/v1", http:ClientConfiguration httpClientConfig= {})
    returns error?{
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }
    remote function showPetById(string 'X\-Request\-ID, string[] 'X\-Request\-Client) returns http:Response | error {
        string  path = string `/pets`;
        map<string|string[]> accHeaders = {
            'X\-Request\-ID: 'X\-Request\-ID,
            'X\-Request\-Client: 'X\-Request\-Client
        };
        http:Response  response = check self.clientEp->get(path, accHeaders, targetType = http:Response );
        return response;
    }
}
