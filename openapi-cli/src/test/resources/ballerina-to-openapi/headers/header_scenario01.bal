import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function get ping(@http:Header {} string X\-Client) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
    resource function get v1(@http:Header {} string[] XClient) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }

}
