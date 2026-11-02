import ballerina/http;

type Config record {|
    int port = 9711;
|};

configurable Config config = ?;

listener http:Listener listenerEp = new (config.port);

service /api/v1 on listenerEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
