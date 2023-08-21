import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Description
    #
    # + 'limit - parameter description
    # + offset - parameter description
    # + next - parameter description
    # + return - An paged array of pets
    resource function get pets(int:Signed32 'limit, int:Signed32? offset, int next) returns http:Ok {
    }
}
