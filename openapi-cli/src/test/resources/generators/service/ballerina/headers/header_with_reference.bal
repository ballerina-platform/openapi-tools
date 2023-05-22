import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Description
    #
    # + 'x\-client - parameter description
    # + 'x\-content - parameter description
    # + 'consent\-id - parameter description
    # + 'x\-count - parameter description
    # + 'x\-valid - parameter description
    # + 'x\-sequence - parameter description
    # + 'x\-rate - parameter description
    # + 'x\-modified - parameter description
    # + 'x\-client\-profiles - parameter description
    # + return - Ok
    resource function get ping(@http:Header XClient 'x\-client, @http:Header XContent? 'x\-content, @http:Header string? 'consent\-id, @http:Header XCount 'x\-count, @http:Header XValid? 'x\-valid, @http:Header XSequence? 'x\-sequence, @http:Header float? 'x\-rate, @http:Header boolean? 'x\-modified, @http:Header XClient[]? 'x\-client\-profiles) returns http:Ok {
    }
}
