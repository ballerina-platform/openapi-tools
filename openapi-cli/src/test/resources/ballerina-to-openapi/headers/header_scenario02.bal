import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function get ping(@http:Header {name: "x-item"} string headerValue) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }

    resource function get ping02(@http:Header {name: "x-item"} string[] headerValue) returns http:Ok {
            http:Ok ok = {body: ()};
            return ok;
        }
}
