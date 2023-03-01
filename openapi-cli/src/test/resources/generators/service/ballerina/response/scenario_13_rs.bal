import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Creates a new pets.
    #
    # + return - A list of users
    resource function post pets() returns BadRequestUserXmlString {
    }
}
