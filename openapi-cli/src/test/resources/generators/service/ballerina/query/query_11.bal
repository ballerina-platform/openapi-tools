import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

@http:ServiceConfig {treatNilableAsOptional: false}
service /v1 on ep0 {
    # Description
    #
    # + 'limit - parameter description
    # + return - An paged array of pets
    resource function get pet(int? 'limit) returns http:Ok {
    }
}
