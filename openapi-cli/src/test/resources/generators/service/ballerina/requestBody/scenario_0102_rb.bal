import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Creates a new user.
    #
    # + payload - parameter description
    # + return - OK
    resource function post user(@http:Payload byte[] payload) returns http:Ok {
    }
    # Creates a new payment.
    #
    # + payload - Details of the pet to be purchased
    # + return - OK
    resource function post payment(@http:Payload byte[] payload) returns http:Ok {
    }
}
