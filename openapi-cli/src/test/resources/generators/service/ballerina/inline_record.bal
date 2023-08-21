import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Creates a new user.
    #
    # + payload - parameter description
    # + return - OK
    resource function post user(@http:Payload User_body payload) returns Inline_response_201 {
    }
}
