import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function get pets(@http:Payload string req) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}
