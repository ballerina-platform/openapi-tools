import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    resource function get v3/'pet\-store/v1(int? 'limit) returns Pets|http:Response {
    }
}
