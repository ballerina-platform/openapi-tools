import  ballerina/http;
import  ballerina/xmldata;

# refComponent
public isolated client class Client {
    final http:Client clientEp;
    # Client initialization.
    #
    # + clientConfig - Client configuration details
    # + serviceUrl - Connector server URL
    # + return -  An error at the failure of client initialization
    public isolated function init(http:ClientConfiguration clientConfig =  {}, string serviceUrl = "http://petstore.openapi.io/v1") returns error? {
        http:Client httpEp = check new (serviceUrl, clientConfig);
        self.clientEp = httpEp;
    }
    # Request Body has allOf with specific properties.
    #
    # + return - OK
    remote isolated function updateXMLUser(Body payload) returns http:Response|error {
        string  path = string `/path01`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody);
        http:Response response = check self.clientEp-> put(path, request, targetType=http:Response);
        return response;
    }
    # Request Body has nested allOf.
    #
    # + return - OK
    remote isolated function postXMLUser(Body1 payload) returns http:Response|error {
        string  path = string `/path01`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
        http:Response response = check self.clientEp-> post(path, request, targetType=http:Response);
        return response;
    }
    # Request Body has Array type AllOf.
    #
    # + return - OK
    remote isolated function postXMLUserInLineArray(CompoundArrayItemPostXMLUserInLineArrayRequest payload) returns http:Response|error {
        string  path = string `/path02`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody);
        http:Response response = check self.clientEp-> post(path, request, targetType=http:Response);
        return response;
    }
}
