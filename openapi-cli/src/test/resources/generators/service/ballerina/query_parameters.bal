import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    resource function get pets(map<json>? tags, int 'limit) returns Pet[]|http:Response {
        }
}
