import ballerina/http;

const portTmp = 8097 + 1;
configurable int port = portTmp;

listener http:Listener listenerEp = new (port);

service /api/v1 on listenerEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
