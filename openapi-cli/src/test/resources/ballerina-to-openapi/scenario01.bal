import ballerina/http;

listener http:Listener ep0 = new(9090);

service  /payloadV on ep0 {
    resource function get pets () returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}
