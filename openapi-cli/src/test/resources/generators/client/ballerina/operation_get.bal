import  ballerina/http;
import  ballerina/url;
import  ballerina/lang.'string;

public isolated client class Client {
    public final http:Client clientEp;

    # Client initialization.
    #
    # + clientConfig - Client configuration details
    # + serviceUrl - Connector server URL
    # + return -  An error at the failure of client initialization
    public isolated function init(http:ClientConfiguration  clientConfig =  {}, string serviceUrl = "http://localhost:9090/petstore/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    remote isolated function  pet() returns http:Response | error {
        string  path = string `/pet`;
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote isolated function getPetId(string petId) returns http:Response | error {
        string  path = string `/pets/${petId}`;
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote isolated function  ImageByimageId(int petId, string imageId) returns http:Response | error {
        string  path = string `/pets/${petId}/Image/${imageId}`;
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote isolated function  pets(int offset) returns http:Response | error {
        string  path = string `/pets`;
        map<anydata> queryParam = {offset: offset};
        path = path + check getPathForQueryParam(queryParam);
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote isolated function  users(string[]? offset) returns http:Response | error {
        string  path = string `/users`;
        map<anydata> queryParam = {offset: offset};
        path = path + getPathForQueryParam(queryParam);
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote isolated function getImage(string? tag, int? 'limit) returns http:Response | error {
        string  path = string `/image`;
        map<anydata> queryParam = {tag: tag, 'limit: 'limit};
        path = path + check getPathForQueryParam(queryParam);
        http:Response  response = check self.clientEp->get(path, targetType = http:Response );
        return response;
    }
    remote isolated function  header(string XClient) returns http:Response | error {
        string  path = string `/header`;
        map<string|string[]> accHeaders = {XClient: XClient};
        http:Response  response = check self.clientEp->get(path, accHeaders, targetType = http:Response );
        return response;
    }
}

isolated function  getPathForQueryParam(map<anydata>   queryParam)  returns  string|error {
    string[] param = [];
    param[param.length()] = "?";
    foreach  var [key, value] in  queryParam.entries() {
        if  value  is  () {
            _ = queryParam.remove(key);
        } else {
            if  string:startsWith( key, "'") {
                 param[param.length()] = string:substring(key, 1, key.length());
            } else {
                param[param.length()] = key;
            }
            param[param.length()] = "=";
            if  value  is  string {
                string updateV =  check url:encode(value, "UTF-8");
                param[param.length()] = updateV;
            } else {
                param[param.length()] = value.toString();
            }
            param[param.length()] = "&";
        }
    }
    _ = param.remove(param.length()-1);
    if  param.length() ==  1 {
        _ = param.remove(0);
    }
    string restOfPath = string:'join("", ...param);
    return restOfPath;
}
