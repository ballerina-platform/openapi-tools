import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Info for a specific pet
    #
    # + 'x\-request\-id - parameter description
    # + 'x\-request\-client - parameter description
    # + return - returns can be any of following types
    # http:Ok (Expected response to a valid request)
    # http:Response (unexpected error)
    resource function get pets(@http:Header string 'x\-request\-id, @http:Header string[] 'x\-request\-client) returns http:Ok|http:Response {
    }
}
