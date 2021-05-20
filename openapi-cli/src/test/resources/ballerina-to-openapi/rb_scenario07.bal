import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function post pets(@http:Payload{} xml payload) returns  http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}

