service / on ep0 {

#
# + caller - Caller client object represents the endpoint
# + req    - Req represents the message which came along over the network

# + payload - Request body payload.
# + return - Error value if an error occurred, and return nil otherwise
    resource function post requestBody(http:Caller caller, http:Request req
    , @http:Payload {} User  payload) returns error? {}

}

