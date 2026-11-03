import ballerina/http;

int port = 8095 + 1;

listener http:Listener listenerEp = new (port);

service on listenerEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
