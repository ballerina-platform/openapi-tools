import ballerina/http;

int port = 8095 + 1;

listener http:Listener listenerEp = new (port);

service /api/v1 on listenerEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
