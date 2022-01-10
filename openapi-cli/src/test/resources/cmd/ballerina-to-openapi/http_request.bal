import ballerina/http;

service / on new http:Listener(9090) {
    resource function get pet(@http:Payload string req) {
    }
}
