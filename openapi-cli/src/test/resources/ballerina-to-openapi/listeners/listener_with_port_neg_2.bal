import ballerina/http;

function getPort() returns int => 8096;

int port = getPort();

listener http:Listener listenerEp = new (port);

service /api/v1 on listenerEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
