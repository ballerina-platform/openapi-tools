import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    resource function post pets() returns User|record {|*http:BadRequest; xml body;|} | record {| *http:NotFound;
    string body;|} {
    }
}
