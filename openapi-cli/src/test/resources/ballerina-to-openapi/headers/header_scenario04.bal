import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function get ping(@http:Header string headerValue, @http:Header{} string[]? x\-request\-client) returns http:Ok {
        http:Ok ok = {body:()};
        return ok;
    }
}
