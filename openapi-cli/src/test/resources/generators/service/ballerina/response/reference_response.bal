import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "api.example.com"});

service / on ep0 {
    resource function get users() returns UnauthorizedError {
    }
    resource function get users/[int id]() returns User|UnauthorizedError|NotFoundError {
    }
}
