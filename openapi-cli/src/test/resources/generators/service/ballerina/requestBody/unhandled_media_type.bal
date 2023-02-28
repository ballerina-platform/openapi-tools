import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "api.example.com"});

service / on ep0 {
    resource function put users(http:Request request) returns http:Ok {
    }
    resource function post users(http:Request request) returns http:Ok {
    }
    resource function put users/[string id](http:Request request) returns http:Ok {
    }
}
