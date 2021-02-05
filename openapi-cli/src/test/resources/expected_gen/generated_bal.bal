import ballerina/http;


listener http:Listener ep0 = new(80, config = {host: "petstore.openapi.io"});


service /v1 on ep0 {

#
# + caller - Caller client object represents the endpoint
# + req    - Req represents the message, which came over the network
# + petId - The id of the pet to retrieve# + petName - The id of the pet to retrieve

# + return - Error value if an error occurred or return `()` otherwise
    resource function get pets/[string petId]/petName/[string petName](http:Caller caller, http:Request req ) returns error? {

    }

}
