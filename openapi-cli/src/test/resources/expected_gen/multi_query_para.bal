import ballerina/http;

listener http:Listener ep0 = new (80, config = {host: "petstore.swagger.io"});

service /api on ep0 {
    # Returns all pets from the system that the user has access
    #
    # + tags - tags to filter by
    # + 'limit - maximum number of results to return
    # + return - pet response or unexpected error
    resource function get pets(string[]? tags, int? 'limit) returns Pet[]|http:Response {
    }
}
