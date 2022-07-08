import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    resource function get .() returns Pets {
    }
    resource function get pets(int? 'limit) returns Pets|http:Response {
    }
    resource function post pets() returns http:Created|http:Response {
    }
    resource function get pets/[string petId]() returns Pets|http:Response {
    }
    resource function get admin/api/'2021\-10/customers/[string customerIdJson](string? fields) returns http:Ok|error {
        if !customerIdJson.endsWith(".json") {
            return error("bad URL");
        }
        string customerId = customerIdJson.substring(0, customerIdJson.length() - 4);
    }
}
