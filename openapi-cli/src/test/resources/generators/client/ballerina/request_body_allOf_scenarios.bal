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
    # Request Body has allOf with specific properties.
    #
    # + return - OK
    remote isolated function updateXMLUser(Body payload) returns error? {
        string  path = string `/path01`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody);
         _ = check self.clientEp-> put(path, request, targetType=http:Response);
    }
    # Request Body has nested allOf.
    #
    # + return - OK
    remote isolated function postXMLUser(Body1 payload) returns error? {
        string  path = string `/path01`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        request.setPayload(jsonBody);
         _ = check self.clientEp-> post(path, request, targetType=http:Response);
    }
    # Request Body has Array type AllOf.
    #
    # + return - OK
    remote isolated function postXMLUserInLineArray(CompoundArrayItemPostXMLUserInLineArrayRequest payload) returns error? {
        string  path = string `/path02`;
        http:Request request = new;
        json jsonBody = check payload.cloneWithType(json);
        xml? xmlBody = check xmldata:fromJson(jsonBody);
        request.setPayload(xmlBody);
         _ = check self.clientEp-> post(path, request, targetType=http:Response);
    }
}
