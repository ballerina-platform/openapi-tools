import ballerina/http;

listener http:Listener ep0 = new (443, config = {host: "petstore.swagger.io"});

service /v2 on ep0 {
    resource function get pets(int? 'limit) returns Pets {
    }
}
