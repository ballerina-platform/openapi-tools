import ballerina/http;

listener http:Listener ep0 = new(80, config = {host: "petstore.swagger.io"});

service /api on ep0 {

# Returns all pets from the system that the user has access.
# + caller - Caller client object represents the endpoint
# + req    - Req represents the message, which came over the network
# + tags - tags to filter by# + 'limit - maximum number of results to return

# + return - Error value if an error occurred or return `()` otherwise
    resource function get pets(http:Caller caller, http:Request req , string[]?  tags,  int?  'limit) returns error? {

    }
}
