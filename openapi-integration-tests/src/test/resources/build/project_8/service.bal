import ballerina/http;

service /\.well\-known/smart\-configuration on new http:Listener(9090) {
    resource function get greeting() returns string {
        return "Hello, World!";
    }
}
