import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: petstore.openapi.io});

service /v1 on ep0 {
    resource function get pet(string petId) returns http:Ok {}
    resource function get [string petId]/petName/[string petName]() returns Pets {}
    resource function get [string petId]() returns Pets {}
}

