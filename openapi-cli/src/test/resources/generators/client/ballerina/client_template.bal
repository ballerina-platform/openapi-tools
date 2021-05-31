import  ballerina/http;
import  ballerina/url;
import  ballerina/lang.'string;

public client class Client {
    public http:Client clientEp;
    public isolated function init(http:ClientConfiguration clientConfig = {}, string serviceUrl = "https://petstore.swagger.io:443/v2")
    returns error?{
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    remote isolated function listPets(int? 'limit) returns Pets|error {
        string  path = string `/pets`;
        map<anydata> queryParam = {'limit: 'limit};
        path = path + getPathForQueryParam(queryParam);
        Pets response = check self.clientEp->get(path, targetType = Pets);
        return response;
    }
    remote isolated function showPetById(string petId) returns Pets|error {
        string  path = string `/pets/${petId}`;
        Pets response = check self.clientEp->get(path, targetType = Pets);
        return response;
    }
}

isolated function  getPathForQueryParam(map<anydata>   queryParam)  returns  string {
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
                string updateV =  checkpanic url:encode(value, "UTF-8");
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
