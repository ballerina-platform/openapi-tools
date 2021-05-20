import  ballerina/http;

public client class Client {
    public http:Client clientEp;
    public isolated function init(string serviceUrl = "https", http:ClientConfiguration  httpClientConfig =  {}) returns error? {
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }
    remote isolated function  requestBody(User payload) returns http:Response | error {
        string  path = string `/requestBody`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        http:Response  response = check self.clientEp->post(path, request, targetType=http:Response );
        return response;
    }
}
