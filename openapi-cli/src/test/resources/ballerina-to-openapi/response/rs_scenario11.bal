import ballerina/http;

listener http:Listener helloEp = new (9090);

type String string;

service /payloadV on helloEp {
    resource function post hi() returns string {
        return "ok";
    }

    resource function get hello(String name) returns String {
        return "Hello, " + name;
    }

    resource function get howdy(String|string name) returns String|string {
        return "Hello, " + name;
    }
}
