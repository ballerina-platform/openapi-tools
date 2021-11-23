import ballerina/http;
//import ballerina/'openapi;

listener http:Listener ep0 = new (9090);

@openapi:ServiceInfo {
    contract: "hello_openapi.yaml"
}
service /payloadV on ep0 {
    resource function get pets() returns http:Ok {
        http:Ok ok = {body: ()};
        return ok;
    }
}
