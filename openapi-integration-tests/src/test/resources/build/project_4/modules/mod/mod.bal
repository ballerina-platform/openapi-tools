import ballerina/http;
service / on new http:Listener(9091) {
    resource function get . () {
    }
    resource function get pet(int id) returns string {
        return "hello world";
    }
}
