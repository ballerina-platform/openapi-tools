import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    resource function put greeting(@http:Payload json payload) returns string {
    }
    resource function post greeting(@http:Payload json payload) returns OkString {
    }
    resource function put greeting02(@http:Payload json payload) returns string {
    }
    resource function post greeting02(@http:Payload xml payload) returns OkString {
    }
    resource function put greeting03(@http:Payload json payload) returns http:Ok {
    }
}
