import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    resource function post user(@http:Payload {mediaType: "application/x-www-form-urlencoded"} User_body payload) returns OkAnydata {
    }
    resource function post user02(@http:Payload {mediaType: "application/x-www-form-urlencoded"} User payload) returns OkAnydata {
    }
    resource function post user03(@http:Payload {mediaType: "application/x-www-form-urlencoded"} map<string> payload) returns OkAnydata {
    }
}
