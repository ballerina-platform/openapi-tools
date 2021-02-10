import ballerina/http;
import ballerina/openapi;

type Pet record {
    *NewPet;
     int id;
};
type NewPet record {
     int name;
     string tag?;
};
type Error record {
     int code;
     string message;
};

listener http:Listener ep0 = new(80, config = {host: "petstore.swagger.io"});
@openapi:ServiceInfo {
    contract: "../../swagger/invalid/all_petstore.yaml",
    tags: [],
    operations: [],
    failOnErrors: false
}

service /api on ep0 {

# Returns all pets from the system that the user has access to.
# + caller - Caller client object represents the endpoint
# + req    - Req represents the message, which came over the network
# + tags - tags to filter by# + 'limit - maximum number of results to return
# + return - Error value if an error occurred or return `()` otherwise
    resource function get pets(http:Caller caller, http:Request req , string[]?  tags,  int ?  'limit1) returns error? {

    }

# Creates a new pet in the store. Duplicates are allowed.
# + caller - Caller client object represents the endpoint
# + req    - Req represents the message, which came over the network
# + payload - Request body payload
# + return - Error value if an error occurred or return `()` otherwise
    resource function post pets(http:Caller caller, http:Request req , @http:Payload {} NewPet  payload) returns error? {

    }

# Returns a user based on a single ID, if the user does not have access to the pet.
# + caller - Caller client object represents the endpoint
# + req    - Req represents the message, which came over the network
# + id - ID of pet to fetch
# + return - Error value if an error occurred or return `()` otherwise
    resource function get pets/[int id](http:Caller caller, http:Request req ) returns error? {

    }

# deletes a single pet based on the ID supplied.
# + caller - Caller client object represents the endpoint
# + req    - Req represents the message, which came over the network
# + id - ID of pet to delete
# + return - Error value if an error occurred or return `()` otherwise
    resource function delete pets/[string id](http:Caller caller, http:Request req ) returns error? {

    }
}

