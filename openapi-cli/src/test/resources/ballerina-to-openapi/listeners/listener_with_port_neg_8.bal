import ballerina/http;

int port = 8095 + 1;

listener http:Listener resolvedEp = new (9090);
listener http:Listener unresolvedEp = new (port);

service /api on resolvedEp, unresolvedEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
