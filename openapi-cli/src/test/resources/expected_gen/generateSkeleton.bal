service /v1 on ep0, ep1 {

# Show a list of pets in the system.
# + caller - Caller client object represents the endpoint
# + req    - Req represents the message, which came over the network
# + 'limit - How many items to return at one time (max 100)
# + return - Error value if an error occurred or return `()` otherwise
    resource function get pets(http:Caller caller, http:Request req
    ,  int ?  'limit) returns error? {

    }

# 
# + caller - Caller client object represents the endpoint
# + req    - Req represents the message, which came over the network
# + return - Error value if an error occurred or return `()` otherwise
    resource function post pets(http:Caller caller, http:Request req
    ) returns error? {

    }

# 
# + caller - Caller client object represents the endpoint
# + req    - Req represents the message, which came over the network
# + petId - The id of the pet to retrieve
# + return - Error value if an error occurred or return `()` otherwise
    resource function get pets/[string petId](http:Caller caller, http:Request req
    ) returns error? {

    }
}
