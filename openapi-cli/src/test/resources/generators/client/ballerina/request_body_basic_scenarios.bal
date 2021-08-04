import  ballerina/http;
import  ballerina/xmldata;

# refComponent
public isolated client class Client {
    final http:Client clientEp;
    # Gets invoked to initialize the `connector`.
    #
    # + clientConfig - The configurations to be used when initializing the `connector`
    # + serviceUrl - URL of the target service
    # + return -  An error at the failure of client initialization
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "https://petstore.swagger.io:443/v2") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    # 02 Example for rb has inline requestbody.
    #
    # + return - OK
    remote isolated function updateUser(Body payload) returns http:Response|error {
        string  path = string `/path01`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        http:Response response = check self.clientEp-> put(path, request, targetType=http:Response);
        return response;
    }
    # 01 Request body with reference.
    #
    # + return - OK
    remote isolated function postUser(User payload) returns http:Response|error {
        string  path = string `/path01`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        http:Response response = check self.clientEp-> post(path, request, targetType=http:Response);
        return response;
    }
    # 04 Example for rb has inline requestbody.
    #
    # + payload - A JSON object containing pet information
    # + return - OK
    remote isolated function updateNewUser(User payload) returns http:Response|error {
        string  path = string `/path02`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        http:Response response = check self.clientEp-> put(path, request, targetType=http:Response);
        return response;
    }
    # 03 Request body with record reference.
    #
    # + return - OK
    remote isolated function postNewUser(User[] payload) returns http:Response|error {
        string  path = string `/path02`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        http:Response response = check self.clientEp-> post(path, request, targetType=http:Response);
        return response;
    }
    # 06 Example for rb has array inline requestbody.
    #
    # + return - OK
    remote isolated function updateXMLUser(Body1 payload) returns http:Response|error {
        string  path = string `/path03`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody);
        http:Response response = check self.clientEp-> put(path, request, targetType=http:Response);
        return response;
    }
    # 05 Example for rb has array inline requestbody.
    #
    # + return - OK
    remote isolated function postXMLUser(Body2 payload) returns http:Response|error {
        string  path = string `/path03`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody);
        http:Response response = check self.clientEp-> post(path, request, targetType=http:Response);
        return response;
    }
    # 07 Example for rb has array inline requestbody.
    #
    # + return - OK
    remote isolated function postXMLUserInLineArray(Body3[] payload) returns http:Response|error {
        string  path = string `/path04`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody);
        http:Response response = check self.clientEp-> post(path, request, targetType=http:Response);
        return response;
    }
}
