import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function get ping(@http:Header {name: "x-client"} string? headerValue = "header1") returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}
