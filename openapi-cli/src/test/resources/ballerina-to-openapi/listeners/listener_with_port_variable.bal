import ballerina/http;

// Positive cases
int port1 = 8091;
configurable int port2 = 8092;
const int port3 = 8093;
const port4 = 8094;
int port5 = port4;

listener http:Listener listener2 = new(port2);
listener http:Listener listener3 = new(port = port3);
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

// Negative cases

function getPort() returns int => 8096;

int port6 = 8095 + 1;
int port7 = getPort();
int port8 = http:STATUS_BAD_GATEWAY;
const port9 = 8097 + 1;
configurable int port10 = port9;
configurable int port11 = ?;

listener http:Listener listener6 = new(port6);
listener http:Listener listener7 = new(port7);
listener http:Listener listener8 = new(port8);
listener http:Listener listener9 = new(port9);
listener http:Listener listener10 = new(port10);
listener http:Listener listener11 = new(port11);

service /api/v6 on listener6 {
    resource function get hello() returns string {
        return "Hello from listener6";
    }
}

service /api/v7 on listener7 {
    resource function get hello() returns string {
        return "Hello from listener7";
    }
}

service /api/v8 on listener8 {
    resource function get hello() returns string {
        return "Hello from listener8";
    }
}

service /api/v9 on listener9 {
    resource function get hello() returns string {
        return "Hello from listener9";
    }
}

service /api/v10 on listener10 {
    resource function get hello() returns string {
        return "Hello from listener10";
    }
}

service /api/v11 on listener11 {
    resource function get hello() returns string {
        return "Hello from listener11";
    }
}
