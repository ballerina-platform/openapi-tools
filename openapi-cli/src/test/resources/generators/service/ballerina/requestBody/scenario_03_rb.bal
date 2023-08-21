import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Add a new pet
    #
    # + request - Optional description in *Markdown*
    # + return - OK
    resource function put pets(http:Request request) returns http:Ok {
    }
    # Add a new pet
    #
    # + payload - Optional description in *Markdown*
    # + return - OK
    resource function post pets(@http:Payload string|xml|map<string>|Pet payload) returns http:Ok {
    }
    # List all pets
    #
    # + payload - parameter description
    # + return - OK
    resource function post pets02(@http:Payload xml|Pet payload) returns http:Created {
    }
}