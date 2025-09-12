import ballerina/http;

// Positive cases
int port1 = 8091;
configurable int 'port2 = 8092;
const int port\$3 = 8093;
const port4 = 8094;
int port5 = port4;

listener http:Listener listener2 = new('port2);
listener http:Listener listener3 = new(port = port\$3);
listener http:Listener listener4 = new(config = {}, port = port4);
listener http:Listener listener5 = new(port5, httpVersion = http:HTTP_1_1);

listener http:Listener defaultListener = http:getDefaultListener();

service /api/v1 on new http:Listener(port1) {
    resource function get hello() returns string {
        return "Hello from listener1";
    }
}

service /api/v2 on listener2 {
    resource function get hello() returns string {
        return "Hello from listener2";
    }
}

service /api/v3 on listener3 {
    resource function get hello() returns string {
        return "Hello from listener3";
    }
}

service /api/v4 on listener4 {
    resource function get hello() returns string {
        return "Hello from listener4";
    }
}

service /api/v5 on listener5 {
    resource function get hello() returns string {
        return "Hello from listener5";
    }
}

service /api/default on defaultListener {
    resource function get hello() returns string {
        return "Hello from default listener";
    }
}
