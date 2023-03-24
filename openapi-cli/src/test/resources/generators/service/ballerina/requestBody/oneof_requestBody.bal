import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    resource function put pet(@http:Payload Pet_body payload) returns http:Ok {
    }
    resource function post pet(@http:Payload Pet_body_1 payload) returns http:Ok {
    }
    resource function put pet02(@http:Payload Pet02_body payload) returns http:Ok {
    }
    resource function post pet02(@http:Payload xml payload) returns http:Ok {
    }
    resource function put pet/[string name](@http:Payload Pet_name_body payload) returns http:Ok|http:BadRequest {
    }
    resource function post pet04(http:Request request) returns http:Ok {
    }
}
