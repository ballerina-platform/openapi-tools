import ballerina/http;

type Config record {|
    int port = 9711;
|};

type Server record {|
    int port;
|};

type Outer record {|
    Server server;
|};

configurable Config config1 = {};
configurable Config config2 = {port: 9712};
configurable Outer outer = {server: {port: 9713}};
final int port4 = outer.server.port;
final Config config5 = {"port": 9715};
configurable record {|int port = 9716;|} config6 = {};

listener http:Listener listener1 = new (config1.port);
listener http:Listener listener2 = new (port = config2.port);
listener http:Listener listener3 = new (outer.server.port);
listener http:Listener listener4 = new (port4);
listener http:Listener listener5 = new (config5.port);
listener http:Listener listener6 = new (config6.port);

service /record/v1 on listener1 {
    resource function get hello() returns string => "Hello from listener1";
}

service /record/v2 on listener2 {
    resource function get hello() returns string => "Hello from listener2";
}

service /record/v3 on listener3 {
    resource function get hello() returns string => "Hello from listener3";
}

service /record/v4 on listener4 {
    resource function get hello() returns string => "Hello from listener4";
}

service /record/v5 on listener5 {
    resource function get hello() returns string => "Hello from listener5";
}

service /record/v6 on listener6 {
    resource function get hello() returns string => "Hello from listener6";
}

type Alias Config;

configurable Alias config7 = {};
final int port = 9718;
final Config config8 = {port};

listener http:Listener listener7 = new (config7.port);
listener http:Listener listener8 = new (config8.port);

service /record/v7 on listener7 {
    resource function get hello() returns string => "Hello from listener7";
}

service /record/v8 on listener8 {
    resource function get hello() returns string => "Hello from listener8";
}
