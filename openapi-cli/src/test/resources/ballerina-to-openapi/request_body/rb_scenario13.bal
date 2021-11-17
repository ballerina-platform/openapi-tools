import ballerina/http;

listener http:Listener helloEp = new (9090);

service /payloadV on helloEp {
    resource function post pets(http:Request req) returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}
