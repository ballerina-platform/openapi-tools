import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    string y = "hello";
    resource function get ping(@http:Header int headerValue) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping02(@http:Header boolean[] headerValue) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get ping03(@http:Header int? headerValue = 3) returns http:Ok {
            http:Ok ok = {body: ()};
            return ok;
    }
}

function getHeader() returns string {
    return "header";
}
