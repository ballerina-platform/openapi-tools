import ballerina/http;

listener http:Listener ep0 = new (9090, config = {host: "localhost"});

service / on ep0 {
    # scenario 01: Request body is with oneOf schema for application/json
    #
    # + payload - parameter description
    # + return - Successful operation.
    resource function put pet(@http:Payload Pet_body payload) returns http:Ok {
    }
    # scenario 02: Customised request body with oneOf schema
    #
    # + payload - Customised request body
    # + return - Successful operation.
    resource function post pet(@http:Payload Pet_body_1 payload) returns http:Ok {
    }
    # scenario 04: Request body with oneOf schema with integer, number (primitives)
    #
    # + payload - Ballerina not support for oneOf data type in request body
    # + return - Successful operation.
    resource function put pet02(@http:Payload Pet02_body payload) returns http:Ok {
    }
    # scenario 03: Request body with oneOf schema with application/xml
    #
    # + payload - Ballerina not support for oneOf data type in request body
    # + return - Successful operation.
    resource function post pet02(@http:Payload xml payload) returns http:Ok {
    }
    # scenarios05: requestBody is with oneOf array type
    #
    # + name - parameter description
    # + payload - parameter description
    # + return - returns can be any of following types
    # http:Ok (Successful operation.)
    # http:BadRequest (Invalid storage space name supplied)
    resource function put pet/[string name](@http:Payload Pet_name_body payload) returns http:Ok|http:BadRequest {
    }
    # Description
    #
    # + request - scenario04: Media type is with not main standard type.
    # + return - Successful operation.
    resource function post pet04(http:Request request) returns http:Ok {
    }
}
