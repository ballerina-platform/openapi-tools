import ballerina/http;
import ballerina/log;

listener http:Listener helloEp = new (9090);
type  Pet record  {
    int  id;
    string  name;
    string  tag?;
    string  'type?;
};

service /payloadV on helloEp {
    resource function get .(http:Caller caller, http:Request request) {
        http:Response res = new;
        res.setPayload("Hello World!");
        var result = caller->respond(res);
        if (result is error) {
           log:printError("Error when responding", err = result);
        }
    }

    resource function get hi(http:Caller caller, @http:Header{} string X\-client) {
        http:Response res = new;
        res.setPayload("Hello World!");
        var result = caller->respond(res);
        if (result is error) {
           log:printError("Error when responding", err = result);
        }
    }
    resource function put hi(http:Caller caller, http:Request request) returns http:Ok {
        http:Response res = new;
        res.setPayload("Hello World!");
        var result = caller->respond(res);
        if (result is error) {
           log:printError("Error when responding", err = result);
        }
    }

    resource function get hi/[int id](int offset) returns error? {
        http:Response res = new;
        res.setPayload("Hello World!");
        var result = caller->respond(res);
        if (result is error) {
           log:printError("Error when responding", err = result);
        }
    }

    resource function post hi(http:Caller caller, http:Request request) returns Pet {
    }

    resource function post v1(@http:Payload{} Pet payload) returns http:NotFound {
    }
}
