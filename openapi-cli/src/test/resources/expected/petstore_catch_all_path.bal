import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore3.swagger.io"});

service /api/v3 on ep0 {
    resource function get '\*(int petId) returns Pet|xml|http:BadRequest|http:NotFound {
    }
    resource function put pet(@http:Payload Pet|xml|map<string> payload) returns Pet|xml|http:BadRequest|http:NotFound|http:MethodNotAllowed {
    }
    resource function post pet(@http:Payload Pet|xml|map<string> payload) returns OkPetXml|http:MethodNotAllowed {
    }
}
