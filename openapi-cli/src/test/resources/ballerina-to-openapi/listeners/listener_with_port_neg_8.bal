import ballerina/http;

type Config record {|
    int port = 9711;
|};

final Config base = {port: 9720};
final Config cfg = {...base};

listener http:Listener listenerEp = new (cfg.port);

service /api/v1 on listenerEp {
    resource function get hello() returns string {
        return "Hello from listener";
    }
}
