import ballerina/http;

service / on new http:Listener(9090) {

    resource function get path/test(string query) returns string {
        return "Hello, " + query;
    }
}