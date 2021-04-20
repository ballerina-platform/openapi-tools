import  ballerina/http;
import ballerina/url;
import ballerina/lang.'string;

listener http:Listener helloEp = new (9090);

type Pet record {
    int id;
    string name;
    string tag?;
 };

 type Dog record {
     Pet perant;
     boolean bark;
  };
 service /payloadV on helloEp {
     resource function post hi(http:Caller caller, http:Request request, @http:Payload {} Dog payload) {

     }}

//Use for getting syntax tree

public client class Client {
    public http:Client clientEp;
    public function init(string serviceUrl = "https://petstore.swagger.io:443/v2", http:ClientConfiguration httpClientConfig= {})
    returns error?{
        http:Client httpEp = check new (serviceUrl, httpClientConfig);
        self.clientEp = httpEp;
        }

    remote function listPets(string tag, int 'limit) returns http:Response | error {
            string path = string `/image`;
                map<anydata> queryParam = {
                    tag: tag,
                    'limit: 'limit
                };
                path =  path + getPathForQueryParam(queryParam);
            http:Response response = check self.clientEp->get(path, targetType = http:Response);
            return response;
           }
        }
    }

    function getPathForQueryParam(map<anydata> queryParam) returns string {
        string[] param = [];
        param[param.length()] = "?";
        foreach var [key, value] in queryParam.entries() {
            if value is () {
                _ = queryParam.remove(key);
            } else {
                if string:startsWith(key, "'") {
                    param[param.length()] = string:substring(key, 1, key.length());
                } else {
                    param[param.length()] = key;
                }
                param[param.length()] = "=";
                if value is string {
                    string updateV =  checkpanic url:encode(value, "UTF-8");
                    param[param.length()] = updateV;
                } else {
                    param[param.length()] = value.toString();
                }
                param[param.length()] = "&";
            }
        }
        _ = param.remove(param.length()-1);
        if param.length() == 1 {
            _ = param.remove(0);
        }
        string restOfPath = string:'join("", ...param);
        return restOfPath;
    }
    ////normal
    //remote function getPets() returns string {
    //    string path = string `/pet`;
    //    http:Response response = check self.clientEp->get(path, targetType = http:Response);
    //    return response;
    //}
    ////Path param
    //remote function showPetById(string petId) returns http:Response | error {
    //       string path = string `/pets/${petId}`
    //       http:Response response = check self.clientEp->get(path, targetType = http:Response);
    //       return response
    //}
    ////multi path param
    //remote function showPetById(int orgId, int memberId) returns http:Response | error {
    //       string path = string `/pets/${orgId}/members/${memberId}`;
    //       http:Response response = check self.clientEp->get(path, targetType = http:Response);
    //       return response
    //   }
    //
    ////Query param- required
    //remote function listPets(int offset) returns http:Response | error {
    //    string path = string `/pets?offset=${offset}`;
    //    http:Response response = check self.clientEp->get(path, targetType = http:Response);
    //    return response;
    //   }
    //
    ////QueryParam - optional
    //remote function listPets(int offset?) returns http:Response | error {
    //    string path = string `/pets`;
    //    if (offset is int) {
    //        path = path + `?offset=${offset}`;
    //    }
    //    http:Response response = check self.clientEp->get(path, targetType = http:Response);
    //    return response;
    //   }
    //}
    //
    ////QueryParam - multiple
    //remote function listPets(string? tag, int? 'limit) returns http:Response | error {
    //        string path = string `/pets?tag=${tags}&limit=${‘limit}`;
    //        if (tag is string) {
    //            path = path + `?tag=${tag}`;
    //        }
    //        if ('limit is string) {
    //            path = path + `?limit=${'limit}`;
    //        }
    //        http:Response response = check self.clientEp->get(path, targetType = http:Response);
    //        return response;
    //    }
    //
    //// Header
    //remote function sendHeader(string xClient) returns http:Response | error {
    //        string path = "/acheader";
    //        map<string|string[]> accHeaders = {"XClient": "headerPayload"};
    //        http:Response response = check self.clientEp->get(path, accHeaders, targetType = http:Response);
    //        return response;
    //    }

    //request body
    //    remote function createPet(Pet createPetBody) returns http:Response | error{
    //        string path = "/pets";
    //        http:Request request = new;
    //        json createPetJsonBody = check createPetBody.cloneWithType(json);
    //        request.setPayload(createPetJsonBody);
    //
    //        // TODO: Update the request as needed
    //        http:Response response = check self.clientEp->post(path, request);
    //        return response;
    //    }
    //
    //    //request body
    //    remote function createPet(Pet createPetBody) returns http:Response | error{
    //        string path = "/pets";
    //        http:Request request = new;
    //        json createPetJsonBody = check createPetBody.cloneWithType(json);
    //        xml createPetXmlBody = check xmldata:fromJson(createPetJsonBody);
    //        request.setPayload(createPetXmlBody);
    //
    //        // TODO: Update the request as needed
    //        http:Response response = check self.clientEp->post(path, request);
    //        return response;
    //    }
    //
    //    //request body
    //    remote function createPet(string body) returns http:Response | error{
    //        string path = "/pets";
    //        http:Request request = new;
    //        request.setPayload(body);
    //
    //        // TODO: Update the request as needed
    //        http:Response response = check self.clientEp->post(path, request);
    //        return response;
    //    }

//}




