import ballerina/http;

import openapi/listener_with_port_neg.foo;

listener http:Listener listenerEp = new (foo:port);

service /payloadV on listenerEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
