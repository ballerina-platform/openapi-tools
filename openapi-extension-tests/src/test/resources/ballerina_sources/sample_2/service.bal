import ballerina/http;

service /hello1 on new http:Listener(9090) {
    resource function get greeting1() returns string {
        return "Hello, World!";
    }
}

service /hello2 on new http:Listener(9091) {
    resource function get greeting2() returns string {
        return "Hello, World!";
    }
}
