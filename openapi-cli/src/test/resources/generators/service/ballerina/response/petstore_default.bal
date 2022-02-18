import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    resource function post user(@http:Payload User payload) returns http:Response {
    }
    resource function put user/[string username](@http:Payload User payload) returns http:Response {
    }
    resource function get pet/findByStatus(string status = "available") returns http:BadRequest|http:Response {
    }
    resource function post user/createWithList(@http:Payload User[] payload) returns User|http:Response {
    }
    resource function get user/logout() returns User|http:Response {
    }
}
