import ballerina/http;

@http:ServiceConfig {basePath: "/api"}
type Contract service object {
    *http:ServiceContract;
    resource function get hello() returns string;
};

int port = 8095 + 1;

listener http:Listener listenerEp = new (port);

service Contract on listenerEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
