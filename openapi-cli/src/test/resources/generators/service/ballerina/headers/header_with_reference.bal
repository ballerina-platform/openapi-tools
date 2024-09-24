import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # + return - Ok
    resource function get ping(@http:Header XClient x\-client, @http:Header XContent? x\-content, @http:Header string? Consent\-ID, @http:Header XCount x\-count, @http:Header XValid? x\-valid, @http:Header XSequence? x\-sequence, @http:Header float? X\-Rate, @http:Header boolean? X\-Modified, @http:Header XClient[]? X\-Client\-Profiles) returns http:Ok {
    }
}
