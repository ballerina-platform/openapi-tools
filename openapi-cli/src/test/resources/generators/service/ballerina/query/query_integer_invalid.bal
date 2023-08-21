import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.openapi.io"});

service /v1 on ep0 {
    # Description
    #
    # + 'limit - parameter description
    # + offset - parameter description
    # + next - parameter description
    # + return - An paged array of pets
    resource function get pets(int 'limit, int? offset, int next) returns int:Signed32 {
    }
}
