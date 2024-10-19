import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Add a new pet
    #
    # + payload - Optional description in *Markdown*
    # + return - OK
    resource function post pets(@http:Payload Pet|xml|map<string>|string payload) returns http:Ok {
    }
}
