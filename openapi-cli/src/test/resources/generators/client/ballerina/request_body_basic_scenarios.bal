import  ballerina/http;
import  ballerina/xmldata;

# refComponent
#
# + clientEp - Connector http endpoint
public client class Client {
    http:Client clientEp;
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://petstore.openapi.io/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    # 02 Example for rb has inline requestbody.
    #
    # + return - OK
    remote isolated function updateUser(Body payload) returns error? {
        string  path = string `/path01`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
         _ = check self.clientEp-> put(path, request, targetType=http:Response);
    }
    # 01 Request body with reference.
    #
    # + return - OK
    remote isolated function postUser(User payload) returns error? {
        string  path = string `/path01`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
         _ = check self.clientEp-> post(path, request, targetType=http:Response);
    }
    # 04 Example for rb has inline requestbody.
    #
    # + payload - A JSON object containing pet information
    # + return - OK
    remote isolated function updateNewUser(User payload) returns error? {
        string  path = string `/path02`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
         _ = check self.clientEp-> put(path, request, targetType=http:Response);
    }
    # 03 Request body with record reference.
    #
    # + return - OK
    remote isolated function postNewUser(User[] payload) returns error? {
        string  path = string `/path02`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
         _ = check self.clientEp-> post(path, request, targetType=http:Response);
    }
    # 06 Example for rb has array inline requestbody.
    #
    # + return - OK
    remote isolated function updateXMLUser(Body1 payload) returns error? {
        string  path = string `/path03`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody);
         _ = check self.clientEp-> put(path, request, targetType=http:Response);
    }
    # 05 Example for rb has array inline requestbody.
    #
    # + return - OK
    remote isolated function postXMLUser(Body2 payload) returns error? {
        string  path = string `/path03`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody);
         _ = check self.clientEp-> post(path, request, targetType=http:Response);
    }
    # 07 Example for rb has array inline requestbody.
    #
    # + return - OK
    remote isolated function postXMLUserInLineArray(Body3[] payload) returns error? {
        string  path = string `/path04`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody);
         _ = check self.clientEp-> post(path, request, targetType=http:Response);
    }
}
