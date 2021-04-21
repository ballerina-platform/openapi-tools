import  ballerina/http;
import  ballerina/url;
import  ballerina/lang.'string;

public client class Client {
    public http:Client clientEp;
    public function init(string serviceUrl = "http://localhost:9090/petstore/v1", http:ClientConfiguration httpClientConfig= {})
    returns error?{
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
    }
    remote function  pet() returns http:Response | error {
        string  path = string `/pet`;
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote function getPetId(string petId) returns http:Response | error {
        string  path = string `/pets/${petId}`;
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote function  ImageByimageId(int petId, string imageId) returns http:Response | error {
        string  path = string `/pets/${petId}/Image/${imageId}`;
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote function  pets(int offset) returns http:Response | error {
        string  path = string `/pets`;
        map<anydata> queryParam = {offset: offset};
        path = path + getPathForQueryParam(queryParam);
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote function  users(string[]? offset) returns http:Response | error {
        string  path = string `/users`;
        map<anydata> queryParam = {offset: offset};
        path = path + getPathForQueryParam(queryParam);
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote function getImage(string? tag, int? 'limit) returns http:Response | error {
        string  path = string `/image`;
        map<anydata> queryParam = {tag: tag, 'limit: 'limit};
        path = path + getPathForQueryParam(queryParam);
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote function  header(string XClient) returns http:Response | error {
        string  path = string `/header`;
        map<string|string[]> accHeader = {XClient: XClient};
        http:Response  response = check self.clientEp->get(path, accHeaders, targetType = http:Response );
        return response;
    }
}
