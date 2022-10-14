import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    resource function post user(@http:Payload User_body payload) returns Inline_response_200 {
    }
}
