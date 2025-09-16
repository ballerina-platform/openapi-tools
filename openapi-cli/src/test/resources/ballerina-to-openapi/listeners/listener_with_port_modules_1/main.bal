import ballerina/http;

import openapi/listener_with_port.foo as _;

int port = 8888;

listener http:Listener listenerEp = new (port);

service /payloadV on listenerEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
