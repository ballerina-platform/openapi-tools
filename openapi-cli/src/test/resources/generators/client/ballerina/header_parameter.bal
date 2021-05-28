import  ballerina/http;

public client class Client {
    public http:Client clientEp;
    public isolated function init(http:ClientConfiguration  clientConfig =  {}, string serviceUrl = "http://petstore.openapi.io/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    remote isolated function showPetById(string 'X\-Request\-ID, string[] 'X\-Request\-Client) returns http:Response | error {
        string  path = string `/pets`;
        map<string|string[]> accHeaders = {
            'X\-Request\-ID: 'X\-Request\-ID,
            'X\-Request\-Client: 'X\-Request\-Client
        };
        http:Response  response = check self.clientEp->get(path, accHeaders, targetType = http:Response );
        return response;
    }
}
