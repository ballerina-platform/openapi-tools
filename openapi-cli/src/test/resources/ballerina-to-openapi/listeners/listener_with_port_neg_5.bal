import ballerina/http;

configurable int port = ?;

listener http:Listener listenerEp = new (port);

service /api/v1 on listenerEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
