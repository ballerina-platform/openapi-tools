import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Description
    #
    # + 'x\-client - parameter description
    # + offest - parameter description
    # + return - Ok
    resource function get ping(@http:Header string? 'x\-client, string offest = "abc") returns http:Ok {
    }
}
