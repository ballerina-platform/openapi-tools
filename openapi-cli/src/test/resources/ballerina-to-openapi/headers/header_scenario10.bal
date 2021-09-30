import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    string y = "hello";
    resource function get ping(@http:Header string headerValue = "default" + y) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping02(@http:Header string headerValue = getHeader()) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}

function getHeader() returns string {
    return "header";
}
