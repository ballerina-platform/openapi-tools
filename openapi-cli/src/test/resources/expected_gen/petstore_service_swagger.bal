import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore.swagger.io"});

service /v2 on ep0 {
    resource function put pet(@http:Payload {mediaType: ["application/json", "application/xml"]} Pet payload) returns http:BadRequest|http:NotFound|http:MethodNotAllowed {
    }
    resource function post pet(@http:Payload {mediaType: ["application/json", "application/xml"]} Pet payload) returns http:MethodNotAllowed {
    }
    resource function get pet/findByStatus(string[] status) returns xml|http:BadRequest {
    }
    resource function get pet/findByTags(string[] tags) returns xml|http:BadRequest {
    }
    resource function get pet/[int petId]() returns xml|http:BadRequest|http:NotFound {
    }
    resource function post pet/[int petId](@http:Payload {mediaType: "application/x-www-form-urlencoded"} Pet_petId_body payload) returns http:MethodNotAllowed {
    }
    resource function delete pet/[int petId](@http:Header string? api_key) returns http:BadRequest|http:NotFound {
    }
    resource function post pet/[int petId]/uploadImage(@http:Payload json payload) returns ApiResponse {
    }
    resource function get store/inventory() returns json {
    }
    resource function post store/'order(@http:Payload json payload) returns xml|http:BadRequest {
    }
    resource function get store/'order/[int orderId]() returns xml|http:BadRequest|http:NotFound {
    }
    resource function delete store/'order/[int orderId]() returns http:BadRequest|http:NotFound {
    }
    resource function post user(@http:Payload json payload) returns http:Response {
    }
    resource function post user/createWithArray(http:Request payload) returns http:Response {
    }
    resource function post user/createWithList(http:Request payload) returns http:Response {
    }
    resource function get user/login(string username, string password) returns xml|http:BadRequest {
    }
    resource function get user/logout() returns http:Response {
    }
    resource function get user/[string username]() returns xml|http:BadRequest|http:NotFound {
    }
    resource function put user/[string username](@http:Payload json payload) returns http:BadRequest|http:NotFound {
    }
    resource function delete user/[string username]() returns http:BadRequest|http:NotFound {
    }
}
