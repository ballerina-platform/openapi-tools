import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service /petstore/v1 on ep0 {
    resource function get pets(int? 'limit) returns http:Response {
    }
}
