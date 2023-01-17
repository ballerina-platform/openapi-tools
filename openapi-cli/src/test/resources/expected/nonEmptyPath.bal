import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service / on ep0 {
    resource function get user() returns json {
    }
    resource function put user() returns json {
    }
    resource function post user() returns OkAnydata {
    }
}
