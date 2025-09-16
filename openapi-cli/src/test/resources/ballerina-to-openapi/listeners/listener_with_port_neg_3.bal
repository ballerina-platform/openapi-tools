import ballerina/http;

int port = http:STATUS_BAD_GATEWAY;

listener http:Listener listenerEp = new (port);

service /api/v1 on listenerEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
