import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    resource function get store/inventory() returns json {
    }
    resource function put store/inventory() returns http:Response {
    }
    resource function post store/inventory() returns json|InternalServerErrorString|http:Response {
    }
    resource function delete store/inventory() returns json|http:Response {
    }
}
